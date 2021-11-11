package util;

import com.google.gson.*;
import models.Location;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

public class YelpAPIParser {
	private static final String AUTH_KEY = "TWG6JZ1MVXlPOKq3g2e7yxdBbrOxHuoPkWdVcPlxguxR8wpU_N18DlFo5u9mG2kFvHdeqafhJDCyLXwBXCqutJOyMoZhxIKMO6AULvsttz3fUCPLKF6Vpm7xbNeBYXYx";
    public static Location getLocation(String restaurant, Location location) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            String builder = "https://api.yelp.com/v3/businesses/search" + "?term=" + restaurant +
                    "&latitude=" + location.getLatitude() + "&longitude=" + location.getLongitude();
            Request request = new Request.Builder().url(builder).method("GET", null).addHeader(
                    "Authorization",
                    "Bearer " + AUTH_KEY)
                    .build();
            Response response = client.newCall(request).execute();
            String responseString = Objects.requireNonNull(response.body()).string();
            if(responseString.contains("error")){
                System.out.println("Yelp API Failure.");
                return null;
            }
            Gson gson = new GsonBuilder().registerTypeAdapter(Location.class, new MyDeserializer()).create();

            Location val = gson.fromJson(responseString, Location.class);
            return val;

            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }
}

// Code adapted from https://stackoverflow.com/questions/23070298/get-nested-json-object-with-gson-using-retrofit
class MyDeserializer implements JsonDeserializer<Location> {
    @Override
    public Location deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        JsonElement content = je.getAsJsonObject().getAsJsonArray("businesses").get(0).getAsJsonObject()
                .get("coordinates");

        return new Gson().fromJson(content, Location.class);
    }
}
