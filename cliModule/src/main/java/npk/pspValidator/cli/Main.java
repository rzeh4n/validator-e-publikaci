package npk.pspValidator.cli;

import nkp.pspValidator.shared.*;
import nkp.pspValidator.shared.engine.Utils;
import nkp.pspValidator.shared.engine.exceptions.InvalidXPathExpressionException;
import nkp.pspValidator.shared.engine.exceptions.PspDataException;
import nkp.pspValidator.shared.engine.exceptions.ValidatorConfigurationException;
import nkp.pspValidator.shared.engine.exceptions.XmlFileParsingException;
import nkp.pspValidator.shared.externalUtils.CliCommand;
import nkp.pspValidator.shared.externalUtils.ExternalUtil;
import nkp.pspValidator.shared.externalUtils.ExternalUtilManager;
import nkp.pspValidator.shared.externalUtils.ExternalUtilManagerFactory;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by Martin Řehánek on 27.9.16.
 */
public class Main {

    public static int DEFAULT_VERBOSITY = 2;

    public static void main(String[] args) throws PspDataException, XmlFileParsingException, InvalidXPathExpressionException, FdmfRegistry.UnknownFdmfException, ValidatorConfigurationException, FdmfRegistry.UnknownFdmfException {
        main(null, args);
        //XsdValidator.testXsds();
    }

    public static void main(Validator.DevParams devParams, String[] args) {
        /*if (true) {
            Properties properties = System.getProperties();
            properties.list(System.out);
            return;
        }*/

        //https://commons.apache.org/proper/commons-cli/usage.html
        Options options = new Options();
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Akce, kterou má být provedena. Povolené hodnoty jsou VALIDATE_PSP a VALIDATE_PSP_GROUP."))
                .hasArg()
                .withArgName("AKCE")
                .withLongOpt(Params.ACTION)
                .create("a"));
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Adresář obsahující formalizované DMF pro jednotlivé verze standardu."))
                .hasArg()
                //.withArgName("ADRESÁŘ_FDMF")
                .withArgName("ADRESAR_CONFIG")
                .withLongOpt(Params.CONFIG_DIR)
                .create());
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Adresář, nebo soubor ZIP obsahující PSP balík. " +
                                "Parametr je povinný pro akci VALIDATE_PSP, u ostatních akcí je ignorován."))
                .hasArg()
                .withArgName("ADRESAR/SOUBOR_ZIP")
                .withLongOpt(Params.PSP)
                .create());
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Adresář, nebo soubor ZIP obsahující skupinu PSP balíků. Ty opět mohou být ve formě adresářů nebo souborů ZIP. " +
                                "Parametr je povinný pro akci VALIDATE_PSP_GROUP, u ostatních akcí je ignorován."))
                .hasArg()
                .withArgName("ADRESAR/SOUBOR_ZIP")
                .withLongOpt(Params.PSP_GROUP)
                .create());
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Adresář pro ukládání xml protokolů s informacemi o validaci každého PSP balíku. Název souboru protokolu je odvozen od názvu PSP balíku a datumu+času začátku validace. " +
                                "Pokud není parametr vyplněn, xml protokol se neukládá (kromě akce VALIDATE_PSP a vyplněného parametru --xml-protocol-file)."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.XML_PROTOCOL_DIR)
                .create());
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Soubor pro uložení xml protokolu s informacemi o validaci PSP balíku. Použije se jen pro akci VALIDATE_PSP. " +
                                "Pokud je zároveň vyplněn parametr --xml-protocol-dir, použije se hodnota z --xml-protocol-file. " +
                                "Pokud není parametr (a zároveň není vyplněn ani --xml-protocol-dir), xml protokol se neukládá. " +
                                "Pokud soubor existuje, je nejprve smazán."))
                .hasArg()
                .withArgName("SOUBOR")
                .withLongOpt(Params.XML_PROTOCOL_FILE)
                .create());
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Adresář pro ukládání dočasných souborů, konkrétně rozbaleních verzí ZIP souborů. " +
                                "Pokud parametr není vyplněn, bude výsledkem pokusu o validaci PSP balíku/skupiny balíků v ZIPu chyba."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.TMP_DIR)
                .create("t"));
        /*TODO: implement*/
       /* options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Počet nevalidních PSP balíku, po jehož dosažení validace předčasně skončí. " +
                                "Bude použito jen pro akci VALIDATE_PSP_GROUP. " +
                                "Pokud parametr není vyplněn, budou validováný všechny balíky."))
                .hasArg()
                .withArgName("N")
                .withLongOpt(Params.QUIT_AFTER_NTH_INVALID_PSP)
                .create());*/

        /*options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Preferovaná verze DMF pro validaci monografií. " +
                                "Použije se k validaci, pokud je balík typu Monografie, data balíku neobsahují informaci o vhodné verzi DMF Monografie " +
                                "a parametr --forced-dmf-mon-version není vyplněn."))
                .hasArg()
                .withArgName("VERZE")
                .withLongOpt(Params.PREFERRED_DMF_MON_VERSION)
                .create());*/
        /*options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Preferovaná verze DMF pro validaci periodik. " +
                                "Použije se k validaci, pokud je balík typu Periodikum, data balíku neobsahují informaci o vhodné verzi DMF Periodikum " +
                                "a parametr --forced-dmf-per-version není vyplněn."))
                .hasArg()
                .withArgName("VERZE")
                .withLongOpt(Params.PREFERRED_DMF_PER_VERSION)
                .create());*/
        /*options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Preferovaná verze DMF pro validaci zvukových dokumentů. " +
                                "Použije se k validaci, pokud je balík typu Zvukový dokument, data balíku neobsahují informaci o vhodné verzi DMF Zvukové dokumenty " +
                                "a parametr --forced-dmf-sr-version není vyplněn."))
                .hasArg()
                .withArgName("VERZE")
                .withLongOpt(Params.PREFERRED_DMF_SR_VERSION)
                .create());*/
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Preferovaná verze DMF pro validaci e-monografií. " +
                                "Použije se k validaci, pokud je balík typu E-Monografie, data balíku neobsahují informaci o vhodné verzi DMF E-Monografie " +
                                "a parametr --forced-dmf-emon-version není vyplněn."))
                .hasArg()
                .withArgName("VERZE")
                .withLongOpt(Params.PREFERRED_DMF_EMON_VERSION)
                .create());
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Preferovaná verze DMF pro validaci e-periodik. " +
                                "Použije se k validaci, pokud je balík typu E-Periodikum, data balíku neobsahují informaci o vhodné verzi DMF E-Periodikum " +
                                "a parametr --forced-dmf-eper-version není vyplněn."))
                .hasArg()
                .withArgName("VERZE")
                .withLongOpt(Params.PREFERRED_DMF_EPER_VERSION)
                .create());


        /*options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Vynucená verze DMF pro validaci monografií. " +
                                "Použije se k validaci všech balíků typu Monografie bez ohledu na data balíků a hodnotu parametru --preferred-dmf-mon-version."))
                .hasArg()
                .withArgName("VERZE")
                .withLongOpt(Params.FORCED_DMF_MON_VERSION)
                .create());*/
        /*options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Vynucená verze DMF pro validaci periodik. " +
                                "Použije se k validaci všech balíků typu Periodikum bez ohledu na data balíků a hodnotu parametru --preferred-dmf-per-version."))
                .hasArg()
                .withArgName("VERZE")
                .withLongOpt(Params.FORCED_DMF_PER_VERSION)
                .create());*/
        /*options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Vynucená verze DMF pro validaci zvukových dokumentů. " +
                                "Použije se k validaci všech balíků typu Zvukový dokument bez ohledu na data balíků a hodnotu parametru --preferred-dmf-sr-version."))
                .hasArg()
                .withArgName("VERZE")
                .withLongOpt(Params.FORCED_DMF_SR_VERSION)
                .create());*/
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Vynucená verze DMF pro validaci e-monografií. " +
                                "Použije se k validaci všech balíků typu E-Monografie bez ohledu na data balíků a hodnotu parametru --preferred-dmf-emon-version."))
                .hasArg()
                .withArgName("VERZE")
                .withLongOpt(Params.FORCED_DMF_EMON_VERSION)
                .create());
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Vynucená verze DMF pro validaci e-periodik. " +
                                "Použije se k validaci všech balíků typu E-Periodikum bez ohledu na data balíků a hodnotu parametru --preferred-dmf-eper-version."))
                .hasArg()
                .withArgName("VERZE")
                .withLongOpt(Params.FORCED_DMF_EPER_VERSION)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Úroveň podrobnosti výpisu." +
                                " 0 vypíše jen celkový počet chyb a rozhodnutí validní/nevalidní." +
                                " 3 vypíše vše včetně sekcí a pravidel bez chyb."))
                .hasArg()
                .withArgName("0-3")
                .withLongOpt(Params.VERBOSITY)
                .create("v"));

        /*options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Adresář s binárními soubory nástroje ImageMagick. " +
                                "Např. C:\\Program Files\\ImageMagick-7.0.3-Q16 pro Windows, /usr/bin pro Linux."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.IMAGEMAGICK_PATH)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Adresář s binárními soubory nástroje JHOVE." +
                                " Např. C:\\Program Files\\jhove-1_11\\\\jhove pro Windows, /usr/bin pro Linux."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.JHOVE_PATH)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Adresář s binárními soubory nástroje Jpylyzer." +
                        " Např. C:\\Program Files\\jpylyzer_1.17.0_win64 pro Windows, /usr/bin pro Linux."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.JPYLYZER_PATH)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Adresář s binárními soubory nástroje Kakadu." +
                        " Např. C:\\Program Files\\Kakadu pro Windows, /usr/bin pro Linux."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.KAKADU_PATH)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Adresář s binárními soubory nástroje MP3val."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.MP3VAL_PATH)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Adresář s binárními soubory nástroje shntool."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.SHNTOOL_PATH)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Adresář s binárními soubory nástroje Checkmate."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.CHECKMATE_PATH)
                .create());*/
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Adresář s binárními soubory nástroje veraPDF."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.VERAPDF_PATH)
                .create());
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Adresář s binárními soubory nástroje EPUBCheck."))
                .hasArg()
                .withArgName("ADRESAR")
                .withLongOpt(Params.EPUBCHECK_PATH)
                .create());

        /*options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Zakáže použití ImageMagick."))
                .withLongOpt(Params.DISABLE_IMAGEMAGICK)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Zakáže použití JHOVE."))
                .withLongOpt("disable-jhove")
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Zakáže použití Jpylyzer."))
                .withLongOpt(Params.DISABLE_JPYLYZER)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Zakáže použití Kakadu."))
                .withLongOpt(Params.DISABLE_KAKADU)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Zakáže použití MP3val."))
                .withLongOpt(Params.DISABLE_MP3VAL)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Zakáže použití shntool."))
                .withLongOpt(Params.DISABLE_SHNTOOL)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Zakáže použití Checkmate."))
                .withLongOpt(Params.DISABLE_CHECKMATE)
                .create());*/
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Zakáže použití veraPDF."))
                .withLongOpt(Params.DISABLE_VERAPDF)
                .create());
        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Zakáže použití EPUBCheck."))
                .withLongOpt(Params.DISABLE_EPUBCHECK)
                .create());

        options.addOption(OptionBuilder
                .withLongOpt(Params.HELP)
                .withDescription(replaceUmlaut("Zobrazit nápovědu."))
                .create());
        options.addOption(OptionBuilder
                .withLongOpt(Params.VERSION)
                .withDescription(replaceUmlaut("Zobrazit informace o verzi."))
                .create());

        CommandLineParser parser = new DefaultParser();
        try {
            // System.out.println(toString(args));
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(Params.HELP)) {
                printHelp(options);
            } else if (line.hasOption(Params.VERSION)) {
                System.out.println(String.format("Validátor e-publikací CLI verze %s, sestaveno: %s", Version.VERSION_CODE, Version.BUILD_DATE));
            } else {

                //action
                if (!line.hasOption(Params.ACTION)) {
                    System.err.println(String.format("Chyba: prázdný parametr %s!", Params.ACTION));
                    printHelp(options);
                    return;
                }
                String actionStr = line.getOptionValue(Params.ACTION);
                Action action;
                try {
                    action = Action.valueOf(actionStr);
                } catch (java.lang.IllegalArgumentException e) {
                    System.err.println(String.format("Chyba: neznámá akce %s!", actionStr));
                    printHelp(options);
                    return;
                }

                //config dir
                if (!line.hasOption(Params.CONFIG_DIR)) {
                    System.err.println(String.format("Chyba: prázdný parametr --%s!", Params.CONFIG_DIR));
                    printHelp(options);
                    return;
                }
                File configDir = new File(line.getOptionValue(Params.CONFIG_DIR));

                //psp / psp-group
                File psp = null;
                File pspGroup = null;
                switch (action) {
                    case VALIDATE_PSP: {
                        if (!line.hasOption(Params.PSP)) {
                            System.err.println(String.format("Chyba: pro akci %s je parametr --%s povinný!", action, Params.PSP));
                            printHelp(options);
                            return;
                        } else {
                            psp = new File(line.getOptionValue(Params.PSP));
                            break;
                        }
                    }
                    case VALIDATE_PSP_GROUP: {
                        if (!line.hasOption(Params.PSP_GROUP)) {
                            System.err.println(String.format("Chyba: pro akci %s je parametr --%s povinný!", action, Params.PSP_GROUP));
                            printHelp(options);
                            return;
                        } else {
                            pspGroup = new File(line.getOptionValue(Params.PSP_GROUP));
                            break;
                        }
                    }
                }

                //xml protocol(x) file/dir
                File xmlProtocolDir = null;
                if (line.hasOption(Params.XML_PROTOCOL_DIR)) {
                    xmlProtocolDir = new File(line.getOptionValue(Params.XML_PROTOCOL_DIR));
                }
                File xmlProtocolFile = null;
                if (line.hasOption(Params.XML_PROTOCOL_FILE)) {
                    xmlProtocolFile = new File(line.getOptionValue(Params.XML_PROTOCOL_FILE));
                }

                //tmp dir
                File tmpDir = null;
                if (line.hasOption(Params.TMP_DIR)) {
                    tmpDir = new File(line.getOptionValue(Params.TMP_DIR));
                }

                //quitAfterInvalidPsps
                Integer quitAfterInvalidPsps = null;
                /*TODO: implement*/
                /*if (line.hasOption(Params.QUIT_AFTER_NTH_INVALID_PSP)) {
                    try {
                        quitAfterInvalidPsps = Integer.valueOf(line.getOptionValue(Params.QUIT_AFTER_NTH_INVALID_PSP));
                        if (quitAfterInvalidPsps < 1) {
                            System.err.println(String.format("Chyba: hodnota parametru --%s musí být celé kladné číslo!", Params.QUIT_AFTER_NTH_INVALID_PSP));
                            printHelp(options);
                            return;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println(String.format("Chyba: hodnota parametru --%s musí být celé kladné číslo!", Params.QUIT_AFTER_NTH_INVALID_PSP));
                        printHelp(options);
                        return;
                    }
                }*/

                //preferred dmf versions
                /*String preferDmfMonVersion = null;
                if (line.hasOption(Params.PREFERRED_DMF_MON_VERSION)) {
                    preferDmfMonVersion = line.getOptionValue(Params.PREFERRED_DMF_MON_VERSION);
                }*/
                /*String preferDmfPerVersion = null;
                if (line.hasOption(Params.PREFERRED_DMF_PER_VERSION)) {
                    preferDmfPerVersion = line.getOptionValue(Params.PREFERRED_DMF_PER_VERSION);
                }*/
                String preferDmfSrVersion = null;
                /*if (line.hasOption(Params.PREFERRED_DMF_SR_VERSION)) {
                    preferDmfSrVersion = line.getOptionValue(Params.PREFERRED_DMF_SR_VERSION);
                }*/
                String preferDmfEmonVersion = null;
                if (line.hasOption(Params.PREFERRED_DMF_EMON_VERSION)) {
                    preferDmfEmonVersion = line.getOptionValue(Params.PREFERRED_DMF_EMON_VERSION);
                }
                String preferDmfEperVersion = null;
                if (line.hasOption(Params.PREFERRED_DMF_EPER_VERSION)) {
                    preferDmfEperVersion = line.getOptionValue(Params.PREFERRED_DMF_EPER_VERSION);
                }

                //force dmf versions
                /*String forceDmfMonVersion = null;
                if (line.hasOption(Params.FORCED_DMF_MON_VERSION)) {
                    forceDmfMonVersion = line.getOptionValue(Params.FORCED_DMF_MON_VERSION);
                }*/
                /*String forceDmfPerVersion = null;
                if (line.hasOption(Params.FORCED_DMF_PER_VERSION)) {
                    forceDmfPerVersion = line.getOptionValue(Params.FORCED_DMF_PER_VERSION);
                }*/
                /*String forceDmfSrVersion = null;
                if (line.hasOption(Params.FORCED_DMF_SR_VERSION)) {
                    forceDmfSrVersion = line.getOptionValue(Params.FORCED_DMF_SR_VERSION);
                }*/
                String forceDmfEmonVersion = null;
                if (line.hasOption(Params.FORCED_DMF_EMON_VERSION)) {
                    forceDmfEmonVersion = line.getOptionValue(Params.FORCED_DMF_EMON_VERSION);
                }
                String forceDmfEperVersion = null;
                if (line.hasOption(Params.FORCED_DMF_EPER_VERSION)) {
                    forceDmfEperVersion = line.getOptionValue(Params.FORCED_DMF_EPER_VERSION);
                }

                //verbosity
                Integer verbosity = DEFAULT_VERBOSITY;
                if (line.hasOption(Params.VERBOSITY)) {
                    try {
                        verbosity = Integer.valueOf(line.getOptionValue(Params.VERBOSITY));
                        if (verbosity < 0 || verbosity > 3) {
                            System.err.println(String.format("Chyba: hodnota parametru --%s musí být číslo (0-3)!", Params.VERBOSITY));
                            printHelp(options);
                            return;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println(String.format("Chyba: hodnota parametru --%s musí být číslo (0-3)!", Params.VERBOSITY));
                        printHelp(options);
                        return;
                    }
                }

                //external utils
                Map<ExternalUtil, File> utilsPaths = new HashMap<>();
                Set<ExternalUtil> utilsDisabled = new HashSet<>();
                /*if (line.hasOption(Params.DISABLE_IMAGEMAGICK)) {
                    utilsDisabled.add(ExternalUtil.IMAGE_MAGICK);
                } else {
                    if (line.hasOption(Params.IMAGEMAGICK_PATH)) {
                        utilsPaths.put(ExternalUtil.IMAGE_MAGICK, new File(line.getOptionValue(Params.IMAGEMAGICK_PATH)));
                    }
                }
                if (line.hasOption(Params.DISABLE_JHOVE)) {
                    utilsDisabled.add(ExternalUtil.JHOVE);
                } else {
                    if (line.hasOption(Params.JHOVE_PATH)) {
                        utilsPaths.put(ExternalUtil.JHOVE, new File(line.getOptionValue(Params.JHOVE_PATH)));
                    }
                }
                if (line.hasOption(Params.DISABLE_JPYLYZER)) {
                    utilsDisabled.add(ExternalUtil.JPYLYZER);
                } else {
                    if (line.hasOption(Params.JPYLYZER_PATH)) {
                        utilsPaths.put(ExternalUtil.JPYLYZER, new File(line.getOptionValue(Params.JPYLYZER_PATH)));
                    }
                }
                if (line.hasOption(Params.DISABLE_KAKADU)) {
                    utilsDisabled.add(ExternalUtil.KAKADU);
                } else {
                    if (line.hasOption(Params.KAKADU_PATH)) {
                        utilsPaths.put(ExternalUtil.KAKADU, new File(line.getOptionValue(Params.KAKADU_PATH)));
                    }
                }
                if (line.hasOption(Params.DISABLE_MP3VAL)) {
                    utilsDisabled.add(ExternalUtil.MP3VAL);
                } else {
                    if (line.hasOption(Params.MP3VAL_PATH)) {
                        utilsPaths.put(ExternalUtil.MP3VAL, new File(line.getOptionValue(Params.MP3VAL_PATH)));
                    }
                }
                if (line.hasOption(Params.DISABLE_SHNTOOL)) {
                    utilsDisabled.add(ExternalUtil.SHNTOOL);
                } else {
                    if (line.hasOption(Params.SHNTOOL_PATH)) {
                        utilsPaths.put(ExternalUtil.SHNTOOL, new File(line.getOptionValue(Params.SHNTOOL_PATH)));
                    }
                }
                if (line.hasOption(Params.DISABLE_CHECKMATE)) {
                    utilsDisabled.add(ExternalUtil.CHECKMATE);
                } else {
                    if (line.hasOption(Params.CHECKMATE_PATH)) {
                        utilsPaths.put(ExternalUtil.CHECKMATE, new File(line.getOptionValue(Params.CHECKMATE_PATH)));
                    }
                }*/
                if (line.hasOption(Params.DISABLE_VERAPDF)) {
                    utilsDisabled.add(ExternalUtil.VERAPDF);
                } else {
                    if (line.hasOption(Params.VERAPDF_PATH)) {
                        utilsPaths.put(ExternalUtil.VERAPDF, new File(line.getOptionValue(Params.VERAPDF_PATH)));
                    }
                }
                if (line.hasOption(Params.DISABLE_EPUBCHECK)) {
                    utilsDisabled.add(ExternalUtil.EPUBCHECK);
                } else {
                    if (line.hasOption(Params.EPUBCHECK_PATH)) {
                        utilsPaths.put(ExternalUtil.EPUBCHECK, new File(line.getOptionValue(Params.EPUBCHECK_PATH)));
                    }
                }

                DmfDetector.Params dmfDetectorParams = new DmfDetector.Params();
                //dmfDetectorParams.forcedDmfMonVersion = forceDmfMonVersion;
                //dmfDetectorParams.forcedDmfPerVersion = forceDmfPerVersion;
                //dmfDetectorParams.forcedDmfSRVersion = forceDmfSrVersion;
                dmfDetectorParams.forcedDmfEmonVersion = forceDmfEmonVersion;
                dmfDetectorParams.forcedDmfEperVersion = forceDmfEperVersion;
                //dmfDetectorParams.preferredDmfMonVersion = preferDmfMonVersion;
                //dmfDetectorParams.preferredDmfPerVersion = preferDmfPerVersion;
                //dmfDetectorParams.preferredDmfSRVersion = preferDmfSrVersion;
                dmfDetectorParams.preferredDmfEmonVersion = preferDmfEmonVersion;
                dmfDetectorParams.preferredDmfEperVersion = preferDmfEperVersion;

                PrintStream out = System.out;
                PrintStream err = System.err;

                switch (action) {
                    case VALIDATE_PSP:
                        validatePsp(psp,
                                configDir, tmpDir,
                                verbosity, out, err, xmlProtocolDir, xmlProtocolFile,
                                dmfDetectorParams,
                                utilsPaths, utilsDisabled,
                                devParams);
                        break;
                    case VALIDATE_PSP_GROUP:
                        validatePspGroup(pspGroup,
                                configDir, tmpDir,
                                verbosity, out, err, xmlProtocolDir,
                                dmfDetectorParams,
                                utilsPaths, utilsDisabled,
                                devParams);
                        break;
                }
            }
        } catch (ParseException exp) {
            System.err.println("Chyba parsování parametrů: " + exp.getMessage());
            printHelp(options);
        } catch (XmlFileParsingException e) {
            System.err.println("Chyba:" + e.getMessage());
        } catch (ValidatorConfigurationException e) {
            System.err.println("Chyba:" + e.getMessage());
        } catch (PspDataException e) {
            System.err.println("Chyba:" + e.getMessage());
        } catch (FdmfRegistry.UnknownFdmfException e) {
            System.err.println("Chyba:" + e.getMessage());
        } catch (InvalidXPathExpressionException e) {
            System.err.println("Chyba:" + e.getMessage());
        }
    }

    /*Docasne odstrani diakritiku, dokud se neopravi problem s kodovanim na Windows*/
    private static String replaceUmlaut(String string) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("á", "a");
        replacements.put("Á", "A");
        replacements.put("é", "e");
        replacements.put("É", "E");
        replacements.put("ě", "e");
        replacements.put("Ě", "E");
        replacements.put("í", "i");
        replacements.put("Í", "I");
        replacements.put("ó", "o");
        replacements.put("Ó", "O");
        replacements.put("ů", "u");
        replacements.put("Ů", "U");
        replacements.put("ú", "u");
        replacements.put("Ú", "U");

        replacements.put("č", "c");
        replacements.put("Č", "C");
        replacements.put("ď", "d");
        replacements.put("Ď", "D");
        replacements.put("ň", "n");
        replacements.put("Ň", "N");
        replacements.put("ř", "r");
        replacements.put("Ř", "R");
        replacements.put("š", "s");
        replacements.put("Š", "S");
        replacements.put("ť", "t");
        replacements.put("Ť", "T");
        replacements.put("ž", "z");
        replacements.put("Ž", "Z");

        for (String letter : replacements.keySet()) {
            string = string.replaceAll(letter, replacements.get(letter));
        }
        return string;
    }

    private static String toString(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (String string : args) {
            builder.append(string).append(' ');
        }
        return builder.toString();
    }

    private static void printHelp(Options options) {
        String header = replaceUmlaut("Validuje PSP balík, nebo sadu PSP balíků podle DMF*." +
                " Typ DMF (Monografie/Periodikum/Zvukový dokument) se vždy získává z dat jednotlivých PSP balíků." +
                " Verze DMF použité pro validaci je možné ovlivnit parametry --preferred-dmf-mon-version, --preferred-dmf-per-version, --preferred-dmf-sr-version, --forced-dmf-mon-version, --forced-dmf-per-version a --forced-dmf-sr-version." +
                " Dále je potřeba pomocí --config-dir uvést adresář, který obsahuje definice fDMF," +
                " napr. monograph_1.3.2 nebo periodical_1.7.\n\n");
        String footer = replaceUmlaut("\n*Definice metadatových formátů. Více na http://www.ndk.cz/standardy-digitalizace/metadata.\n" +
                "Více informací o validátoru najdete na https://github.com/NLCR/validator-e-publikaci.");
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        formatter.setOptionComparator(null);
        formatter.printHelp("java -jar ValidatorEpublikaci.jar", header, options, footer, true);
    }

    private static void validatePspGroup(File pspGroup,
                                         File configDir, File tmpDir,
                                         Integer verbosity, PrintStream out, PrintStream err,
                                         File xmlProtocolDir,
                                         DmfDetector.Params dmfDetectorParams,
                                         Map<ExternalUtil, File> utilsPaths, Set<ExternalUtil> utilsDisabled, Validator.DevParams devParams) throws ValidatorConfigurationException, XmlFileParsingException, FdmfRegistry.UnknownFdmfException, PspDataException, InvalidXPathExpressionException {
        Platform platform = Platform.detectOs();
        out.println(String.format("Platforma: %s", platform.toReadableString()));

        //validator configuration
        out.println(String.format("Kořenový adresář konfigurace validátoru: %s", configDir.getAbsolutePath()));
        ValidatorConfigurationManager validatorConfigManager = new ValidatorConfigurationManager(configDir);
        ExternalUtilManager externalUtilManager = new ExternalUtilManagerFactory(validatorConfigManager.getExternalUtilsConfigFile()).buildExternalUtilManager(platform.getOperatingSystem());
        externalUtilManager.setPaths(utilsPaths);
        detectImageTools(out, externalUtilManager, utilsDisabled);


        //pspDirOrZipFile dir or zip file?
        if (!pspGroup.exists()) {
            throw new IllegalStateException(String.format("Soubor %s neexistuje", pspGroup.getAbsolutePath()));
        } else {
            if (pspGroup.isDirectory()) {
                if (!pspGroup.canRead()) {
                    throw new IllegalStateException(String.format("Nelze číst adresář %s", pspGroup.getAbsolutePath()));
                }
                validatePspGroupDir(pspGroup,
                        configDir, tmpDir,
                        verbosity, out, err, xmlProtocolDir,
                        dmfDetectorParams,
                        utilsPaths, utilsDisabled,
                        devParams);
            } else {
                validatePspGroupZip(pspGroup,
                        configDir, tmpDir,
                        verbosity, out, err, xmlProtocolDir,
                        dmfDetectorParams,
                        utilsPaths, utilsDisabled,
                        devParams);
            }
        }
    }

    private static void validatePspGroupZip(File pspGroupFile,
                                            File configDir, File tmpDir,
                                            Integer verbosity, PrintStream out, PrintStream err,
                                            File xmlProtocolDir,
                                            DmfDetector.Params dmfDetectorParams,
                                            Map<ExternalUtil, File> utilsPaths, Set<ExternalUtil> utilsDisabled,
                                            Validator.DevParams devParams) throws PspDataException, XmlFileParsingException, FdmfRegistry.UnknownFdmfException, ValidatorConfigurationException, InvalidXPathExpressionException {
        try {
            try {
                new ZipFile(pspGroupFile);
            } catch (ZipException e) {
                out.println(String.format("Soubor %s není adresář ani soubor ZIP, ignoruji.", pspGroupFile.getAbsolutePath()));
                return;
            }
            if (tmpDir == null) {
                err.println(String.format("Chyba: prázdný parametr --%s: adresář pro dočasné soubory je potřeba pro rozbalení ZIP souboru %s!", Params.TMP_DIR, pspGroupFile.getAbsolutePath()));
            } else if (!tmpDir.exists()) {
                err.println(String.format("Chyba: adresář %s neexistuje!", tmpDir.getAbsolutePath()));
            } else if (!tmpDir.isDirectory()) {
                err.println(String.format("Chyba: soubor %s není adresář!", tmpDir.getAbsolutePath()));
            } else if (!tmpDir.canWrite()) {
                err.println(String.format("Chyba: nemůžu zapisovat do adresáře %s!", tmpDir.getAbsolutePath()));
            } else {
                File containerDir = new File(tmpDir, pspGroupFile.getName() + "_extracted");
                if (containerDir.exists()) {
                    out.println(String.format("Mažu adresář %s", containerDir.getAbsolutePath()));
                    Utils.deleteNonemptyDir(containerDir);
                }
                out.println(String.format("Rozbaluji %s do adresáře %s", pspGroupFile.getAbsolutePath(), containerDir.getAbsolutePath()));
                Utils.unzip(pspGroupFile, containerDir);
                File[] filesInContainer = containerDir.listFiles();
                if (filesInContainer.length == 1 && filesInContainer[0].isDirectory()) {
                    validatePspGroupDir(filesInContainer[0],
                            configDir, tmpDir,
                            verbosity, out, err, xmlProtocolDir,
                            dmfDetectorParams,
                            utilsPaths, utilsDisabled,
                            devParams);
                } else {
                    validatePspGroupDir(containerDir,
                            configDir, tmpDir,
                            verbosity, out, err, xmlProtocolDir,
                            dmfDetectorParams,
                            utilsPaths, utilsDisabled,
                            devParams);
                }
            }
        } catch (IOException e) {
            out.println(String.format("Chyba zpracování ZIP souboru %s: %s!", pspGroupFile.getAbsolutePath(), e.getMessage()));
        }
    }

    private static void validatePspGroupDir(File pspGroupDir,
                                            File configDir, File tmpDir,
                                            Integer verbosity, PrintStream out, PrintStream err,
                                            File xmlProtocolDir,
                                            DmfDetector.Params dmfDetectorParams,
                                            Map<ExternalUtil, File> utilsPaths, Set<ExternalUtil> utilsDisabled,
                                            Validator.DevParams devParams) throws XmlFileParsingException, FdmfRegistry.UnknownFdmfException, PspDataException, ValidatorConfigurationException, InvalidXPathExpressionException {
        for (File pspDirOrZip : pspGroupDir.listFiles()) {
            //TODO: pocitat nevalidni baliky
            validatePsp(pspDirOrZip,
                    configDir, tmpDir,
                    verbosity, out, err,
                    xmlProtocolDir, null,
                    dmfDetectorParams,
                    utilsPaths, utilsDisabled,
                    devParams);
        }
    }

    private static void validatePsp(File pspDirOrZipFile,
                                    File configDir, File tmpDir,
                                    Integer verbosity, PrintStream out, PrintStream err,
                                    File xmlProtocolDir, File xmlProtocolFile,
                                    DmfDetector.Params dmfDetectorParams,
                                    Map<ExternalUtil, File> utilsPaths, Set<ExternalUtil> utilsDisabled,
                                    Validator.DevParams devParams) throws ValidatorConfigurationException, FdmfRegistry.UnknownFdmfException, PspDataException, InvalidXPathExpressionException, XmlFileParsingException {
        Platform platform = Platform.detectOs();
        out.println(String.format("Platforma: %s", platform.toReadableString()));

        //validator configuration
        out.println(String.format("Kořenový adresář konfigurace validátoru: %s", configDir.getAbsolutePath()));
        ValidatorConfigurationManager validatorConfigManager = new ValidatorConfigurationManager(configDir);
        ExternalUtilManager externalUtilManager = new ExternalUtilManagerFactory(validatorConfigManager.getExternalUtilsConfigFile()).buildExternalUtilManager(platform.getOperatingSystem());
        externalUtilManager.setPaths(utilsPaths);
        detectImageTools(out, externalUtilManager, utilsDisabled);

        //pspDirOrZipFile dir or zip file?
        if (!pspDirOrZipFile.exists()) {
            throw new IllegalStateException(String.format("Soubor %s neexistuje", pspDirOrZipFile.getAbsolutePath()));
        } else {
            if (pspDirOrZipFile.isDirectory()) {
                if (!pspDirOrZipFile.canRead()) {
                    throw new IllegalStateException(String.format("Nelze číst adresář %s", pspDirOrZipFile.getAbsolutePath()));
                }
                validatePspDir(pspDirOrZipFile,
                        externalUtilManager, validatorConfigManager,
                        out, verbosity, xmlProtocolDir, xmlProtocolFile,
                        dmfDetectorParams,
                        devParams);
            } else {
                validatePspZip(pspDirOrZipFile,
                        tmpDir,
                        externalUtilManager, validatorConfigManager,
                        out, err, verbosity, xmlProtocolDir, xmlProtocolFile,
                        dmfDetectorParams,
                        devParams);
            }
        }
    }

    private static void validatePspZip(File pspZipFile,
                                       File tmpDir,
                                       ExternalUtilManager externalUtilManager, ValidatorConfigurationManager validatorConfigManager,
                                       PrintStream out, PrintStream err, Integer verbosity,
                                       File xmlProtocolDir, File xmlProtocolFile,
                                       DmfDetector.Params dmfDetectorParams,
                                       Validator.DevParams devParams) throws XmlFileParsingException, FdmfRegistry.UnknownFdmfException, PspDataException, ValidatorConfigurationException, InvalidXPathExpressionException {
        try {
            try {
                new ZipFile(pspZipFile);
            } catch (ZipException e) {
                out.println(String.format("Soubor %s není adresář ani soubor ZIP, ignoruji.", pspZipFile.getAbsolutePath()));
                return;
            }
            if (tmpDir == null) {
                err.println(String.format("Chyba: prázdný parametr --%s: adresář pro dočasné soubory je potřeba pro rozbalení ZIP souboru %s!", Params.TMP_DIR, pspZipFile.getAbsolutePath()));
            } else if (!tmpDir.exists()) {
                err.println(String.format("Chyba: adresář %s neexistuje!", pspZipFile.getAbsolutePath()));
            } else if (!tmpDir.isDirectory()) {
                err.println(String.format("Chyba: soubor %s není adresář!", pspZipFile.getAbsolutePath()));
            } else if (!tmpDir.canWrite()) {
                err.println(String.format("Chyba: nemůžu zapisovat do adresáře %s!", pspZipFile.getAbsolutePath()));
            } else {
                File containerDir = new File(tmpDir, pspZipFile.getName() + "_extracted");
                if (containerDir.exists()) {
                    out.println(String.format("Mažu adresář %s", containerDir.getAbsolutePath()));
                    Utils.deleteNonemptyDir(containerDir);
                }
                out.println(String.format("Rozbaluji %s do adresáře %s", pspZipFile.getAbsolutePath(), containerDir.getAbsolutePath()));
                Utils.unzip(pspZipFile, containerDir);
                File[] filesInContainer = containerDir.listFiles();
                if (filesInContainer.length == 1 && filesInContainer[0].isDirectory()) {
                    validatePspDir(filesInContainer[0],
                            externalUtilManager, validatorConfigManager,
                            out, verbosity, xmlProtocolDir, xmlProtocolFile,
                            dmfDetectorParams,
                            devParams);
                } else {
                    validatePspDir(containerDir,
                            externalUtilManager, validatorConfigManager,
                            out, verbosity, xmlProtocolDir, xmlProtocolFile,
                            dmfDetectorParams,
                            devParams);
                }
            }
        } catch (IOException e) {
            out.println(String.format("Chyba zpracování ZIP souboru %s: %s!", pspZipFile.getAbsolutePath(), e.getMessage()));
        }
    }

    private static void validatePspDir(File pspDir,
                                       ExternalUtilManager externalUtilManager, ValidatorConfigurationManager validatorConfigManager,
                                       PrintStream out, Integer verbosity,
                                       File xmlProtocolDir, File xmlProtocolFile,
                                       DmfDetector.Params dmfDetectorParams,
                                       Validator.DevParams devParams) throws ValidatorConfigurationException, FdmfRegistry.UnknownFdmfException, PspDataException, XmlFileParsingException, InvalidXPathExpressionException {
        //psp dir, dmf detection
        checkReadableDir(pspDir);
        out.println(String.format("Zpracovávám PSP balík %s", pspDir.getAbsolutePath()));


        Dmf dmfResolved = new DmfDetector().resolveDmf(pspDir, dmfDetectorParams);
        out.println(String.format("Bude použita verze standardu %s", dmfResolved));

        //initializes j2k profiles according to selected fDMF
        FdmfConfiguration fdmfConfig = new FdmfRegistry(validatorConfigManager).getFdmfConfig(dmfResolved);
        fdmfConfig.initBinaryFileProfiles(externalUtilManager);

        //xml protocol file
        if (xmlProtocolFile == null) {
            if (xmlProtocolDir != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                String filename = String.format("%s_%s.xml", simpleDateFormat.format(new Date(System.currentTimeMillis())), pspDir.getName());
                xmlProtocolFile = new File(xmlProtocolDir, filename);
            }
        }

        //validate
        Validator validator = ValidatorFactory.buildValidator(fdmfConfig, pspDir, validatorConfigManager.getDictionaryManager());
        out.println(String.format("Validátor inicializován, spouštím validace"));
        ValidationState.ProgressListener progressListener = null;
        validator.run(xmlProtocolFile, out, verbosity, devParams, null, progressListener, null);
    }


    private static void detectImageTools(PrintStream out, ExternalUtilManager externalUtilManager, Set<ExternalUtil> utilsDisabled) {
        for (ExternalUtil util : ExternalUtil.values()) {
            if (utilsDisabled.contains(util)) {
                out.println(String.format("Vypnuto použití nástroje %s.", util.getUserFriendlyName()));
            } else {
                out.print(String.format("Kontroluji dostupnost nástroje %s: ", util.getUserFriendlyName()));
                if (!externalUtilManager.isVersionDetectionDefined(util)) {
                    out.println("není definován způsob detekce verze");
                } else if (externalUtilManager.isUtilAvailable(util)) {
                    out.println("je definován způsob spuštění");
                } else {
                    out.println("není definován způsob spuštění");
                    try {
                        out.print(String.format("Detekuji verzi nástroje %s: ", util.getUserFriendlyName()));
                        String version = externalUtilManager.runUtilVersionDetection(util);
                        if (version != null) {
                            out.println("zjištěna verze: " + version + ", nástroj bude dostupný");
                            externalUtilManager.setUtilAvailable(util, true);
                        } else {
                            out.println("verze nezjištěna, nástroj nebude dostupný");
                        }
                    } catch (CliCommand.CliCommandException e) {
                        out.println(String.format("nepodařilo se zjistit verzi: %s", shortenErrorMessage(e.getMessage())));
                    }
                }
            }
        }
    }

    private static String shortenErrorMessage(String message) {
        if (message == null) {
            return null;
        } else {
            int maxLength = 100;
            if (message.length() < 100) {
                return message.replaceAll("\\s+", " ").trim();
            } else {
                String suffix = "...";
                return message.substring(0, maxLength - suffix.length()).replaceAll("\\s+", " ").trim() + suffix;
            }
        }
    }

    private static void checkReadableDir(File file) {
        if (!file.exists()) {
            throw new IllegalStateException(String.format("Soubor %s neexistuje", file.getAbsolutePath()));
        } else if (!file.isDirectory()) {
            throw new IllegalStateException(String.format("Soubor %s není adresář", file.getAbsolutePath()));
        } else if (!file.canRead()) {
            throw new IllegalStateException(String.format("Nelze číst adresář %s", file.getAbsolutePath()));
        }
    }

}
