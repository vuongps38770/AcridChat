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

import java.util.List;
import java.util.Optional;

public class ACP_QueqeAdapter extends RecyclerView.Adapter<ACP_QueqeAdapter.ViewHolder> {
    private Context context;
    private List<People> list;
    private OnACPbtnClickListener onACPbtnClickListener;
    public void setData(List<People> list){
        Log.e("setData: ", list.size()+"");
        this.list = list;
        notifyDataSetChanged();
    }
    public ACP_QueqeAdapter(Context context, List<People> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.acp_queqe_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        People thisPeople = list.get(position);
        Glide.with(holder.profileIMG)
                .load(thisPeople.getUserUID())
                .placeholder(R.drawable.load)
                .error(R.drawable.img)
                .into(holder.profileIMG);
        holder.status.setText(Optional.ofNullable(thisPeople.getDescription()).orElse("Không có mô tả"));
        holder.name.setText(thisPeople.getIDName());
        holder.add.setOnClickListener(view -> {
            Log.e("click: ", "click");
            if (onACPbtnClickListener != null) {
                Log.e("click: ", "click");
                onACPbtnClickListener.onClicked(thisPeople, holder.getAdapterPosition());
            } else Log.e("click: ", "not");
        });
            if (thisPeople.getFriendStatus().equals(DB.FRIEND_STATUS.ACCEPTED.toString())) {
            holder.add.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        Log.d("getItemCount", "getItemCount: " + (list == null ? "null" : list.size()));
        if (list != null) return list.size();
        return 0;
    }

    public void setOnAddFriendClickedListener(OnACPbtnClickListener onACPbtnClickListener) {
        this.onACPbtnClickListener = onACPbtnClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

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
    public interface OnACPbtnClickListener {
        void onClicked(People people, int pos);
    }
}
