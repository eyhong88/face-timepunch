package com.sbux.wfm.face.punch.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.sbux.wfm.face.punch.R;
import com.sbux.wfm.face.punch.utils.DatabaseHelper;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class PunchActivity extends AppCompatActivity {
    private String partnerNbr;
    private String firstName;
    private String lastName;
    private DatabaseHelper db;
    Button startMeal;
    Button endMeal;
    Button endShift;
    Button clockIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeclock);

        startMeal = this.findViewById(R.id.startMeal);
        startMeal.setClickable(false);
        endMeal = this.findViewById(R.id.endMeal);
        endMeal.setClickable(false);
        endShift = this.findViewById(R.id.endShift);
        endShift.setClickable(false);
        clockIn = this.findViewById(R.id.startShift);

        db = new DatabaseHelper(this);

        //get the first/last/partner nbr
        Bundle b = getIntent().getExtras();

        if(null != b && !b.isEmpty()) {
            partnerNbr = b.getString("partnerNbr");
            firstName = b.getString("firstName");
            lastName = b.getString("lastName");
        }
        DateTimeFormatter format = DateTimeFormatter.ofPattern("ccc MM/dd/yyyy");
        ((TextView)this.findViewById(R.id.clockDate)).setText(LocalDate.now().format(format));
        ((TextView)this.findViewById(R.id.userNameText)).setText(getString(R.string.greetings, partnerNbr));

        if(createTable(partnerNbr)) {
            clockIn.setOnClickListener((v) -> {
                DateTimeFormatter dtFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm:ss a");
                String punchDtm = LocalDateTime.now(Clock.system(ZoneId.of("PST"))).format(dtFormat);
                System.out.println(firstName + " " + lastName + " has successfully punched at " + punchDtm);
                db.savePunch(partnerNbr, "PUNCH_IN", punchDtm, "test");

                Intent intent = new Intent(this, PopupActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("firstName", firstName);
                bundle.putString("lastName", lastName);
                bundle.putString("punchDtm", punchDtm);
                intent.putExtras(bundle);
                startActivity(intent);
                this.finish();
            });
        }
    }

    // This method needs to change to break out the db call, table creation, and the button disabling.
    private boolean createTable(String partnerNbr) {
        TableLayout table = findViewById(R.id.timeCardTableLayout);
        RelativeLayout layout = findViewById(R.id.timeCardLayout);
        String punchType = "";
        String punchDtm;
        Cursor punch_curs = db.getPunchesForPartner(partnerNbr);
        while(punch_curs.moveToNext()){

            punchType = punch_curs.getString(2);
            punchDtm = punch_curs.getString(3);

            TableRow row = new TableRow(this);
            TextView typeTv = new TextView(this);
            TextView punchTv = new TextView(this);
            typeTv.setTextColor(Color.BLACK);
            punchTv.setTextColor(Color.BLACK);
            typeTv.setTextSize(10);
            punchTv.setTextSize(10);
            typeTv.setText(punchType);
            punchTv.setText(punchDtm);
            row.addView(typeTv);
            row.addView(punchTv);
            table.addView(row);
        }

        if(punch_curs.moveToLast()){
            if(punchType.equalsIgnoreCase("punch_in")) {
                clockIn.setClickable(false);
                startMeal.setClickable(true);
                endMeal.setClickable(true);
                endShift.setClickable(true);

                startMeal.setBackgroundColor(Color.parseColor("#0da352"));
                endMeal.setBackgroundColor(Color.parseColor("#0da352"));
                endShift.setBackgroundColor(Color.parseColor("#0da352"));
                clockIn.setBackgroundColor(Color.parseColor("#545454"));

                return false;
            }
        }
        layout.removeAllViews();
        layout.addView(table);
        return true;
    }
}