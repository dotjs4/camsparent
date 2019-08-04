package net.sajak.android.camera2basic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.view.CropImageView;

import java.io.File;

import static java.security.AccessController.getContext;


public class ImageEditor extends AppCompatActivity {

    private ImageView image;
    private final int CODE_IMG_GALLERY = 1;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPref = getSharedPreferences("name", Context.MODE_PRIVATE);
        setContentView(R.layout.edit_images);

        init();

        final Uri imageUri = Uri.parse(getIntent().getStringExtra("IMAGE_1"));

        image.setImageURI(imageUri);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivityForResult(new Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*"), CODE_IMG_GALLERY);
                StartCrop(imageUri);
            }
        });
    }

    private void init() {
        this.image = findViewById(R.id.imageOne);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_IMG_GALLERY && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            if (imageUri != null) {
                StartCrop(imageUri);
            }

        }
        else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri imageUriResultCrop = UCrop.getOutput(data);

            new AlertDialog.Builder(ImageEditor.this)
                    .setTitle("Sicher?")
                    .setMessage(String.valueOf(imageUriResultCrop))

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();


            if (imageUriResultCrop != null) {
                image.setImageURI(null);
                image.setImageURI(imageUriResultCrop);

            }
        }
    }

    private void StartCrop(@NonNull Uri uri) {
        String destinationFileName = SAMPLE_CROPPED_IMG_NAME;
        destinationFileName += ".jpg";

        UCrop ucrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));

        ucrop.useSourceImageAspectRatio();

        ucrop.withOptions(getCropOptions());

        ucrop.start(this);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(100);

        //options.setCompressionFormat(Bitmap.CompressFormat.PNG);

        //UI
        options.setHideBottomControls(true);
        options.setFreeStyleCropEnabled(false);
        options.setAllowedGestures(UCropActivity.ALL, UCropActivity.ALL, UCropActivity.ALL);
        //Colors
        options.setStatusBarColor(getResources().getColor(R.color.menuColor));
        options.setToolbarColor(getResources().getColor(R.color.menuColor));

        return options;
    }
}
