package cyder.genesis;

import cyder.ui.CyderFrame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class Test {
    private File returnFile;

    private File getFile(String title) {
        CyderFrame frame = new CyderFrame(1,1, new ImageIcon(""));
        frame.setTitle("");
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setVisible(true);

        AtomicReference<File> ret = new AtomicReference<>();

        Platform.runLater(() -> ret.set(innerGetFile(fxPanel, title)));

        while (ret.get() == null)
            Thread.onSpinWait();

        return ret.get();
    }

    private File innerGetFile(JFXPanel fxPanel, String title) {
        try {
            Stage primaryStage = new Stage();
            HBox root = new HBox();
            Scene scene = new Scene(root, 1, 1);
            fxPanel.setScene(scene);

            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.setScene(scene);
            FileChooser fc = new FileChooser();
            fc.setTitle(title);
            returnFile = fc.showOpenDialog(primaryStage);

        } catch (Exception e) {
            e.printStackTrace();
        }

        File ret = returnFile;
        //clearFile();
        return ret;
    }

    public static void main(String[] args) {
        new Test();
    }

    public Test() {
        SwingUtilities.invokeLater(() -> System.out.println(getFile("Choose the new user's background file")));
    }
}