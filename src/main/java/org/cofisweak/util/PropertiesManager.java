package org.cofisweak.util;

import org.cofisweak.exception.CannotLoadPropertiesException;

import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {
    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            loadProperties();
        } catch (CannotLoadPropertiesException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadProperties() throws CannotLoadPropertiesException {
        var stream = PropertiesManager.class.getClassLoader().getResourceAsStream("application.properties");
        try {
            PROPERTIES.load(stream);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new CannotLoadPropertiesException("Unable to load server properties");
        }
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }
}
