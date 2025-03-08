package cn.techwolf.server.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String fetchString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T fetchObject(String content, Class<T> valueType) throws Exception {
        return objectMapper.readValue(content, valueType);
    }

    // 新增支持 JavaType 的方法
    public static <T> T fetchObject(String content, JavaType valueType) throws Exception {
        return objectMapper.readValue(content, valueType);
    }
}