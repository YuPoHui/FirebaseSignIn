package com.lucasyu.firebasesignin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "FirebaseSignIn";

    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private MyFirebase myFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        myFirebase = new MyFirebase(this);
        updateUI(myFirebase.currentUser);
    }


    private void setListener() {
        findViewById(R.id.signIn_btnGoogleLogin).setOnClickListener(btnLogInOnClickListener);
        findViewById(R.id.signIn_btnLogout).setOnClickListener(btnLogInOnClickListener);
    }

    private View.OnClickListener btnLogInOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.signIn_btnGoogleLogin:
                    Log.d(TAG, "Google Sign In");
                    signInGoogle();
                    break;
                case R.id.signIn_btnLogout:
                    Log.d(TAG, "User Sign out");
                    signOut();
                default:
                    break;
            }
        }
    };

    private void signInGoogle() {
        // Configure Google Sign In
        //try {
        //showProgress(true);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

        /*} finally {
            showProgress(false);
            Log.d(TAG, "Sign in user: " + bj_firebase.currentUser);
            updateUI(bj_firebase.currentUser);
        }*/
    }

    public void signOut() {
        try {
            showProgress(true);
            FirebaseAuth.getInstance().signOut();
        } finally {
            showProgress(false);
            updateUI(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgress(true);
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        myFirebase.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            myFirebase = new MyFirebase(MainActivity.this);
                            updateUI(myFirebase.currentUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        showProgress(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        showProgress(false);

        TextView txtEmail = findViewById(R.id.signIn_txtUserEmail);
        if (user != null) {
            Log.d(TAG, "user email: " + user.getEmail());
            Log.d(TAG, "user UID: " + user.getUid());
            txtEmail.setText(getString(R.string.userEmail, user.getEmail()));

            findViewById(R.id.signIn_btnGoogleLogin).setVisibility(View.GONE);
            txtEmail.setVisibility(View.VISIBLE);


        } else {
            txtEmail.setText(getString(R.string.userEmail, ""));

            findViewById(R.id.signIn_btnGoogleLogin).setVisibility(View.VISIBLE);
            txtEmail.setVisibility(View.GONE);
        }
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        findViewById(R.id.login_linearLayout).setVisibility(show ? View.GONE : View.VISIBLE);
        findViewById(R.id.login_linearLayout).animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.login_linearLayout).setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        findViewById(R.id.progress).setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.progress).animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.progress).setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
