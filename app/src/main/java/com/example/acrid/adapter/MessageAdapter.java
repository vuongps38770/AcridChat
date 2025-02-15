package com.example.acrid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.acrid.Model.Message;
import com.example.acrid.R;
import com.example.acrid.utils.TimeUtils;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final int MSG_RIGHT = 1;
    private static final int MSG_LEFT = 0;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId().equals(currentUserId)) {
            return MSG_RIGHT;
        } else {
            return MSG_LEFT;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_left, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.tvMessage.setText(message.getMessage());
        String date = TimeUtils.getTimeAgo(message.getTimestamp());
        if (position > 0 && TimeUtils.getTimeAgo(messageList.get(position - 1).getTimestamp()).equals(date)) {
            holder.time.setVisibility(View.GONE);
        } else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(date);
        }
        // Hiển thị trạng thái tin nhắn
//        if (holder.imgStatus != null) {
//            switch (message.getStatus()) {
//                case "sent":
//                    holder.imgStatus.setImageResource(R.drawable.ic_sent);
//                    break;
//                case "delivered":
//                    holder.imgStatus.setImageResource(R.drawable.ic_delivered);
//                    break;
//                case "seen":
//                    holder.imgStatus.setImageResource(R.drawable.ic_seen);
//                    break;
//            }
//        }
    }

    @Override
    public int getItemCount() {
        if(messageList!=null)
            return messageList.size();
        return 0;
    }

    public void setData(List<Message> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, time;
        ImageView imgStatus;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            imgStatus = itemView.findViewById(R.id.img_status);
            time = itemView.findViewById(R.id.time);
        }
    }
}
