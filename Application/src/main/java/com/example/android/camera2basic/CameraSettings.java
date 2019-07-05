package com.example.android.camera2basic;

import android.content.Intent;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class CameraSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_settings);

        findViewById(R.id.goBackToCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        Switch flashSwitch = (Switch)  findViewById(R.id.flashSwitch);
        flashSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }

        });
    }

    private void buttonClick(int view) {
        switch(view) {
            case R.id.ratio43:
                break;
            case R.id.ratio169:
                break;
            case R.id.ratioImage:
                break;
        }
    }



    private void goBack() {
        Intent i = new Intent(this, CameraActivity.class);
        startActivity(i);
    }
}
