package it.antedesk.socialloginhtmlit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;


public class LoginActivity extends AppCompatActivity  implements View.OnClickListener{

    private final String TAG = "LoginProcess";

    private FirebaseAuth mFirebaseAuth;

    private TwitterLoginButton mLoginButton;

    private TextView mUserNameTV;
    private ImageView mUserImageIV;
    private ProgressDialog mProgressDialog;
    private LinearLayout mUserInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Twitter SDK
        initializeTwitter();

        // Set contentView must be called after the Twitter initialization
        // otherwise the SDK will raise an exception
        setContentView(R.layout.activity_login);

        mUserNameTV = findViewById(R.id.tv_user_name);
        mUserImageIV = findViewById(R.id.iv_user_image);
        mUserInfoLayout = findViewById(R.id.linear_layout_user_info);
        findViewById(R.id.bt_log_out).setOnClickListener(this);

        Log.d(TAG, "Initialize Firebase Auth");
        mFirebaseAuth = FirebaseAuth.getInstance();

        mLoginButton = findViewById(R.id.bt_log_in);
        mLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }
            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                updateUI(null);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the Twitter login button.
        mLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);
        showProgressDialog();

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void initializeTwitter(){
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(
                        getString(R.string.com_twitter_sdk_android_CONSUMER_KEY),
                        getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    private void updateUI(FirebaseUser user) {
        Log.d(TAG, "Checking user");
        if (user != null) {
            Log.d(TAG, "user not null");

            mLoginButton.setVisibility(View.GONE);
            mUserInfoLayout.setVisibility(View.VISIBLE);

            mUserNameTV.setText(user.getDisplayName());
            String imageUrl = user.getPhotoUrl().toString();  // image size small

            Log.d(TAG, imageUrl);
            Picasso.with(this)
                    .load(imageUrl)
                    // image powered by Grace Baptist (http://gbchope.com/events-placeholder/)
                   // .placeholder(R.drawable.placeholder)
                    .into(mUserImageIV);
        } else {
            Log.d(TAG, "user is null");

            mLoginButton.setVisibility(View.VISIBLE);
            mUserInfoLayout.setVisibility(View.GONE);
        }
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        TwitterCore.getInstance().getSessionManager().clearActiveSession();
        updateUI(null);
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_log_in) {
            signOut();
        }
    }
}