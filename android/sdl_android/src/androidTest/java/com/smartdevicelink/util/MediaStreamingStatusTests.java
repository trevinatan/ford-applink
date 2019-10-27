package com.smartdevicelink.util;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

import com.smartdevicelink.AndroidTestCase2;
import com.smartdevicelink.managers.SdlManager;
import com.smartdevicelink.managers.SdlManagerListener;
import com.smartdevicelink.proxy.rpc.enums.AppHMIType;
import com.smartdevicelink.test.Test;
import com.smartdevicelink.transport.MultiplexTransportConfig;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Vector;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class MediaStreamingStatusTests extends AndroidTestCase2 {



    @Mock
    private AudioManager audioManager = mock(AudioManager.class);

    @Mock
    Context mockedContext;

    MediaStreamingStatus defaultMediaStreamingStatus;

    private Answer<Object> onGetSystemService = new Answer<Object>() {
        @Override
        public Object answer(InvocationOnMock invocation) {
            Object[] args = invocation.getArguments();
            String serviceName = (String) args[0];
            if(serviceName != null && serviceName.equalsIgnoreCase(Context.AUDIO_SERVICE)){
                return audioManager;
            }else{
                return null;
            }
        }
    };


    @Override
    public void setUp() throws Exception{
        mockedContext = mock(Context.class);
        doAnswer(onGetSystemService).when(mockedContext).getSystemService(Context.AUDIO_SERVICE);
        defaultMediaStreamingStatus = new MediaStreamingStatus(mockedContext, mock(MediaStreamingStatus.Callback.class));
    }


    public void testEmptyAudioDeviceInfoList(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assertNotNull(mockedContext);
            MediaStreamingStatus mediaStreamingStatus = new MediaStreamingStatus(mockedContext, new MediaStreamingStatus.Callback() {
                @Override
                public void onAudioNoLongerAvailable() {

                }
            });
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return new AudioDeviceInfo[0];
                }
            }).when(audioManager).getDevices(AudioManager.GET_DEVICES_OUTPUTS);


            assertFalse(mediaStreamingStatus.isAudioOutputAvailable());
        }
    }

    public void testNullAudioDeviceInfoList(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assertNotNull(mockedContext);
            MediaStreamingStatus mediaStreamingStatus = new MediaStreamingStatus(mockedContext, mock(MediaStreamingStatus.Callback.class));
            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return null;
                }
            }).when(audioManager).getDevices(AudioManager.GET_DEVICES_OUTPUTS);

            assertFalse(mediaStreamingStatus.isAudioOutputAvailable());
        }
    }


    public void testSdlManagerMedia(){
        SdlManager.Builder builder = new SdlManager.Builder(getContext(), Test.GENERAL_FULL_APP_ID, Test.GENERAL_STRING, mock(SdlManagerListener.class));
        Vector<AppHMIType> appType = new Vector<>();
        appType.add(AppHMIType.MEDIA);
        builder.setAppTypes(appType);
        MultiplexTransportConfig multiplexTransportConfig = new MultiplexTransportConfig(getContext(),Test.GENERAL_FULL_APP_ID);

        assertNull(multiplexTransportConfig.requiresAudioSupport());
        builder.setTransportType(multiplexTransportConfig);

        SdlManager manager = builder.build();
        manager.start();

        //Original reference should be updated
        assertTrue(multiplexTransportConfig.requiresAudioSupport());
    }

    public void testSdlManagerNonMedia(){
        SdlManager.Builder builder = new SdlManager.Builder(getContext(), Test.GENERAL_FULL_APP_ID, Test.GENERAL_STRING, mock(SdlManagerListener.class));
        Vector<AppHMIType> appType = new Vector<>();
        appType.add(AppHMIType.DEFAULT);
        builder.setAppTypes(appType);
        MultiplexTransportConfig multiplexTransportConfig = new MultiplexTransportConfig(getContext(),Test.GENERAL_FULL_APP_ID);

        assertNull(multiplexTransportConfig.requiresAudioSupport());
        builder.setTransportType(multiplexTransportConfig);

        SdlManager manager = builder.build();
        manager.start();

        //Original reference should be updated
        assertFalse(multiplexTransportConfig.requiresAudioSupport());
    }

    public void testAcceptedBTDevices(){
        MediaStreamingStatus mediaStreamingStatus = spy(new MediaStreamingStatus(getContext(), mock(MediaStreamingStatus.Callback.class)));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return true;
            }
        }).when(mediaStreamingStatus).isBluetoothActuallyAvailable();

        assertTrue(mediaStreamingStatus.isBluetoothActuallyAvailable());
        assertTrue(mediaStreamingStatus.isSupportedAudioDevice(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP));
    }

    public void testAcceptedUSBDevices(){
        MediaStreamingStatus mediaStreamingStatus = spy(new MediaStreamingStatus(getContext(), mock(MediaStreamingStatus.Callback.class)));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return true;
            }
        }).when(mediaStreamingStatus).isUsbActuallyConnected();

        assertTrue(mediaStreamingStatus.isUsbActuallyConnected());
        assertTrue(mediaStreamingStatus.isSupportedAudioDevice(AudioDeviceInfo.TYPE_USB_DEVICE));
        assertTrue(mediaStreamingStatus.isSupportedAudioDevice(AudioDeviceInfo.TYPE_USB_ACCESSORY));
        assertTrue(mediaStreamingStatus.isSupportedAudioDevice(AudioDeviceInfo.TYPE_USB_HEADSET));
        assertTrue(mediaStreamingStatus.isSupportedAudioDevice(AudioDeviceInfo.TYPE_DOCK));
    }

    public void testAcceptedLineDevices(){
        assertTrue(defaultMediaStreamingStatus.isSupportedAudioDevice(AudioDeviceInfo.TYPE_LINE_ANALOG));
        assertTrue(defaultMediaStreamingStatus.isSupportedAudioDevice(AudioDeviceInfo.TYPE_LINE_DIGITAL));
        assertTrue(defaultMediaStreamingStatus.isSupportedAudioDevice(AudioDeviceInfo.TYPE_AUX_LINE));
        assertTrue(defaultMediaStreamingStatus.isSupportedAudioDevice(AudioDeviceInfo.TYPE_WIRED_HEADSET));
        assertTrue(defaultMediaStreamingStatus.isSupportedAudioDevice(AudioDeviceInfo.TYPE_WIRED_HEADPHONES));
    }


}
