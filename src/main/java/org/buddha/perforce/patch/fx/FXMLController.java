package org.buddha.perforce.patch.fx;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.server.IServer;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import static java.util.prefs.Preferences.userNodeForPackage;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.buddha.perforce.patch.Item;
import org.buddha.perforce.patch.util.Mapping;
import org.buddha.perforce.patch.util.P4Manager;
import org.buddha.perforce.patch.Config;
import static org.buddha.perforce.patch.util.StringUtils.concatItems;

public class FXMLController implements Initializable {

	@FXML
	private Text info;

	@FXML
	private Text actiontarget;

	@FXML
	private TextField p4PortField;

	@FXML
	private TextField userNameField;

	@FXML
	private TextField passwordField;

	@FXML
	private Button signInButton;

	@FXML
	private ComboBox workspaceField;

	@FXML
	private ComboBox changeListField;

	@FXML
	private Button generatePatchButton;

	@FXML
	private ProgressBar progressField;

	private IServer server;
	private IClient client;
	private Task worker;
	List<Item> items;

	@FXML
	private void handleSignInButtonAction(ActionEvent event) {
		try {
			worker = createLoginWorker();

			info.textProperty().bind(worker.messageProperty());

			Thread thread = new Thread(worker);
			thread.setDaemon(true);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
			info.setText("Login Failed");
		}
	}

	@FXML
	private void handleGeneratePatchButtonAction(ActionEvent event) throws ConnectionException, RequestException, AccessException, InterruptedException {
		generatePatchButton.setDisable(true);
		progressField.setProgress(0);
		worker = createPatchWorker();

		progressField.progressProperty().unbind();
		progressField.progressProperty().bind(worker.progressProperty());
		generatePatchButton.disableProperty().unbind();
		generatePatchButton.disableProperty().bind(worker.runningProperty());

		new Thread(worker).start();

	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		Preferences prefs = userNodeForPackage(MainApp.class);
		Config.P4CHANGELIST = prefs.getInt(Config.P4CHANGELIST_KEY, 0);
		Config.P4USER = prefs.get(Config.P4USER_KEY, "");
		Config.P4PASSWORD = prefs.get(Config.P4PASSWORD_KEY, "");
		Config.P4CLIENT = prefs.get(Config.P4CLIENT_KEY, "");
		Config.P4PORT = prefs.get(Config.P4PORT_KEY, "");

		signInButton.disableProperty().bind(Bindings.equal("", p4PortField.textProperty())
				.or(Bindings.equal("", userNameField.textProperty()))
				.or(Bindings.equal("", passwordField.textProperty()))
				.or(Bindings.not(generatePatchButton.disableProperty())));
		changeListField.setDisable(true);
		workspaceField.setDisable(true);
		generatePatchButton.setDisable(true);
		info.setText("Sign In");
		p4PortField.setText(Config.P4PORT);
		userNameField.setText(Config.P4USER);
		passwordField.setText(Config.P4PASSWORD);
		userNameField.requestFocus();
	}

	public Task createPatchWorker() {
		return new Task() {
			@Override
			protected Object call() {
				try {
					client = P4Manager.getClient(workspaceField.getValue().toString());
					updateProgress(0.1, 1.0);
					Mapping map = new Mapping(client);
					items = new ArrayList<Item>();
					List<IFileSpec> files = P4Manager.getChangelistFiles(Integer.parseInt(changeListField.getValue().toString()));
					updateProgress(0.2, 1.0);
					double d = 0.7 / files.size();
					double p = 0.2;
					for (IFileSpec fileSpec : files) {
						Item item = new Item(fileSpec, map);
						items.add(item);
						p += d;
						updateProgress(p, 1.0);
					}

					String out = concatItems(items, System.lineSeparator());
					FileWriter writer = new FileWriter(Config.P4CHANGELIST + ".diff");
					writer.write(out);
					updateProgress(1.0, 1.0);
					return true;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return true;
			}
		};
	}

	public Task createLoginWorker() {
		return new Task() {
			@Override
			protected void succeeded() {
				info.textProperty().unbind();
			}

			@Override
			public Void call() throws InterruptedException {
				updateMessage("Logging In");
				try {
					server = P4Manager.connect(p4PortField.getText(), userNameField.getText(), passwordField.getText());
					updateMessage("Login Success");
					changeListField.setDisable(false);
					workspaceField.setDisable(false);
					workspaceField.setValue(Config.P4CLIENT);
					changeListField.setValue(Config.P4CHANGELIST);
					generatePatchButton.setDisable(false);
					userNameField.setDisable(true);
					passwordField.setDisable(true);
					p4PortField.setDisable(true);
				} catch (Exception ex) {
					ex.printStackTrace();
					updateMessage("Login Failed");
				}
				return null;
			}
		};
	}
}
