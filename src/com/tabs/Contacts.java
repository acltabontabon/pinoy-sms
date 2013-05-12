package com.tabs;

import java.io.Serializable;

public class Contacts implements Serializable {
    
    private String name, number;

    public Contacts(String name, String number) {
        this.number = number;
        this.name = name;
    }

    public String getNumber() { return number; }

    public void setNumber(String number) { this.number = number; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
