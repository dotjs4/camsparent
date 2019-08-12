package net.sajak.android.camera2basic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


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
    private RelativeLayout ll;
    private RelativeLayout.LayoutParams v1params, v2params;
    private int height;
    Uri imageUri, imageUri2;
    int k = 0;

    Matrix initialMatrix1, initialMatrix2;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPref = getSharedPreferences("name", Context.MODE_PRIVATE);
        setContentView(R.layout.edit_images);

        imageUri = Uri.parse(getIntent().getStringExtra("IMAGE_1"));
        imageUri2 = Uri.parse(getIntent().getStringExtra("IMAGE_2"));
        final Uri imageUri_full = Uri.parse(getIntent().getStringExtra("IMAGE_1_FULL"));

        height = getIntent().getIntExtra("HEIGHT", 200);
        ll = (RelativeLayout) findViewById(R.id.relativeLayout);
        ViewGroup.LayoutParams params = ll.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        params.height = height;
        ll.setLayoutParams(params);

        //the photo from the gallery
        view1 = (ImageView) findViewById(R.id.imageOne);

        //the taken photo
        view2 = (ImageView) findViewById(R.id.imageTwo);


        v1params =  new RelativeLayout.LayoutParams(view1.getLayoutParams());
        v2params = new RelativeLayout.LayoutParams(view2.getLayoutParams());
        v1params.setMargins(0,0,0,height / 2);
        v2params.setMargins(0,height / 2,0,0);
        view1.setLayoutParams(v1params);
        view2.setLayoutParams(v2params);

        view3 = (ImageView) findViewById(R.id.mergedImage);

        Button saveMergedImage = findViewById(R.id.saveMergedImage);
        Button setChangesBack = findViewById(R.id.setChangesBack);


        view1.setImageURI(imageUri);
        view2.setImageURI(imageUri2);

        int actWidth1 = view1.getDrawable().getIntrinsicWidth();
        int actHeight1 = view1.getDrawable().getIntrinsicHeight();
        int actWidth2 = view2.getDrawable().getIntrinsicWidth();
        int actHeight2 = view2.getDrawable().getIntrinsicHeight();


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inTempStorage = new byte[24*1024*1024];
        options.outWidth = width;
        options.outHeight = height;
        options.inJustDecodeBounds = false;
        options.inSampleSize=1;
        Bitmap bmp1=BitmapFactory.decodeFile(imageUri_full.getPath(),options);
        Bitmap b1;
        if ( actWidth1 > actHeight1) {
            b1 = ThumbnailUtils.extractThumbnail(bmp1, height, width);
        } else {
            b1 = ThumbnailUtils.extractThumbnail(bmp1, width, height);
        }
        view1.setImageBitmap(b1);
        if(bmp1!=null){
            bmp1.recycle();
        }
        bmp1=BitmapFactory.decodeFile(imageUri2.getPath(),options);
        Bitmap b2;
        if ( actWidth1 > actHeight1) {
            b2 = ThumbnailUtils.extractThumbnail(bmp1, height, width);
        } else {
            b2 = ThumbnailUtils.extractThumbnail(bmp1, width, height);
        }
        view2.setImageBitmap(b2);
        if(bmp1!=null){
            bmp1.recycle();
        }


        int w1 = view1.getDrawable().getIntrinsicWidth();
        int h1 = view1.getDrawable().getIntrinsicHeight();
        int w2 = view2.getDrawable().getIntrinsicWidth();
        int h2 = view2.getDrawable().getIntrinsicHeight();

        initialMatrix1 = new Matrix();
        initialMatrix2 = new Matrix();



        Log.d("TAGGA", "img size w " + w1);
        Log.d("TAGGA", "img size act " + actWidth1);
        Log.d("TAGGA", "device width " + width);

        Log.d("TAGGA", "img size h " + h1);
        Log.d("TAGGA", "img size act " + actHeight1);
        Log.d("TAGGA", "device height " + height);


        if (actWidth1 > actHeight1) {

            //initialMatrix1.setScale(width / height, width / height);

            initialMatrix1.postRotate(90);
            initialMatrix1.postScale(1.0f, (float) 1.0f);
            initialMatrix1.postTranslate(width, 0);
            //initialMatrix1.postTranslate(width, - height / 4);
            Log.d("TAGGA", "needed to rotate gallery photo");
        } else {
            initialMatrix2.postTranslate(0, - height / 2);
        }
        if (actWidth2 > actHeight2) {
            /*sx2 = (float) height / actWidth2;
            sy2 = (float) width / actHeight2;

            initialMatrix2.setScale(sx2, sy2);*/
            initialMatrix2.postRotate(90);
            initialMatrix2.postTranslate(width, - height / 2);
            Log.d("TAGGA", "needed to rotate taken photo");
        }


        view1.setImageMatrix(initialMatrix1);
        view2.setImageMatrix(initialMatrix2);

        matrix[0].set(initialMatrix1);
        matrix[1].set(initialMatrix2);


        points1[0] = 0f;
        points1[1] = 0f;
        points1[2] = width;
        points1[3] = 0f;
        points1[4] = width;
        points1[5] = height;
        points1[6] = 0f;
        points1[7] = height;

        for (int i = 0; i < points1.length; i++) {
            points1bak[i] = points1[i];
        }


        view1.setOnTouchListener(this);
        view2.setOnTouchListener(this);

        //TODO: passt erst nach verschieben beider Bilder
        BitmapDrawable drawable = (BitmapDrawable) view1.getDrawable();
        bmapImageOne = drawable.getBitmap();
        BitmapDrawable drawable2 = (BitmapDrawable) view2.getDrawable();
        bmapImageTwo = drawable2.getBitmap();


        saveMergedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bmapImageOne != null && bmapImageTwo != null) {
                    Bitmap mergedBitmap = createMergedImage();
                    if (mergedBitmap != null) {
                        view3.setImageBitmap(mergedBitmap);
                    }
                    else {
                        view3.setImageBitmap(bmapImageOne);
                    }
                }

            }
        });

        setChangesBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view1.setImageMatrix(initialMatrix1);
                view2.setImageMatrix(initialMatrix2);
            }
        });

        findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(i);
            }
        });
    }

    public Bitmap createMergedImage() {

        Bitmap result = Bitmap.createBitmap(bmapImageOne.getWidth(), bmapImageOne.getHeight(), bmapImageOne.getConfig());
        Canvas canvas = new Canvas(result);
        Bitmap croppedBitmapOne = Bitmap.createBitmap(bmapImageOne, 0,0,bmapImageOne.getWidth(), bmapImageOne.getHeight() / 2);
        Bitmap croppedBitmapTwo = Bitmap.createBitmap(bmapImageTwo, 0,0,bmapImageTwo.getWidth(), bmapImageTwo.getHeight() / 2);
        canvas.drawBitmap(croppedBitmapOne, 0f, 0, null);
        canvas.drawBitmap(croppedBitmapTwo, 0,  bmapImageOne.getHeight() / 2, null);

        saveImage(result);
        return result;
    }

    Matrix tmpM = new Matrix();

    public boolean pointIsOnImage(ImageView v, Matrix mat, float x, float y) {
        // Float array that will hold the mapped point (see 'mapPoints' below)
        float[] p1 = {0, 0};

        // Float array that holds the touch position
        final float[] p2 = {x, y};

        // Reset temporary matrix
        tmpM.reset();

        // Get the inverse matrix of the current transformation matrix and store it in the temporary matrix
        mat.invert(tmpM);

        // Map the touch position on the inverse matrix
        tmpM.mapPoints(p1, 0, p2, 0, 1);

        // Check if touch position is in the drawable bounds
        return v.getDrawable().getBounds().contains((int) p1[0], (int) p1[1]);
    }

    Matrix temp = new Matrix();
    Matrix bak = new Matrix();
    float[] points1 = new float[8];
    float[] points1bak = new float[8];

    public void tryTransform1(int k, float dx, float dy, float rot) {
        ImageView v;
        if (k == 0) v = this.view1; else v = this.view2;

        for (int i = 0; i < points1.length; i++) {
            points1[i] = points1bak[i];
        }

        matrix[k].mapPoints(points1);
        temp.set(matrix[k]);
        bak.set(matrix[k]);

        temp.postTranslate(dx, 0);
        if (pointIsOnImage(v, temp, 0, 0)) {
            matrix[k].postTranslate(dx, 0);
        } else {
            matrix[k].postTranslate(- points1[0], 0);
            Log.d("TAGGA", "x korrigiert " + points1[0]);
        }

        temp.set(bak);
        temp.postTranslate(0, dy);
        if (pointIsOnImage(v, temp, 0, 0)) {
            matrix[k].postTranslate(0, dy);
        } else {
            //Log.d("TAGGA", "y korrigiert " + points1[0] + " " + points1[1]);
            matrix[k].postTranslate(0, - points1[1]);
            /*Log.d("TAGGA", "rotation: " + rot);
            Log.d("TAGGA", "translate y: " + (- points1[0] * (float) Math.tan((float) Math.toRadians(rot + 90))));*/
            //matrix[k].postTranslate(0, + points1[0] * (float) Math.tan((float) Math.toRadians(rot + 90)));
        }
    }

    private void saveImage(Bitmap finalBitmap) {

        long currentTime = System.currentTimeMillis();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + currentTime + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean oneTime = true;
    int moveCounter = 0;
    boolean layoutChanged = false;
    float prevDx, prevDy;

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
                oneTime = true;
                layoutChanged = false;
                moveCounter = 0;
                //Log.d("TAGGA", "down called");
                return true;
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
                //Log.d("TAGGA", "up called");
                oneTime = false;
                if (!layoutChanged || moveCounter <= 10) {
                    if (k == 0) {
                        view2.setAlpha(1f);
                    } else {
                        view1.setAlpha(1f);
                    }
                    return true;
                }
                if (k == 0) {
                    view2.setAlpha(1f);
                    v1params =  new RelativeLayout.LayoutParams(view1.getLayoutParams());
                    v1params.setMargins(0,0,0,height / 2);
                    view1.setLayoutParams(v1params);
                } else {
                    view1.setAlpha(1f);
                    v2params =  new RelativeLayout.LayoutParams(view2.getLayoutParams());
                    v2params.setMargins(0,height / 2,0,0);
                    view2.setLayoutParams(v2params);
                    matrix[1].postTranslate(0, - height / 2);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                moveCounter++;
                //Log.d("TAGGA", "move called");
                if (oneTime && (mode == DRAG || mode == ZOOM) && moveCounter == 5) {
                    layoutChanged = true;
                    if (k == 0) {
                        view2.setAlpha(0.5f);
                        view2.bringToFront();
                        v1params.setMargins(0, 0, 0, 0);
                        view1.setLayoutParams(v1params);
                    } else {
                        view1.setAlpha(0.5f);
                        view1.bringToFront();
                        v2params.setMargins(0, 0, 0, 0);
                        view2.setLayoutParams(v2params);
                    }
                    oneTime = false;
                    return true;
                }

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
        if (oneTime) return true;

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
        //Log.d("TAGGA", "ROTATION: " + (float) Math.toDegrees(radians));
        return (float) Math.toDegrees(radians);
    }
}
