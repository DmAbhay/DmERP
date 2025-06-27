package dataman.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

//@Component
//public class ExternalConfigAuthService {
//
//    private static final String CONFIG_DIR = "C:/Users/Dataman/myconfigfile";
//
//    // Map of fileName -> Properties
//    private final Map<String, Properties> propertiesMap = new HashMap<>();
//
//    @PostConstruct
//    public void loadAllProperties() throws IOException {
//        File folder = new File(CONFIG_DIR);
//        File[] files = folder.listFiles((dir, name) -> name.endsWith(".properties"));
//
//        if (files == null || files.length == 0) {
//            throw new IllegalStateException("No .properties files found in " + CONFIG_DIR);
//        }
//
//        for (File file : files) {
//            Properties props = new Properties();
//            try (FileInputStream fis = new FileInputStream(file)) {
//                props.load(fis);
//                propertiesMap.put(file.getName(), props);
//            }
//        }
//    }
//
//    /**
//     * Get property value from a specific file
//     * @param fileName e.g. "myconfig1.properties"
//     * @param key      e.g. "sqlHostName"
//     * @return value or null
//     */
//    public String getProperty(String fileName, String key) {
//        Properties props = propertiesMap.get(fileName);
//        return props != null ? props.getProperty(key) : null;
//    }
//
//    public Set<String> getLoadedFiles() {
//        return propertiesMap.keySet();
//    }
//}






@Component
public class ExternalConfigAuthMService {

    //private static final String CONFIG_DIR = "C:/Users/Dataman/myconfigfile";
    private static final String CONFIG_DIR = System.getProperty("CONFIG_DIR");

    // Map of fileName -> Properties
    private final Map<String, Properties> propertiesMap = new HashMap<>();

    @PostConstruct
    public void loadAllProperties() throws IOException {
        File folder = new File(CONFIG_DIR);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".properties"));

        if (files == null || files.length == 0) {
            throw new IllegalStateException("No .properties files found in " + CONFIG_DIR);
        }

        for (File file : files) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                propertiesMap.put(file.getName(), props);
            }
        }
    }

    /**
     * Get a specific key from a specific .properties file
     */
    public String getProperty(String fileName, String key) {
        Properties props = propertiesMap.get(fileName);
        return props != null ? props.getProperty(key) : null;
    }

    /**
     * ✅ New method to return the entire Properties object
     * for a given file name.
     */
    public Properties getProperties(String fileName) {
        return propertiesMap.get(fileName);
    }

    public Set<String> getLoadedFiles() {
        return propertiesMap.keySet();
    }
}


