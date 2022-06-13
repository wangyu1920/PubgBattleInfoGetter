package com.example.pubghelper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pubghelper.R;

import java.util.List;

public class FloatAdapter extends RecyclerView.Adapter<FloatAdapter.MyViewHolder> {

    List<DataBean> dataBeans;
    Context context;

    public FloatAdapter(List<DataBean> dataBeans, Context context) {
        this.dataBeans = dataBeans;
        this.context = context;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.recyclerview_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.imageView.setImageBitmap(dataBeans.get(position).bitmap);
//        StringBuilder stringBuilder = new StringBuilder();
//        double[] doubles = dataBeans.get(position).doubles;
//        stringBuilder.append(doubles[0]);
//        stringBuilder.append(" ");
//        stringBuilder.append(doubles[1]);
//        holder.textView.setText(stringBuilder);
    }


    @Override
    public int getItemCount() {
        if (dataBeans != null) {
            return dataBeans.size();
        }
        return 0;
    }

    protected static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_imageview);
//            textView = itemView.findViewById(R.id.item_textview);
        }
    }

    public static class DataBean {
        public Bitmap bitmap;
        public double[] doubles;

        public DataBean(Bitmap bitmap, double[] doubles) {
            this.bitmap = bitmap;
            this.doubles = doubles;
        }
    }
}
