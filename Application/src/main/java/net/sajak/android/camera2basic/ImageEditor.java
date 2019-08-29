package net.sajak.android.camera2basic;

import android.app.Activity;
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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
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
    private Matrix[] matrix = { new Matrix(), new Matrix(), new Matrix(), new Matrix()};
    private Matrix[] savedMatrix = {new Matrix(), new Matrix(), new Matrix(), new Matrix() };

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
    private ImageView view4;
    private ImageView mergedView;
    private  Bitmap bmapImageOne;
    private  Bitmap bmapImageTwo;
    private  Bitmap bmapImageThree;
    private  Bitmap bmapImageFour;
    private RelativeLayout ll;
    private RelativeLayout.LayoutParams v1params, v2params;
    private LinearLayout.LayoutParams v3params, v4params;
    private int height, width;
    private boolean isHorizontalCrop = true;
    Uri imageUri, imageUri2;
    int k = 0;
    boolean saved = false;
    boolean switched = false;

    Matrix initialMatrix1, initialMatrix2, initialMatrix3, initialMatrix4;
    Matrix defaultMatrix1, defaultMatrix2, defaultMatrix3, defaultMatrix4;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPref = getSharedPreferences("name", Context.MODE_PRIVATE);
        setContentView(R.layout.edit_images);

        changeCropOrientation();

        imageUri = Uri.parse(getIntent().getStringExtra("IMAGE_1"));
        imageUri2 = Uri.parse(getIntent().getStringExtra("IMAGE_2"));
        final Uri imageUri_full = Uri.parse(getIntent().getStringExtra("IMAGE_1_FULL"));

        height = getIntent().getIntExtra("HEIGHT", 200);
        width = getIntent().getIntExtra("WIDTH", 200);
        ll = (RelativeLayout) findViewById(R.id.relativeLayout);
        ViewGroup.LayoutParams params = ll.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        params.height = height;
        ll.setLayoutParams(params);

        //the photo from the gallery
        view1 = (ImageView) findViewById(R.id.imageOne);
        view3 = (ImageView) findViewById(R.id.imageThree);

        //the taken photo
        view2 = (ImageView) findViewById(R.id.imageTwo);
        view4 = (ImageView) findViewById(R.id.imageFour);

        view1.setImageURI(imageUri);
        view2.setImageURI(imageUri2);
        view3.setImageURI(imageUri);
        view4.setImageURI(imageUri2);

        v1params =  new RelativeLayout.LayoutParams(view1.getLayoutParams().height, view1.getLayoutParams().width);
        v2params = new RelativeLayout.LayoutParams(view2.getLayoutParams().height, view2.getLayoutParams().width);
        view3.getLayoutParams().height = height;
        view4.getLayoutParams().height = height;
        v1params.setMargins(0,0,0,height / 2);
        v2params.setMargins(0,height / 2,0,0);
        view1.setLayoutParams(v1params);
        view2.setLayoutParams(v2params);

        mergedView = (ImageView) findViewById(R.id.mergedImage);

        final Button saveMergedImage = findViewById(R.id.saveMergedImage);
        ImageButton toggleCropOrientation = findViewById(R.id.toggleCropOrientation);
        ImageButton switchImages = findViewById(R.id.switchImages);
        Button setChangesBack = findViewById(R.id.setChangesBack);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;


        int actWidth1 = view1.getDrawable().getIntrinsicWidth();
        int actHeight1 = view1.getDrawable().getIntrinsicHeight();
        int actWidth2 = view2.getDrawable().getIntrinsicWidth();
        int actHeight2 = view2.getDrawable().getIntrinsicHeight();
        int actWidth3 = view3.getDrawable().getIntrinsicWidth();
        int actHeight3 = view3.getDrawable().getIntrinsicHeight();
        int actWidth4 = view4.getDrawable().getIntrinsicWidth();
        int actHeight4 = view4.getDrawable().getIntrinsicHeight();

        float sx1, sy1, sx2, sy2, sx3, sy3, sx4, sy4;

        if (actWidth1 > actHeight1) {
            //pic1 is landscape
            sx1 = (float) width / actHeight1;
            sy1 = (float) height / actWidth1;
        } else {
            //pic1 is portrait
            sx1 = (float) width / actWidth1;
            sy1 = (float) height / actHeight1;
        }
        if (actWidth2 > actHeight2) {
            //pic2 is landscape
            sx2 = (float) width / actHeight2;
            sy2 = (float) height / actWidth2;
        } else {
            //pic2 is portrait
            sx2 = (float) width / actWidth2;
            sy2 = (float) height / actHeight2;
        }
        if (actWidth3 > actHeight3) {
            //pic2 is landscape
            sx3 = (float) width / actHeight3;
            sy3 = (float) height / actWidth3;
        } else {
            //pic2 is portrait
            sx3 = (float) width / actWidth3;
            sy3 = (float) height / actHeight3;
        }
        if (actWidth4 > actHeight4) {
            //pic1 is landscape
            sx4 = (float) width / actHeight4;
            sy4 = (float) height / actWidth4;
        } else {
            //pic1 is portrait
            sx4 = (float) width / actWidth4;
            sy4 = (float) height / actHeight4;
        }

        initialMatrix1 = new Matrix();
        initialMatrix2 = new Matrix();
        initialMatrix3 = new Matrix();
        initialMatrix4 = new Matrix();

        initialMatrix1.setScale(sx1, sy1);
        initialMatrix2.setScale(sx2, sy2);
        initialMatrix3.setScale(sx3, sy3);
        initialMatrix4.setScale(sx4, sy4);


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inTempStorage = new byte[24*1024*1024];
        options.outWidth = width;
        options.outHeight = height;
        options.inJustDecodeBounds = false;
        options.inSampleSize=1;


        if (actWidth1 > actHeight1) {
            initialMatrix1.postRotate(90);
            initialMatrix1.postTranslate(width, 0);
            Log.d("TAGGA", "needed to rotate gallery photo");
        } else {
            //initialMatrix2.postTranslate(0, - height / 2);
            Log.d("TAGGA", "did not rotate gallery photo");
        }
        if (actWidth2 > actHeight2) {
            initialMatrix2.postRotate(90);
            initialMatrix2.postTranslate(width, - height / 2);
            Log.d("TAGGA", "needed to rotate taken photo");
        } else {
            initialMatrix2.postTranslate(0, - height / 2);
        }
        if (actWidth3 > actHeight3) {
            initialMatrix3.postRotate(90);
            initialMatrix3.postTranslate(width, 0);
            Log.d("TAGGA", "needed to rotate taken photo");
        }
        if (actWidth4 > actHeight4) {
            initialMatrix4.postRotate(90);
            initialMatrix4.postTranslate(width/2, 0);
            Log.d("TAGGA", "needed to rotate taken photo");
        } else {
            initialMatrix4.postTranslate(- width /2, 0);
        }

        defaultMatrix1 = initialMatrix1;
        defaultMatrix2 = initialMatrix2;
        defaultMatrix3 = initialMatrix3;
        defaultMatrix4 = initialMatrix4;

        view1.setImageMatrix(initialMatrix1);
        view2.setImageMatrix(initialMatrix2);
        view3.setImageMatrix(initialMatrix3);
        view4.setImageMatrix(initialMatrix4);

        matrix[0].set(initialMatrix1);
        matrix[1].set(initialMatrix2);
        matrix[2].set(initialMatrix3);
        matrix[3].set(initialMatrix4);


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
        view3.setOnTouchListener(this);
        view4.setOnTouchListener(this);

        BitmapDrawable drawable = (BitmapDrawable) view1.getDrawable();
        bmapImageOne = drawable.getBitmap();
        BitmapDrawable drawable2 = (BitmapDrawable) view2.getDrawable();
        bmapImageTwo = drawable2.getBitmap();
        BitmapDrawable drawable3 = (BitmapDrawable) view3.getDrawable();
        bmapImageThree = drawable3.getBitmap();
        BitmapDrawable drawable4 = (BitmapDrawable) view4.getDrawable();
        bmapImageFour = drawable4.getBitmap();


        view1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bmapImageOne = Bitmap.createBitmap(view1.getWidth(), view1.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bmapImageOne);
                view1.draw(canvas);
            }
        });
        view2.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bmapImageTwo = Bitmap.createBitmap(view2.getWidth(), view2.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bmapImageTwo);
                view2.draw(canvas);
            }
        });
        view3.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bmapImageThree = Bitmap.createBitmap(view3.getWidth(), view3.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bmapImageThree);
                view3.draw(canvas);
            }
        });
        view4.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bmapImageFour = Bitmap.createBitmap(view4.getWidth(), view4.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bmapImageFour);
                view4.draw(canvas);
            }
        });


        saveMergedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHorizontalCrop) {
                    if (bmapImageOne != null && bmapImageTwo != null) {
                        Bitmap mergedBitmap = createMergedImage();
                        if (mergedBitmap != null) {
                            //mergedView.setImageBitmap(mergedBitmap);
                            saveMergedImage.setText("SAVED");
                            saveMergedImage.setBackgroundColor(0x77ffd802);
                            saved = true;
                        }
                        else {
                            //mergedView.setImageBitmap(bmapImageOne);
                        }
                        Log.d("TAGGA", "saved image");
                    } else {
                        Log.d("TAGGA", "herehin");
                    }
                }
                else {
                    if (bmapImageThree != null && bmapImageFour != null) {
                        Bitmap mergedBitmap = createMergedImage();
                        if (mergedBitmap != null) {
                            //mergedView.setImageBitmap(mergedBitmap);
                            saveMergedImage.setText("SAVED");
                            saveMergedImage.setBackgroundColor(0x77ffd802);
                            saved = true;
                        }
                        else {
                            //mergedView.setImageBitmap(bmapImageOne);
                        }
                        Log.d("TAGGA", "saved image");
                    } else {
                        Log.d("TAGGA", "herehin");
                    }
                }


            }
        });

        toggleCropOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isHorizontalCrop = !isHorizontalCrop;

                ImageButton button = (ImageButton) view.findViewById(R.id.toggleCropOrientation);
                if (isHorizontalCrop) {
                    button.setBackgroundResource(R.drawable.crophorizontal);
                }
                else {
                    button.setBackgroundResource(R.drawable.cropvertical);
                }


                changeCropOrientation();
            }
        });

        switchImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("switch", "switch images");


                if (isHorizontalCrop) {
                    defaultMatrix1.postTranslate(0, -height/2);
                    defaultMatrix2.postTranslate(0, height/2);

                    Matrix tempDefaultMatrix = defaultMatrix1;
                    defaultMatrix1 = defaultMatrix2;
                    defaultMatrix2 = tempDefaultMatrix;

                    matrix[0].set(defaultMatrix1);
                    matrix[1].set(defaultMatrix2);

                    view1.setImageMatrix(matrix[0]);
                    view2.setImageMatrix(matrix[1]);

                    if (!switched) {
                        view1.setImageURI(imageUri2);
                        view2.setImageURI(imageUri);
                        switched = true;
                    }
                    else {
                        view1.setImageURI(imageUri);
                        view2.setImageURI(imageUri2);
                        switched = false;
                    }
                }
                else {
                    defaultMatrix3.postTranslate(-width/2, 0);
                    defaultMatrix4.postTranslate(width/2, 0);

                    Matrix tempDefaultMatrix = defaultMatrix3;
                    defaultMatrix3 = defaultMatrix4;
                    defaultMatrix4 = tempDefaultMatrix;

                    matrix[2].set(defaultMatrix3);
                    matrix[3].set(defaultMatrix4);

                    view3.setImageMatrix(matrix[2]);
                    view4.setImageMatrix(matrix[3]);

                    if (!switched) {
                        view3.setImageURI(imageUri2);
                        view4.setImageURI(imageUri);
                        switched = true;
                    }
                    else {
                        view3.setImageURI(imageUri);
                        view4.setImageURI(imageUri2);
                        switched = false;
                    }
                }

            }
        });

        setChangesBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHorizontalCrop) {
                    matrix[0].set(defaultMatrix1);
                    matrix[1].set(defaultMatrix2);
                    view1.setImageMatrix(defaultMatrix1);
                    view2.setImageMatrix(defaultMatrix2);
                    bmapImageOne = Bitmap.createBitmap(view1.getWidth(), view1.getHeight(), Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(bmapImageOne);
                    view1.draw(canvas);

                    bmapImageTwo = Bitmap.createBitmap(view2.getWidth(), view2.getHeight(), Bitmap.Config.RGB_565);
                    canvas = new Canvas(bmapImageTwo);
                    view2.draw(canvas);
                }
                else {
                    matrix[2].set(defaultMatrix3);
                    matrix[3].set(defaultMatrix4);
                    view3.setImageMatrix(defaultMatrix3);
                    view4.setImageMatrix(defaultMatrix4);
                    bmapImageThree = Bitmap.createBitmap(view3.getWidth(), view3.getHeight(), Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(bmapImageThree);
                    view3.draw(canvas);

                    bmapImageFour = Bitmap.createBitmap(view4.getWidth(), view4.getHeight(), Bitmap.Config.RGB_565);
                    canvas = new Canvas(bmapImageFour);
                    view4.draw(canvas);
                }


            }
        });

        findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                //startActivity(i);
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    private void changeCropOrientation() {
        if (isHorizontalCrop) {
            findViewById(R.id.relativeLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.verticalLayout).setVisibility(View.INVISIBLE);
        }
        else {
            findViewById(R.id.relativeLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.verticalLayout).setVisibility(View.VISIBLE);
        }
    }

    public Bitmap createMergedImage() {
        if (isHorizontalCrop) {
            Bitmap result = Bitmap.createBitmap(bmapImageOne.getWidth(), bmapImageOne.getHeight() * 2, bmapImageOne.getConfig());
            Canvas canvas = new Canvas(result);
            Bitmap croppedBitmapOne = Bitmap.createBitmap(bmapImageOne, 0,0,bmapImageOne.getWidth(), bmapImageOne.getHeight());
            Bitmap croppedBitmapTwo = Bitmap.createBitmap(bmapImageTwo, 0,0,bmapImageTwo.getWidth(), bmapImageTwo.getHeight());
            canvas.drawBitmap(croppedBitmapOne, 0f, 0, null);
            canvas.drawBitmap(croppedBitmapTwo, 0,  bmapImageOne.getHeight(), null);

            saveImage(result);
            return result;
        }
        else {
            Bitmap result = Bitmap.createBitmap(bmapImageThree.getWidth() * 2, bmapImageThree.getHeight(), bmapImageThree.getConfig());
            Canvas canvas = new Canvas(result);
            Bitmap croppedBitmapOne = Bitmap.createBitmap(bmapImageThree, 0,0,bmapImageThree.getWidth(), bmapImageThree.getHeight());
            Bitmap croppedBitmapTwo = Bitmap.createBitmap(bmapImageFour, 0,0,bmapImageFour.getWidth(), bmapImageFour.getHeight());
            canvas.drawBitmap(croppedBitmapOne, 0f, 0, null);
            canvas.drawBitmap(croppedBitmapTwo, bmapImageThree.getWidth(), 0,  null);

            saveImage(result);
            return result;
        }

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
        if (k == 0) {
            v = this.view1;
        }
        else if (k == 1) {
            v = this.view2;
        }
        else if (k == 2) {
            v = this.view3;
        }
        else {
            v = this.view4;
        }

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

        if (saved) {
            Button saveBtn = findViewById(R.id.saveMergedImage);
            saveBtn.setText("SAVE");
            saveBtn.setBackgroundColor(0xFFffd802);
        }

        if (v.getId() == R.id.imageOne)
        {
            view1 = (ImageView) v;
            k = 0;
        }
        else if(v.getId() == R.id.imageTwo) {
            view2 = (ImageView) v;
            k = 1;
        }
        else if(v.getId() == R.id.imageThree) {
            view3 = (ImageView) v;
            k = 2;
        }
        else if(v.getId() == R.id.imageFour) {
            view4 = (ImageView) v;
            k = 3;
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
                    return true;
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
        else if(v.getId() == R.id.imageThree) {
            view3.setImageMatrix(matrix[k]);
            bmapImageThree = Bitmap.createBitmap(view3.getWidth(), view3.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bmapImageThree);
            view3.draw(canvas);
        }
        else if(v.getId() == R.id.imageFour) {
            view4.setImageMatrix(matrix[k]);
            bmapImageFour = Bitmap.createBitmap(view4.getWidth(), view4.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bmapImageFour);
            view4.draw(canvas);
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
