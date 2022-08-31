package it.nethesis.models.contactlist;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

@Keep
public class ContactList {

    @SerializedName("count")
    @Expose
    private int count;

    @SerializedName("rows")
    @Expose
    private List<Contact> rows = null;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Contact> getRows() {
        return rows;
    }

    public void setRows(List<Contact> rows) {
        this.rows = rows;
    }
}
