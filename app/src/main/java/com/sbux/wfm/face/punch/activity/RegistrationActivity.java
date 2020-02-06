package com.sbux.wfm.face.punch.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.rest.ClientException;
import com.sbux.wfm.face.punch.R;
import com.sbux.wfm.face.punch.utils.DatabaseHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.sbux.wfm.face.punch.utils.TimePunchConstants.GROUP_NAME;
import static com.sbux.wfm.face.punch.utils.TimePunchConstants.HOST;
import static com.sbux.wfm.face.punch.utils.TimePunchConstants.SUB_KEY;

public class RegistrationActivity extends AppCompatActivity {
    private final static int REGISTRATION = 2;
    private String partnerNbr;
    private String firstName;
    private String lastName;
    private boolean isPictureTaken = false;
    private DatabaseHelper db;
    private Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_main);

        Button cancel = this.findViewById(R.id.registerCancelBtn);
        cancel.setOnClickListener((v) -> this.finish());

        db = new DatabaseHelper(this);
        this.findViewById(R.id.partnerNbrTextBox).requestFocus();

        this.findViewById(R.id.registerCameraButton).setOnClickListener((v) -> {
            Intent intent = new Intent(/*"android.media.action.IMAGE_CAPTURE"*/android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 3);
        });

        Button submit = this.findViewById(R.id.registerSubmitButton);
        submit.setOnClickListener((v) -> {
            if(isPictureTaken) {
                firstName = ((TextView)this.findViewById(R.id.firstNameTextBox)).getText().toString();
                lastName = ((TextView)this.findViewById(R.id.lastNameTextBox)).getText().toString();
                partnerNbr = ((TextView)this.findViewById(R.id.partnerNbrTextBox)).getText().toString();

                System.out.println("First Name: " + firstName);
                System.out.println("Last Name: " + lastName);
                System.out.println("Partner Number: " + partnerNbr);

                createPartner(photo);

                Intent intent = new Intent(this, MainActivity.class);
                startActivityForResult(intent, REGISTRATION);
            }
            else {
                ((TextView)this.findViewById(R.id.photoRequiredText)).setError("Please attach a photo before continuing.");
            }
        });
    }

    private void createPartner(Bitmap photo) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, os);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        FaceServiceClient client = new FaceServiceRestClient(HOST, SUB_KEY);
        try {
            // Create the Partner
            CreatePersonResult result = client.createPerson(GROUP_NAME, partnerNbr, firstName + " " + lastName);
            String personId = result.personId.toString();

            // Add the face to the Partner
            client.addPersonFace(GROUP_NAME, result.personId, is, null, null);
            //Train the group, to memorize the face
            client.trainPersonGroup(GROUP_NAME);

            db.saveUserProfile(partnerNbr, firstName, lastName, personId);

        } catch (ClientException | IOException e) {
            e.printStackTrace();
            System.out.println("Could not persist profile. Please try again later.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");

            ((ImageButton)this.findViewById(R.id.registerCameraButton)).setImageBitmap(photo);
            isPictureTaken = true;
        }
    }
}
