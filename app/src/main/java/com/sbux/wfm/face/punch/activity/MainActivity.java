package com.sbux.wfm.face.punch.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.rest.ClientException;
import com.sbux.wfm.face.punch.R;
import com.sbux.wfm.face.punch.utils.DatabaseHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import static com.sbux.wfm.face.punch.utils.TimePunchConstants.HOST;
import static com.sbux.wfm.face.punch.utils.TimePunchConstants.SUB_KEY;

public class MainActivity extends AppCompatActivity {

    private final static int SNAP_PIC = 1;
    private ProgressDialog detectionProgressDialog;
    private ImageView imageView;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        setThreadPolicy();

        db = new DatabaseHelper(this);

        imageView = this.findViewById(R.id.starbucksLogoLogin);
        Button login = findViewById(R.id.loginButton);
        login.setOnClickListener((v) -> {
            /*"android.media.action.IMAGE_CAPTURE"*/
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, SNAP_PIC);
        });

        Button register = findViewById(R.id.registerButton);
        register.setOnClickListener((v) -> {
            Intent intent = new Intent(this, RegistrationActivity.class);
            this.startActivity(intent);
        });

        detectionProgressDialog = new ProgressDialog(this);
    }

    private void setThreadPolicy() {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SNAP_PIC && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            startActivity(detectAndIdentifyFace(photo));
        }
    }

    public Intent detectAndIdentifyFace(Bitmap face){
        FaceServiceClient faceServiceClient = new FaceServiceRestClient(HOST, SUB_KEY);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        face.compress(Bitmap.CompressFormat.JPEG, 100, os);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        try {
            Face[] faces = faceServiceClient.detect(is, true, false, null);
            if (faces.length > 0) {

                UUID faceToIdentifyId = faces[0].faceId;
                System.out.println("REAL faceToIdentify UUID: " + faceToIdentifyId.toString());

                UUID[] uuids = new UUID[1];
                uuids[0] = faceToIdentifyId;

                /// END TEST STUFF

                IdentifyResult[] identifyResults = faceServiceClient.identity("ssc-person-group", uuids, 1);

                if (identifyResults[0].candidates.size() > 0) {
                    String identifiedPersonId = identifyResults[0].candidates.get(0).personId.toString();
                    double confidence = identifyResults[0].candidates.get(0).confidence;
                    String tempFaceId = identifyResults[0].faceId.toString();

                    System.out.println("identifiedPersonId: " + identifiedPersonId);
                    System.out.println("confidence: " + confidence);
                    System.out.println("tempFaceId: " + tempFaceId);

                    if (confidence > 0.5) {
                        Cursor result = db.getPartnerByPersonId(identifiedPersonId);
                        while (result.moveToNext()) {
                            if (result.getCount() > 0) {
                                String firstName = result.getString(result.getColumnIndex("firstName"));
                                String lastName = result.getString(result.getColumnIndex("lastName"));
                                String partnerNbr = result.getString(result.getColumnIndex("partnerNbr"));

                                Intent intent = new Intent(MainActivity.this, PunchActivity.class);
                                Bundle b = new Bundle();
                                b.putString("firstName", firstName);
                                b.putString("lastName", lastName);
                                b.putString("partnerNbr", partnerNbr);
                                intent.putExtras(b);
                                System.out.println("Found you! Please choose your punch type.");
                                return intent;
                            }
                        }
                    }
                }
            }
        } catch (ClientException | IOException e) {
            e.printStackTrace();
        }

        System.out.println("You could not be identified in the system, please register.");
        return new Intent(MainActivity.this, PopupActivity.class);
    }
}
