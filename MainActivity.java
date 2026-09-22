
package com.freeps3emulator;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Gravity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textView = new TextView(this);
        textView.setText("Free PS3 Emulator\nApp Started Successfully!");
        textView.setTextSize(22);
        textView.setGravity(Gravity.CENTER);
        
        setContentView(textView);
    }
}
