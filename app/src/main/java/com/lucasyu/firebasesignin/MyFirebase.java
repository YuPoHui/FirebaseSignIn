package com.lucasyu.firebasesignin;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyFirebase {
    private Context mCtx;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    MyFirebase(Context context){
        FirebaseApp.initializeApp(context);
        this.mCtx = context;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }
}
