package com.tabs;

import java.io.Serializable;

public class Outbox implements Serializable {
    
    String number;
    String message;

    public Outbox(String number, String message) {
        this.number = number;
        this.message = message;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
