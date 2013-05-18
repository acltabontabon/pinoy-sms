package com.tabs;

import java.io.*;
import java.util.List;
import tabs.*;

public class Config {

    public static void writeFirm(String key, String secret) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("config"))) {
            oos.writeObject(new Firm(key, secret));
        } catch (Exception ex) {
            FXDialog.showMessageDialog(ex.getMessage(), "ERROR", Message.ERROR);
        }
    }
    
    public static Firm loadFirm() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("config"))) {
            return ((Firm) ois.readObject());
        } catch (Exception ex) {
            return null;
        } 
    }
    
    public static void writeContacts(List<Contacts> lst) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("contacts"))) {
            oos.writeObject(lst);
        } catch (Exception ex) {
            FXDialog.showMessageDialog(ex.getMessage(), "ERROR", Message.ERROR);
        }
    }
    
    public static List<Contacts> loadContacts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("contacts"))) {
            return ((List<Contacts>) ois.readObject());
        } catch (Exception ex) {
            return null;
        } 
    }
    
    public static void writeSentItems(List<SentMessage> lst) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("sent"))) {
            oos.writeObject(lst);
        } catch (Exception ex) {
            FXDialog.showMessageDialog(ex.getMessage(), "ERROR", Message.ERROR);
        }
    }
    
    public static List<SentMessage> loadSentItems() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("sent"))) {
            return ((List<SentMessage>) ois.readObject());
        } catch (Exception ex) {
            return null;
        } 
    }
}
