/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.echocat.marquardt.example.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class ExampleClient extends Application {
    private static final String FXML_MAIN_SCENE = "/ui/exampleclient.fxml";
    private static final String WINDOW_TITLE = "Marquardt Demo Client";

    @Override
    public void start(final Stage primaryStage) throws Exception {
       final MainSceneController mainController = new MainSceneController();
        final Scene mainScene = createScene(loadFxml(mainController, FXML_MAIN_SCENE));
        primaryStage.setResizable(false);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.show();
        primaryStage.toFront();
    }

    private Scene createScene(final Node content) {
        final StackPane layout = new StackPane(content);
        return new Scene(layout);
    }

    private Node loadFxml(final Object controller, final String resource) throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(factory -> controller);
        try (final InputStream resourceAsStream = getClass().getResourceAsStream(resource)) {
            return loader.load(resourceAsStream);
        }
    }

}
