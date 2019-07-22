package com.example.wifi_configuration.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by rahul on 12/7/18.
 */

public class LightFontTextView extends TextView {

    public LightFontTextView(Context paramContext)
    {
        super(paramContext);
        init();
    }

    public LightFontTextView(Context paramContext, AttributeSet paramAttributeSet)
    {
        super(paramContext, paramAttributeSet);
        init();
    }

    public LightFontTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    public void init()
    {
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Helvetica-Light.ttf"), 1);
    }

}
