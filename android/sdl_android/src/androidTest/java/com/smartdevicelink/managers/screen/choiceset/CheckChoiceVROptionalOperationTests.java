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
import com.smartdevicelink.managers.screen.choiceset.CheckChoiceVROptionalInterface;
import com.smartdevicelink.managers.screen.choiceset.CheckChoiceVROptionalOperation;
import com.smartdevicelink.proxy.interfaces.ISdl;
import com.smartdevicelink.proxy.rpc.Choice;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSet;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSet;

import static org.mockito.Mockito.mock;

public class CheckChoiceVROptionalOperationTests extends AndroidTestCase2 {

	private CheckChoiceVROptionalOperation checkChoiceVROptionalOperation;

	@Override
	public void setUp() throws Exception{
		super.setUp();

		ISdl internalInterface = mock(ISdl.class);
		CheckChoiceVROptionalInterface checkChoiceVROptionalInterface = mock(CheckChoiceVROptionalInterface.class);
		checkChoiceVROptionalOperation = new CheckChoiceVROptionalOperation(internalInterface, checkChoiceVROptionalInterface);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCreateChoiceNoVR(){
		CreateInteractionChoiceSet setNoVR = checkChoiceVROptionalOperation.testCellWithVR(false);
		assertNotNull(setNoVR);
		// This set only has one choice
		Choice choice = setNoVR.getChoiceSet().get(0);
		assertNull(choice.getVrCommands());
	}

	public void testCreateChoiceWithVR(){
		CreateInteractionChoiceSet setNoVR = checkChoiceVROptionalOperation.testCellWithVR(true);
		assertNotNull(setNoVR);
		// This set only has one choice
		Choice choice = setNoVR.getChoiceSet().get(0);
		assertEquals(choice.getVrCommands().get(0), "Test VR");
	}

	public void testDeleteInteractionChoiceSet(){
		DeleteInteractionChoiceSet deleteSet = checkChoiceVROptionalOperation.createDeleteInteractionChoiceSet();
		assertNotNull(deleteSet);
	}

}
