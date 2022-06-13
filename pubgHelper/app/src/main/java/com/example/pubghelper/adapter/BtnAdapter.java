package com.example.pubghelper.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BtnAdapter extends RecyclerView.Adapter<BtnAdapter.MyViewHolder>{

    Context context;
    List<Button> buttons;

    public BtnAdapter(Context context, List<Button> buttons) {
        this.context = context;
        this.buttons = buttons;
    }

    @NonNull
    @Override
    public BtnAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout layout = new LinearLayout(context);
        return new BtnAdapter.MyViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull BtnAdapter.MyViewHolder holder, int position) {
        if (buttons.get(position).getParent() == null) {
            holder.viewGroup.addView(buttons.get(position));
        }

    }


    @Override
    public int getItemCount() {
        if (buttons == null) {
            return 0;
        }
        return buttons.size();
    }

    protected static class MyViewHolder extends RecyclerView.ViewHolder {
        ViewGroup viewGroup;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            viewGroup = (ViewGroup) itemView;
        }
    }
}
