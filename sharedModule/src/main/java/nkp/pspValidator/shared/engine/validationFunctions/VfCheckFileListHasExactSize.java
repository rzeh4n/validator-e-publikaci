package nkp.pspValidator.shared.engine.validationFunctions;

import nkp.pspValidator.shared.engine.Engine;
import nkp.pspValidator.shared.engine.Level;
import nkp.pspValidator.shared.engine.ValueEvaluation;
import nkp.pspValidator.shared.engine.ValueType;
import nkp.pspValidator.shared.engine.exceptions.ContractException;
import nkp.pspValidator.shared.engine.params.ValueParam;

import java.io.File;
import java.util.List;

/**
 * Created by Martin Řehánek on 27.10.16.
 */
public class VfCheckFileListHasExactSize extends ValidationFunction {

    public static final String PARAM_FILES = "files";
    public static final String PARAM_SIZE = "size";
    public static final String PARAM_LEVEL = "level";

    public VfCheckFileListHasExactSize(String name, Engine engine) {
        super(name, engine, new Contract()
                .withValueParam(PARAM_FILES, ValueType.FILE_LIST, 1, 1)
                .withValueParam(PARAM_SIZE, ValueType.INTEGER, 1, 1)
                .withValueParam(PARAM_LEVEL, ValueType.LEVEL, 0, 1)
        );
    }

    @Override
    public ValidationResult validate() {
        try {
            checkContractCompliance();

            ValueEvaluation paramFiles = valueParams.getParams(PARAM_FILES).get(0).getEvaluation();
            List<File> fileList = (List<File>) paramFiles.getData();
            if (fileList == null) {
                return invalidValueParamNull(PARAM_FILES, paramFiles);
            }

            ValueEvaluation paramSize = valueParams.getParams(PARAM_SIZE).get(0).getEvaluation();
            Integer expectedSize = (Integer) paramSize.getData();
            if (expectedSize == null) {
                return invalidValueParamNull(PARAM_SIZE, paramSize);
            }

            Level level = Level.ERROR;
            List<ValueParam> paramsLevel = valueParams.getParams(PARAM_LEVEL);
            if (!paramsLevel.isEmpty()) {
                ValueParam paramLevel = paramsLevel.get(0);
                ValueEvaluation evaluation = paramLevel.getEvaluation();
                if (evaluation.getData() == null) {
                    return invalidValueParamNull(PARAM_LEVEL, evaluation);
                } else {
                    level = (Level) evaluation.getData();
                }
            }

            return validate(expectedSize, fileList, level);
        } catch (ContractException e) {
            return invalidContractNotMet(e);
        } catch (Throwable e) {
            return invalidUnexpectedError(e);
        }
    }

    private ValidationResult validate(Integer expectedSize, List<File> fileList, Level level) {
        if (fileList.size() != expectedSize) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < fileList.size(); i++) {
                builder.append(fileList.get(i).getName());
                if (i != fileList.size() - 1) {
                    builder.append(", ");
                }
            }
            String filenamesStr = builder.toString();
            return singlErrorResult(invalid(level, "seznam obsahuje %d souborů namísto očekávaných %d: %s", fileList.size(), expectedSize, filenamesStr));
        } else {
            return new ValidationResult();
        }
    }
}
