package it.nethesis.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.linphone.BuildConfig;
import org.linphone.utils.FileManager;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class NethPermissionWithOpGroups {

    public NethPermission nethPermission;
    public HashMap<String, OpGroupsUsers> opGroupUsers = new HashMap<>();

    public NethPermissionWithOpGroups(
            NethPermission _nethPermission,
            HashMap<String, OpGroupsUsers> _opGroupUsers
    ){
        this.nethPermission = _nethPermission;
        this.opGroupUsers = _opGroupUsers;
    }

    public String getFirstNethPermissionKeyString() {
        if (opGroupUsers == null || opGroupUsers.isEmpty()) return "";
        return (String) opGroupUsers.keySet().toArray()[0];
    }

    public static void sortListOfNethPermissionWithOpGroups(
            ArrayList<NethPermissionWithOpGroups> list
    ) {
        Collections.sort(list, new Comparator<NethPermissionWithOpGroups>() {
            @Override
            public int compare(NethPermissionWithOpGroups neth1, NethPermissionWithOpGroups neth2) {
                String keyToString1 = neth1.getFirstNethPermissionKeyString();
                String keyToString2 = neth2.getFirstNethPermissionKeyString();
                return keyToString1.compareToIgnoreCase(keyToString2);
            }
        });
    }

    /**
     * Assegna ad ogni NethPermission il gruppo di Utenti corrispondente
     * */
    public static ArrayList<NethPermissionWithOpGroups> getNethsWithAssignedGroup(
            ArrayList<NethPermission> neths,
            HashMap<String, OpGroupsUsers> opGroupUsers
    ) {
        ArrayList<NethPermissionWithOpGroups> nethsWithAssignedGroup = new ArrayList<>();

        for (String key : opGroupUsers.keySet()) {
            for (NethPermission nethPermission : neths) {
                if (groupNameToGroupId(key).equals(nethPermission.name) && nethPermission.value) {
                    HashMap<String, OpGroupsUsers> map = new HashMap<>();
                    map.put(key, opGroupUsers.get(key));

                    nethsWithAssignedGroup.add(new NethPermissionWithOpGroups(
                            nethPermission,
                            map
                    ));
                    break;
                }
            }
        }
        return nethsWithAssignedGroup;
    }

    private static String groupNameToGroupId(String groupName) {
        return "grp_" + groupName.replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase()
                .trim();
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static File getGroupsUserFile(Context context) {
        File path = new File(context.getFilesDir(), SECURE_FOLDER);
        return new File(path, SECURE_FILE);
    }

    public static void saveGroupUser(Context context, String json, File store) {
        FileManager fileManager = new FileManager();
        fileManager.writeToFile(json, store.getParent(), store.getName());
    }

    public static NethPermissionWithOpGroups restoreGroupUser(Context context, File store) {
        if(!store.exists()) return null;
        FileManager fileManager = new FileManager();

        String json = fileManager.readJSONFromFile(store);
        return new Gson().fromJson(json, NethPermissionWithOpGroups.class);
    }

    public static ArrayList<NethPermissionWithOpGroups> restoreGroupsUser(Context context, File store) {
        if(!store.exists()) return new ArrayList<>();
        FileManager fileManager = new FileManager();

        Type collectionType = new TypeToken<Collection<NethPermissionWithOpGroups>>(){}.getType();
        String json = fileManager.readJSONFromFile(store);
        return new Gson().fromJson(
                json,
                collectionType
        );
    }

    public static void removeSelected(Context context) {
        File file = getGroupsUserFile(context);
        boolean deleted = file.delete();
        if(BuildConfig.DEBUG) Log.e("remove selected group", String.valueOf(deleted));
    }

    public static final String SECURE_FOLDER = "/groups/";
    public static final String SECURE_FILE = "associated_groups.json";

}
