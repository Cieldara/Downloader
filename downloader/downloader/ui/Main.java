/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.ui;

import downloader.fc.Downloader;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author gontardb
 */
public class Main extends Application {

    /* Crée, à partir d'une chaîne de caractères, un téléchargement */
    public void createDownload(String text, VBox box) {
        Downloader downloader;
        ProgressBar p = new ProgressBar();
        try {
            BorderPane pane = new BorderPane();
            downloader = new Downloader(text);
            Thread t = new Thread(downloader);
            t.setDaemon(true);
            Label label = new Label(text);
            Button playPause = new Button("||");
            playPause.setOnAction((ActionEvent event2) -> {
                downloader.isSuspended = !downloader.isSuspended;
                if (!downloader.isSuspended) {
                    downloader.play();
                    playPause.setText("||");
                    p.setStyle("");
                } else {
                    downloader.pause();
                    playPause.setText(">");
                    p.setStyle("-fx-accent: yellow");
                }
            });
            Button stop = new Button("X");
            stop.setOnAction((ActionEvent event2) -> {
                t.interrupt();
                box.getChildren().remove(pane);
            });
            //Quand le téléchargement est terminé.
            downloader.setOnSucceeded(new EventHandler() {
                @Override
                public void handle(Event event) {
                    p.setStyle("-fx-accent: green");
                    label.setText(label.getText() + " : " + "Terminé");
                    playPause.setVisible(false);
                }
            });

            HBox tool = new HBox(playPause, stop);
            p.progressProperty().bind(downloader.progressProperty());
            t.setDaemon(true);
            t.start();
            p.prefWidthProperty().bind(pane.widthProperty());
            pane.setTop(label);
            pane.setCenter(p);
            pane.setRight(tool);
            box.getChildren().add(pane);

        } catch (RuntimeException e) {
            System.err.format("skipping %s %s\n", text, e);
        }

    }

    @Override
    public void start(Stage stage) throws Exception {
        //BorderPane à la racine de notre fenêtre
        BorderPane root = new BorderPane();
        //Liste déroulante pour les téléchargements
        ScrollPane dlList = new ScrollPane();
        //VBox qui contiendra les téléchargements
        VBox box = new VBox();
        //BorderPane pour le textfield et le bouton ajouter qui sera en bas de notre fenêtre
        BorderPane newUrl = new BorderPane();
        TextField text = new TextField();
        newUrl.setCenter(text);
        Button add = new Button("add");
        add.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createDownload(text.getText(), box);
                text.clear();
            }
        });
        newUrl.setRight(add);
        root.setBottom(newUrl);

        //Création des téléchargements initiaux à partir des paramètres
        for (String expression : getParameters().getRaw()) {
            createDownload(expression, box);
        }
        dlList.setContent(box);
        dlList.setFitToWidth(true);

        root.setCenter(dlList);

        stage.setTitle(
                "Downloader");
        stage.setScene(
                new Scene(root));
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
