package org.buddha.perforce.patch.util;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.buddha.perforce.patch.P4Manager;
import org.buddha.temp.TempConfig;

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
    private TextField changeListIdField;

    @FXML
    private Button generatePatchButton;
    
    @FXML
    private void handleSignInButtonAction(ActionEvent event) {
        try {
            Task <Void> task = new Task<Void>() {
                
                @Override 
                protected void succeeded() {
                    info.textProperty().unbind();
                }
                
                @Override 
                public Void call() throws InterruptedException {
                    updateMessage("Logging In");
                    try {
                        P4Manager.connect(p4PortField.getText(), userNameField.getText(), passwordField.getText());
                        updateMessage("Login Success"); 
                        changeListIdField.setDisable(false);
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

            info.textProperty().bind(task.messageProperty());

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();  
        } catch(Exception e) {
            e.printStackTrace();
            info.setText("Login Failed");
        }
    }
    
    @FXML
    private void handleGeneratePatchButtonAction(ActionEvent event){
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        signInButton.disableProperty().bind(Bindings.equal("", p4PortField.textProperty())
                                        .or(Bindings.equal("", userNameField.textProperty()))
                                        .or(Bindings.equal("", passwordField.textProperty()))
                                        .or(Bindings.not(generatePatchButton.disableProperty())));
        changeListIdField.setDisable(true);
        generatePatchButton.setDisable(true);
        info.setText("Sign In");
        p4PortField.setText(TempConfig.P4PORT);
		userNameField.setText(TempConfig.P4USER);
        userNameField.requestFocus();
    }    
}
