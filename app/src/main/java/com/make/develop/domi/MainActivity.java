package com.make.develop.domi;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.make.domi_java.Remote.ICloudFunctions;
import com.make.domi_java.Remote.RetrofitCloudClient;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171; //any number
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ICloudFunctions cloudFunctions;


    @Override
    protected void onStart(){

        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override

    protected void  onStop(){
        if(listener != null){
            firebaseAuth.removeAuthStateListener(listener);
            compositeDisposable.clear();
        }
        super.onStop();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }
    private void init(){
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions.class);
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //already login

                    Toast.makeText(MainActivity.this,"already login",Toast.LENGTH_SHORT).show();


                }else{
                    //Not login
                    AccessToken accessToken = AccountKit.getCurrentAccessToken();
                    if(accessToken != null){
                        getCustomToken(accessToken);
                    }else{
                        phoneLogin();
                    }
                }
            }
        };
    }
}
