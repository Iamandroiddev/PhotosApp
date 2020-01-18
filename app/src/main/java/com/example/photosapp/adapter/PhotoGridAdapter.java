package com.example.photosapp.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.photosapp.R;
import com.example.photosapp.viewmodel.PhotoListModel;


import java.util.List;


public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridAdapter.PhotoGridViewHolder> {

    private List<PhotoListModel> mPhotos;
    private OnClickListener listener;
    private Context context;

    public PhotoGridAdapter(List<PhotoListModel> photos, Context context) {
        this.mPhotos = photos;
        this.context=context;
    }

    public void setDataSource(List<PhotoListModel> photos) {
        if (mPhotos != null) {
            mPhotos.clear();
        }
        mPhotos = photos;
    }

    public void addPhotos(List<PhotoListModel> photos) {
        if (mPhotos != null) {
            mPhotos.addAll(photos);
        }
    }

    @NonNull
    @Override
    public PhotoGridAdapter.PhotoGridViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new PhotoGridViewHolder(
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_photo_item, viewGroup, false));

    }


    @Override
    public void onBindViewHolder(@NonNull PhotoGridAdapter.PhotoGridViewHolder photoGridViewHolder, int i) {

        Glide.with(photoGridViewHolder.itemView.getContext())
                .load(mPhotos.get(i).getUrl())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(photoGridViewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return mPhotos != null ? mPhotos.size() : 0;
    }




    public class PhotoGridViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        LinearLayout linearLayout;
        public PhotoGridViewHolder(@NonNull final View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.imageView);
            linearLayout=itemView.findViewById(R.id.linear);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(itemView,getAdapterPosition());
                }
            });
        }

    }


    public interface OnClickListener
    {
        void onClick(View view, int position);
    }
    public void setOnClickListener(OnClickListener listener)
    {
        this.listener=listener;
    }


}
