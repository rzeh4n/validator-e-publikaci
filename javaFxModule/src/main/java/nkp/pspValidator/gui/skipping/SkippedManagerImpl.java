package nkp.pspValidator.gui.skipping;

import nkp.pspValidator.gui.ConfigurationManager;
import nkp.pspValidator.gui.ValidationDataManager;
import nkp.pspValidator.gui.VersionComparator;
import nkp.pspValidator.gui.validation.Utils;
import nkp.pspValidator.shared.Dmf;
import nkp.pspValidator.shared.FdmfRegistry;
import nkp.pspValidator.shared.Validator;
import nkp.pspValidator.shared.engine.RulesSection;
import nkp.pspValidator.shared.engine.exceptions.ValidatorConfigurationException;

import java.util.*;

/**
 * Created by Martin Řehánek on 9.4.18.
 */
public class SkippedManagerImpl implements SkippedManager {

    private final ConfigurationManager configurationManager;
    private final List<Dmf> dmfList;
    private final Map<Dmf, Skipped> dmfToSkipped;

    public SkippedManagerImpl(ConfigurationManager configurationManager, ValidationDataManager mgr) {
        this.configurationManager = configurationManager;
        dmfList = buildDmfList(mgr);
        dmfToSkipped = buildDmfToSkipped(mgr);
    }

    private List<Dmf> buildDmfList(ValidationDataManager mgr) {
        List<Dmf> result = new ArrayList<>();
        /*//monograph
        List<String> monVersions = new ArrayList<>();
        monVersions.addAll(mgr.getFdmfRegistry().getMonographFdmfVersions());
        Collections.sort(monVersions, new VersionComparator());
        for (String monVersion : monVersions) {
            result.add(new Dmf(Dmf.Type.MONOGRAPH, monVersion));
        }
        //periodical
        List<String> perVersions = new ArrayList<>();
        perVersions.addAll(mgr.getFdmfRegistry().getPeriodicalFdmfVersions());
        Collections.sort(perVersions, new VersionComparator());
        for (String perVersion : perVersions) {
            result.add(new Dmf(Dmf.Type.PERIODICAL, perVersion));
        }
        //sound recording
        List<String> srVersions = new ArrayList<>();
        srVersions.addAll(mgr.getFdmfRegistry().getSoundRecordingFdmfVersions());
        Collections.sort(srVersions, new VersionComparator());
        for (String srVersion : srVersions) {
            result.add(new Dmf(Dmf.Type.SOUND_RECORDING, srVersion));
        }*/
        //e-monograph
        List<String> emonVersions = new ArrayList<>();
        emonVersions.addAll(mgr.getFdmfRegistry().getEmonographFdmfVersions());
        Collections.sort(emonVersions, new VersionComparator());
        for (String version : emonVersions) {
            result.add(new Dmf(Dmf.Type.EMONOGRAPH, version));
        }
        //e-periodical
        List<String> eperVersions = new ArrayList<>();
        eperVersions.addAll(mgr.getFdmfRegistry().getEperiodicalFdmfVersions());
        Collections.sort(eperVersions, new VersionComparator());
        for (String version : eperVersions) {
            result.add(new Dmf(Dmf.Type.EPERIODICAL, version));
        }
        return result;
    }

    private Map<Dmf, Skipped> buildDmfToSkipped(ValidationDataManager mgr) {
        Map<Dmf, Skipped> result = new HashMap<>();
        //load from engine
        for (Dmf dmf : dmfList) {
            result.put(dmf, buildSkipped(mgr, dmf));
        }
        //disable sections from config
        for (Dmf dmf : result.keySet()) {
            Skipped skipped = result.get(dmf);
            Set<String> skippedSections = configurationManager.getStringSet(configurationManager.propSkippedValidationSections(dmf));
            for (String skippedSectionName : skippedSections) {
                RulesSection section = skipped.getSectionByName(skippedSectionName);
                if (section != null) {
                    section.setEnabled(false);
                }
            }
        }
        return result;
    }

    private Skipped buildSkipped(ValidationDataManager mgr, Dmf dmf) {
        try {
            Skipped result = new Skipped();
            //inicializace fake validatoru pro ziskani pravidel
            Validator validator = Utils.buildValidator(mgr, dmf, null);
            List<RulesSection> sections = validator.getEngine().getRuleSections();
            //pro jistotu hard copy to avoid memory leaks from references to Engine etc
            List<RulesSection> sectionsCopy = new ArrayList<>(sections.size());
            for (RulesSection section : sections) {
                sectionsCopy.add(section.copy());
            }
            result.setAllSections(sectionsCopy);
            return result;
        } catch (FdmfRegistry.UnknownFdmfException e) {
            //should never happen
            throw new RuntimeException(e);
        } catch (ValidatorConfigurationException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Skipped getSkippedForDmf(Dmf dmf) {
        return dmfToSkipped.get(dmf);
    }

    @Override
    public void setSkippedForDmf(Dmf dmf, Skipped skipped) {
        dmfToSkipped.put(dmf, skipped);
    }

    @Override
    public List<Dmf> getDmfList() {
        return dmfList;
    }

    @Override
    public void save() {
        for (Dmf dmf : dmfList) {
            Skipped skipped = dmfToSkipped.get(dmf);
            Set<String> namesOfDisabledSections = skipped.getNamesOfSkippedSections();
            configurationManager.setStringSet(configurationManager.propSkippedValidationSections(dmf), namesOfDisabledSections);
        }
    }
}
