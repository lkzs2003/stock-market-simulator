package org.example.market.service;

import com.google.gson.*;
import org.example.market.model.StockTrader;
import org.example.market.model.Trader;

import java.lang.reflect.Type;

public class TraderAdapter implements JsonSerializer<Trader>, JsonDeserializer<Trader> {

    @Override
    public JsonElement serialize(Trader src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = context.serialize(src).getAsJsonObject();
        jsonObject.addProperty("type", src.getClass().getSimpleName());
        return jsonObject;
    }

    @Override
    public Trader deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Get the type of the element
        String type = jsonObject.has("type") ? jsonObject.get("type").getAsString() : "StockTrader";

        Class<? extends Trader> clazz;

        switch (type) {
            case "StockTrader":
                clazz = StockTrader.class;
                break;
            default:
                throw new JsonParseException("Unknown element type: " + type);
        }

        return context.deserialize(jsonObject, clazz);
    }
}
