package com.itdr.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    public static String getValue(String key) throws IOException {
        Properties p = new Properties();
        InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream("sets.properties");
        p.load(in);
        String property = p.getProperty(key);
        return property;
    }
}
