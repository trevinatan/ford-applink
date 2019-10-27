/*
 * Copyright (c) 2019 Livio, Inc.
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
 */

package com.smartdevicelink.managers.screen.menu;

import com.smartdevicelink.AndroidTestCase2;
import com.smartdevicelink.managers.BaseSubManager;
import com.smartdevicelink.managers.CompletionListener;
import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.interfaces.ISdl;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.enums.HMILevel;
import com.smartdevicelink.proxy.rpc.enums.TriggerSource;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCNotificationListener;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class VoiceCommandManagerTests extends AndroidTestCase2 {

	private VoiceCommand command, command3;
	private List<VoiceCommand> commands;
	private VoiceCommandManager voiceCommandManager;
	private static final int voiceCommandIdMin = 1900000000;
	private OnRPCNotificationListener onHMIStatusListener, commandListener;

	// SETUP / HELPERS

	@Override
	public void setUp() throws Exception{
		super.setUp();

		VoiceCommandSelectionListener mockListener = mock(VoiceCommandSelectionListener.class);
		command = new VoiceCommand(Arrays.asList("Command one", "Command two"), null);
		VoiceCommand command2 = new VoiceCommand(Arrays.asList("Command three", "Command four"), null);
		command3 = new VoiceCommand(Arrays.asList("Command five", "Command six"), mockListener);
		commands = Arrays.asList(command,command2);

		ISdl internalInterface = mock(ISdl.class);

		// When internalInterface.addOnRPCNotificationListener(FunctionID.ON_HMI_STATUS, OnRPCNotificationListener) is called
		// inside the VoiceCommandManager's constructor, then keep a reference to the OnRPCNotificationListener so we can trigger it later
		// to emulate what Core does when it sends OnHMIStatus notification
		Answer<Void> onHMIStatusAnswer = new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				onHMIStatusListener = (OnRPCNotificationListener) args[1];
				return null;
			}
		};
		doAnswer(onHMIStatusAnswer).when(internalInterface).addOnRPCNotificationListener(eq(FunctionID.ON_HMI_STATUS), any(OnRPCNotificationListener.class));

		Answer<Void> onCommandAnswer = new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				commandListener = (OnRPCNotificationListener) args[1];
				return null;
			}
		};
		doAnswer(onCommandAnswer).when(internalInterface).addOnRPCNotificationListener(eq(FunctionID.ON_COMMAND), any(OnRPCNotificationListener.class));

		voiceCommandManager = new VoiceCommandManager(internalInterface);

		// Check some stuff during setup
		assertEquals(voiceCommandManager.currentHMILevel, HMILevel.HMI_NONE);
		assertEquals(voiceCommandManager.getState(), BaseSubManager.SETTING_UP);
		assertEquals(voiceCommandManager.lastVoiceCommandId, voiceCommandIdMin);
		assertFalse(voiceCommandManager.hasQueuedUpdate);
		assertFalse(voiceCommandManager.waitingOnHMIUpdate);
		assertNotNull(voiceCommandManager.commandListener);
		assertNotNull(voiceCommandManager.hmiListener);
		assertNull(voiceCommandManager.voiceCommands);
		assertNull(voiceCommandManager.oldVoiceCommands);
		assertNull(voiceCommandManager.inProgressUpdate);
	}

	@Override
	public void tearDown() throws Exception {

		voiceCommandManager.dispose();

		assertEquals(voiceCommandManager.lastVoiceCommandId, voiceCommandIdMin);
		assertNull(voiceCommandManager.voiceCommands);
		assertNull(voiceCommandManager.oldVoiceCommands);
		assertNull(voiceCommandManager.currentHMILevel);
		assertNull(voiceCommandManager.inProgressUpdate);
		assertFalse(voiceCommandManager.hasQueuedUpdate);
		assertFalse(voiceCommandManager.waitingOnHMIUpdate);
		// after everything, make sure we are in the correct state
		assertEquals(voiceCommandManager.getState(), BaseSubManager.SHUTDOWN);

		super.tearDown();
	}

	public void testStartVoiceCommandManager(){

		voiceCommandManager.start(new CompletionListener() {
			@Override
			public void onComplete(boolean success) {
				assertTrue(success);
				// Make sure the state has changed, as the Screen Manager is dependant on it
				assertEquals(voiceCommandManager.getState(), BaseSubManager.READY);
			}
		});
	}

	public void testHMINotReady(){

		voiceCommandManager.currentHMILevel = HMILevel.HMI_NONE;
		voiceCommandManager.setVoiceCommands(commands);

		// updating voice commands before HMI is ready
		assertNull(voiceCommandManager.inProgressUpdate);
		assertTrue(voiceCommandManager.waitingOnHMIUpdate);
		// these are the 2 commands we have waiting
		assertEquals(voiceCommandManager.voiceCommands.size(), 2);
		assertEquals(voiceCommandManager.currentHMILevel, HMILevel.HMI_NONE);

		// The VCM should send the pending voice commands once HMI full occurs
		sendFakeCoreOnHMIFullNotifications();
		// Listener should be triggered - which sets new HMI level and should proceed to send our pending update
		assertEquals(voiceCommandManager.currentHMILevel, HMILevel.HMI_FULL);
		// This being false means it received the hmi notification and sent the pending commands
		assertFalse(voiceCommandManager.waitingOnHMIUpdate);
	}

	public void testUpdatingCommands(){

		// we have previously sent 2 VoiceCommand objects. we will now update it and have just one

		// make sure the system returns us 2 delete commands
		assertEquals(voiceCommandManager.deleteCommandsForVoiceCommands(commands).size(), 2);
		// when we only send one command to update, we should only be returned one add command
		assertEquals(voiceCommandManager.addCommandsForVoiceCommands(Collections.singletonList(command)).size(), 1);

		// Send a new single command, and test that its listener works, as it gets called from the VCM
		voiceCommandManager.setVoiceCommands(Collections.singletonList(command3));

		// Fake onCommand - we want to make sure that we can pass back onCommand events to our VoiceCommand Objects
		OnCommand onCommand = new OnCommand();
		onCommand.setCmdID(command3.getCommandId());
		onCommand.setTriggerSource(TriggerSource.TS_VR); // these are voice commands
		commandListener.onNotified(onCommand); // send off the notification

		// verify the mock listener has only been hit once
		verify(command3.getVoiceCommandSelectionListener(), times(1)).onVoiceCommandSelected();
	}

	// Emulate what happens when Core sends OnHMIStatus notification
	private void sendFakeCoreOnHMIFullNotifications() {
		OnHMIStatus onHMIStatusFakeNotification = new OnHMIStatus();
		onHMIStatusFakeNotification.setHmiLevel(HMILevel.HMI_FULL);
		onHMIStatusListener.onNotified(onHMIStatusFakeNotification);
	}

}
