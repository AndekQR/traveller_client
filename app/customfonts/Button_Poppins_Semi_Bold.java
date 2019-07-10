package com.client.traveller.ui.customfonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class Button_Poppins_Semi_Bold extends Button_Poppins_Regular {

    public Button_Poppins_Semi_Bold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Button_Poppins_Semi_Bold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Button_Poppins_Semi_Bold(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Poppins-SemiBold.ttf");
            setTypeface(tf);
        }
    }

}