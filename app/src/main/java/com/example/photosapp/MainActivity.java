package com.example.photosapp;

import android.content.Intent;
import android.os.Bundle;

import com.example.photosapp.adapter.PhotoGridAdapter;
import com.example.photosapp.common.GridLayoutScrollListener;
import com.example.photosapp.common.enums.ErrorEnum;
import com.example.photosapp.viewmodel.MainViewModel;
import com.example.photosapp.viewmodel.PhotoListModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOGGER_TAG = MainActivity.class.getSimpleName();
    private MainViewModel mMainViewModel;
    private PhotoGridAdapter mPhotoGridAdapter;
    private int mInitialPhotoRange = 0;
    private boolean mIgnoreFetching = false;
    private ProgressBar mProgressBar = null;


    private Runnable mResetFetchingFlag = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing()) {
                mIgnoreFetching = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mProgressBar = findViewById(R.id.progress);
        final RecyclerView recyclerView = findViewById(R.id.rv_photo_grid);
        recyclerView.setNestedScrollingEnabled(true);


        mMainViewModel.getError().observe(this, new Observer<ErrorEnum>() {
            @Override
            public void onChanged(@Nullable ErrorEnum errorEnum) {
                if (errorEnum != null) {
                    if (errorEnum == ErrorEnum.UNABLE_TO_FETCH_DATA) {
                        Toast.makeText(MainActivity.this, errorEnum.getErrorResource(), Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

        GridLayoutManager layoutManager=new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        final List<PhotoListModel> photos = new ArrayList<>();
        mPhotoGridAdapter = new PhotoGridAdapter(photos,this);
        recyclerView.setAdapter(mPhotoGridAdapter);




        mMainViewModel.getLoading().observe((LifecycleOwner) this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean showLoading) {
                mProgressBar.setVisibility(showLoading == Boolean.TRUE ? View.VISIBLE : View.INVISIBLE);
            }
        });
        mMainViewModel.getPhotos().observe((LifecycleOwner) this, new Observer<List<PhotoListModel>>() {
            @Override
            public void onChanged(@Nullable final List<PhotoListModel> photoListModels) {
                if (photoListModels != null) {
                    if (photoListModels.isEmpty()) {

                        mPhotoGridAdapter.setDataSource(new ArrayList<PhotoListModel>());
                        mPhotoGridAdapter.notifyDataSetChanged();
                        mInitialPhotoRange = 0;
                    } else {

                        final List<PhotoListModel> subList = photoListModels.subList(mInitialPhotoRange, mInitialPhotoRange + (photoListModels.size() - mInitialPhotoRange));
                        mPhotoGridAdapter.addPhotos(subList);
                        mPhotoGridAdapter.notifyItemRangeChanged(mInitialPhotoRange, subList.size());
                        mPhotoGridAdapter.setOnClickListener(new PhotoGridAdapter.OnClickListener() {
                            @Override
                            public void onClick(View view, int position) {
                                Intent intent=new Intent(MainActivity.this, DetailActivity.class);
                                intent.putExtra("image",photoListModels.get(position).getUrl());
                                startActivity(intent);
                            }
                        });
                        Log.d(LOGGER_TAG, "SubList Size:" + subList.size() + " start:" + mInitialPhotoRange + " total:" + mPhotoGridAdapter.getItemCount());
                        mInitialPhotoRange = mInitialPhotoRange + (photoListModels.size() - mInitialPhotoRange);
                    }
                }
            }
        });


        recyclerView.addOnScrollListener(new GridLayoutScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                if (!mIgnoreFetching) {
                    mIgnoreFetching = true;
                    new Handler(Looper.getMainLooper()).postDelayed(mResetFetchingFlag, 500);
                    Log.d(LOGGER_TAG, "Load more items called");
                    mMainViewModel.fetchPhotos();
                }
            }

            @Override
            public boolean isLastPage() {
                return mMainViewModel.isLastPageFetched();
            }

            @Override
            public boolean isLoading() {
                return mMainViewModel.isLoading();
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy != 0) {
                    //Toast.makeText(MainActivity.this," Loading......", Toast.LENGTH_SHORT).show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }






}
