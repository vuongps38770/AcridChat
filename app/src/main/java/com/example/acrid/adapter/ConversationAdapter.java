package com.example.acrid.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.Conversation;
import com.example.acrid.Model.Friend;
import com.example.acrid.Model.Message;
import com.example.acrid.R;
import com.example.acrid.utils.TimeUtils;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder>{

    private final Context context;
    private final String userUID=UserRepo.getCurrentUserUID();
    private OnConversationClickListener onConversationClickListener;
    private OnConversationLongClickListener onConversationLongClickListener;
    private String currentQuery = "";

    public ConversationAdapter(Context context, List<Conversation> list) {
        this.context = context;
        this.list = list;
        this.ogList=list;
    }

    public void setOnConversationClickListener(OnConversationClickListener onConversationClickListener) {
        this.onConversationClickListener = onConversationClickListener;
    }

    public void setOnConversationLongClickListener(OnConversationLongClickListener onConversationLongClickListener) {
        this.onConversationLongClickListener = onConversationLongClickListener;
    }

    public void setData(List<Conversation> list){
        this.list = list;
        this.ogList=list;
        notifyDataSetChanged();
    }
    private List<Conversation> list;
    private List<Conversation> ogList;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.conversation_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation thisConversation = list.get(position);
        Message lastMessage = thisConversation.getLastMessage();
        String displayName = thisConversation.getPartner().getDisplayName();
        holder.name.setText(thisConversation.getPartner().getDisplayName());
        highlightSearchResults(holder.name, displayName, currentQuery);
        if(lastMessage!=null){
            holder.lastMSG.setText(lastMessage.getMessage());
            holder.lastTime.setText(TimeUtils.getTimeAgo(thisConversation.getLastUpdatedAt()));
        }
        Glide.with(holder.profileIMG)
                .load(thisConversation.getPartner().getProfileIMG())
                .placeholder(R.drawable.load)
                .error(R.drawable.img)
                .into(holder.profileIMG);

        holder.itemView.setOnClickListener(view -> {
            if(onConversationClickListener==null) return;
            onConversationClickListener.onClicked(thisConversation);
        });
        if(thisConversation.getReadList()!=null&&!thisConversation.getReadList().contains(userUID)){
            holder.mark.setVisibility(View.VISIBLE);
        }else holder.mark.setVisibility(View.GONE);

        holder.itemView.setOnLongClickListener(view -> {
            if(onConversationLongClickListener!=null) {
                onConversationLongClickListener.onLongClicked(thisConversation,view);
            }

            return true;
        });
        holder.itemView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.5f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1.0f);
                    break;
            }
            return false;
        });
    }
    public void search(String query) {
        currentQuery = query;
        if (query.isEmpty()) {
            this.list = new ArrayList<>(ogList);
        } else {
            this.list = ogList.stream()
                    .filter(conversation ->
                            conversation.getPartner().getIDName().toLowerCase().equals(query.toLowerCase()) ||
                                    containsAllCharactersIgnoreCase(conversation.getPartner().getDisplayName(), query))
                    .collect(Collectors.toList());
        }
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        if(list!=null) return list.size();
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView profileIMG,mark;
        TextView name;
        TextView lastMSG;
        TextView lastTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIMG = itemView.findViewById(R.id.imgProfileIMG);
            name = itemView.findViewById(R.id.txtName);
            lastMSG = itemView.findViewById(R.id.txtLastMSG);
            lastTime = itemView.findViewById(R.id.lastTime);
            mark = itemView.findViewById(R.id.mark);

        }

    }
    public interface OnConversationClickListener{
        void onClicked(Conversation conversation);
    }
    public interface OnConversationLongClickListener{
        void onLongClicked(Conversation conversation, View anchorView);
    }


    public void highlightSearchResults(TextView textView, String originalText, String query) {
        if (textView == null || originalText == null || query == null) {
            Log.e("HighlightDebug", "TextView, OriginalText, or Query is null");
            return;
        }

        if (query.isEmpty() || originalText.isEmpty()) {
            textView.setText(originalText);
            return;
        }

        SpannableString spannable = new SpannableString(originalText);
        String lowerOriginalText = originalText.toLowerCase();
        String lowerQuery = query.toLowerCase();

        for (char queryChar : lowerQuery.toCharArray()) {
            int startIndex = 0;
            while (true) {
                startIndex = lowerOriginalText.indexOf(queryChar, startIndex);
                if (startIndex == -1) {
                    break;
                }


                spannable.setSpan(
                        new ForegroundColorSpan(ContextCompat.getColor(context,R.color.mainChild)),
                        startIndex,
                        startIndex + 1,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                );

                startIndex++;
            }
        }

        textView.setText(spannable);
    }


    private boolean isSubsequence(String query, String text) {
        int i = 0, j = 0;
        while (i < query.length() && j < text.length()) {
            if (query.charAt(i) == text.charAt(j)) {
                i++;
            }
            j++;
        }
        return i == query.length();
    }

    private boolean containsAllCharactersIgnoreCase(String text, String query) {
        if (text == null || query == null) {
            return false;
        }

        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();

        for (char c : lowerQuery.toCharArray()) {
            if (lowerText.indexOf(c) == -1) {
                return false; // Nếu một ký tự trong query không có trong text, trả về false
            }
        }
        return true; // Tất cả ký tự trong query đều có trong text
    }
}
