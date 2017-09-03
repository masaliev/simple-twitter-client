package com.github.masaliev.simpletwitterclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import retrofit2.Call;

public class NewTweetActivity extends AppCompatActivity {

    private final static int MAX_SYMBOLS = 140;

    private EditText etStatus;
    private TextView tvSymbols;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tweet);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        etStatus = (EditText) findViewById(R.id.etStatus);
        tvSymbols = (TextView) findViewById(R.id.tvSymbols);
        btnSend = (Button) findViewById(R.id.btnSend);

        etStatus.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_SYMBOLS)});
        etStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable != null && editable.length() > 0){
                    tvSymbols.setText(getString(R.string.symbols_count, editable.length(), MAX_SYMBOLS));
                    btnSend.setEnabled(true);
                } else {
                    tvSymbols.setText(getString(R.string.symbols_count, 0, MAX_SYMBOLS));
                    btnSend.setEnabled(false);
                }
            }
        });

        Editable editable = etStatus.getText();
        if(editable != null) {
            tvSymbols.setText(getString(R.string.symbols_count, editable.length(), MAX_SYMBOLS));
        } else {
            tvSymbols.setText(getString(R.string.symbols_count, 0, MAX_SYMBOLS));
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTweet();
            }
        });

    }

    private void sendTweet(){
        Editable editable = etStatus.getText();
        if(editable != null && editable.length() > 0){

            final ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.sending), getString(R.string.please_wait));

            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            Call<Tweet> call = twitterApiClient.getStatusesService()
                    .update(editable.toString(), null, null, null, null, null, null, null, null);
            call.enqueue(new Callback<Tweet>() {
                @Override
                public void success(Result<Tweet> result) {
                    progressDialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra("tweet_id", result.data.getId());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }

                @Override
                public void failure(TwitterException exception) {
                    progressDialog.dismiss();
                    Toast.makeText(NewTweetActivity.this, R.string.error_an_error_occurred_try_again, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
