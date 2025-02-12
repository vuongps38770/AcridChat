package com.example.acrid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.acrid.Model.Conversation;
import com.example.acrid.Model.Friend;
import com.example.acrid.R;

import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Optional;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder>{

    private Context context;
    private OnConversationClickListener onConversationClickListener;
    private OnConversationLongClickListener onConversationLongClickListener;

    public ConversationAdapter(Context context, List<Conversation> list) {
        this.context = context;
        this.list = list;
    }
    public void setData(List<Conversation> list){
        this.list = list;
        notifyDataSetChanged();
    }
    private List<Conversation> list;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.conversation_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation thisConversation = list.get(position);
        Glide.with(holder.profileIMG)
                .load(thisConversation.getPartner().getProfileIMG())
                .placeholder(R.drawable.load)
                .error(R.drawable.img)
                .into(holder.profileIMG);
        holder.name.setText(thisConversation.getPartner().getIDName());
        holder.itemView.setOnClickListener(view -> {
            if(onConversationClickListener==null) return;
            onConversationClickListener.onClicked(thisConversation);
        });

        holder.itemView.setOnLongClickListener(view -> {

            if(onConversationLongClickListener!=null) {
                onConversationLongClickListener.onLongClicked(thisConversation,view);
            }

            return true;
        });
    }

    @Override
    public int getItemCount() {
        if(list!=null) return list.size();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView profileIMG;
        TextView name;
        TextView lastMSG;
        TextView lastTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIMG = itemView.findViewById(R.id.imgProfileIMG);
            name = itemView.findViewById(R.id.txtName);
            lastMSG = itemView.findViewById(R.id.txtLastMSG);
            lastTime = itemView.findViewById(R.id.lastTime);
        }

    }
    public interface OnConversationClickListener{
        void onClicked(Conversation conversation);
    }
    public interface OnConversationLongClickListener{
        void onLongClicked(Conversation conversation, View anchorView);
    }
}
