package org.grupolys.spring.service;

import org.grupolys.spring.service.exception.ConfigServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;

@Service
public class ConfigService {
    public static final String TAGGERS_PATH_KEY = "TAGGERS_PATH";
    public static final String PARSERS_PATH_KEY = "PARSERS_PATH";
    public static final String UUUSA_PATH_KEY = "UUUSA_PATH";
    public static final String PROFILES_PATH_KEY = "PROFILES_PATH";

    // User home directory
    private static final String USER_DIR = System.getProperty("user.dir");

    // Default UUUSA path
    private static final String DEFAULT_UUUSA_PATH = USER_DIR;

    // Actual UUUSA path that will be used
    public static final String UUUSA_PATH = getUUUSAPath(System.getenv(UUUSA_PATH_KEY));

    // UUUSA path where profiles data is stored
    public static final String UUUSA_PROFILES_PATH = Paths.get(UUUSA_PATH,"data", "profiles").toString();

    // UUUSA path where parsers are located
    public static final String UUUSA_PARSERS_PATH = Paths.get(UUUSA_PATH, "data", "parsers").toString();

    // UUUSA path where taggers are located
    public static final String UUUSA_TAGGERS_PATH = Paths.get(UUUSA_PATH,"data", "taggers").toString();

    // UUUSA path where config is stored
    public static final String UUUSA_CONFIG_PATH = Paths.get(UUUSA_PATH, "config").toString();

    @Autowired
    ConfigService() throws ConfigServiceException {
        createApplicationPaths();
    }

    /**
     * Creates application paths.
     * @throws ConfigServiceException is any path could not be created
     */
    private void createApplicationPaths() throws ConfigServiceException {
        String[] applicationPaths = {
            UUUSA_PATH,
            UUUSA_PROFILES_PATH,
            UUUSA_CONFIG_PATH,
            UUUSA_TAGGERS_PATH,
            UUUSA_PARSERS_PATH
        };
        for (String path: applicationPaths) {
            createPath(path);
        }
    }

    /**
     * Creates path directory is does not exists previously.
     * @param path directory to create
     * @throws ConfigServiceException if path could not be created
     */
    private void createPath(String path) throws ConfigServiceException {
        File uuusaPath = new File(path);
        if (!uuusaPath.exists()) {
            if (!uuusaPath.mkdirs()) {
                throw new ConfigServiceException("Unable to create directory: '" + path + "'");
            }
        }
    }

    /**
     * Wraps a default value into an actual value.
     * @param path
     * @return
     */
    private static String getUUUSAPath(String path) {
        if (path == null) {
            return DEFAULT_UUUSA_PATH;
        }

        return path;
    }
}
