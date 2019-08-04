package net.sajak.android.camera2basic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class ImageEditor extends AppCompatActivity {

    Bitmap imageOne;

    Bitmap imageTwo;

    View myview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPref = getSharedPreferences("name", Context.MODE_PRIVATE);
        setContentView(R.layout.edit_images);
    }

    /*public void onViewCreated(final View view, Bundle savedInstanceState) {

        myview = view;

        SeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    /*SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ImageView imageView = myview.findViewById(R.id.imageOverlay);
            if (imageView != null) {
                imageView.setImageAlpha(progress*255/100);
            }
            else {
                Log.e("test", "falsch");
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };*/
}
