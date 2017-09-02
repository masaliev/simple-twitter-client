package com.github.masaliev.simpletwitterclient;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int ITEMS_PER_PAGE = 20;

    private SwipeRefreshLayout swipeRefreshLayout;

    private TweetAdapter mAdapter;

    private StatusesService mStatusesService;
    private boolean mIsLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources (android.R.color.holo_red_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light);

        mAdapter = new TweetAdapter(this);

        RecyclerView rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvTweets.setItemAnimator(new DefaultItemAnimator());

        rvTweets.setAdapter(mAdapter);
        rvTweets.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int visibleThreshold = 2;
            private int lastVisibleItem, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!mIsLoading && totalItemCount <= (lastVisibleItem + visibleThreshold) && dy > 0) {
                    Log.d("SCROLL_TEST", "getOlderTweets");
                    getOlderTweets();
                }
            }
        });

        getOlderTweets();
    }

    private void getNewTweets(){
        Long sinceId = mAdapter.getSinceId();
        swipeRefreshLayout.setRefreshing(true);
        Call<List<Tweet>> call = getTimelineCall(null, sinceId, null);
        call.enqueue(new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                swipeRefreshLayout.setRefreshing(false);
                mAdapter.addItems(result.data, 0);
            }

            @Override
            public void failure(TwitterException exception) {
                swipeRefreshLayout.setRefreshing(false);
                exception.printStackTrace();
                Toast.makeText(MainActivity.this, R.string.error_an_error_occurred_try_again, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getOlderTweets(){
        mIsLoading = true;
        Long maxId = mAdapter.getMaxId();
        // timeline api provides results which are inclusive, decrement the maxId to get
        // exclusive results
        if(maxId != null){
            maxId = maxId - 1;
        }

        mAdapter.addLoadingItem();
        Call<List<Tweet>> call = getTimelineCall(ITEMS_PER_PAGE, null, maxId);
        call.enqueue(new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                mAdapter.removeLoadingItem();
                mAdapter.addItems(result.data);
                mIsLoading = false;
            }

            @Override
            public void failure(TwitterException exception) {
                mAdapter.removeLoadingItem();
                mIsLoading = false;
                exception.printStackTrace();
                Toast.makeText(MainActivity.this, R.string.error_an_error_occurred_try_again, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Call<List<Tweet>> getTimelineCall(Integer count, Long sinceId, Long maxId){
        if(mStatusesService == null){
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            mStatusesService = twitterApiClient.getStatusesService();
        }
        return mStatusesService.homeTimeline(count, sinceId, maxId, null, null, null, null);
    }

    @Override
    public void onRefresh() {
        getNewTweets();
    }
}
