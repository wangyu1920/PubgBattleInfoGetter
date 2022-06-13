package com.example.pubghelper.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImgAdapter extends RecyclerView.Adapter<ImgAdapter.MyViewHolder>{

    Context context;
    List<Bitmap> bitmaps;

    public ImgAdapter(Context context, List<Bitmap> bitmaps) {
        this.context = context;
        this.bitmaps = bitmaps;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setPadding(2,5,2,5);
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setAdjustViewBounds(true);
        layout.addView(imageView);
        return new MyViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.imageView.setImageBitmap(bitmaps.get(position));
    }


    @Override
    public int getItemCount() {
        if (bitmaps == null) {
            return 0;
        }
        return bitmaps.size();
    }

    protected static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ViewGroup layout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = (ViewGroup) itemView;
            imageView = (ImageView) ((ViewGroup) itemView).getChildAt(0);
        }
    }
}
