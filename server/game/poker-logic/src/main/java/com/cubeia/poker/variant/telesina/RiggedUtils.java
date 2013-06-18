/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.poker.variant.telesina;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * @author Jeremy Comte
 */
public class RiggedUtils {
    private static Properties settings = null;
    private static File settingsFile = null;
    private static long settingsTimestamp = 0;
    private static Logger logger = Logger.getLogger(RiggedUtils.class);

    /**
     * Loads the specified .properties file into the specified Properties object
     *
     * @param file       the file to load
     * @param properties the old properties
     * @return the new properties
     */
    private static Properties loadPropertiesFromFile(File file, Properties properties) {
        InputStream is = null;
        try {
            if (file.exists()) {
                is = new FileInputStream(file);
                properties.load(is);
                is.close();
            } else {
                logger.debug("File " + file + " not found");
            }
        } catch (FileNotFoundException e) {
            logger.debug("File " + file + " not found");
        } catch (IOException e) {
            logger.debug("Can't read " + file + "");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.debug("Failed closing stream.");
                }
            }
        }
        return properties;
    }

    /**
     * Loads the settings file into a new Properties object
     *
     * @param path the path to the settings file to load
     * @return the loaded Properties or a new empty if the file does not exist or can't be read
     */
    public static Properties loadSettingsFromFile(String path) {
        return loadSettingsFromFile(new File(path));
    }

    /**
     * Loads the settings file into a new Properties object
     *
     * @param file the settings file to load
     * @return the loaded Properties or a new empty if the file does not exist or can't be read
     */
    public static Properties loadSettingsFromFile(File file) {
        settings = loadPropertiesFromFile(file, new Properties());
        settingsFile = file;
        return settings;
    }

    /**
     * Reloads the settings file
     *
     * @return the loaded Properties or a new empty if the file does not exist or can't be read
     */
    public static void reloadSettings() {
        if (settingsFile != null && settingsFile.lastModified() > settingsTimestamp) {
            settings = loadPropertiesFromFile(settingsFile, new Properties());
            settingsTimestamp = settingsFile.lastModified();
        }
    }

    /**
     * Gets the settings property list
     *
     * @return the settings or a new empty if the property list is null
     */
    public static Properties getSettings() {
        reloadSettings();
        if (settings == null) {
            return new Properties();
        }
        return settings;
    }

    public static Double getPropertyAsDouble(String property) {
        return getPropertyAsDouble(property, null);
    }

    public static Double getPropertyAsDouble(String property, Double defaultValue) {
        Double value = defaultValue;
        try {
            value = Double.parseDouble(getSettings().getProperty(property));
        } catch (Exception e) {
            logger.error("can't read the property " + property + " from file " + settingsFile.getName());
        }
        return value;
    }

    public static Long getPropertyAsLong(String property) {
        return getPropertyAsLong(property, null);
    }

    public static Long getPropertyAsLong(String property, Long defaultValue) {
        Long value = defaultValue;
        try {
            value = Long.parseLong(getSettings().getProperty(property));
        } catch (Exception e) {
            logger.error("can't read the property " + property + " from file " + settingsFile.getName());
        }
        return value;
    }

    public static Integer getPropertyAsInteger(String property) {
        return getPropertyAsInteger(property, null);
    }

    public static Integer getPropertyAsInteger(String property, Integer defaultValue) {
        Integer value = defaultValue;
        try {
            value = Integer.parseInt(getSettings().getProperty(property));
        } catch (Exception e) {
            logger.error("can't read the property " + property + " from file " + settingsFile.getName());
        }
        return value;
    }

}
