package invmod.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

public class Config {
    private Properties properties;
    private Set<String> keys;

    public void loadConfig(File configFile) {
        InvasionMod.log("Loading config");
        properties = new Properties();
        try {
            if (!configFile.exists()) {
                InvasionMod.LOGGER.info("Config not found. Creating file '" + configFile.getName() + "' in minecraft directory");
                if (!configFile.createNewFile()) {
                    InvasionMod.LOGGER.info("Unable to create new config file.");
                }
            } else {
                try (FileReader configRead = new FileReader(configFile)) {
                    this.properties.load(configRead);
                    this.keys = properties.keySet().stream().map(i -> i.toString()).collect(Collectors.toSet());
                }
            }

        } catch (IOException e) {
            InvasionMod.LOGGER.error("Proceeding with default config", e);
        }
    }

    public void writeProperty(BufferedWriter writer, String key) throws IOException {
        writeProperty(writer, key, null);
    }

    public void writeProperty(BufferedWriter writer, String key, String comment) throws IOException {
        if (comment != null) {
            writeLine(writer, "# " + comment);
        }
        writeLine(writer, key + "=" + properties.getProperty(key));
    }

    protected void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    public void setProperty(String key, String value) {
        this.properties.setProperty(key, value);
    }

    public Set<String> keySet() {
        return keys;
    }

    public String getProperty(String key, String defaultValue) {
        return this.properties.getProperty(key, defaultValue);
    }

    public int getPropertyValueInt(String keyName, int defaultValue) {
        String property = properties.getProperty(keyName, null);
        if (!property.equals("null")) {
            return Integer.parseInt(property);
        }

        properties.setProperty(keyName, Integer.toString(defaultValue));
        return defaultValue;
    }

    public float getPropertyValueFloat(String keyName, float defaultValue) {
        @Nullable String property = properties.getProperty(keyName, null);
        if (property != null) {
            return Float.parseFloat(property);
        }

        properties.setProperty(keyName, Float.toString(defaultValue));
        return defaultValue;
    }

    public boolean getPropertyValueBoolean(String keyName, boolean defaultValue) {
        @Nullable String property = properties.getProperty(keyName, null);
        if (property != null) {
            return Boolean.parseBoolean(property);
        }

        properties.setProperty(keyName, Boolean.toString(defaultValue));
        return defaultValue;
    }

    public String getPropertyValueString(String keyName, String defaultValue) {
        @Nullable String property = properties.getProperty(keyName, null);
        if (property != null) {
            return property;
        }

        properties.setProperty(keyName, defaultValue);
        return defaultValue;
    }
}