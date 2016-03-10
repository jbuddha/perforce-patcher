package org.buddha.perforce.patch.fx;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.ImageIcon;
import org.buddha.perforce.patch.util.P4Manager;
import org.buddha.perforce.patch.Config;
import org.buddha.perforce.patch.util.StringUtils;

public class MainApp extends Application {
    
    static HashMap options = new HashMap();

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Patchy.fxml"));
        Config.STAGE = stage;
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        stage.getIcons().add(new Image("/images/logo.png"));
        stage.setTitle("Perforce Patcher");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        try {
            URL iconURL = MainApp.class.getResource("/images/logo.png");
            java.awt.Image image = new ImageIcon(iconURL).getImage();
            Class clazz1 = Class.forName("com.apple.eawt.Application");
            Method method = clazz1.getMethod("getApplication", (Class[]) null);
            Object o = method.invoke(clazz1, (Object[]) null);
            Method m2 = o.getClass().getMethod("setDockIconImage", java.awt.Image.class);
            m2.invoke(o, image);
        } catch (RuntimeException e) {
            // Won't work on Windows or Linux.
        }
        stage.show();
        stage.setOnCloseRequest(createCloseHandler());
    }

    private EventHandler<WindowEvent> createCloseHandler() {
        return new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                P4Manager.disconnect();
            }
        };
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        options = StringUtils.parseArgs(args);
        launch(args);
    }

}
