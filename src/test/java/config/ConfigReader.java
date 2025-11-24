package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties;

    static {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties = new Properties();
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties. Please ensure it is in src/test/resources/");
                throw new RuntimeException("config.properties not found in classpath.");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load config.properties due to IO error", ex);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
