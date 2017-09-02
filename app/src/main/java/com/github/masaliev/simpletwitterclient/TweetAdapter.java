package com.github.masaliev.simpletwitterclient;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetBuilder;
import com.twitter.sdk.android.tweetui.CompactTweetView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbt on 9/2/17.
 */

public class TweetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_TWEET = 0;
    private static final int ITEM_TYPE_LOADING = 1;

    private ArrayList<Tweet> items;
    private Context mContext;
    private Callback<Tweet> mActionCallback;


    public TweetAdapter(Context context) {
        this.mContext = context;
        mActionCallback = new ReplaceTweetCallback();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ITEM_TYPE_LOADING){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loading, parent, false);
            return new LoadingViewHolder(view);
        }else {
            final Tweet tweet = new TweetBuilder().build();
            final CompactTweetView compactTweetView = new CompactTweetView(mContext, tweet, R.style.tw__TweetLightWithActionsStyle);
            compactTweetView.setOnActionCallback(mActionCallback);
            return new TweetViewHolder(compactTweetView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof TweetViewHolder) {
            final Tweet tweet = items.get(position);
            final CompactTweetView compactTweetView = (CompactTweetView) holder.itemView;
            compactTweetView.setTweet(tweet);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(items == null) {
            return super.getItemViewType(position);
        }

        return items.get(position) == null ? ITEM_TYPE_LOADING : ITEM_TYPE_TWEET;
    }

    public void addLoadingItem(){
        removeLoadingItem();
        if(items == null){
            items = new ArrayList<>();
        }

        items.add(null);
        notifyItemInserted(items.size() - 1);
    }

    public void removeLoadingItem(){
        if(items != null && items.size() > 0){
            if(items.get(items.size() - 1) == null){
                items.remove(items.size() - 1);
                notifyItemRemoved(items.size());
            }
        }
    }

    public void setItems(List<Tweet> items){
        this.items = new ArrayList<>();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void addItems(List<Tweet> items){
        if(this.items == null){
            setItems(items);
            return;
        }
        int start = this.items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(start, items.size());
    }

    public void addItems(List<Tweet> items, int index){
        if (this.items == null){
            setItems(items);
            return;
        }
        this.items.addAll(index, items);
        notifyItemRangeChanged(index, items.size());
    }

    public Long getSinceId(){
        if(items == null || items.size() == 0){
            return null;
        }
        return items.get(0).getId();
    }

    public Long getMaxId(){
        if (items == null || items.size() == 0){
            return null;
        }

        return items.get(items.size() - 1).getId();
    }

    public class TweetViewHolder extends RecyclerView.ViewHolder {
        public TweetViewHolder(CompactTweetView itemView) {
            super(itemView);
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    /*
     * On success, sets the updated Tweet in the TimelineDelegate to replace any old copies
     * of the same Tweet by id.
     */
    private class ReplaceTweetCallback extends Callback<Tweet> {
        @Override
        public void success(Result<Tweet> result) {
            if(items != null){
                Tweet tweet = result.data;
                for (int i = 0; i < items.size(); i++){
                    if(tweet.getId() == items.get(i).getId()){
                        items.set(i, tweet);
                    }
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public void failure(TwitterException exception) {
            exception.printStackTrace();
        }
    }
}
