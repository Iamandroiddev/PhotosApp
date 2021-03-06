package com.example.photosapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.photosapp.common.DaoManager;
import com.example.photosapp.common.DaoToUi;
import com.example.photosapp.common.ResultCallbackListener;
import com.example.photosapp.common.enums.ErrorEnum;
import com.example.photosapp.dao.Photo;
import com.example.photosapp.repo.MainRepo;
import com.example.photosapp.responsemodel.FlickrRootResponseModel;


import java.util.ArrayList;
import java.util.List;


public class MainViewModel extends AndroidViewModel {

    private static final String LOGGER_TAG = MainViewModel.class.getSimpleName();
    public static final int INCREMENT_COUNT = 50;
    private static final int MAX_PER_PAGE_COUNT = 100;

    private int mCurrentPage = 1;
    private int mTotalPage = 0;
    private int mCurrentPerPageCount = 0;

    private int mStartIndex = 0;

    private MutableLiveData<List<PhotoListModel>> mPhotos;
    private MutableLiveData<Boolean> mShowLoading;
    private MutableLiveData<ErrorEnum> mError;
    private MainRepo mMainModel;
    private Observer<List<Photo>> mObserver;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mMainModel = new MainRepo(application);
    }

    public MutableLiveData<List<PhotoListModel>> getPhotos() {
        if (mPhotos == null) {
            mPhotos = new MutableLiveData<>();
            Log.d(LOGGER_TAG, "Fetching photos with default tag");
            fetchPhotos();
        }

        return mPhotos;
    }

    private void setPhotos(List<PhotoListModel> photos) {
        if (photos != null) {
            List<PhotoListModel> currentPhotos = mPhotos.getValue();
            if (currentPhotos != null && !currentPhotos.isEmpty()) {
                currentPhotos.addAll(photos);
                mPhotos.postValue(currentPhotos);
            } else {
                mPhotos.postValue(photos);
            }
        } else {
            mPhotos.postValue(new ArrayList<PhotoListModel>());
        }
    }

    public LiveData<Boolean> getLoading() {
        if (mShowLoading == null) {
            mShowLoading = new MutableLiveData<>();
        }

        return mShowLoading;
    }

    private void setLoading(boolean isLoading) {
        mShowLoading.postValue(isLoading);
    }

    public MutableLiveData<ErrorEnum> getError() {
        if (mError == null) {
            mError = new MutableLiveData<>();
        }

        return mError;
    }

    private void setError(ErrorEnum error) {
        if (mError != null) {
            mError.postValue(error);
        }
    }


    private void setupListenerForLastAddedPhotos() {
        if (mObserver == null) {
            mObserver = new Observer<List<Photo>>() {
                @Override
                public void onChanged(@Nullable List<Photo> photos) {
                    if (photos != null && !photos.isEmpty()) {
                        List<PhotoListModel> photoListModels = new ArrayList<>();
                        for (Photo daoItem : photos) {
                            photoListModels.add(DaoToUi.toUi(daoItem));
                        }
                        Log.d(LOGGER_TAG, "Fetched rows from web " + photoListModels.size());
                        setPhotos(photoListModels);
                    }
                }
            };

        }
    }

    public void fetchPhotos() {
        DaoManager.getInstance(getApplication()).submitQuery(
                new Runnable() {
                    @Override
                    public void run() {
                        List<Photo> photos = mMainModel.getPhotosInRange(mStartIndex, INCREMENT_COUNT);
                        onDbPhotosReceived(photos);
                    }
                });
    }

    private void onDbPhotosReceived(List<Photo> daoPhotos) {
        if (daoPhotos == null || daoPhotos.isEmpty()) {
            fetchPhotosFromWeb();
        } else {
            List<PhotoListModel> photoListModels = new ArrayList<>();
            for (Photo daoItem : daoPhotos) {
                photoListModels.add(DaoToUi.toUi(daoItem));
            }
            Log.d(LOGGER_TAG, "Fetched rows from db " + photoListModels.size());
            setPhotos(photoListModels);
        }

        mStartIndex += INCREMENT_COUNT;
    }

    private void fetchPhotosFromWeb() {
        if (!isLoading()) {
            setupListenerForLastAddedPhotos();
            setLoading(true);
            if (mCurrentPerPageCount == MAX_PER_PAGE_COUNT) {
                mCurrentPerPageCount = INCREMENT_COUNT;
                mCurrentPage += 1;
            } else {
                mCurrentPerPageCount += INCREMENT_COUNT;
            }
            mMainModel.getPhotosFromWeb( new ResultCallbackListener<FlickrRootResponseModel, Throwable>() {
                @Override
                public void onSuccess(FlickrRootResponseModel response) {
                    setLoading(false);
                    mTotalPage = response.getPhotosResponseModel().getPages();
                }

                @Override
                public void onFailure(Throwable error) {
                    setLoading(false);
                    setError(ErrorEnum.UNABLE_TO_FETCH_DATA);
                }
            }, mCurrentPage, mCurrentPerPageCount);
        }
    }

    public boolean isLastPageFetched() {
        return mCurrentPage == mTotalPage;
    }

    public boolean isLoading() {
        return mShowLoading != null && (mShowLoading.getValue() != null ? mShowLoading.getValue() : false);
    }

    @Override
    protected void onCleared() {
        mMainModel.getLastAddedPhotos().removeObserver(mObserver);
    }
}
