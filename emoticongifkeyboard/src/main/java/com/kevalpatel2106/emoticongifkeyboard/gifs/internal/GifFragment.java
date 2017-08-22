package com.kevalpatel2106.emoticongifkeyboard.gifs.internal;


import android.content.Context;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.kevalpatel2106.emoticongifkeyboard.R;
import com.kevalpatel2106.emoticongifkeyboard.emoticons.EmoticonSelectListener;
import com.kevalpatel2106.emoticongifkeyboard.gifs.Gif;
import com.kevalpatel2106.emoticongifkeyboard.gifs.GifProviderProtocol;
import com.kevalpatel2106.emoticongifkeyboard.gifs.GifSelectListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public final class GifFragment extends Fragment implements GifGridAdapter.ItemSelectListener {
    private Context mContext;

    //Array list to hold currently displaying emoticons list
    private List<Gif> mGifs;

    //Adapter to display emoticon grids.
    private GifGridAdapter mGifGridAdapter;

    //View flipper for show different states.
    private ViewFlipper mViewFlipper;

    //Gif loader protocol
    private GifProviderProtocol mGifProvider;

    //Error text view.
    private TextView mErrorTv;

    /**
     * Async task to load the trending GIFs.
     */
    private AsyncTask<Void, Void, List<Gif>> mTrendingGifTask;

    //Listener to notify when any gif select
    private GifSelectListener mGifSelectListener;

    public GifFragment() {
        // Required empty public constructor
    }

    /**
     * Get new instance of {@link GifFragment}. This function is for internal use only.
     *
     * @return {@link GifFragment}
     */
    public static GifFragment getNewInstance() {
        return new GifFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gif, container, false);
    }

    /**
     * Set the GIF loader. This function is for internal use only.
     *
     * @param gifProvider {@link GifProviderProtocol}
     */
    @SuppressWarnings("ConstantConditions")
    public void setGifProvider(@NonNull GifProviderProtocol gifProvider) {
        if (gifProvider == null) throw new RuntimeException("Set GIF loader.");
        mGifProvider = gifProvider;
    }

    /**
     * Set the {@link EmoticonSelectListener} to get notify whenever the emoticon is selected or deleted.
     *
     * @param gifSelectListener {@link EmoticonSelectListener}
     */
    public void setGifSelectListener(@NonNull GifSelectListener gifSelectListener) {
        mGifSelectListener = gifSelectListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Set the progressbar
        ProgressBar progressBar = view.findViewById(R.id.loading_progressbar);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(mContext, R.color.icon_selected),
                        PorterDuff.Mode.SRC_ATOP);

        mViewFlipper = view.findViewById(R.id.gif_view_flipper);
        mErrorTv = view.findViewById(R.id.error_textview);

        //Set the grid view
        mGifs = new ArrayList<>();
        mGifGridAdapter = new GifGridAdapter(mContext, mGifs, this);
        RecyclerView recyclerView = view.findViewById(R.id.gif_gridView);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        recyclerView.setAdapter(mGifGridAdapter);

        //Load the list of trending GIFs.
        if (mTrendingGifTask != null) mTrendingGifTask.cancel(true);
        mTrendingGifTask = new TrendingGifTask();
        mTrendingGifTask.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Cancel trending GIF.
        if (mTrendingGifTask != null) mTrendingGifTask.cancel(true);
    }

    @Override
    public void OnListItemSelected(@NonNull Gif gif) {
        if (mGifSelectListener != null) mGifSelectListener.onGifSelected(gif);
    }

    private class TrendingGifTask extends AsyncTask<Void, Void, List<Gif>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Display loading view.
            mViewFlipper.setDisplayedChild(0);
        }

        @Override
        protected List<Gif> doInBackground(Void... voids) {
            return mGifProvider.getTrendingGifs(20);
        }

        @Override
        protected void onPostExecute(@Nullable List<Gif> gifs) {
            super.onPostExecute(gifs);

            if (gifs == null) { //Error occurred.
                mErrorTv.setText("Something went wrong.");
                mViewFlipper.setDisplayedChild(2);
            } else if (gifs.isEmpty()) { //No result found.
                mErrorTv.setText("No GIF found.");
                mViewFlipper.setDisplayedChild(2);
            } else {
                //Load the tending gifs
                mGifs.clear();
                mGifs.addAll(gifs);
                mGifGridAdapter.notifyDataSetChanged();

                mViewFlipper.setDisplayedChild(1);
            }
        }
    }
}
