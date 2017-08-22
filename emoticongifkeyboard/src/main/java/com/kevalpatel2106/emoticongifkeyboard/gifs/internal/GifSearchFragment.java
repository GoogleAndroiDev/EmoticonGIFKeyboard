package com.kevalpatel2106.emoticongifkeyboard.gifs.internal;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
public class GifSearchFragment extends Fragment implements GifGridAdapter.ItemSelectListener {

    //List of GIFs to display
    private ArrayList<Gif> mGifs;

    //GIF adapter
    private GifGridAdapter mGifGridAdapter;

    //Listener to notify when gif selected.
    private GifSelectListener mGifSelectListener;

    private GifProviderProtocol mGifProvider;

    private ViewFlipper mViewFlipper;

    private SearchGifTask mSearchTask;

    private TextView mErrorTv;

    public GifSearchFragment() {
        // Required empty public constructor
    }

    public static GifSearchFragment getNewInstance() {
        return new GifSearchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gif_search, container, false);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGifs = new ArrayList<>();
        mViewFlipper = view.findViewById(R.id.gif_search_vieW_pager);
        mErrorTv = view.findViewById(R.id.error_tv);

        //Set the list
        mGifGridAdapter = new GifGridAdapter(getActivity(), mGifs, this);
        RecyclerView recyclerView = view.findViewById(R.id.gif_search_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(mGifGridAdapter);

        //Set the search interface
        final EditText searchEt = view.findViewById(R.id.search_box_et);
        view.findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (searchEt.getText().length() > 0) {
                    if (mSearchTask != null) mSearchTask.cancel(true);

                    mSearchTask = new SearchGifTask();
                    mSearchTask.execute(searchEt.getText().toString());
                }
            }
        });

        //Set up button
        EmoticonGifImageView backBtn = view.findViewById(R.id.up_arrow);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSearchTask != null) mSearchTask.cancel(true);
    }

    @Override
    public void OnListItemSelected(@NonNull Gif gif) {
        if (mGifSelectListener != null) mGifSelectListener.onGifSelected(gif);
    }

    private class SearchGifTask extends AsyncTask<String, Void, List<Gif>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Display loading view.
            mViewFlipper.setDisplayedChild(0);
        }

        @Override
        protected List<Gif> doInBackground(String... strings) {
            return mGifProvider.searchGifs(20, strings[0]);
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

            mSearchTask = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mSearchTask = null;
        }
    }
}
