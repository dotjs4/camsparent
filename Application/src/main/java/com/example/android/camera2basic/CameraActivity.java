/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2basic;

import android.animation.ObjectAnimator;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends AppCompatActivity {

    OrientationEventListener mOrientationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            boolean isHorizontal = false;
            boolean animStarted = false;
            boolean isUpsideDown = false;
            @Override
            public void onOrientationChanged(int orientation) {
                if ((orientation >= 240 && orientation <= 300) || (orientation >= 60 && orientation <= 120)) {
                    //Toast.makeText(getApplication(), "" + findViewById(R.id.imgSel).getRotation(), Toast.LENGTH_SHORT).show();
                    if (!isHorizontal && !animStarted) {
                        RotateAnimation rotateAnimation, rotateAnimation2;
                        if (orientation >= 60 && orientation <= 120) {
                            rotateAnimation = new RotateAnimation(0, -90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            rotateAnimation2 = new RotateAnimation(0, -90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            isUpsideDown = true;
                        } else {
                            rotateAnimation = new RotateAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            rotateAnimation2 = new RotateAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            isUpsideDown = false;
                        }
                        rotateAnimation.setDuration(500);
                        rotateAnimation.setFillAfter(true);
                        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                             @Override
                             public void onAnimationStart(Animation animation) {
                                animStarted = true;
                             }

                             @Override
                             public void onAnimationEnd(Animation animation) {
                                isHorizontal = true;
                                animStarted = false;
                             }

                             @Override
                             public void onAnimationRepeat(Animation animation) {

                             }
                            });

                            rotateAnimation2.setDuration(500);
                            rotateAnimation2.setFillAfter(true);
                            findViewById(R.id.imgSel).startAnimation(rotateAnimation);
                            findViewById(R.id.imgCap).startAnimation(rotateAnimation2);
                            findViewById(R.id.imgSet).startAnimation(rotateAnimation);
                    }
                } else if (orientation < 240 || orientation > 300) {
                    //Toast.makeText(getApplication(), "" + findViewById(R.id.imgSel).getRotation(), Toast.LENGTH_SHORT).show();
                    if (isHorizontal && !animStarted) {
                        RotateAnimation rotateAnimation, rotateAnimation2;

                        if (isUpsideDown) {
                            rotateAnimation = new RotateAnimation(-90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            rotateAnimation2 = new RotateAnimation(-90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        } else {
                            rotateAnimation = new RotateAnimation(90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            rotateAnimation2 = new RotateAnimation(90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        }

                        rotateAnimation.setDuration(500);
                        rotateAnimation.setFillAfter(true);
                        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                animStarted = true;
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                isHorizontal = false;
                                animStarted = false;
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        rotateAnimation2.setDuration(500);
                        rotateAnimation2.setFillAfter(true);
                        findViewById(R.id.imgSel).startAnimation(rotateAnimation);
                        findViewById(R.id.imgCap).startAnimation(rotateAnimation2);
                        findViewById(R.id.imgSet).startAnimation(rotateAnimation);
                    }
                }
            }
        };

        if (mOrientationListener.canDetectOrientation() == true) {
            mOrientationListener.enable();
        } else {
            mOrientationListener.disable();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOrientationListener.disable();
    }

}
