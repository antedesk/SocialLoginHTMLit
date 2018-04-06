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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "LoginProcess";

    private FirebaseAuth mFirebaseAuth;

    private CallbackManager mCallbackManager;

    private TextView mUserNameTV;
    private ImageView mUserImageIV;
    private LoginButton mLoginButton;
    private ProgressDialog mProgressDialog;
    private LinearLayout mUserInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserNameTV = findViewById(R.id.tv_user_name);
        mUserImageIV = findViewById(R.id.iv_user_image);
        mUserInfoLayout = findViewById(R.id.linear_layout_user_info);
        findViewById(R.id.bt_log_out).setOnClickListener(this);

        Log.d(TAG, "Initialize Firebase Auth");
        mFirebaseAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "Initialize Facebook Login");
        mCallbackManager = CallbackManager.Factory.create();
        mLoginButton = findViewById(R.id.fb_login_button);
        mLoginButton.setReadPermissions("email", "public_profile");
        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess:" + loginResult);
                mLoginButton.setVisibility(View.GONE);
                handleAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel");
                updateUI(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError", error);
                updateUI(null);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleAccessToken(AccessToken accessToken) {
        showProgressDialog();

        Log.d(TAG, "AccessToken:" + accessToken);
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
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
                    }
                });
    }

    /**
     * Update the UI according to the FirebaseUser
     * @param user
     */
    private void updateUI(FirebaseUser user) {
        hideProgressDialog();

        Log.d(TAG, "Checking user");

        if (user != null) {
            Log.d(TAG, "user not null");

            //mLoginButton.setVisibility(View.GONE);
            mUserInfoLayout.setVisibility(View.VISIBLE);

            mUserNameTV.setText(user.getDisplayName());
            //String imageUrl = user.getPhotoUrl().toString();  // image size small
            String userId = "";
            for(UserInfo profile : user.getProviderData()){
                if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                    userId = profile.getUid();
                }
            }
            //type must be one of the following values: small, normal, album, large, square"
            String imageUrl =  "https://graph.facebook.com/" + userId + "/picture?type=large";

            Log.d(TAG, imageUrl);
            Picasso.with(this)
                    .load(imageUrl)
                    // image powered by Grace Baptist (http://gbchope.com/events-placeholder/)
                    .placeholder(R.drawable.placeholder)
                    .into(mUserImageIV);
        } else {
            Log.d(TAG, "user is null");

            mLoginButton.setVisibility(View.VISIBLE);
            mUserInfoLayout.setVisibility(View.GONE);
        }
    }

    public void signOut() {
        mFirebaseAuth.signOut();
        LoginManager.getInstance().logOut();
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
        if (i == R.id.bt_log_out) {
            signOut();
        }
    }
}