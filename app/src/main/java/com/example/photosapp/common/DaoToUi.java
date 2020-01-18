package com.example.photosapp.common;



import androidx.annotation.Nullable;

import com.example.photosapp.dao.Photo;
import com.example.photosapp.viewmodel.PhotoListModel;



public class DaoToUi {

    @Nullable
    public static PhotoListModel toUi(Photo photo) {
        PhotoListModel photoListModel = null;
        if (photo != null) {
            photoListModel = new PhotoListModel();
            photoListModel.setUrl(photo.getPhotoUrl());
            photoListModel.setId(photo.getPhotoId());
        }

        return photoListModel;
    }
}
