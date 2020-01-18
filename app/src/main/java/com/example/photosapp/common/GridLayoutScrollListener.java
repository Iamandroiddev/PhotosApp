package com.example.photosapp.common;

import androidx.recyclerview.widget.GridLayoutManager;


public abstract class GridLayoutScrollListener extends PaginationScrollListener {

    public GridLayoutScrollListener(GridLayoutManager layoutManager) {
        super(layoutManager);
    }

    @Override
    public int getFirstVisibleItemPosition() {
        return ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
    }
}
