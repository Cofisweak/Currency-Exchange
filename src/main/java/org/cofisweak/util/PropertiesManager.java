package org.cofisweak.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.cofisweak.exception.CannotLoadPropertiesException;

import java.io.IOException;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesManager {
    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    @SneakyThrows
    private static void loadProperties() {
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
