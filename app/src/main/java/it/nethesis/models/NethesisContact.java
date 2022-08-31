package it.nethesis.models;

import java.util.ArrayList;
import java.util.List;
import org.linphone.R;
import org.linphone.contacts.LinphoneContact;

public class NethesisContact extends LinphoneContact {
    private int id;
    private String type,
            workemail,
            fax,
            title,
            email,
            notes,
            source,
            ownerId,
            homeemail,
            homestreet,
            homepob,
            homecity,
            homeprovince,
            homepostalcode,
            homecountry,
            workstreet,
            workpob,
            workcity,
            workprovince,
            workpostalcode,
            workcountry,
            url,
            speeddialNum;
    private List<NethesisNumberOrAddress> mAddresses;
    private boolean mHasExtensions;

    public NethesisContact() {
        id = 0;
        type = null;
        workemail = null;
        fax = null;
        title = null;
        email = null;
        notes = null;
        source = null;
        ownerId = null;
        homeemail = null;
        homestreet = null;
        homepob = null;
        homecity = null;
        homeprovince = null;
        homepostalcode = null;
        homecountry = null;
        workstreet = null;
        workpob = null;
        workcity = null;
        workprovince = null;
        workpostalcode = null;
        workcountry = null;
        url = null;
        speeddialNum = null;
        mAddresses = new ArrayList<>();
        mHasExtensions = false;
    }

    public void addNumberOrAddress(NethesisNumberOrAddress noa) {
        if (noa == null) return;
        if (noa.isSIPAddress()) {
            mHasExtensions = true;
            mAddresses.add(noa);
        } else {
            boolean found = false;
            // Check for duplicated phone numbers but with different formats
            for (NethesisNumberOrAddress number : mAddresses) {
                if (!number.isSIPAddress()
                        && noa.getNormalizedPhone().equals(number.getNormalizedPhone())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                mAddresses.add(noa);
            }
        }
    }

    public synchronized boolean hasAddress(String address) {
        for (NethesisNumberOrAddress noa : getNethesisNumbersOrAddresses()) {
            if (noa.isSIPAddress()) {
                String value = noa.getValue();
                if (address.startsWith(value) || value.equals("sip:" + address)) {
                    // Startswith is to workaround the fact that the
                    // address may have a ;gruu= at the end...
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized List<NethesisNumberOrAddress> getNethesisNumbersOrAddresses() {
        return mAddresses;
    }

    public synchronized void addOrUpdateNumberOrAddress(NethesisNumberOrAddress noa) {
        if (noa != null && noa.getValue() != null) {

            if (noa.isSIPAddress()) {
                if (!noa.getValue().startsWith("sip:")) {
                    noa.setValue("sip:" + noa.getValue());
                }
            }
            if (noa.getOldValue() != null) {
                if (noa.isSIPAddress()) {
                    if (!noa.getOldValue().startsWith("sip:")) {
                        noa.setOldValue("sip:" + noa.getOldValue());
                    }
                }
                for (NethesisNumberOrAddress address : mAddresses) {
                    if (noa.getOldValue().equals(address.getValue())
                            && noa.isSIPAddress() == address.isSIPAddress()) {
                        address.setValue(noa.getValue());
                        break;
                    }
                }
            } else {
                mAddresses.add(noa);
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWorkemail() {
        return workemail;
    }

    public void setWorkemail(String workemail) {
        this.workemail = workemail;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getHomeemail() {
        return homeemail;
    }

    public void setHomeemail(String homeemail) {
        this.homeemail = homeemail;
    }

    public String getHomestreet() {
        return homestreet;
    }

    public void setHomestreet(String homestreet) {
        this.homestreet = homestreet;
    }

    public String getHomepob() {
        return homepob;
    }

    public void setHomepob(String homepob) {
        this.homepob = homepob;
    }

    public String getHomecity() {
        return homecity;
    }

    public void setHomecity(String homecity) {
        this.homecity = homecity;
    }

    public String getHomeprovince() {
        return homeprovince;
    }

    public void setHomeprovince(String homeprovince) {
        this.homeprovince = homeprovince;
    }

    public String getHomepostalcode() {
        return homepostalcode;
    }

    public void setHomepostalcode(String homepostalcode) {
        this.homepostalcode = homepostalcode;
    }

    public String getHomecountry() {
        return homecountry;
    }

    public void setHomecountry(String homecountry) {
        this.homecountry = homecountry;
    }

    public String getWorkstreet() {
        return workstreet;
    }

    public void setWorkstreet(String workstreet) {
        this.workstreet = workstreet;
    }

    public String getWorkpob() {
        return workpob;
    }

    public void setWorkpob(String workpob) {
        this.workpob = workpob;
    }

    public String getWorkcity() {
        return workcity;
    }

    public void setWorkcity(String workcity) {
        this.workcity = workcity;
    }

    public String getWorkprovince() {
        return workprovince;
    }

    public void setWorkprovince(String workprovince) {
        this.workprovince = workprovince;
    }

    public String getWorkpostalcode() {
        return workpostalcode;
    }

    public void setWorkpostalcode(String workpostalcode) {
        this.workpostalcode = workpostalcode;
    }

    public String getWorkcountry() {
        return workcountry;
    }

    public void setWorkcountry(String workcountry) {
        this.workcountry = workcountry;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSpeeddialNum() {
        return speeddialNum;
    }

    public void setSpeeddialNum(String speeddialNum) {
        this.speeddialNum = speeddialNum;
    }

    public boolean hasAddress() {
        return mHasExtensions;
    }

    public int GetResourceByString(String type) {
        int resource = 0;
        switch (type) {
            case "workPhone":
                resource = R.string.work_tel;
                break;
            case "homePhone":
                resource = R.string.home_tel;
                break;
            case "cellPhone":
                resource = R.string.cell;
                break;
        }
        return resource;
    }
}
