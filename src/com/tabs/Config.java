package com.tabs;

import java.io.*;
import java.util.List;
import tabs.*;

public class Config {

    private Config() { }

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
    
    public static void writeOutbox(List<Outbox> lst) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("outbox"))) {
            oos.writeObject(lst);
        } catch (Exception ex) {
            FXDialog.showMessageDialog(ex.getMessage(), "ERROR", Message.ERROR);
        }
    }
    
    public static List<Outbox> loadOutbox() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("outbox"))) {
            return ((List<Outbox>) ois.readObject());
        } catch (Exception ex) {
            return null;
        } 
    }
}
