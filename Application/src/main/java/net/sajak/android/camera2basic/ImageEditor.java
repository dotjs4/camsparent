package net.sajak.android.camera2basic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import com.yalantis.ucrop.view.CropImageView;

import java.io.File;

import static java.security.AccessController.getContext;


//https://judepereira.com/blog/multi-touch-in-android-translate-scale-and-rotate/

public class ImageEditor extends AppCompatActivity implements View.OnTouchListener  {

    private ImageView imageView1;
    private final int CODE_IMG_GALLERY = 1;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";


    // these matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;
    private ImageView view, fin;
    private  Bitmap bmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPref = getSharedPreferences("name", Context.MODE_PRIVATE);
        setContentView(R.layout.edit_images);

        final Uri imageUri = Uri.parse(getIntent().getStringExtra("IMAGE_1"));
        view = (ImageView) findViewById(R.id.imageOne);
        view.setImageURI(imageUri);

        view.setOnTouchListener(this);
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
                imageView1.setImageURI(null);
                imageView1.setImageURI(imageUriResultCrop);

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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // handle touch events here

        view = (ImageView) v;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                savedMatrix.set(matrix);
                midPoint(mid, event);
                start.set(event.getX(), event.getY());
                mode = ZOOM;
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX(0) - start.x;
                    float dy = event.getY(0) - start.y;
                    matrix.postTranslate(dx, dy);
                } else
                if (mode == ZOOM) {
                    float newDist = spacing(event);

                    matrix.set(savedMatrix);
                    float scale = (newDist / oldDist);
                    matrix.postScale(scale, scale, mid.x, mid.y);

                    if (lastEvent != null && event.getPointerCount() == 2 || event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (view.getWidth() / 2) * sx;
                        float yc = (view.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);

                        float dx = event.getX(0) - start.x;
                        float dy = event.getY(0) - start.y;
                        matrix.postTranslate(dx, dy);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);

        bmap= Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmap);
        view.draw(canvas);

        //fin.setImageBitmap(bmap);
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        float s=x * x + y * y;
        return (float)Math.sqrt(s);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }
}
