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

    @Override
    public void start(Stage stage) throws Exception {

        BorderPane root = new BorderPane();
        ScrollPane dlList = new ScrollPane();
        VBox box = new VBox();
        BorderPane newUrl = new BorderPane();
        TextField text = new TextField();
        newUrl.setCenter(text);
        Button add = new Button("add");
        add.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                Downloader downloader;
                ProgressBar p = new ProgressBar();
                try {
                    BorderPane pane = new BorderPane();
                    downloader = new Downloader(text.getText());
                    Thread t = new Thread(downloader);
                    t.setDaemon(true);
                    Button playPause = new Button("||");
                    playPause.setOnAction((ActionEvent event2) -> {
                        downloader.isSuspended = !downloader.isSuspended;
                        if (!downloader.isSuspended) {
                            downloader.play();
                            playPause.setText("||");
                        } else {
                            downloader.pause();
                            playPause.setText(">");
                        }
                    });
                    Button stop = new Button("X");
                    stop.setOnAction((ActionEvent event2) -> {
                        t.interrupt();
                        box.getChildren().remove(pane);
                    });

                    downloader.setOnSucceeded(new EventHandler() {

                        @Override
                        public void handle(Event event) {
                        }
                    });

                    HBox tool = new HBox(playPause, stop);
                    p.progressProperty().bind(downloader.progressProperty());
                    t.setDaemon(true);
                    t.start();
                    p.prefWidthProperty().bind(pane.widthProperty());
                    pane.setTop(new Label(text.getText()));
                    pane.setCenter(p);
                    pane.setRight(tool);
                    box.getChildren().add(pane);
                    text.clear();

                } catch (RuntimeException e) {
                    System.err.format("skipping %s %s\n", text.getText(), e);
                }

            }
        }
        );
        newUrl.setRight(add);

        root.setBottom(newUrl);

        getParameters()
                .getRaw().stream().map((expression) -> {
                    BorderPane pane = new BorderPane();
                    Downloader dl = new Downloader(expression);
                    Thread t = new Thread(dl);
                    Button playPause = new Button("||");
                    Button stop = new Button("X");
                    stop.setOnAction((ActionEvent event) -> {
                        t.interrupt();
                        box.getChildren().remove(pane);
                    });
                    HBox tool = new HBox(playPause, stop);
                    ProgressBar p = new ProgressBar();
                    playPause.setOnAction((ActionEvent event) -> {
                        dl.isSuspended = !dl.isSuspended;
                        if (!dl.isSuspended) {
                            dl.play();
                            playPause.setText("||");
                        } else {
                            dl.pause();
                            playPause.setText(">");
                        }

                    });
                    dl.setOnSucceeded(new EventHandler() {

                        @Override
                        public void handle(Event event) {
                            p.getStyleClass().add("green-bar");
                        }
                    });
                    p.progressProperty().bind(dl.progressProperty());
                    t.setDaemon(true);
                    t.start();
                    p.prefWidthProperty().bind(pane.widthProperty());
                    pane.setTop(new Label(expression));
                    pane.setCenter(p);
                    pane.setRight(tool);
                    return pane;
                }
                ).forEach(
                        (pane) -> {
                            box.getChildren().add(pane);
                        }
                );
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
