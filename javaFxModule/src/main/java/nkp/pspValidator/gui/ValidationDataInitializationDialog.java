package nkp.pspValidator.gui;

import javafx.stage.Stage;

import java.net.URL;

/**
 * Created by martin on 13.12.16.
 */
public class ValidationDataInitializationDialog extends AbstractDialog {

    public ValidationDataInitializationDialog(Stage stage, Main main) {
        super(stage, main);
    }

    @Override
    URL getFxmlResource() {
        return getClass().getResource("/fxml/validationDataInitialization.fxml");
    }

    @Override
    int getWidth() {
        return 1000;
    }

    @Override
    int getHeight() {
        return 300;
    }

    @Override
    String getTitle() {
        return "Inicializace validačních dat";
    }

    @Override
    void setControllerData(DialogController controller) {
        //nothing
    }

}