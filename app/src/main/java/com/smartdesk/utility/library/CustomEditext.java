package com.smartdesk.utility.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class CustomEditext extends TextInputEditText implements TextView.OnEditorActionListener {
    public CustomEditext(Context context) {
        super(context);
        this.setOnEditorActionListener(this);
    }

    public CustomEditext(Context context, AttributeSet attribute_set) {
        super(context, attribute_set);
    }

    public CustomEditext(Context context, AttributeSet attribute_set, int def_style_attribute) {
        super(context, attribute_set, def_style_attribute);
    }

    @Override
    public boolean onKeyPreIme(int key_code, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
            this.clearFocus();
        return super.onKeyPreIme(key_code, event);
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            this.clearFocus();
        }
        return false;
    }

    @Override
    public void onEditorAction(int actionCode) {
        super.onEditorAction(actionCode);
        if (actionCode == EditorInfo.IME_ACTION_DONE) {
            this.clearFocus();
        }
    }
}
