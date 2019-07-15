package com.example.android.camera2basic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.Iterator;
import java.util.Map;

public class CameraSettings extends AppCompatActivity {

    private double ratio = 0;
    private boolean flash = false;
    private boolean sound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final SharedPreferences sharedPref = getSharedPreferences("name", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_camera_settings);

        findViewById(R.id.goBackToCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sharedPref.getBoolean("sound", false) == ((Switch) findViewById(R.id.soundSwitch)).isChecked()) {
                    if (sharedPref.getBoolean("flash", false) == ((Switch) findViewById(R.id.flashSwitch)).isChecked()) {
                        goBack();
                        return;
                    }
                }


                new AlertDialog.Builder(CameraSettings.this)
                        .setTitle("Sicher?")
                        .setMessage("Bist du dir absolut sicher, dass du zurück zum Hausptbildschirm gehen möchtest, ohne zu speichern???")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                goBack();
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
        findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               SharedPreferences.Editor editor = sharedPref.edit();
               editor.putBoolean("flash", ((Switch) findViewById(R.id.flashSwitch)).isChecked());
               editor.putBoolean("sound", ((Switch) findViewById(R.id.soundSwitch)).isChecked());
               editor.commit();

               goBack();
           }
        });

        findViewById(R.id.ratio43).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(R.id.ratio43);
            }
        });
        findViewById(R.id.ratio169).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(R.id.ratio169);
            }
        });
        findViewById(R.id.ratioImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(R.id.ratioImage);
            }
        });
        //Log.d("TAGGA", String.valueOf(sharedPref.getBoolean("flash", false)));
        ((Switch) findViewById(R.id.soundSwitch)).setChecked(sharedPref.getBoolean("sound", false));
        ((Switch) findViewById(R.id.flashSwitch)).setChecked(sharedPref.getBoolean("flash", false));
    }

    private void buttonClick(int view) {
        switch(view) {
            case R.id.ratio43:
                ratio = 4 / 3;
                break;
            case R.id.ratio169:
                ratio = 16 / 9;
                break;
            case R.id.ratioImage:
                ratio = 1;
                break;
        }
    }



    private void goBack() {
        /*Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);*/

        Intent returnIntent = new Intent();
        returnIntent.putExtra("ratio", ratio);
        returnIntent.putExtra("sound", getPreferences(Context.MODE_PRIVATE).getBoolean("sound", false));
        returnIntent.putExtra("flash", getPreferences(Context.MODE_PRIVATE).getBoolean("flash", false));
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
