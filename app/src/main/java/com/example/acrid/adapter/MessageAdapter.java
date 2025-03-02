package com.example.acrid.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
    private final String SENT= DB.CHAT_STATUS.SENT.toString();
    private final String FAILED= DB.CHAT_STATUS.FAILED.toString();
    private final String SENDING= DB.CHAT_STATUS.SENDING.toString();

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

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


        holder.itemView.setOnClickListener(view -> {
            if(itemClickListener==null) return;
            itemClickListener.onclicked(message);
        });


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


//        if (holder.imgStatus != null&&holder.msgStatus!=null) {
//            holder.msgStatus.setVisibility(View.GONE);
//            Log.e( "onBindViewHolderd: ",message.getStatus() );
//            if(message.getStatus().equals(SENT)){
//
//                holder.msgStatus.setVisibility(View.VISIBLE);
//                holder.msgStatus.setText("Đã gửi");
//                new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                    holder.msgStatus.setVisibility(View.GONE);
//                }, 3000);
//            } else if (message.getStatus().equals(SENDING)) {
//
//                holder.msgStatus.setVisibility(View.VISIBLE);
//                holder.msgStatus.setText("Đang gửi");
//
//            }else if (message.getStatus().equals(FAILED)){
//                holder.msgStatus.setVisibility(View.VISIBLE);
//                holder.msgStatus.setText("Gửi thất bại");
//
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
        int oldSize = messageList.size();
        int newSize = messages.size();

        messageList.clear();
        messageList.addAll(messages);
        Log.e("setData: ", messageList.size() + "");

        if (newSize > oldSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        } else {
            notifyDataSetChanged();
        }

        calculateLastSeenIndex();
    }
    private boolean isRecentMessage(Message message) {
        long currentTime = System.currentTimeMillis();
        return message.getTimestamp() > (currentTime - 5000);
    }
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, time,msgStatus;
        ImageView imgStatus,img;
        LinearLayout messageTextContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            imgStatus = itemView.findViewById(R.id.img_status);
            time = itemView.findViewById(R.id.time);
            img = itemView.findViewById(R.id.img);
            messageTextContainer = itemView.findViewById(R.id.messageTextContainer);
            msgStatus = itemView.findViewById(R.id.msgStatus);

        }
    }


    public  static interface ItemClickListener{
        void onclicked(Message message);
    }
}
