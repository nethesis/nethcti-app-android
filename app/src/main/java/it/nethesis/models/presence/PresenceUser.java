package it.nethesis.models.presence;

import static it.nethesis.models.decorator.MainPresenceDecorator.MAIN_PRESENCE_STATUS_DISABLED;
import static it.nethesis.models.decorator.PresenceDecorator.PRESENCE_STATUS_DISABLED;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.linphone.BuildConfig;
import org.linphone.utils.FileManager;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import it.nethesis.models.Endpoints;
import it.nethesis.models.NethProfile;
import it.nethesis.models.NethUser;


public class PresenceUser {

    public PresenceUser(
            String name,
            String mainPresence,
            String username,
            Endpoints endpoints,
            NethProfile profile
    ) {
        this.name = name;
        this.mainPresence = mainPresence;
        this.username = username;
        this.endpoints = endpoints;
        this.profile = profile;
    }

    @SerializedName("name")
    public String name = "";

    @SerializedName("mainPresence")
    public String mainPresence = "";

    @SerializedName("username")
    public String username;

    @SerializedName("presence")
    public String presence;

    @SerializedName("endpoints")
    public Endpoints endpoints;

    @SerializedName("profile")
    public NethProfile profile;

    // public String presenceOnBusy;

    // public String presenceOnUnavailable;

    // public Profile profile;

    // public DefaultDevice defaultDevice;

    // public Settings_ settings;

    public String getMainPresence(boolean disabled) {
        return disabled ? MAIN_PRESENCE_STATUS_DISABLED : mainPresence;
    }

    public String getInitials() {
        if (name.isEmpty()) return "";
        int idxLastWhitespace = name.lastIndexOf(' ');

        if (idxLastWhitespace == -1)
            return name.substring(0, 1).toUpperCase();

        try {
            return name.substring(0, 1).toUpperCase() + name.substring(idxLastWhitespace + 1, idxLastWhitespace + 2).toUpperCase();
        } catch (Exception e) {
            e.fillInStackTrace();
            return name.substring(0, 1).toUpperCase();
        }

    }

    private static File getPresenceFavoritesFile(Context context) {
        File path = new File(context.getFilesDir(), SECURE_FOLDER);
        return new File(path, SECURE_FILE);
    }

    public static void removeAllFavorites(Context context) {
        File file = getPresenceFavoritesFile(context);
        boolean deleted = file.delete();
        if(BuildConfig.DEBUG) Log.e("remove favorites result", String.valueOf(deleted));
    }

    private static void saveFavoritesFile(Context context, String json, File store) {
        FileManager fileManager = new FileManager();
        fileManager.writeToFile(json, store.getParent(), store.getName());
    }

    private static ArrayList<String> restoreFavorites(Context context, File store) {
        if(!store.exists()) return new ArrayList<>();
        FileManager fileManager = new FileManager();

        Type collectionType = new TypeToken<Collection<String>>(){}.getType();
        String json = fileManager.readJSONFromFile(store);
        return new Gson().fromJson(
                json,
                collectionType
        );
    }

    private static ArrayList<String> getFavoritesFromFile(Context context) {
        return restoreFavorites(
                context,
                getPresenceFavoritesFile(context)
        );
    }

    public static void putFavorite(Context context, String username) {
        ArrayList<String> usersPrefs = getFavoritesFromFile(context);
        usersPrefs.add(username);
        saveFavoriteList(context, usersPrefs);
    }

    public static void removeFavorite(Context context, String username) {
        ArrayList<String> usersPrefs = getFavoritesFromFile(context);
        usersPrefs.remove(username);
        saveFavoriteList(context, usersPrefs);
    }

    public boolean checkIfSelected(Context context) {
        ArrayList<String> usersPrefs = getFavoritesFromFile(context);

        if (usersPrefs.isEmpty()) return false;
        for (String username : usersPrefs) {
            if (this.username.equals(username))
                return true;
        }

        return false;
    }

    public static void saveFavoriteList(Context context, ArrayList<String> usersPrefs) {
        saveFavoritesFile(
                context,
                new Gson().toJson(usersPrefs),
                getPresenceFavoritesFile(context)
        );
    }

    public static ArrayList<PresenceUser> getFavoritesContacts(
            Context context,
            ArrayList<PresenceUser> presenceUsers
    ) {
        ArrayList<PresenceUser> contactsByFavorites = new ArrayList();
        ArrayList<String> favoritesStrings = getFavoritesFromFile(context);

        if (!favoritesStrings.isEmpty()) {
            for (String username : favoritesStrings) {
                if (presenceUsers.isEmpty()) return contactsByFavorites;
                for (PresenceUser user : presenceUsers) {
                    if (username.equals(user.username)) {
                        contactsByFavorites.add(user);
                        break;
                    }
                }
            }
        }

        return contactsByFavorites;
    }

    public static boolean getActionCall(String status){
        return status.equals(NethUser.STATUS_ONLINE) ||
                status.equals(NethUser.STATUS_CELLPHONE) ||
                status.equals(NethUser.STATUS_VOICEMAIL) ||
                status.equals(NethUser.STATUS_CALL_FORWARD);
    }

    public static boolean getActionBusy(String status){
        return status.equals(NethUser.STATUS_BUSY);
    }

    public static boolean getActionOnline(String status){
        return status.equals(NethUser.STATUS_ONLINE);
    }

    public static boolean getActionCellphone(String status){
        return status.equals(NethUser.STATUS_CELLPHONE);
    }

    public static boolean getActionIncoming(String status){
        return status.equals(NethUser.STATUS_RINGING);
    }

    public static final String SECURE_FOLDER = "/presence/";
    public static final String SECURE_FILE = "presence_favorites.json";

}
