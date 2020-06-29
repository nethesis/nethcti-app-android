package it.nethesis.utils;

import org.linphone.core.Address;

public class CallTransferManager {

    private static CallTransferManager instance;

    private boolean mCallTransfer;
    private Address mTransferCallId;

    private CallTransferManager() {
        mCallTransfer = false;
        mTransferCallId = null;
    }

    public static CallTransferManager instance() {
        if (instance == null) {
            synchronized (CallTransferManager.class) {
                if (instance == null) {
                    instance = new CallTransferManager();
                }
            }
        }

        return instance;
    }

    public boolean ismCallTransfer() {
        return mCallTransfer;
    }

    public void setmCallTransfer(boolean mCallTransfer) {
        this.mCallTransfer = mCallTransfer;
    }

    public Address getmTransferCallId() {
        return mTransferCallId;
    }

    public void setmTransferCallId(Address mTransferCallId) {
        this.mTransferCallId = mTransferCallId;
    }
}
