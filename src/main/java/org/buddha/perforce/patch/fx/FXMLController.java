package org.buddha.perforce.patch.fx;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.impl.generic.core.file.FilePath;
import com.perforce.p4java.server.IServer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static java.util.prefs.Preferences.userNodeForPackage;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.buddha.perforce.patch.Item;
import org.buddha.perforce.patch.util.Mapping;
import org.buddha.perforce.patch.util.P4Manager;
import org.buddha.perforce.patch.Config;
import org.buddha.perforce.patch.util.StringUtils;
import static org.buddha.perforce.patch.util.StringUtils.concatItems;

public class FXMLController implements Initializable {

    @FXML
    private Accordion accordion;

    @FXML
    private TitledPane connectPane;

    @FXML
    private TitledPane generatePane;

    @FXML
    private TitledPane logPane;

    @FXML
    private TextField p4PortField;

    @FXML
    private TextField userNameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button signInButton;

    @FXML
    private TextField workspaceField;

    @FXML
    private TextField changeListField;

    @FXML
    private Button generatePatchButton;

    @FXML
    private TextArea console;
	
	@FXML
	private CheckBox remember;

    private IServer server;
    private IClient client;
    private Task worker;
    List<Item> items;

    @FXML
    private void handleSignInButtonAction(ActionEvent event) throws InterruptedException {
        try {
            worker = createLoginWorker();
            worker.messageProperty().addListener(loggerListener());
            Thread thread = new Thread(worker);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            console.appendText(StringUtils.exceptionToString(e));
        }
    }

    @FXML
    private void handleGeneratePatchButtonAction(ActionEvent event) throws ConnectionException, RequestException, AccessException, InterruptedException {
        worker = createPatchWorker();
        worker.messageProperty().addListener(loggerListener());
        new Thread(worker).start();
    }
	
	@FXML
    private void handleRemember(ActionEvent event) throws ConnectionException, RequestException, AccessException, InterruptedException {
		P4Manager.remember(remember.isSelected());
		console.appendText("Remember Setting: " + remember.isSelected()+System.lineSeparator());
    }

    private ChangeListener<String> loggerListener() {
        return new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                accordion.setExpandedPane(logPane);
                synchronized (FXMLController.class) {
                    console.appendText(t1 + System.lineSeparator());
                }
            }
        };
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Preferences prefs = userNodeForPackage(MainApp.class);
        Config.P4CHANGELIST = prefs.getInt(Config.P4CHANGELIST_KEY, 0);
        Config.P4USER = prefs.get(Config.P4USER_KEY, "");
        Config.P4PASSWORD = prefs.get(Config.P4PASSWORD_KEY, "");
        Config.P4CLIENT = prefs.get(Config.P4CLIENT_KEY, "");
        Config.P4PORT = prefs.get(Config.P4PORT_KEY, "");
        accordion.setExpandedPane(connectPane);
        signInButton.disableProperty().bind(Bindings.equal("", p4PortField.textProperty())
                .or(Bindings.equal("", userNameField.textProperty()))
                .or(Bindings.equal("", passwordField.textProperty()))
                .or(Bindings.not(generatePatchButton.disableProperty())));
        changeListField.setDisable(true);
        workspaceField.setDisable(true);
        generatePatchButton.setDisable(true);
        p4PortField.setText(Config.P4PORT);
        userNameField.setText(Config.P4USER);
        passwordField.setText(Config.P4PASSWORD);
    }

    public Task createPatchWorker() {
        return new Task() {

            @Override
            protected void succeeded() {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName(Config.P4CHANGELIST + ".diff");
                fileChooser.setTitle("Save Patch");
                File file = fileChooser.showSaveDialog(Config.STAGE);
                String out = concatItems(items, System.lineSeparator() + System.lineSeparator());
                if (file != null) {
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(out);
                        updateMessage("Patch Generated");
                    } catch (IOException ex) {
                        StringWriter writer = new StringWriter();
                        ex.printStackTrace(new PrintWriter(writer));
                        updateMessage(writer.toString());
                    }
                } else {
                    updateMessage("Patch Cancelled");
                }
            }

            @Override
            protected Object call() {
                try {
                    updateMessage("Fetching Workspace: " + workspaceField.getText());
                    client = P4Manager.getClient(workspaceField.getText());
                    updateProgress(0.1, 1.0);

                    Mapping map = new Mapping(client);
                    items = new ArrayList<>();
                    updateMessage("Gathering Files");
                    List<IFileSpec> files = P4Manager.getChangelistFiles(Integer.parseInt(changeListField.getText()));
                    updateProgress(0.2, 1.0);
                    double d = 0.7 / files.size();
                    double p = 0.2;
                    updateMessage("Collecting Revisions");
                    for (IFileSpec fileSpec : files) {
                        String[] pathStrings = fileSpec.getPath(FilePath.PathType.DEPOT).getPathString().split("/");
                        updateMessage("Analysing: " + pathStrings[pathStrings.length - 1]);
                        Item item = new Item(fileSpec, map);
                        items.add(item);
                        p += d;
                        updateProgress(p, 1.0);
                    }
                    updateMessage("Comparing Revisions");
                    updateProgress(1.0, 1.0);
                    return true;
                } catch (ConnectionException | RequestException | AccessException | BackingStoreException | NumberFormatException | IOException ex) {
                    StringWriter writer = new StringWriter();
                    ex.printStackTrace(new PrintWriter(writer));
                    updateMessage(writer.toString());
                }
                return false;
            }
        };
    }

    public Task createLoginWorker() {
        return new Task() {
            @Override
            protected void succeeded() {
                accordion.setExpandedPane(generatePane);
            }

            @Override
            public Void call() {
                updateMessage("Logging In");
                updateProgress(5, 10);
                try {
                    server = P4Manager.connect(p4PortField.getText(), userNameField.getText(), passwordField.getText());
                    updateMessage("Login Success");
                    updateProgress(10, 10);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            changeListField.setDisable(false);
                            workspaceField.setDisable(false);
                            workspaceField.setText(Config.P4CLIENT);
                            changeListField.setText("" + Config.P4CHANGELIST);
                            generatePatchButton.setDisable(false);
                            userNameField.setDisable(true);
                            passwordField.setDisable(true);
                            p4PortField.setDisable(true);
                        }
                    });
                } catch (Exception ex) {
                    updateMessage(StringUtils.exceptionToString(ex));
                    updateProgress(0, 10);
                }
                return null;
            }
        };
    }
}
