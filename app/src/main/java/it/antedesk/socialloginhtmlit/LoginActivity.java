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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "LoginProcess";
    //Request Code (RC) for Google Log in
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mFirebaseAuth;

    private GoogleSignInClient mGoogleSignInClient;

    private TextView mUserNameTV;
    private ImageView mUserImageIV;
    private ProgressDialog mProgressDialog;
    private LinearLayout mUserInfoLayout;
    private SignInButton mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserNameTV = findViewById(R.id.tv_user_name);
        mUserImageIV = findViewById(R.id.iv_user_image);
        mUserInfoLayout = findViewById(R.id.linear_layout_user_info);
        mLoginButton = findViewById(R.id.bt_log_in);

        mLoginButton.setOnClickListener(this);
        mLoginButton.setSize(SignInButton.SIZE_WIDE);
        findViewById(R.id.bt_log_out).setOnClickListener(this);

        Log.d(TAG, "Initialize Firebase Auth");
        mFirebaseAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "Configure Google Log in");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        Log.d(TAG, "Initialize Google log in");
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "FirebaseAuth with Google:" + account.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
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

    private void updateUI(FirebaseUser user) {
        Log.d(TAG, "Checking user");
        if (user != null) {
            Log.d(TAG, "user not null");

            mLoginButton.setVisibility(View.GONE);
            mUserInfoLayout.setVisibility(View.VISIBLE);

            mUserNameTV.setText(user.getDisplayName());
            String imageUrl = user.getPhotoUrl().toString();  // image size small
            // workaround to get a bigger image for our imageview
            imageUrl = imageUrl.replace("/s96-c/","/s200-c/");

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

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        // Firebase sign out
        mFirebaseAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
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
            signIn();
        } else if (i == R.id.bt_log_out) {
            signOut();
        }
    }

}
