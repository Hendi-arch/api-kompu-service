package com.kompu.api.infrastructure.shared;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SharedUseCase {

    private final ObjectMapper objectMapper;

    public SharedUseCase(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Convert object to JSON string
     * 
     * @param obj object to serialize
     * @return JSON string representation
     */
    public String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object to JSON", e);
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Convert JSON string to object of specified type
     * 
     * @param json JSON string
     * @param type target class type
     * @param <T>  generic type parameter
     * @return deserialized object
     */
    public <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON to type: {}", type.getName(), e);
            throw new RuntimeException("Failed to deserialize JSON to type: " + type.getName(), e);
        }
    }

    /**
     * Check if string is null or empty
     * 
     * @param str string to check
     * @return true if null or empty
     */
    public boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if object is null
     * 
     * @param obj object to check
     * @return true if null
     */
    public boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * Check if collection is null or empty
     * 
     * @param collection collection to check
     * @return true if null or empty
     */
    public boolean isNullOrEmpty(java.util.Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Get safe map value with default
     * 
     * @param map          source map
     * @param key          map key
     * @param defaultValue default value if key not found
     * @param <K>          key type
     * @param <V>          value type
     * @return value or default
     */
    public <K, V> V getMapValue(Map<K, V> map, K key, V defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * Convert JSON string to map
     * 
     * @param json JSON string
     * @return map representation
     */
    public Map<String, Object> jsonToMap(String json) {
        if (isNullOrEmpty(json)) {
            return Collections.emptyMap();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(json, Map.class);
            return result;
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to map", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Convert JSON string to list of objects
     * 
     * @param json        JSON array string
     * @param elementType element class type
     * @param <T>         generic type parameter
     * @return list of objects
     */
    public <T> List<T> jsonToList(String json, Class<T> elementType) {
        if (isNullOrEmpty(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to list", e);
            return Collections.emptyList();
        }
    }

    /**
     * Pretty print JSON string (formatted)
     * 
     * @param json JSON string
     * @return formatted JSON
     */
    public String prettyPrintJson(String json) {
        if (isNullOrEmpty(json)) {
            return json;
        }
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error formatting JSON", e);
            return json;
        }
    }

    /**
     * Merge two objects into a map
     * 
     * @param obj1 first object
     * @param obj2 second object
     * @return merged map
     */
    public Map<String, Object> mergeObjects(Object obj1, Object obj2) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map1 = objectMapper.convertValue(obj1, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> map2 = objectMapper.convertValue(obj2, Map.class);

        if (map1 == null) {
            map1 = new java.util.HashMap<>();
        }
        if (map2 == null) {
            map2 = new java.util.HashMap<>();
        }

        map1.putAll(map2);
        return map1;
    }

    /**
     * Check if two objects are equal (null-safe)
     * 
     * @param obj1 first object
     * @param obj2 second object
     * @return true if equal
     */
    public boolean safeEquals(Object obj1, Object obj2) {
        return Objects.equals(obj1, obj2);
    }

    /**
     * Get object map representation (convert object to map)
     * 
     * @param obj object to convert
     * @return map representation
     */
    public Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = objectMapper.convertValue(obj, Map.class);
        return result;
    }

    /**
     * Convert map to object of specified type
     * 
     * @param map  source map
     * @param type target class type
     * @param <T>  generic type parameter
     * @return converted object
     */
    public <T> T mapToObject(Map<String, Object> map, Class<T> type) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return objectMapper.convertValue(map, type);
    }

}
