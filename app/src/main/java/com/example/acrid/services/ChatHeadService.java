package com.example.acrid.services;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.acrid.R;
import com.example.acrid.View.Activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class ChatHeadService extends Service {
    private WindowManager windowManager;
    private List<ImageView> chatHeads = new ArrayList<>();
    private ImageView deleteArea;
    private WindowManager.LayoutParams deleteParams;
    private boolean isInsideDeleteArea = false;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        addDeleteArea();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            addChatHead();
        }
        return START_NOT_STICKY;
    }

    public void addChatHead() {
        if (chatHeads.size() >= 5) {
            return; // Giới hạn tối đa 5 chat head
        }

        ImageView chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.icons8_binance);

        WindowManager.LayoutParams chatParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        chatParams.gravity = Gravity.TOP | Gravity.START;
        chatParams.x = windowManager.getDefaultDisplay().getWidth() / 2;
        chatParams.y = 100 * chatHeads.size(); // Xếp dọc xuống

        windowManager.addView(chatHead, chatParams);
        chatHeads.add(chatHead);

        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isDragging = false;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = chatParams.x;
                        initialY = chatParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);

                        if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                            isDragging = true;
                        }

                        chatParams.x = initialX + deltaX;
                        chatParams.y = initialY + deltaY;
                        windowManager.updateViewLayout(chatHead, chatParams);

                        // Kiểm tra và di chuyển các chat head khác nếu cần
                        for (ImageView otherHead : chatHeads) {
                            if (otherHead != chatHead) {
                                WindowManager.LayoutParams otherParams = (WindowManager.LayoutParams) otherHead.getLayoutParams();
                                if (isNear(chatParams, otherParams)) {
                                    otherParams.x = chatParams.x + 100; // Dính vào bên phải
                                    otherParams.y = chatParams.y;
                                    windowManager.updateViewLayout(otherHead, otherParams);
                                }
                            }
                        }
                        showDeleteArea();

                        // Kiểm tra xem chatHead có vào vùng delete không
                        if (isInsideDeleteArea(chatParams)) {
                            isInsideDeleteArea = true;
                        } else {
                            isInsideDeleteArea = false;
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            Intent intent = new Intent(ChatHeadService.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        hideDeleteArea();

                        if (isInsideDeleteArea) {
                            removeChatHead(chatHead);
                        } else if (!isDragging) {
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

    /**
     * Kiểm tra nếu 2 chat head gần nhau thì dính vào nhau
     */
    private boolean isNear(WindowManager.LayoutParams p1, WindowManager.LayoutParams p2) {
        int distanceX = Math.abs(p1.x - p2.x);
        int distanceY = Math.abs(p1.y - p2.y);
        return distanceX < 150 && distanceY < 150; // Khoảng cách tối thiểu để dính vào nhau
    }
    private boolean isInsideDeleteArea(WindowManager.LayoutParams chatParams) {
        int chatCenterX = chatParams.x + chatParams.width / 2;
        int chatCenterY = chatParams.y + chatParams.height / 2;
        int deleteCenterX = deleteParams.x + deleteParams.width / 2;
        int deleteCenterY = deleteParams.y + deleteParams.height / 2;

        int distanceX = Math.abs(chatCenterX - deleteCenterX);
        int distanceY = Math.abs(chatCenterY - deleteCenterY);

        return distanceX < 100 && distanceY < 100; // Điều chỉnh khoảng cách phù hợp
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

    private void removeChatHead(ImageView chatHead) {
        windowManager.removeView(chatHead);
        chatHeads.remove(chatHead);
        if (chatHeads.isEmpty()) {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (ImageView chatHead : chatHeads) {
            windowManager.removeView(chatHead);
        }
        chatHeads.clear();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
