package com.example.acrid.View.Dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.acrid.R;

public class PopupBuilder {
    private final int dividerHeight=1;
    private Context context;
    private LinearLayout container;
    private PopupWindow popupWindow;
    private static PopupWindow currentPopup = null;

    public PopupBuilder(Context context) {
        this.context = context;
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(20, 20, 20, 20);
        container.setBackgroundResource(R.drawable.transparent);
    }

    public PopupBuilder addItem(String text, View.OnClickListener listener) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setPadding(20, 20, 20, 20);
        textView.setBackgroundResource(R.drawable.transparent);
        textView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(v);
            dismiss();
        });
        container.addView(textView);

        // Nếu không phải item cuối cùng, thêm divider
//        View divider = new View(context);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight
//        );
//        divider.setLayoutParams(params);
//        divider.setBackgroundColor(Color.GRAY);
//        container.addView(divider);
        return this;
    }
    public PopupBuilder addItem(String text,int textColor, View.OnClickListener listener) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setPadding(20, 20, 20, 20);
        textView.setBackgroundResource(R.drawable.add_friend_button);
        textView.setTextColor(textColor);
        textView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(v);
            dismiss();
        });
        container.addView(textView);

//        // Nếu không phải item cuối cùng, thêm divider
//        View divider = new View(context);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT, dividerHeight
//        );
//        divider.setLayoutParams(params);
//        divider.setBackgroundColor(Color.GRAY);
//        container.addView(divider);
        return this;
    }

    public PopupBuilder build() {
        popupWindow = new PopupWindow(container,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        return this;
    }

    public void show(View anchor) {
        if (currentPopup != null) {
            currentPopup.dismiss();
        }

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int anchorX = location[0];
        int anchorY = location[1];
        int anchorWidth = anchor.getWidth();
        int anchorHeight = anchor.getHeight();

        // Lấy kích thước popup
        container.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = container.getMeasuredWidth();
        int popupHeight = container.getMeasuredHeight();

        int x = anchorX + anchorWidth;
        int y = anchorY + anchorHeight; // Mặc định hiển thị dưới anchor

        // Nếu không đủ chỗ bên dưới, hiển thị bên trên
        if (y + popupHeight > anchor.getRootView().getHeight()) {
            y = anchorY - popupHeight;
        }

        // Nếu vẫn không đủ chỗ, đặt lệch nhẹ sang trái/phải
        if (y < 0) {
            y = anchorY + (anchorHeight / 2);
            x = anchorX + (anchorWidth / 2) - (popupWidth / 2);
        }

        popupWindow.showAtLocation(anchor, 0, x, y);
        currentPopup = popupWindow;
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            currentPopup = null;
        }
    }
}

