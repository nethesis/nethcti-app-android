package org.linphone.assistant;

/*
QrCodeFragment.java
Copyright (C) 2018  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import org.linphone.LinphoneManager;
import org.linphone.R;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;

public class QrCodeFragment extends Fragment {
    private CoreListenerStub mListener;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qrcode, container, false);

        TextureView mQrcodeView = view.findViewById(R.id.qrcodeCaptureSurface);

        LinphoneManager.getLc().setNativePreviewWindowId(mQrcodeView);

        mListener =
                new CoreListenerStub() {
                    @Override
                    public void onQrcodeFound(Core lc, String result) {
                        enableQrcodeReader(false);
                        /*
                         * Display Remote Provisioning was the old implementation.
                         * Now we go to Login Fragment.
                         */
                        // AssistantActivity.instance().displayRemoteProvisioning(result);
                        AssistantActivity.instance().displayLoginGeneric(result);
                    }
                };

        return view;
    }

    private void enableQrcodeReader(boolean enable) {
        LinphoneManager.getLc().enableQrcodeVideoPreview(enable);
        LinphoneManager.getLc().enableVideoPreview(enable);
        if (enable) {
            LinphoneManager.getLc().addListener(mListener);
        } else {
            LinphoneManager.getLc().removeListener(mListener);
        }
    }

    private void setBackCamera() {
//        int camId = 0;
//        AndroidCameraConfiguration.AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
//        for (AndroidCameraConfiguration.AndroidCamera camera : cameras) {
//            if (!camera.frontFacing) camId = camera.id;
//        }
        String[] devices = LinphoneManager.getLc().getVideoDevicesList();
        String cameraDevice = devices[0];
        for (String device : devices) {
            if(device.contains("Back")) cameraDevice = device;
        }
//        String newDevice = devices[camId];
        LinphoneManager.getLc().setVideoDevice(cameraDevice);
    }

    private void launchQrcodeReader() {
        setBackCamera();

        enableQrcodeReader(true);
    }

    @Override
    public void onResume() {
        launchQrcodeReader();
        super.onResume();
    }

    @Override
    public void onPause() {
        enableQrcodeReader(false);
        // setBackCamera(false);
        super.onPause();
    }
}
