package com.tabs;

import com.nexmo.messaging.sdk.NexmoSmsClient;
import com.nexmo.messaging.sdk.SmsSubmissionResult;
import com.nexmo.messaging.sdk.messages.TextMessage;
import java.net.URL;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import tabs.*;

public class MainController implements Initializable {
    
    //Write Message StackPane
    @FXML private ToggleButton tgWrite;
    @FXML private ToggleButton tgOutbox;
    @FXML private ToggleButton tgConfiguration;
    @FXML private StackPane spWrite;
    @FXML private StackPane spOutbox;
    @FXML private StackPane spConfiguration;
    @FXML private TextField tfPhoneNo;
    @FXML private TextField tfSender;
    @FXML private TextArea tfMessage;
    @FXML private Label lblRemainingChars;
    
    //Outbox StackPane
    @FXML private TableView tblOutbox;
    @FXML private TableColumn colNumber;
    @FXML private TableColumn colMessage;
    
    //Configuration StackPane
    @FXML private TextField tfKey;
    @FXML private TextField tfSecret;
    
    private static MainController instance;
    private static Firm firm;
    private static List<Outbox> outboxList;
    private static ObservableList<Outbox> observableOutbox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;

        spWrite.visibleProperty().bind(tgWrite.selectedProperty());
        spOutbox.visibleProperty().bind(tgOutbox.selectedProperty());
        spConfiguration.visibleProperty().bind(tgConfiguration.selectedProperty());

        tfPhoneNo.lengthProperty().addListener(new TextRestriction("\\d", tfPhoneNo, 12));
        tfSender.lengthProperty().addListener(new TextRestriction("[a-zA-Z]", tfSender, 10));
        tfMessage.lengthProperty().addListener(new TextRestriction("[\\d\\D\\s]", tfMessage, 200));
        
        colMessage.setCellValueFactory(new PropertyValueFactory<Outbox, String>("message"));
        colNumber.setCellValueFactory(new PropertyValueFactory<Outbox, String>("number"));
        observableOutbox = FXCollections.observableArrayList();
        outboxList = Config.loadOutbox();
        tblOutbox.setItems(observableOutbox);
        
        loadFirm();
        loadOutboxList();
    }   
    
    private void loadFirm() {
        firm = Config.loadFirm();
    }
    
    private void loadOutboxList() {
        observableOutbox.clear();
        for (int i = 0; i < outboxList.size(); i++) {
            observableOutbox.add(new Outbox(outboxList.get(i).number, outboxList.get(i).message));
        }
        
        Config.writeOutbox(outboxList);
    }
    
    @FXML private void sendMessage(ActionEvent evt) {
        try {
            NexmoSmsClient client = new NexmoSmsClient(firm.getKey(), firm.getSecret());
            TextMessage message = new TextMessage(tfSender.getText(), tfPhoneNo.getText(), tfMessage.getText());
            SmsSubmissionResult[] result = client.submitMessage(message);
            
            for(int i=0; i<result.length; i++) {
                if(result[i].getStatus() == 0) {
                    FXDialog.showMessageDialog("Your message has been sent.", "Free SMS", Message.INFORMATION);
                    tfMessage.requestFocus();
                }
                else {
                    if (result[i].getStatus() != 2) {
                        outboxList.add(new Outbox(tfPhoneNo.getText(), tfMessage.getText()));
                        loadOutboxList();
                    }
                    throw new Exception(result[i].getErrorText());
                }
            }

        } catch (Exception e) {
            FXDialog.showMessageDialog(e.getLocalizedMessage(), "Error", Message.ERROR);
        }
    }
    
    @FXML private void deleteAllOutbox(ActionEvent evt) {
        outboxList.clear();
        loadOutboxList();
    }
    
    @FXML private void deleteInOutbox(ActionEvent evt) {
        if (tblOutbox.getSelectionModel().getSelectedIndex() != -1) {
            outboxList.remove(tblOutbox.getSelectionModel().getSelectedIndex());
            loadOutboxList();
        }
    }
    
    @FXML private void sendOutbox(ActionEvent evt) {
        if(tblOutbox.getSelectionModel().getSelectedIndex() != -1) {
            tfPhoneNo.setText(outboxList.get(tblOutbox.getSelectionModel().getSelectedIndex()).number);
            tfMessage.setText(outboxList.get(tblOutbox.getSelectionModel().getSelectedIndex()).message);
        
            tgWrite.setSelected(true);
        }
    }
    
    @FXML private void configure(ActionEvent evt) {
        Config.writeFirm(tfKey.getText(), tfSecret.getText());
        loadFirm();
        
        tfKey.clear();
        tfSecret.clear();
        
        FXDialog.showMessageDialog("API Configuration is now updated.", "Free SMS", Message.INFORMATION);
    }

    public static MainController getInstance() {
        return instance;
    }
    
    public void updateRemainingChars(int r) {
        lblRemainingChars.setText(r + "");
    }
}
