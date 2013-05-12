package com.tabs;

import javafx.beans.value.*;
import javafx.scene.control.*;

public class TextRestriction implements ChangeListener {
    
    private TextField tf;
    private TextArea ta;
    private String regex;

    public TextRestriction(String matcher, TextField tf, int charSize) {
        this.tf = tf;
        this.regex = String.format("%s{0,%d}$", matcher, charSize);
    }
    
    public TextRestriction(String matcher, TextArea ta, int charSize) {
        this.ta = ta;
        this.regex = String.format("%s{0,%d}$", matcher, charSize);
    }
    
    @Override
    public void changed(ObservableValue ov, Object newVal, Object oldVal) {

        if (tf != null) {
            if (!tf.getText().matches(regex)) {
                if (tf.getText().length() > 0) {
                    tf.setText(tf.getText().substring(0, tf.getText().length() - 1));
                }
            }
        }
        else {
            if (!ta.getText().matches(regex)) {
                if (ta.getText().length() > 0) {
                    ta.setText(ta.getText().substring(0, ta.getText().length() - 1));
                }
            }
            MainController.getInstance().updateRemainingChars(200 - ta.getText().length());
        }
    }
}
