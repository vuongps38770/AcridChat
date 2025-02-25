package com.example.acrid.services;


import static com.example.acrid.Helper.FireBaseService.CHANNEL_ID;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.core.app.NotificationCompat;

import com.example.acrid.R;
import com.example.acrid.View.Activity.MainActivity;

public class ChatHeadService extends Service {
    private WindowManager windowManager;
    private ImageView chatHead, deleteArea;
    private WindowManager.LayoutParams chatParams, deleteParams;
    private boolean isInsideDeleteArea = false;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        addChatHead();
        addDeleteArea();
    }

    private void addChatHead() {
        chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.icons8_binance);

        chatParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        chatParams.gravity = Gravity.TOP | Gravity.START;
        chatParams.x = windowManager.getDefaultDisplay().getWidth() - chatHead.getWidth();
        chatParams.y = 100;

        windowManager.addView(chatHead, chatParams);

        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isClick = true;
            private static final int CLICK_THRESHOLD = 10;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = chatParams.x;
                        initialY = chatParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        showDeleteArea();
                        isClick=true;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(deltaX) > CLICK_THRESHOLD || Math.abs(deltaY) > CLICK_THRESHOLD) {
                            isClick = false;
                        }
                        chatParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        chatParams.y = initialY + (int) (event.getRawY() - initialTouchY);

                        windowManager.updateViewLayout(chatHead, chatParams);


                        int[] deleteLocation = new int[2];
                        deleteArea.getLocationOnScreen(deleteLocation);
                        int deleteCenterY = deleteLocation[1] + deleteArea.getHeight() / 2;

                        if (chatParams.y + chatHead.getHeight() > deleteCenterY) {
                            isInsideDeleteArea = true;
                            deleteArea.setImageResource(R.drawable.chat);
                        } else {
                            isInsideDeleteArea = false;
                            deleteArea.setImageResource(R.drawable.eye_svgrepo_com);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        hideDeleteArea();

                        if (isInsideDeleteArea) {
                            stopSelf();
                        } else {
                            int screenWidth = windowManager.getDefaultDisplay().getWidth();
                            int centerX = chatParams.x + (chatHead.getWidth() / 2);

                            int targetX = (centerX < screenWidth / 2) ? 0 : screenWidth - chatHead.getWidth();


                            ValueAnimator animator = ValueAnimator.ofInt(chatParams.x, targetX);
                            animator.setDuration(300);
                            animator.addUpdateListener(animation -> {
                                chatParams.x = (int) animation.getAnimatedValue();
                                windowManager.updateViewLayout(chatHead, chatParams);
                            });
                            animator.start();

                            windowManager.updateViewLayout(chatHead, chatParams);
                        }
                        if (isClick) {
                            Intent intent = new Intent(ChatHeadService.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void addDeleteArea() {
        deleteArea = new ImageView(this);
        deleteArea.setImageResource(R.drawable.eye_svgrepo_com);

        deleteParams = new WindowManager.LayoutParams(
                200, 200,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        deleteParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        deleteParams.y = 100;

        windowManager.addView(deleteArea, deleteParams);
        deleteArea.setVisibility(View.GONE);
    }

    private void showDeleteArea() {
        deleteArea.setVisibility(View.VISIBLE);
    }

    private void hideDeleteArea() {
        deleteArea.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null) windowManager.removeView(chatHead);
        if (deleteArea != null) windowManager.removeView(deleteArea);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
