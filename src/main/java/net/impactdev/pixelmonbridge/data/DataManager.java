package net.impactdev.pixelmonbridge.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.impactdev.pixelmonbridge.ImpactDevPokemon;
import net.impactdev.pixelmonbridge.data.factory.JArray;
import net.impactdev.pixelmonbridge.data.factory.JElement;
import net.impactdev.pixelmonbridge.data.factory.JObject;
import net.impactdev.pixelmonbridge.details.Query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface DataManager<P extends ImpactDevPokemon<?>> {

    JObject serialize(P pokemon);

    P deserialize(JsonObject json);

    Map<String, JObject> PARENTS = Maps.newHashMap();

    /**
     * Recursively pieces together a Query into a writable JObject, with direction of writing
     * primarily focused on the last index of a Query.
     *
     * This function will read a Query from the top level all the way to its final layer, in which
     * we will then write the passed in object to the last component of the Query.
     *
     * @param parent The primary focus of the JSON structure we are writing to
     * @param query The current query representing how far we are in the intended structure
     * @param toWrite The value we wish to write to the index marked by the Query
     * @return The JObject representing the final result of the JSON output
     */
    default JObject writeToQuery(JObject parent, Query query, Object toWrite) {
        if(query.getParts().size() <= 1) {
            write(parent, query.getParts().get(query.getParts().size() - 1), toWrite);
            return parent;
        }

        String key = query.getParts().get(0);
        if(PARENTS.containsKey(key)) {
            parent.add(key, this.writeToQuery(PARENTS.get(key), query.pop(), toWrite));
        } else {
            JObject working = new JObject();
            parent.add(key, this.writeToQuery(working, query.pop(), toWrite));
            PARENTS.put(key, working);
        }

        return parent;
    }

    default void write(@Nonnull JElement element, @Nullable String key, @Nonnull Object value) {
        if(element instanceof JObject) {
            JObject writer = (JObject) element;
            Preconditions.checkNotNull(key);
            Preconditions.checkNotNull(value, "Value for key '" + key + "' is null...");

            if (value instanceof String) {
                writer.add(key, (String) value);
            } else if (value instanceof Number) {
                writer.add(key, (Number) value);
            } else if (value instanceof Boolean) {
                writer.add(key, (Boolean) value);
            } else if (value instanceof Iterable) {
                writer.add(key, toArray((Iterable<?>) value));
            } else if(value instanceof Map) {
                writer.add(key, toMap((Map<?, ?>) value));
            } else if(value instanceof Writable) {
                writer.add(key, ((Writable<?>)value).serialize());
            } else {
                throw new IllegalArgumentException("Unable to translate object to JSON: " + value);
            }
        } else if(element instanceof JArray) {
            JArray writer = (JArray) element;

            if (value instanceof String) {
                writer.add((String) value);
            } else if (value instanceof Number) {
                writer.add((Number) value);
            } else if (value instanceof Iterable) {
                writer.add(toArray((Iterable<?>) value));
            } else if(value instanceof Map) {
                writer.add(toMap((Map<?, ?>) value));
            } else {
                throw new IllegalArgumentException("Unable to translate object to JSON: " + value);
            }
        }
    }

    default JArray toArray(Iterable<?> iterable) {
        JArray array = new JArray();
        for(Object value : iterable) {
            write(array, null, value);
        }

        return array;
    }

    default JObject toMap(Map<?, ?> map) {
        JObject mapped = new JObject();
        for(Map.Entry<?, ?> entry : map.entrySet()) {
            write(mapped, entry.getKey().toString(), entry.getValue());
        }

        return mapped;
    }

}