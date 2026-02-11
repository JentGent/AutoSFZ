package io.github.jentgent.autosfz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onSampleLoadButtonClick(ActionEvent event) {
        var chooser = new FileChooser();
        chooser.setTitle("Select audio samples");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("WAV Files", "*.wav")
        );

        var window = ((Node) event.getSource()).getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(window);

        if (files == null) {
            System.out.println("No files selected");
        } else {
            for (var file : files) {
                System.out.println(file.getPath());
            }
        }
    }
}
