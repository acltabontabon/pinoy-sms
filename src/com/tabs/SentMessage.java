package com.tabs;

import java.io.Serializable;

public class SentMessage implements Serializable {
    
    private String message, number;

    public SentMessage(String message, String number) {
        this.message = message;
        this.number = number;
    }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public String getNumber() { return number; }

    public void setNumber(String number) { this.number = number; }
}
