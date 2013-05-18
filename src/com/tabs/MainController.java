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
    
    //Write Message StackPane
    @FXML private ToggleButton tgWrite;
    @FXML private ToggleButton tgOutbox;
    @FXML private ToggleButton tgConfiguration;
    @FXML private StackPane spWrite;
    @FXML private StackPane spContacts;
    @FXML private StackPane spConfiguration;
    @FXML private TextField tfPhoneNo;
    @FXML private TextField tfSender;
    @FXML private TextArea tfMessage;
    @FXML private Label lblRemainingChars;
    
    //Contact StackPane
    @FXML private TableView tblContact;
    @FXML private TableColumn colName;
    @FXML private TableColumn colNumber;
    
    //Configuration StackPane
    @FXML private TextField tfKey;
    @FXML private TextField tfSecret;
    
    private static MainController instance;
    private static Firm firm;
    private static List<Contacts> contactList;
    private static ObservableList<Contacts> observableContacts;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        contactList = Config.loadContacts();
        
        // Bind the StackPane visibility to its designated toggle button        
        spWrite.visibleProperty().bind(tgWrite.selectedProperty());
        spContacts.visibleProperty().bind(tgOutbox.selectedProperty());
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
        
        initFirm();
        reloadContactList();
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
    
    @FXML private void sendMessage(ActionEvent evt) {
        try {
            NexmoSmsClient client = new NexmoSmsClient(firm.getKey(), firm.getSecret());
            TextMessage message = new TextMessage(tfSender.getText(), tfPhoneNo.getText(), tfMessage.getText());
            SmsSubmissionResult[] result = client.submitMessage(message);
            
            for(int i=0; i<result.length; i++) {
                if(result[i].getStatus() == 0) {
                    FXDialog.showMessageDialog("Your message has been sent.\n\nRemaining Balance: " + result[i].getRemainingBalance() + "\nMessage Cost: " + result[i].getMessagePrice(), "Pinoy SMS", Message.INFORMATION);
                    tfMessage.requestFocus();
                }
                else {
                    throw new Exception(result[i].getErrorText() + "\n\nRemaining Balance: " + result[i].getRemainingBalance());
                }
            }
        } catch (Exception e) {
            FXDialog.showMessageDialog(e.getLocalizedMessage(), "Error", Message.ERROR);
        }
    }
        
    @FXML private void deleteContact(ActionEvent evt) {
        if (tblContact.getSelectionModel().getSelectedIndex() != -1) {
            contactList.remove(tblContact.getSelectionModel().getSelectedIndex());
            reloadContactList();
        }
    }
    
    @FXML private void addContactNumber(ActionEvent evt) {
        String name = FXDialog.showInputDialog("Enter new contact name:", "Add Contact");
        if (name != null && !name.trim().equals("")) {
            String number = FXDialog.showInputDialog("Enter new contact number:", "Add Contact");

            if (number != null && !number.trim().equals("")) {
                contactList.add(new Contacts(name, number));
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
        Config.writeFirm(tfKey.getText(), tfSecret.getText());
        initFirm();
        
        tfKey.clear();
        tfSecret.clear();
        
        FXDialog.showMessageDialog("API Configuration has been updated.", "Pinoy SMS", Message.INFORMATION);
    }
}
