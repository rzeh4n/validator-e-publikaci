package nkp.pspValidator.shared.externalUtils.validation.rules;

import nkp.pspValidator.shared.engine.Level;
import nkp.pspValidator.shared.engine.Utils;
import nkp.pspValidator.shared.externalUtils.validation.DataRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Martin Řehánek on 17.11.16.
 */
public abstract class AbstractDataRule implements DataRule {
    private final String validationName;
    private final Level level;

    public AbstractDataRule(String validationName, Level level) {
        this.validationName = validationName;
        this.level = level;
    }

    public final String getValidationName() {
        return validationName;
    }

    public final Level getLevel() {
        return level;
    }

    String error(String message) {
        return String.format("%s: %s", validationName, message);
    }

    List<String> singleError(String error) {
        List<String> list = new ArrayList<>();
        list.add(error);
        return list;
    }

    List<String> noErrors() {
        return Collections.emptyList();
    }

    public String toString(Object data) {
        if (data == null) {
            return null;
        } else if (data instanceof List) {
            return Utils.listToString((List) data);
        } else {
            return data.toString();
        }
    }
}
