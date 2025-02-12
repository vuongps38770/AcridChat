package com.example.acrid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.acrid.Model.Friend;
import com.example.acrid.Model.People;
import com.example.acrid.R;

import java.util.List;
import java.util.Optional;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private Context context;
    private List<Friend> list;
    OnSeeMoreClickListener onSeeMoreClickListener;
    public void setData(List<Friend> list){
        this.list = list;
        notifyDataSetChanged();
    }

    public void setOnSeeMoreClickListener(OnSeeMoreClickListener onSeeMoreClickListener) {
        this.onSeeMoreClickListener = onSeeMoreClickListener;
    }

    public FriendAdapter(Context context, List<Friend> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.friend_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend thisFriend= list.get(position);
        Glide.with(holder.profileIMG)
                .load(thisFriend.getUserUID())
                .placeholder(R.drawable.load)
                .error(R.drawable.img)
                .into(holder.profileIMG);
        holder.name.setText(thisFriend.getIDName());
        holder.seeMore.setOnClickListener(view -> {
            if(onSeeMoreClickListener!=null){
                onSeeMoreClickListener.onClicked(holder.seeMore,thisFriend,holder.getAdapterPosition());
            }
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
        ImageView seeMore;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIMG = itemView.findViewById(R.id.imgProfileIMG);
            name = itemView.findViewById(R.id.txtName);
            seeMore = itemView.findViewById(R.id.seeMore);
        }

    }
    public interface OnSeeMoreClickListener{
        void onClicked(View view,Friend friend,int pos);
    }
}
