package com.example.acrid.View.Activity;

import static android.provider.UserDictionary.Words.APP_ID;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.acrid.BuildConfig;
import com.example.acrid.Constant.Const;
import com.example.acrid.R;

import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class CallActivity extends AppCompatActivity {
    private final String APP_KEY= BuildConfig.APPLICATION_ID;
    private RtcEngine mRtcEngine;
    private String channelId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_call);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        channelId = getIntent().getStringExtra(Const.CHANNEL_NAME);
        if(checkPermissions()){
            initializeAgoraVoiceSDK();
            joinChannel();
        }else {
            requestPermissions();
        }
    }

    // Khởi tạo Agora SDK
    private void initializeAgoraVoiceSDK() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = APP_KEY;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi khởi tạo RTC engine: " + e.getMessage());
        }
    }

    // Hàm tham gia kênh
    private void joinChannel() {
        if (mRtcEngine != null && channelId != null) {
            String token = null; // Nếu không dùng token, để null
            int uid = 0; // UID = 0 để hệ thống tự gán
            mRtcEngine.joinChannel(token, channelId, null, uid);
            showToast("Đã tham gia kênh: " + channelId);
        } else {
            showToast("Lỗi: Không có Channel ID hợp lệ!");
        }
    }

    // Callback từ Agora
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            runOnUiThread(() -> showToast("Tham gia thành công kênh: " + channel));
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            runOnUiThread(() -> showToast("Người dùng tham gia: " + uid));
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            runOnUiThread(() -> showToast("Người dùng rời kênh: " + uid));
        }
    };

    // Hiển thị thông báo ngắn (Toast)
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(CallActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    // Kiểm tra quyền truy cập
    private static final int PERMISSION_REQ_ID = 22;
    private boolean checkPermissions() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // Yêu cầu cấp quyền
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);
    }

    // Danh sách quyền cần thiết
    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{Manifest.permission.RECORD_AUDIO};
        }
    }

    // Xử lý kết quả khi người dùng cấp quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkPermissions()) {
            initializeAgoraVoiceSDK();
            joinChannel();
        } else {
            showToast("Bạn cần cấp quyền để sử dụng tính năng gọi thoại.");
        }
    }

    // Rời kênh và giải phóng tài nguyên khi thoát Activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }

}