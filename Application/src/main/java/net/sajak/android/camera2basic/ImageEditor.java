package net.sajak.android.camera2basic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


//https://judepereira.com/blog/multi-touch-in-android-translate-scale-and-rotate/

public class ImageEditor extends AppCompatActivity implements View.OnTouchListener  {

    private ImageView imageView1;
    private ImageView imageView2;
    private final int CODE_IMG_GALLERY = 1;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";


    // these matrices will be used to move and zoom image
    private Matrix[] matrix = { new Matrix(), new Matrix()};
    private Matrix[] savedMatrix = {new Matrix(), new Matrix() };

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
    private ImageView view1;
    private ImageView view2;
    private ImageView view3;
    private  Bitmap bmapImageOne;
    private  Bitmap bmapImageTwo;

    int k = 0;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPref = getSharedPreferences("name", Context.MODE_PRIVATE);
        setContentView(R.layout.edit_images);

        final Uri imageUri = Uri.parse(getIntent().getStringExtra("IMAGE_1"));
        final Uri imageUri2 = Uri.parse(getIntent().getStringExtra("IMAGE_2"));
        final Uri imageUri_full = Uri.parse(getIntent().getStringExtra("IMAGE_1_FULL"));

        int height = getIntent().getIntExtra("HEIGHT", 200);
        final LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        ViewGroup.LayoutParams params = ll.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        params.height = height;
        ll.setLayoutParams(params);

        //the photo from the gallery
        view1 = (ImageView) findViewById(R.id.imageOne);

        //the taken photo
        view2 = (ImageView) findViewById(R.id.imageTwo);

        view3 = (ImageView) findViewById(R.id.mergedImage);

        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        view1.setImageURI(imageUri);
        view2.setImageURI(imageUri2);

        createMergedImage();


        Matrix initialMatrix1 = new Matrix();
        Matrix initialMatrix2 = new Matrix();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        int actWidth1 = view1.getDrawable().getIntrinsicWidth();
        int actHeight1 = view1.getDrawable().getIntrinsicHeight();
        int actWidth2 = view2.getDrawable().getIntrinsicWidth();
        int actHeight2 = view2.getDrawable().getIntrinsicHeight();

        float sx1 = (float) width / actWidth1;
        float sy1 = (float) height / actHeight1;
        float sx2 = (float) width / actWidth2;
        float sy2 = (float) height / actHeight2;


        initialMatrix1.setScale(sx1, sy1);
        initialMatrix2.setScale(sx2, sy2);
        initialMatrix2.postTranslate(0, - height / 2);


        if (actWidth1 > actHeight1) {
            sx1 = (float) height / actWidth1;
            sy1 = (float) width / actHeight1;

            initialMatrix1.setScale(sx1, sy1);
            initialMatrix1.postRotate(90);
            initialMatrix1.postTranslate(width, 0);
            Log.d("TAGGA", "needed to rotate gallery photo");
        }
        if (actWidth2 > actHeight2) {
            sx2 = (float) height / actWidth2;
            sy2 = (float) width / actHeight2;

            initialMatrix2.setScale(sx2, sy2);
            initialMatrix2.postRotate(90);
            initialMatrix2.postTranslate(width, - height / 2);
            Log.d("TAGGA", "needed to rotate taken photo");
        }



        view1.setImageMatrix(initialMatrix1);
        view2.setImageMatrix(initialMatrix2);

        matrix[0].set(initialMatrix1);
        matrix[1].set(initialMatrix2);


        view1.setOnTouchListener(this);
        view2.setOnTouchListener(this);
    }

    public void createMergedImage() {
        BitmapDrawable drawable = (BitmapDrawable) view1.getDrawable();
        Bitmap bitmapOne = drawable.getBitmap();
        Matrix matrix = view1.getImageMatrix();
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmapOne, 0,  0,bitmapOne.getWidth(), bitmapOne.getHeight(), matrix, true);
        drawable = (BitmapDrawable) view2.getDrawable();
        Bitmap bitmapTwo = drawable.getBitmap();

        Bitmap result = Bitmap.createBitmap(bitmapOne.getWidth(), bitmapOne.getHeight(), bitmapOne.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bitmapOne, 0f, 0f, null);
        canvas.drawBitmap(bitmapTwo, 0,  bitmapOne.getHeight() / 2, null);


    }

    // Gets an Images Orientation
    public static int getOrientationEXIF(Context context, Uri uri) {

        int orientation = 0;

        try {

            ExifInterface exif = new ExifInterface(uri.getPath());

            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;
                    return orientation;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;
                    return orientation;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // handle touch events here

        if (v.getId() == R.id.imageOne)
        {
            view1 = (ImageView) v;
            k = 0;
        }
        else if(v.getId() == R.id.imageTwo) {
            view2 = (ImageView) v;
            k = 1;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix[k].set(matrix[k]);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                savedMatrix[k].set(matrix[k]);
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
                    matrix[k].set(savedMatrix[k]);
                    float dx = event.getX(0) - start.x;
                    float dy = event.getY(0) - start.y;
                    matrix[k].postTranslate(dx, dy);
                } else
                if (mode == ZOOM) {
                    float newDist = spacing(event);

                    matrix[k].set(savedMatrix[k]);
                    float scale = (newDist / oldDist);
                    matrix[k].postScale(scale, scale, mid.x, mid.y);

                    if (lastEvent != null && event.getPointerCount() == 2 || event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix[k].getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (view1.getWidth() / 2) * sx;
                        float yc = (view1.getHeight() / 2) * sx;
                        matrix[k].postRotate(r, tx + xc, ty + yc);

                        float dx = event.getX(0) - start.x;
                        float dy = event.getY(0) - start.y;
                        matrix[k].postTranslate(dx, dy);
                    }
                }
                break;
        }


        if (v.getId() == R.id.imageOne)
        {
            view1.setImageMatrix(matrix[k]);
            bmapImageOne = Bitmap.createBitmap(view1.getWidth(), view1.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bmapImageOne);
            view1.draw(canvas);
        }
        else if(v.getId() == R.id.imageTwo) {
            view2.setImageMatrix(matrix[k]);
            bmapImageTwo = Bitmap.createBitmap(view2.getWidth(), view2.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bmapImageTwo);
            view2.draw(canvas);
        }

        if (bmapImageOne != null && bmapImageTwo != null) {
            view3.setImageBitmap(bmapImageOne);
        }


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
