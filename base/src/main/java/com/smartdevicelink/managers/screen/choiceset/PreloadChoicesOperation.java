/*
 * Copyright (c)  2019 Livio, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the Livio Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Created by brettywhite on 6/12/19 1:52 PM
 *
 */

package com.smartdevicelink.managers.screen.choiceset;

import android.support.annotation.NonNull;

import com.smartdevicelink.managers.CompletionListener;
import com.smartdevicelink.managers.file.FileManager;
import com.smartdevicelink.managers.file.MultipleFileCompletionListener;
import com.smartdevicelink.managers.file.filetypes.SdlArtwork;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.interfaces.ISdl;
import com.smartdevicelink.proxy.rpc.Choice;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSet;
import com.smartdevicelink.proxy.rpc.DisplayCapabilities;
import com.smartdevicelink.proxy.rpc.Image;
import com.smartdevicelink.proxy.rpc.ImageField;
import com.smartdevicelink.proxy.rpc.TextField;
import com.smartdevicelink.proxy.rpc.enums.ImageFieldName;
import com.smartdevicelink.proxy.rpc.enums.Result;
import com.smartdevicelink.proxy.rpc.enums.TextFieldName;
import com.smartdevicelink.proxy.rpc.listeners.OnMultipleRequestListener;
import com.smartdevicelink.util.DebugTool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class PreloadChoicesOperation implements Runnable {

	private WeakReference<ISdl> internalInterface;
	private WeakReference<FileManager> fileManager;
	private DisplayCapabilities displayCapabilities;
	private HashSet<ChoiceCell> cellsToUpload;
	private CompletionListener completionListener;
	private boolean isRunning;
	private boolean isVROptional;

	PreloadChoicesOperation(ISdl internalInterface, FileManager fileManager, DisplayCapabilities displayCapabilities,
								   Boolean isVROptional, HashSet<ChoiceCell> cellsToPreload, CompletionListener listener){
		this.internalInterface = new WeakReference<>(internalInterface);
		this.fileManager = new WeakReference<>(fileManager);
		this.displayCapabilities = displayCapabilities;
		this.isVROptional = isVROptional;
		this.cellsToUpload = cellsToPreload;
		this.completionListener = listener;
	}

	@Override
	public void run() {
		DebugTool.logInfo("Choice Operation: Executing preload choices operation");
		preloadCellArtworks(new CompletionListener() {
			@Override
			public void onComplete(boolean success) {
				preloadCells();
			}
		});
	}

	void removeChoicesFromUpload(HashSet<ChoiceCell> choices){
		if (isRunning){ return; }
		cellsToUpload.removeAll(choices);
	}

	private void preloadCellArtworks(@NonNull final CompletionListener listener){
		isRunning = true;

		List<SdlArtwork> artworksToUpload = artworksToUpload();

		if (artworksToUpload.size() == 0){
			DebugTool.logInfo("Choice Preload: No Choice Artworks to upload");
			listener.onComplete(true);
			isRunning = false;
			return;
		}

		if (fileManager.get() != null){
			fileManager.get().uploadArtworks(artworksToUpload, new MultipleFileCompletionListener() {
				@Override
				public void onComplete(Map<String, String> errors) {
					if (errors != null && errors.size() > 0){
						DebugTool.logError("Error uploading choice cell Artworks: "+ errors.toString());
						listener.onComplete(false);
						isRunning = false;
					}else{
						DebugTool.logInfo("Choice Artworks Uploaded");
						listener.onComplete(true);
						isRunning = false;
					}
				}
			});
		}else{
			DebugTool.logError("File manager is null in choice preload operation");
			listener.onComplete(false);
			isRunning = false;
		}
	}

	private void preloadCells(){
		isRunning = true;
		List<CreateInteractionChoiceSet> choiceRPCs = new ArrayList<>(cellsToUpload.size());
		for (ChoiceCell cell : cellsToUpload){
			CreateInteractionChoiceSet csCell = choiceFromCell(cell);
			if (csCell != null){
				choiceRPCs.add(csCell);
			}
		}

		if (choiceRPCs.size() == 0){
			DebugTool.logError(" All Choice cells to send are null, so the choice set will not be shown");
			completionListener.onComplete(true);
			isRunning = false;
			return;
		}

		if (internalInterface.get() != null){
			internalInterface.get().sendRequests(choiceRPCs, new OnMultipleRequestListener() {
				@Override
				public void onUpdate(int remainingRequests) {

				}

				@Override
				public void onFinished() {
					isRunning = false;
					DebugTool.logInfo("Finished pre loading choice cells");
					completionListener.onComplete(true);
				}

				@Override
				public void onError(int correlationId, Result resultCode, String info) {
					DebugTool.logError("There was an error uploading a choice cell: "+ info + " resultCode: " + resultCode);
				}

				@Override
				public void onResponse(int correlationId, RPCResponse response) {

				}
			});
		}else{
			DebugTool.logError("Internal Interface null in preload choice operation");
			isRunning = false;
			completionListener.onComplete(false);
		}
	}

	private CreateInteractionChoiceSet choiceFromCell(ChoiceCell cell){

		List<String> vrCommands;
		if (cell.getVoiceCommands() == null){
			vrCommands = isVROptional ? null : Collections.singletonList(String.valueOf(cell.getChoiceId()));
		}else{
			vrCommands = cell.getVoiceCommands();
		}

		String menuName = hasTextFieldOfName(TextFieldName.menuName) ? cell.getText() : null;
		if (menuName == null){
			DebugTool.logError("Could not convert Choice Cell to CreateInteractionChoiceSet. It will not be shown. Cell: "+ cell.toString());
			return null;
		}

		String secondaryText = hasTextFieldOfName(TextFieldName.secondaryText) ? cell.getSecondaryText() : null;
		String tertiaryText = hasTextFieldOfName(TextFieldName.tertiaryText) ? cell.getTertiaryText() : null;

		Image image = hasImageFieldOfName(ImageFieldName.choiceImage) && cell.getArtwork() != null ? cell.getArtwork().getImageRPC() : null;
		Image secondaryImage = hasImageFieldOfName(ImageFieldName.choiceSecondaryImage) && cell.getSecondaryArtwork() != null ? cell.getSecondaryArtwork().getImageRPC() : null;

		Choice choice = new Choice(cell.getChoiceId(), menuName);
		choice.setVrCommands(vrCommands);
		choice.setSecondaryText(secondaryText);
		choice.setTertiaryText(tertiaryText);
		choice.setIgnoreAddingVRItems(true);

		if (fileManager.get() != null){
			if (image != null && fileManager.get().hasUploadedFile(cell.getArtwork())) {
				choice.setImage(image);
			}
			if (secondaryImage != null && fileManager.get().hasUploadedFile(cell.getSecondaryArtwork())) {
				choice.setSecondaryImage(secondaryImage);
			}
		}

		return new CreateInteractionChoiceSet(choice.getChoiceID(), Collections.singletonList(choice));
	}

	// HELPERS

	List<SdlArtwork> artworksToUpload(){
		List<SdlArtwork> artworksToUpload = new ArrayList<>(cellsToUpload.size());
		for (ChoiceCell cell : cellsToUpload){
			if (hasImageFieldOfName(ImageFieldName.choiceImage) && artworkNeedsUpload(cell.getArtwork())){
				artworksToUpload.add(cell.getArtwork());
			}
			if (hasImageFieldOfName(ImageFieldName.choiceSecondaryImage) && artworkNeedsUpload(cell.getSecondaryArtwork())){
				artworksToUpload.add(cell.getSecondaryArtwork());
			}
		}
		return artworksToUpload;
	}

	boolean artworkNeedsUpload(SdlArtwork artwork){
		if (fileManager.get() != null){
			return (artwork != null && !fileManager.get().hasUploadedFile(artwork) && !artwork.isStaticIcon());
		}
		return false;
	}

	boolean hasImageFieldOfName(ImageFieldName name){
		if (displayCapabilities == null ){ return false; }
		if (displayCapabilities.getGraphicSupported() == null || !displayCapabilities.getGraphicSupported()) { return false; }
		if (displayCapabilities.getImageFields() != null){
			for (ImageField field : displayCapabilities.getImageFields()){
				if (field.getName().equals(name)){
					return true;
				}
			}
		}
		return false;
	}

	boolean hasTextFieldOfName(TextFieldName name){
		if (displayCapabilities == null ){ return false; }
		if (displayCapabilities.getTextFields() != null){
			for (TextField field : displayCapabilities.getTextFields()){
				if (field.getName().equals(name)){
					return true;
				}
			}
		}
		return false;
	}

}