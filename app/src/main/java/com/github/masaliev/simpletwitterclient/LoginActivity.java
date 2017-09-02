package com.github.masaliev.simpletwitterclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class LoginActivity extends AppCompatActivity {

    private TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if(session != null) {
            TwitterAuthToken authToken = session.getAuthToken();

            if(authToken != null && !authToken.isExpired()){
                goToMainActivity();
                return;
            }
        }

        setContentView(R.layout.activity_login);

        loginButton = (TwitterLoginButton) findViewById(R.id.btnTwitterLogin);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                goToMainActivity();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(LoginActivity.this, R.string.error_an_error_occurred_try_again, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        loginButton.onActivityResult(requestCode, resultCode, data);
    }
}
