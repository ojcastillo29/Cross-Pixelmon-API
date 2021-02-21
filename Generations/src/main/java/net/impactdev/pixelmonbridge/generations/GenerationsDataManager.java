package net.impactdev.pixelmonbridge.generations;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.pixelmonbridge.data.common.BaseDataManager;
import net.impactdev.pixelmonbridge.data.factory.JObject;
import net.impactdev.pixelmonbridge.details.Query;
import net.impactdev.pixelmonbridge.details.SpecKey;
import net.impactdev.pixelmonbridge.details.SpecKeys;
import net.impactdev.pixelmonbridge.details.components.generic.JSONWrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Supplier;

public class GenerationsDataManager extends BaseDataManager<GenerationsPokemon> {

    public GenerationsDataManager() {
        this.customReaders.put(SpecKeys.GENERATIONS_DATA, data -> new JSONWrapper().deserialize((JsonObject) data));
    }

    @Override
    public JObject serialize(GenerationsPokemon pokemon) {
        JObject out = new JObject();

        for(Map.Entry<SpecKey<?>, Object> data : pokemon.getAllDetails().entrySet()) {
            Query query = data.getKey().getQuery();
            Object value = data.getValue();

            this.writeToQuery(out, query, value);
        }

        PARENTS.clear();

        return out;
    }

    @Override
    public GenerationsPokemon deserialize(JsonObject json) {
        GenerationsPokemon result = new GenerationsPokemon();
        if(!json.has("species")) {
            throw new IllegalStateException("JSON data is lacking pokemon species");
        }

        List<String> queries = this.track(json);

        for(SpecKey<?> key : SpecKeys.getKeys()) {
            result.offerUnsafe(key, this.translate(json, key));

            queries.removeIf(s -> s.startsWith(key.getQuery().toString()));
        }

        if(!queries.isEmpty()) {
            JObject incompatible = new JObject();
            for(String query : queries) {
                incompatible.add(query, this.getLeaf(json, query));
            }


        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T translate(JsonObject data, SpecKey<T> key) {
        Query query = key.getQuery();
        return (T) this.translate$1(data, query, key);
    }

    private Object translate$1(JsonObject data, Query query, SpecKey<?> key) {
        String index = query.getHead();
        if(!data.has(index)) {
            return null;
        }

        if(query.getParts().size() > 1) {
            return this.translate$1(data.get(index).getAsJsonObject(), query.pop(), key);
        } else {
            if(customReaders.containsKey(key)) {
                return customReaders.get(key).read(data.get(index));
            } else {
                throw new IllegalStateException("Unsure how to deserialize key: " + key.getName());
            }
        }
    }

    private List<String> track(JsonObject json) {
        List<String> track = Lists.newArrayList();
        for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if(entry.getValue().isJsonObject()) {
                track.addAll(this.track$1(entry.getValue().getAsJsonObject(), entry.getKey()));
            } else {
                track.add(entry.getKey());
            }
        }

        return track;
    }

    private List<String> track$1(JsonObject json, String current) {
        List<String> results = Lists.newArrayList();

        for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if(entry.getValue().isJsonObject()) {
                List<String> next = this.track$1(entry.getValue().getAsJsonObject(), entry.getKey());
                next.forEach(s -> {
                    StringJoiner x = new StringJoiner(".");
                    x.add(current);
                    x.add(s);
                    results.add(x.toString());
                });
            } else {
                StringJoiner x = new StringJoiner(".");
                x.add(current);
                x.add(entry.getKey());
                results.add(x.toString());
            }
        }

        return results;
    }

    private static <T> T read(SpecKey<?> key, Supplier<JsonElement> supplier, Function<JsonElement, T> mapper) {
        return Optional.ofNullable(supplier.get()).map(mapper).orElseThrow(() -> new IllegalStateException("Failed to translate data for key: " + key.getName()));
    }

    private static <T> T readAndAllowNull(Supplier<JsonElement> supplier, Function<JsonElement, T> mapper) {
        return Optional.ofNullable(supplier.get()).map(mapper).orElse(null);
    }

    private JsonElement getLeaf(JsonObject json, String query) {
        String[] components = query.split("[.]");
        JsonElement element = json.get(components[0]);
        for(int i = 1; i < components.length; i++) {
            element = element.getAsJsonObject().get(components[i]);
        }

        return element;
    }
}
