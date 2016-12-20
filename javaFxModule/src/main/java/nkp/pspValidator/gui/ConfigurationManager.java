package nkp.pspValidator.gui;

import nkp.pspValidator.shared.Platform;

import java.io.*;
import java.util.Properties;

/**
 * Created by martin on 2.12.16.
 */
public class ConfigurationManager {

    public static boolean DEV_MODE = true;

    private static final String DEFAULT_LOG_DIR = "logs";
    private static final String DEFAULT_FDMF_DIR = "fDMF";

    private static File CONFIG_FILE_PRODUCTION = new File("config.properties");
    private static File CONFIG_FILE_DEV_WIN = new File("../../resources/main/dev/config-win.properties");
    private static File CONFIG_FILE_DEV_MAC = new File("../../resources/main/dev/config-mac.properties");
    private static File CONFIG_FILE_DEV_LINUX = new File("../../resources/main/dev/config-linux.properties");

    //fdmf
    public static final String PROP_DMF_DIR = "fdmf.dir";

    //image tools
    public static final String PROP_IMAGE_TOOLS_CHECK_SHOWN = "image_tools_check.shown";
    public static final String PROP_JHOVE_DIR = "jhove.dir";
    public static final String PROP_JPYLYZER_DIR = "jpylyzer.dir";
    public static final String PROP_IMAGE_MAGICK_DIR = "imageMagick.dir";
    public static final String PROP_KAKADU_DIR = "kakadu.dir";

    //validation
    public static final String PROP_LAST_PSP_DIR = "last.psp.dir";
    public static final String PROP_FORCE_MON_VERSION = "force.monograph.version";
    public static final String PROP_FORCE_PER_VERSION = "force.periodical.version";
    public static final String PROP_PSP_VALIDATION_CREATE_TXT_LOG = "psp_validation.create_txt_log";
    public static final String PROP_PSP_VALIDATION_CREATE_XML_LOG = "psp_validation.create_xml_log";
    public static final String PROP_LOG_DIR = "validation.log_dir";

    private final Platform platform;
    private final File configFile;
    private final Properties properties = new Properties();

    public ConfigurationManager(Platform platform) throws IOException {
        try {
            this.platform = platform;
            this.configFile = selectConfigFile();
            //System.out.println("path: " + new File(".").getAbsolutePath());
            //System.out.println("config file: " + configFile.getAbsolutePath());
            loadProperties();
            initDefaultProperties();
        } catch (IOException e) {
            throw new IOException(new File(".").getAbsolutePath(), e);
        }
    }

    private void initDefaultProperties() {
        //fDMF dir
        File fdmfDir = getFileOrNull(PROP_DMF_DIR);
        if (fdmfDir == null) {
            fdmfDir = new File(DEFAULT_FDMF_DIR);
            setFile(PROP_DMF_DIR, fdmfDir);
        }
        //log dir
        File logDir = getFileOrNull(PROP_LOG_DIR);
        if (logDir == null) {
            logDir = new File(DEFAULT_LOG_DIR);
            setFile(PROP_LOG_DIR, logDir);
        }
    }

    private File selectConfigFile() {
        if (DEV_MODE) {
            switch (platform.getOperatingSystem()) {
                case LINUX:
                    return CONFIG_FILE_DEV_LINUX;
                case WINDOWS:
                    return CONFIG_FILE_DEV_WIN;
                case MAC:
                    return CONFIG_FILE_DEV_MAC;
                default:
                    return CONFIG_FILE_PRODUCTION;
            }
        } else {
            return CONFIG_FILE_PRODUCTION;
        }
    }

    private void loadProperties() throws IOException {
        if (configFile.exists()) {
            properties.load(new FileInputStream(configFile));
        }
    }

    public File getFileOrNull(String propertyName) {
        String file = properties.getProperty(propertyName);
        return file == null ? null : new File(file);
    }

    public boolean getBooleanOrDefault(String propertyName, boolean defaultValue) {
        String stringVal = properties.getProperty(propertyName);
        if (stringVal == null) {
            return defaultValue;
        } else {
            return Boolean.valueOf(stringVal);
        }
    }

    public void setBoolean(String propertyName, Boolean value) {
        properties.setProperty(propertyName, value.toString());
        saveProperties();
    }


    public void setFile(String propertyName, File file) {
        try {
            properties.setProperty(propertyName, file.getCanonicalPath());
            saveProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveProperties() {
        try {
            OutputStream out = new FileOutputStream(configFile);
            properties.store(out, null);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(String.format("chyba při zápisu do souboru %s", configFile.getAbsolutePath()), e);
        }

    }

    public Platform getPlatform() {
        return platform;
    }


}