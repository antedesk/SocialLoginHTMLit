package it.antedesk.socialloginhtmlit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class LoginActivity extends AppCompatActivity {

    private TwitterLoginButton twitterLoginButton;
    private Button twitterLogoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeTwitter();
        // set contentView must be called after the Twitter initialization
        //otherwise the SDK will raise an exception!
        setContentView(R.layout.activity_login);

        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                updateUI(true);
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                Toast.makeText(getApplicationContext(), "Logged! My token is: "+authToken.token, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                updateUI(false);
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        twitterLogoutButton = (Button) findViewById(R.id.twitter_logout_button);
        twitterLogoutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                if (session == null)
                    Toast.makeText(getApplicationContext(), "Logged out!", Toast.LENGTH_LONG).show();
                updateUI(false);
            }
        });
    }

    private void initializeTwitter(){
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.com_twitter_sdk_android_CONSUMER_KEY), getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            twitterLoginButton.setVisibility(View.GONE);
            twitterLogoutButton.setVisibility(View.VISIBLE);
        } else {
            twitterLoginButton.setVisibility(View.VISIBLE);
            twitterLogoutButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }



}