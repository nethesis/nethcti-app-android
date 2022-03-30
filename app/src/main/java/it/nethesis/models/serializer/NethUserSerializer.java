package it.nethesis.models.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import it.nethesis.models.NethUser;

public class NethUserSerializer implements JsonDeserializer<NethUser>, JsonSerializer<NethUser> {

    @Override
    public NethUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        NethUser nethUser;
        JsonElement mJson =  JsonParser.parseString(json.toString());
        Gson gson = new Gson();
        nethUser = gson.fromJson(mJson, NethUser.class);
        return nethUser;
    }

    @Override
    public JsonElement serialize(NethUser src, Type typeOfSrc, JsonSerializationContext context) {
        Gson gson = new GsonBuilder().create();
        return gson.toJsonTree(src, NethUser.class);
    }

}
