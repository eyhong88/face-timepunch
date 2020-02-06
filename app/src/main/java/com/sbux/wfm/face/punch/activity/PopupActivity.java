package com.sbux.wfm.face.punch.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sbux.wfm.face.punch.R;

public class PopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popwindow);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = (int)(dm.widthPixels * .8);
        int height = (int)(dm.heightPixels * .6);
        getWindow().setLayout(width, height);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            String first = b.getString("firstName");
            String last = b.getString("lastName");
            String date = b.getString("punchDtm");

            ((TextView)findViewById(R.id.displayText)).setText(getString(R.string.registered, first, last, date));

            RelativeLayout layout = findViewById(R.id.popup);

            Button closeBtn = new Button(this);
            closeBtn.setText("Close");
            RelativeLayout.LayoutParams registerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            registerParams.setMargins(width/2 - 120, height-150, 0, 0);
            closeBtn.setLayoutParams(registerParams);
            layout.addView(closeBtn);

            closeBtn.setOnClickListener((v) -> this.finish());
        }
        else {
            ((TextView)findViewById(R.id.displayText)).setText(getString(R.string.unregistered));

            RelativeLayout layout = findViewById(R.id.popup);

            Button backBtn = new Button(this);
            backBtn.setText("Back");
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(190, height-150, 0, 0);
            backBtn.setLayoutParams(params);
            layout.addView(backBtn);
            backBtn.setOnClickListener((v) -> this.finish());

            Button registerBtn = new Button(this);
            registerBtn.setText("Register");
            RelativeLayout.LayoutParams registerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            registerParams.setMargins(width-400, height-150, 0, 0);
            registerBtn.setLayoutParams(registerParams);
            layout.addView(registerBtn);
            registerBtn.setOnClickListener((v) -> {
                startActivity(new Intent(this, RegistrationActivity.class));
                finish();
            });

        }

    }
}
