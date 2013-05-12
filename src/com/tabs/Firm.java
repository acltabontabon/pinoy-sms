package com.tabs;

import java.io.Serializable;

public class Firm implements Serializable {
    
    private final String key, secret;
    
    public Firm(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }
    
    public String getKey() { return key; }
    
    public String getSecret() { return secret; }
}
