package org.buddha.perforce.patch.fx;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.impl.generic.core.file.FilePath;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
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
import org.buddha.perforce.patch.util.PreferenceCache;
import org.buddha.perforce.patch.util.StringUtils;
import static org.buddha.perforce.patch.util.StringUtils.concatItems;

/**
 * Controller for all the UI operations
 * 
 * @author jbuddha
 */
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
    private ChoiceBox workspaceField;

    @FXML
    private ChoiceBox changeListField;

    @FXML
    private Button generatePatchButton;

    @FXML
    private TextArea console;

    @FXML
    private CheckBox remember;

    private IClient client;
    private Task worker;
    private List<Item> items;
	private Map<String, ArrayList<String>> changelists;
	private String[] workspaces;
	PreferenceCache prefs = PreferenceCache.getInstance();
	
    @FXML
    private void handleSignInButtonAction(ActionEvent event) throws InterruptedException {
        try {
            worker = getLoginWorker();
            worker.messageProperty().addListener(getLoggerListener());
            Thread thread = new Thread(worker);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            console.appendText(StringUtils.exceptionToString(e));
        }
    }

    @FXML
    private void handleGeneratePatchButtonAction(ActionEvent event) throws ConnectionException, RequestException, AccessException, InterruptedException {
        worker = getPatchWorker();
        worker.messageProperty().addListener(getLoggerListener());
        new Thread(worker).start();
    }

    @FXML
    private void handleRemember(ActionEvent event) throws ConnectionException, RequestException, AccessException, InterruptedException {
        prefs.persist(remember.isSelected());
        console.appendText("Remember Setting: " + remember.isSelected() + System.lineSeparator());
    }
	
	@FXML
    private void handleClickGithubRepoLink(ActionEvent event) {
		try {
			java.awt.Desktop.getDesktop().browse(new URI("https://github.com/jbuddha/perforce-patcher/"));
		} catch (IOException | URISyntaxException ex) {
			Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
		}
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
		
        accordion.setExpandedPane(connectPane);
        signInButton.disableProperty().bind(Bindings.equal("", p4PortField.textProperty())
                .or(Bindings.equal("", userNameField.textProperty()))
                .or(Bindings.equal("", passwordField.textProperty()))
                .or(Bindings.not(generatePatchButton.disableProperty())));
        changeListField.setDisable(true);
        workspaceField.setDisable(true);
        generatePatchButton.setDisable(true);
        if (!MainApp.options.isEmpty()) {
            try {
            p4PortField.setText(MainApp.options.get("-p").toString());
            } catch (Exception e) {
                console.appendText("Unable to get port from command line.\n");
            }
            try {
                userNameField.setText(MainApp.options.get("-u").toString());
            } catch (Exception e) {
                console.appendText("Unable to get user from command line.\n");
            }
             
        } else {
            p4PortField.setText(prefs.getP4port());
            userNameField.setText(prefs.getUsername());
            passwordField.setText(prefs.getPassword());
        }
	workspaceField.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(changelists != null && changelists.containsKey(newValue)) {
                    changeListField.getItems().setAll(changelists.get(newValue));
                    changeListField.getSelectionModel().selectFirst();
                }
            }
	});
    }

	private ChangeListener<String> getLoggerListener() {
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
	
    public Task getPatchWorker() {
        return new Task() {

            @Override
            protected void succeeded() {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName(prefs.getChangelist() + ".diff");
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
                    updateMessage("Fetching Workspace: " + workspaceField.getValue().toString());
                    client = P4Manager.getClient(workspaceField.getValue().toString());
                    updateProgress(0.1, 1.0);

                    Mapping map = new Mapping(client);
                    items = new ArrayList<>();
                    updateMessage("Gathering Files");
                    List<IFileSpec> files = P4Manager.getChangelistFiles(Integer.parseInt(changeListField.getValue().toString().split("-")[0]));
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

    public Task getLoginWorker() {
        return new Task() {
			
            @Override
            protected void succeeded() {
                if (getProgress() > 0.1) {
                    accordion.setExpandedPane(generatePane);
                } else {
                    connectPane.setText("Connect - Failed");
                    accordion.setExpandedPane(connectPane);
                }

            }

            @Override
            public Void call() {
                updateMessage("Logging In");
                updateProgress(1, 10);
                try {
                    updateMessage("Connecting to " + p4PortField.getText() + " as " + userNameField.getText());
                    P4Manager.connect(p4PortField.getText(), userNameField.getText(), passwordField.getText());
                    updateMessage("Login Success");
					updateProgress(5, 10);
					Thread.sleep(100);
					updateMessage("Fetching Pending Changelists & Workspaces");
					changelists = P4Manager.getPendingChangeLists();
					workspaces = (String[]) changelists.keySet().toArray(new String[0]);
                    updateProgress(10, 10);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            changeListField.setDisable(false);
                            workspaceField.setDisable(false);
							workspaceField.getItems().addAll(workspaces);
							if(workspaceField.getItems().contains(prefs.getWorkspace())) {
								workspaceField.setValue(prefs.getWorkspace());
							}
							else
							{
								if(workspaces.length > 0)
									workspaceField.getSelectionModel().selectFirst();
							}
                            generatePatchButton.setDisable(false);
                            userNameField.setDisable(true);
                            passwordField.setDisable(true);
                            p4PortField.setDisable(true);
                            connectPane.setText("Connect - Success");
                        }
                    });
                } catch (Exception ex) {
                    updateProgress(0, 10);
                    updateMessage(StringUtils.exceptionToString(ex));

                }
                return null;
            }
        };
    }
}
