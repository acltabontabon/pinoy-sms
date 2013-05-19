package com.tabs;

import com.nexmo.messaging.sdk.*;
import com.nexmo.messaging.sdk.messages.TextMessage;
import java.net.URL;
import java.util.*;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import tabs.*;

public class MainController implements Initializable {

    private static MainController instance;
    private static Firm firm;
    private static List<Contacts> contactList;
    private static List<SentMessage> sentMessageList;
    private static ObservableList<Contacts> observableContacts;
    private static ObservableList<SentMessage> observableSentMessages;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        contactList = Config.loadContacts();
        sentMessageList = Config.loadSentItems();
        
        // Bind the StackPane visibility to its designated toggle button        
        spWrite.visibleProperty().bind(tgWrite.selectedProperty());
        spContacts.visibleProperty().bind(tgOutbox.selectedProperty());
        spSent.visibleProperty().bind(tgSent.selectedProperty());
        spConfiguration.visibleProperty().bind(tgConfiguration.selectedProperty());

        // Add validation for the input fields
        tfPhoneNo.lengthProperty().addListener(new TextRestriction("\\d", tfPhoneNo, 12));
        tfSender.lengthProperty().addListener(new TextRestriction("[a-zA-Z]", tfSender, 10));
        tfMessage.lengthProperty().addListener(new TextRestriction("[\\d\\D\\s]", tfMessage, 200));
        
        // Sets the tableView for the contacts list
        colName.setCellValueFactory(new PropertyValueFactory<Contacts, String>("name"));
        colNumber.setCellValueFactory(new PropertyValueFactory<Contacts, String>("number"));
        observableContacts = FXCollections.observableArrayList();
        tblContact.setItems(observableContacts);
        
        // Sets the tableView for the sent messages list
        colRecipient.setCellValueFactory(new PropertyValueFactory<SentMessage, String>("number"));
        colMessage.setCellValueFactory(new PropertyValueFactory<SentMessage, String>("message"));
        observableSentMessages = FXCollections.observableArrayList();
        tblSentMessage.setItems(observableSentMessages);
        
        initFirm();
        reloadContactList();
        reloadSentMessageList();
    }   
    
    public static MainController getInstance() { return instance; }
    
    // Create an instance of the Firm class to access api keys
    private void initFirm() { firm = Config.loadFirm(); }
    
    public void updateRemainingChars(int r) { lblRemainingChars.setText(r + ""); }
        
    /**
     * Updates the tableView for every changes in the contact list.
     */
    private void reloadContactList() {
        observableContacts.clear();
        
        for (int i = 0; i < contactList.size(); i++) {
            observableContacts.add(new Contacts(contactList.get(i).getName(), contactList.get(i).getNumber()));
        }
        
        Config.writeContacts(contactList);
    }
    
    private void reloadSentMessageList() {
        observableSentMessages.clear();
        
        for (int i = 0; i < sentMessageList.size(); i++) {
            observableSentMessages.add(new SentMessage(sentMessageList.get(i).getMessage(), sentMessageList.get(i).getNumber()));
        }
        
        Config.writeSentItems(sentMessageList);
    }
    
    private String checkPrefix(String num) {
        if (num.startsWith("0") && num.length() == 11)
            num = "63" + num.substring(1);
        
        return num;
    }
    
    @FXML private void sendMessage(ActionEvent evt) {
        boolean succeeded = false;  // sending status
        int attempt = 0;            // number of sending attempts.
        
        SmsSubmissionResult[] result =  null;
        
        try {
            NexmoSmsClient client = new NexmoSmsClient(firm.getKey(), firm.getSecret());
            TextMessage message = new TextMessage(tfSender.getText(), checkPrefix(tfPhoneNo.getText()), tfMessage.getText());
            result = client.submitMessage(message);

            for (int i = 0; i < result.length; i++) {
                if (result[i].getStatus() == 0) {    // Success
                    attempt = i;
                    succeeded = true;
                } else if (result[i].getStatus() == 2) {    // Missing params
                    throw new Exception("Your request is incomplete and missing some mandatory fields.");
                } else if (result[i].getStatus() == 4) {    // Invalid credentials
                    throw new Exception("The api_key / api_secret you supplied is either invalid or disabled.");
                } else if (result[i].getStatus() == 6 || result[i].getStatus() == 15) {    // Invalid message
                    throw new Exception("The Nexmo platform was unable to process this message, for example, \nan un-recognized number prefix.");
                } else if (result[i].getStatus() == 9) {    // Partner quota exceeded
                    throw new Exception("Your trial account does not have sufficient credit to process this \nmessage.");
                } else if (result[i].getStatus() == 10) {    // Too many existing binds
                    throw new Exception("The number of simultaneous connections to the platform exceeds the \ncapabilities of your account.");
                } else {
                    throw new Exception(String.format("Error Code: %d \nError Text: %s", result[i].getStatus(), result[i].getErrorText()));
                }
            }
        } catch (Exception e) {
            FXDialog.showMessageDialog(e.getLocalizedMessage(), "Error", Message.ERROR);
        } finally {
            if (succeeded) {
                sentMessageList.add(new SentMessage(tfMessage.getText(), tfPhoneNo.getText()));
                reloadSentMessageList();
                FXDialog.showMessageDialog("Your message has been sent.\n\nRemaining Balance: " + result[attempt].getRemainingBalance() + "\nMessage Cost: " + result[attempt].getMessagePrice(), "Pinoy SMS", Message.INFORMATION);
            }
            tfMessage.requestFocus();
        }
    }
        
    @FXML private void deleteContact(ActionEvent evt) {
        if (tblContact.getSelectionModel().getSelectedIndex() != -1) {
            contactList.remove(tblContact.getSelectionModel().getSelectedIndex());
            reloadContactList();
        }
    }
    
    @FXML private void deleteSentMessage(ActionEvent evt) {
        if (tblSentMessage.getSelectionModel().getSelectedIndex() != -1) {
            sentMessageList.remove(tblSentMessage.getSelectionModel().getSelectedIndex());
            reloadSentMessageList();
        }
    }
    
    @FXML private void addContactNumber(ActionEvent evt) {
        String name = FXDialog.showInputDialog("Enter new contact name:", "Add Contact");
        if (name != null && !name.trim().equals("")) {
            String number = FXDialog.showInputDialog("Enter new contact number:", "Add Contact");

            if (number != null && !number.trim().equals("")) {
                contactList.add(new Contacts(name, checkPrefix(number)));
                reloadContactList();
            }
        }
    }
    
    @FXML private void selectContact(ActionEvent evt) {
        if(tblContact.getSelectionModel().getSelectedIndex() != -1) {
            tfPhoneNo.setText(colNumber.getCellData((int) tblContact.getSelectionModel().getSelectedIndex()) + "");
            tgWrite.setSelected(true);
        }
    }

    @FXML private void configure(ActionEvent evt) {
        if (!tfKey.getText().trim().equals("") && !tfSecret.getText().trim().equals("")) {
            Config.writeFirm(tfKey.getText(), tfSecret.getText());
            initFirm();

            tfKey.clear();
            tfSecret.clear();

            FXDialog.showMessageDialog("API Configuration has been updated.", "Pinoy SMS", Message.INFORMATION);
        }
    }

    @FXML private ToggleButton tgWrite;
    @FXML private ToggleButton tgOutbox;
    @FXML private ToggleButton tgConfiguration;
    @FXML private ToggleButton tgSent;
    @FXML private StackPane spSent;
    @FXML private StackPane spWrite;
    @FXML private StackPane spContacts;
    @FXML private StackPane spConfiguration;
    
    //Write Message StackPane
    @FXML private TextField tfPhoneNo;
    @FXML private TextField tfSender;
    @FXML private TextArea tfMessage;
    @FXML private Label lblRemainingChars;
    
    //Contact StackPane
    @FXML private TableView tblContact;
    @FXML private TableColumn colName;
    @FXML private TableColumn colNumber;
    
    //Sent Message StackPane
    @FXML private TableView tblSentMessage;
    @FXML private TableColumn colRecipient;
    @FXML private TableColumn colMessage;
    
    //Configuration StackPane
    @FXML private TextField tfKey;
    @FXML private TextField tfSecret;
}
