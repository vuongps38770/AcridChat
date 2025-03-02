package com.example.acrid.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.acrid.Constant.DB;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.People;
import com.example.acrid.R;

import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Optional;

public class FindPeopleAdapter extends RecyclerView.Adapter<FindPeopleAdapter.ViewHolder> {
    public void setData(List<People> list){
        this.list = list;
        notifyDataSetChanged();
    }

    private final Context context;
    private List<People> list;
    private final String userUID;
    private OnAddFriendClickedListener onAddFriendClickedListener;

    public OnAddFriendClickedListener getOnAddFriendClickedListener() {
        return onAddFriendClickedListener;
    }

    public void setOnAddFriendClickedListener(OnAddFriendClickedListener onAddFriendClickedListener) {
        this.onAddFriendClickedListener = onAddFriendClickedListener;
    }

    public FindPeopleAdapter(Context context, List<People> list,String userUID) {
        this.context = context;
        this.list = list;
        this.userUID=userUID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.find_people_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        People thisPeople= list.get(position);

        Glide.with(holder.profileIMG)
                .load(thisPeople.getProfileIMG())
                .placeholder(R.drawable.load)
                .error(R.drawable.img)
                .into(holder.profileIMG);
        holder.status.setText(thisPeople.getIDName());
//        if(UserRepo.getFriendUIDList(UserRepo.getCurrentUserUID()).getValue().contains(thisPeople.getUserUID())){
//            holder.add.setVisibility(View.GONE);
//        }
        holder.name.setText(thisPeople.getDisplayName());

        holder.add.setVisibility(View.GONE);
        if(thisPeople.getFriendStatus().equals(DB.FRIEND_STATUS.PENDING.toString()) ){
            holder.add.setVisibility(View.VISIBLE);
            holder.add.setText("Đã gửi lời mời");
            holder.add.setEnabled(false);
        }else if (thisPeople.getFriend_uid_list().contains(userUID) ){
            holder.add.setVisibility(View.VISIBLE);
            holder.add.setText("Bạn bè");
            holder.add.setEnabled(false);
        }else {
            holder.add.setVisibility(View.VISIBLE);
            holder.add.setOnClickListener(view -> {
                if(onAddFriendClickedListener!=null){
                    onAddFriendClickedListener.onClicked(thisPeople,holder.getAdapterPosition());
                }else Log.e("click: ", "not");
            });
        }
    }

    @Override
    public int getItemCount() {
        if(list!=null) return list.size();
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView profileIMG;
        TextView name;
        TextView status;
        Button add;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileIMG = itemView.findViewById(R.id.imgProfileIMG);
            name = itemView.findViewById(R.id.txtName);
            status = itemView.findViewById(R.id.txtStatus);
            add = itemView.findViewById(R.id.btnAdd);
        }

    }
    public interface OnAddFriendClickedListener{
        void onClicked(People people,int pos);
    }
}
