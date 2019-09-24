package com.itdr.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * 通用的Json与java对象互相转换通用类
 */
public class JsonUtils {


    private static ObjectMapper objectMapper = new ObjectMapper();

//    static {
//        //对象中所有的字段序列化
//        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);
//        //取消默认timestamp格式
//        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
//        //忽略空bean转json错误
//        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
//        //设置日期格式
//        objectMapper.setDateFormat(new SimpleDateFormat(DateUtils.STANDARD_FORMAT));
//        //忽略在json字符串中存在，但是在java中不存在的属性，防止出错
//        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//    }

    /**
     * 将对象转成字符串
     */

    public static <T> String obj2String(T obj) {

        if (obj == null) {
            return null;
        }

        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> String obj2StringPretty(T obj) {

        if (obj == null) {
            return null;
        }

        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 字符串转对象
     */

    public static <T> T string2Obj(String str, Class<T> clazz) {

        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }

        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json数组转集合
     */
    public static <T> T string2Obj(String str, TypeReference<T> typeReference) {

        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }

        try {
            return (T) (typeReference.getType().equals(String.class) ? (T) str : objectMapper.readValue(str, typeReference));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static <T> T string2Obj(String str, Class<?> collectionClass, Class<?>... elements) {

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elements);

        try {
            return objectMapper.readValue(str, javaType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {

    }

}
