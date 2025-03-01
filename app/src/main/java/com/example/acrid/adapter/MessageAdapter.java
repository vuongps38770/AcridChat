package com.example.acrid.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.acrid.Constant.DB;
import com.example.acrid.Model.Message;
import com.example.acrid.R;
import com.example.acrid.utils.TimeUtils;

import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final int MSG_RIGHT = 1;
    private static final int MSG_LEFT = 0;
    private RecyclerView recyclerView;
    private Context context;
    private List<Message> messageList;
    private String currentUserId;
    private String partnerUID;
    private Map<String,Long> lastSeenMap;

    public void setLastSeenMap(Map<String, Long> lastSeenMap) {
        this.lastSeenMap = lastSeenMap;
        int oldLastSeenIndex = lastSeenIndex;
        calculateLastSeenIndex();
        if (oldLastSeenIndex != lastSeenIndex) {
            notifyItemChanged(oldLastSeenIndex);
            notifyItemChanged(lastSeenIndex);
            if (lastSeenIndex == messageList.size() - 1) {
                recyclerView.postDelayed(() -> {
                    recyclerView.smoothScrollBy(0, 150);
                }, 300);
            }
        }
    }

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId,String partnerUID, RecyclerView recyclerView) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.partnerUID =partnerUID;
        this.recyclerView= recyclerView;
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
        holder.messageTextContainer.setVisibility(View.GONE);
        holder.img.setVisibility(View.GONE);

        if(message.getType().equals(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.TEXT.toString())){
            holder.messageTextContainer.setVisibility(View.VISIBLE);
            holder.tvMessage.setText(message.getMessage());
        }


        else if (message.getType().equals(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.IMAGE.toString())){
            holder.img.setVisibility(View.VISIBLE);
            Glide.with(holder.img)
                    .load(message.getMessage())
                    .placeholder(R.drawable.load)
                    .error(R.drawable.err)
                    .into(holder.img);
        }


        else holder.itemView.setVisibility(View.GONE);

        String date = TimeUtils.getTimeAgo(message.getTimestamp());
        Log.e("onBindViewHolder: ",message.getTimestamp()+"////"+date );
        if (position > 0 && TimeUtils.getTimeAgo(messageList.get(position - 1).getTimestamp()).equals(date)) {
            holder.time.setVisibility(View.GONE);
        } else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(date);
        }





        if(lastSeenMap!=null&&getItemViewType(position)==MSG_RIGHT){
            Log.e("onBindViewHolder: ","not null" );
            holder.imgStatus.setVisibility(View.GONE);
            if (position == lastSeenIndex) {
                Log.e("onBindViewHolder: ","ok" );
                holder.imgStatus.setImageResource(R.drawable.eye_svgrepo_com);
                holder.imgStatus.setVisibility(View.VISIBLE);
            }else {
                holder.imgStatus.setVisibility(View.GONE);

            }
        }else {
            Log.e("onBindViewHolder: ","null" );
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
    private int lastSeenIndex = -1;
    private void calculateLastSeenIndex() {
        lastSeenIndex = -1;
        if (lastSeenMap != null && lastSeenMap.containsKey(partnerUID)) {
            long lastSeenTime = lastSeenMap.get(partnerUID);
            Log.e("calculateLastSeenIndex: ",lastSeenTime+"" );

            for (int i = messageList.size() - 1; i >= 0; i--) {
                Message msg = messageList.get(i);
                Log.e( "calculateLastSeenIndex: ", msg.getTimestamp()+"///"+msg.getMessage());
                if (msg.getSenderId().equals(currentUserId) && msg.getTimestamp() <= lastSeenTime) {
                    lastSeenIndex = i;
                    Log.e("calculateLastSeenIndex:", "lastSeenIndex = " + lastSeenIndex+"///"+messageList.get(lastSeenIndex).getMessage());
                    break;
                }
            }
        }
    }
    @Override
    public int getItemCount() {
        if(messageList!=null)
            return messageList.size();
        return 0;
    }

    public void setData(List<Message> messages) {

        messageList.clear();
        messageList.addAll(messages);
        Log.e("setData: ",messageList.size()+"" );
        notifyDataSetChanged();
        calculateLastSeenIndex();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, time;
        ImageView imgStatus,img;
        LinearLayout messageTextContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            imgStatus = itemView.findViewById(R.id.img_status);
            time = itemView.findViewById(R.id.time);
            img = itemView.findViewById(R.id.img);
            messageTextContainer = itemView.findViewById(R.id.messageTextContainer);

        }
    }
}
