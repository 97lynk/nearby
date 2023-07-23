package io.a97lynk.nearby;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String object2Json(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static <T> T jsonToObject(String value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
