package io.github.jentgent.autosfz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class MainController {
    @FXML private Label welcomeText;
    @FXML private ComboBox<String> pitchAlgo;
    @FXML private CheckBox correctPitch;
    @FXML private Text outputText;

    @FXML
    protected void onSampleLoadButtonClick(ActionEvent event) {
        var window = ((Node) event.getSource()).getScene().getWindow();
        File folder = getFolder(window);
        File[] files = getFiles(folder);
        if (files == null || files.length == 0) {
            System.out.println("Canceled");
            outputText.setText("No .wav files found");
            return;
        }

        var pitchToSample = new HashMap<Integer, Sample>();
        var keys = new TreeSet<Integer>();

        outputText.setText("");
        for (var file : files) {
            System.out.println(file.getPath());
            try {
                var sample = new Sample(file, AudioAnalysis.getPitchAlgo(pitchAlgo.getValue()));
                int closestKey = (int)Math.round(sample.getKey());
                while (pitchToSample.containsKey(closestKey)) closestKey++;
//                if (pitchToSample.containsKey(closestKey) && pitchToSample.get(closestKey).getProb() >= sample.getProb()) continue;
                pitchToSample.put(closestKey, sample);
                keys.add(closestKey);
            } catch (Exception e) {
                outputText.setText(outputText.getText() + "\n" + file.getName() + " failed: " + e.getMessage());
                System.out.println(e.getMessage());
            }

//            try {
//                var stream = AudioSystem.getAudioInputStream(file);
//                var clip = AudioSystem.getClip();
//                clip.open(stream);
//                clip.start();
//                Thread.sleep(clip.getMicrosecondLength() / 1000);
//                clip.close();
//                stream.close();
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
//
//            try {
//                var dispatcher = AudioDispatcherFactory.fromFloatArray(new float[44100], 44100, 1024, 0);
//                var wave = new SineGenerator(0.5, pitch);
//                System.out.println("Playing frequency");
//                dispatcher.addAudioProcessor(wave);
//                var format = new AudioFormat(44100, 16, 1, true, false);
//                dispatcher.addAudioProcessor(new AudioPlayer(format));
//                dispatcher.run();
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
        }

        File file = saveFile(window, folder);
        if (file == null) {
            System.out.println("Save file is null");
            outputText.setText("Couldn't save file");
            return;
        }
        try (var fw = new FileWriter(file);
             var s = new PrintWriter(fw)) {
            for (int key : keys) {
                var sample = pitchToSample.get(key);
                var closestKey = (int)Math.round(sample.getKey());
                int offset = 0;
                s.print("<region> sample=" + sample.getPath());
                s.print(" pitch_keycenter=" + (closestKey + offset));
                if (correctPitch.isSelected()) s.print(" tune=" + (int)((closestKey - sample.getKey()) * 100));
                if (keys.lower(key) != null) s.print(" lokey=" + (key + offset));
                var hikey = keys.higher(key);
                if (hikey != null) s.print(" hikey=" + (hikey + offset - 1));
                s.println();
            }
            System.out.println(file.getName() + " generated");
            outputText.setText(outputText.getText() + "\n" + file.getName() + " saved");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private static File getFolder(Window window) {
        var chooser = new DirectoryChooser();
        chooser.setTitle("Select sample folder");
        return chooser.showDialog(window);
    }
    private static File[] getFiles(File folder) {
        if (folder == null) {
            return new File[] {};
        }
        return folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
    }
    private static File saveFile(Window window, File folder) {
        var chooser = new FileChooser();
        chooser.setTitle("Save SFZ file");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SFZ files", ".sfz"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        chooser.setInitialFileName("soundfont.sfz");
        chooser.setInitialDirectory(folder);
        return chooser.showSaveDialog(window);
    }
}
