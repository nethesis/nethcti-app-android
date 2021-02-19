package it.nethesis.models;

import java.io.Serializable;

public class NethesisNumberOrAddress implements Serializable, Comparable<NethesisNumberOrAddress> {

    private final boolean mIsSIPAddress;
    private String mValue, mOldValueForUpdatePurpose, mType;
    private final String mNormalizedPhone;

    public NethesisNumberOrAddress(String v, boolean isSIP, String type) {
        mValue = v;
        mIsSIPAddress = isSIP;
        mOldValueForUpdatePurpose = null;
        mNormalizedPhone = null;
        mType = type;
    }

    public NethesisNumberOrAddress(String v, String normalizedV) {
        mValue = v;
        mNormalizedPhone = normalizedV != null ? normalizedV : v;
        mIsSIPAddress = false;
        mOldValueForUpdatePurpose = null;
    }

    public NethesisNumberOrAddress(String v, boolean isSip, String old, String type) {
        this(v, isSip, type);
        mOldValueForUpdatePurpose = old;
    }

    @Override
    public int compareTo(NethesisNumberOrAddress noa) {
        if (mValue != null) {
            if (noa.isSIPAddress() && isSIPAddress()) {
                return mValue.compareTo(noa.getValue());
            } else if (!noa.isSIPAddress() && !isSIPAddress()) {
                return getNormalizedPhone().compareTo(noa.getNormalizedPhone());
            }
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != NethesisNumberOrAddress.class) return false;
        NethesisNumberOrAddress noa = (NethesisNumberOrAddress) obj;
        return (this != null && this.compareTo(noa) == 0);
    }

    public boolean isSIPAddress() {
        return mIsSIPAddress;
    }

    public String getOldValue() {
        return mOldValueForUpdatePurpose;
    }

    public void setOldValue(String v) {
        mOldValueForUpdatePurpose = v;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String v) {
        mValue = v;
    }

    public String getNormalizedPhone() {
        return mNormalizedPhone != null ? mNormalizedPhone : mValue;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }
}
