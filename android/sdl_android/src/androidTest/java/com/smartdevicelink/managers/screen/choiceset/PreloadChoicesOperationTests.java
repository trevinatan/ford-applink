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

import com.smartdevicelink.AndroidTestCase2;
import com.smartdevicelink.managers.file.FileManager;
import com.smartdevicelink.managers.file.filetypes.SdlArtwork;
import com.smartdevicelink.managers.screen.choiceset.ChoiceCell;
import com.smartdevicelink.managers.screen.choiceset.PreloadChoicesOperation;
import com.smartdevicelink.proxy.interfaces.ISdl;
import com.smartdevicelink.proxy.rpc.DisplayCapabilities;
import com.smartdevicelink.proxy.rpc.ImageField;
import com.smartdevicelink.proxy.rpc.TextField;
import com.smartdevicelink.proxy.rpc.enums.CharacterSet;
import com.smartdevicelink.proxy.rpc.enums.FileType;
import com.smartdevicelink.proxy.rpc.enums.ImageFieldName;
import com.smartdevicelink.proxy.rpc.enums.TextFieldName;
import com.smartdevicelink.test.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.mock;

public class PreloadChoicesOperationTests extends AndroidTestCase2 {

	private PreloadChoicesOperation preloadChoicesOperation;

	@Override
	public void setUp() throws Exception{

		super.setUp();

		ChoiceCell cell1 = new ChoiceCell("cell 1");
		ChoiceCell cell2 = new ChoiceCell("cell 2", null, Test.GENERAL_ARTWORK);
		HashSet<ChoiceCell> cellsToPreload = new HashSet<>();
		cellsToPreload.add(cell1);
		cellsToPreload.add(cell2);

		ImageField imageField = new ImageField(ImageFieldName.choiceImage, Arrays.asList(FileType.GRAPHIC_PNG, FileType.GRAPHIC_JPEG));
		TextField textField = new TextField(TextFieldName.menuName, CharacterSet.CID1SET, 2, 2);

		DisplayCapabilities displayCapabilities = new DisplayCapabilities();
		displayCapabilities.setGraphicSupported(true);
		displayCapabilities.setImageFields(Collections.singletonList(imageField));
		displayCapabilities.setTextFields(Collections.singletonList(textField));

		ISdl internalInterface = mock(ISdl.class);
		FileManager fileManager = mock(FileManager.class);
		preloadChoicesOperation = new PreloadChoicesOperation(internalInterface, fileManager, displayCapabilities, true, cellsToPreload, null);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testHasTextFieldOfName(){

		TextField textField = new TextField();
		textField.setName(TextFieldName.secondaryText);

		DisplayCapabilities capabilities = new DisplayCapabilities();
		capabilities.setTextFields(Collections.singletonList(textField));

		boolean test = preloadChoicesOperation.hasTextFieldOfName(TextFieldName.secondaryText);
		assertFalse(test);

		boolean test2 = preloadChoicesOperation.hasTextFieldOfName(TextFieldName.menuName);
		assertTrue(test2);
	}

	public void testHasImageFieldOfName(){

		ImageField imageField = new ImageField();
		imageField.setName(ImageFieldName.choiceImage);

		DisplayCapabilities capabilities = new DisplayCapabilities();
		capabilities.setImageFields(Collections.singletonList(imageField));

		boolean test = preloadChoicesOperation.hasImageFieldOfName(ImageFieldName.choiceImage);
		assertTrue(test);

		boolean test2 = preloadChoicesOperation.hasImageFieldOfName(ImageFieldName.appIcon);
		assertFalse(test2);
	}

	public void testArtworkNeedsUpload(){
		boolean test = preloadChoicesOperation.artworkNeedsUpload(Test.GENERAL_ARTWORK);
		assertTrue(test);
	}

	public void testArtworksToUpload(){
		List<SdlArtwork> artworksToUpload = preloadChoicesOperation.artworksToUpload();
		assertNotNull(artworksToUpload);
		assertEquals(artworksToUpload.size(), 1);
	}

}
