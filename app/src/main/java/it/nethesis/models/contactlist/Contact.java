package it.nethesis.models.contactlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Contact {

    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("owner_id")
    @Expose
    private String ownerId;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("homeemail")
    @Expose
    private String homeemail;

    @SerializedName("workemail")
    @Expose
    private String workemail;

    @SerializedName("homephone")
    @Expose
    private String homephone;

    @SerializedName("workphone")
    @Expose
    private String workphone;

    @SerializedName("cellphone")
    @Expose
    private String cellphone;

    @SerializedName("fax")
    @Expose
    private String fax;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("company")
    @Expose
    private String company;

    @SerializedName("notes")
    @Expose
    private String notes;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("homestreet")
    @Expose
    private String homestreet;

    @SerializedName("homepob")
    @Expose
    private String homepob;

    @SerializedName("homecity")
    @Expose
    private String homecity;

    @SerializedName("homeprovince")
    @Expose
    private String homeprovince;

    @SerializedName("homepostalcode")
    @Expose
    private String homepostalcode;

    @SerializedName("homecountry")
    @Expose
    private String homecountry;

    @SerializedName("workstreet")
    @Expose
    private String workstreet;

    @SerializedName("workpob")
    @Expose
    private String workpob;

    @SerializedName("workcity")
    @Expose
    private String workcity;

    @SerializedName("workprovince")
    @Expose
    private String workprovince;

    @SerializedName("workpostalcode")
    @Expose
    private String workpostalcode;

    @SerializedName("workcountry")
    @Expose
    private String workcountry;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("extension")
    @Expose
    private String extension;

    @SerializedName("speeddial_num")
    @Expose
    private String speeddialNum;

    @SerializedName("source")
    @Expose
    private String source;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHomeemail() {
        return homeemail;
    }

    public void setHomeemail(String homeemail) {
        this.homeemail = homeemail;
    }

    public String getWorkemail() {
        return workemail;
    }

    public void setWorkemail(String workemail) {
        this.workemail = workemail;
    }

    public String getHomephone() {
        if (homephone != null) {
            return !homephone.isEmpty() ? homephone : null;
        }
        return null;
    }

    public void setHomephone(String homephone) {
        this.homephone = homephone;
    }

    public String getWorkphone() {
        if (workphone != null) {
            return !workphone.isEmpty() ? workphone : null;
        }
        return null;
    }

    public void setWorkphone(String workphone) {
        this.workphone = workphone;
    }

    public String getCellphone() {
        if (cellphone != null) {
            return !cellphone.isEmpty() ? cellphone : null;
        }
        return null;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getExtension() {
        return extension != null ? (!extension.isEmpty() ? extension : null) : null;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getSpeeddialNum() {
        return speeddialNum;
    }

    public void setSpeeddialNum(String speeddialNum) {
        this.speeddialNum = speeddialNum;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
