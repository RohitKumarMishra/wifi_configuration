package com.example.wifi_configuration.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by rahul on 11/03/18.
 */

public class CustomTextView extends TextView {

    public CustomTextView(Context paramContext)
    {
        super(paramContext);
        init();
    }

    public CustomTextView(Context paramContext, AttributeSet paramAttributeSet)
    {
        super(paramContext, paramAttributeSet);
        init();
    }

    public CustomTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    public void init()
    {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Helvetica-Bold.ttf"), 1);
    }

}
