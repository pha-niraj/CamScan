package com.example.phaniraj.camscan;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.intsig.csopen.sdk.CSOpenApiFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private ImageView imageView;
    private TextView textView;
    private CallbackManager callbackManager;
    private CamScanAcessActivity camScanAcessActivity;
    private Button camScanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.checkDir(CamScanAcessActivity.DIR_IMAGE);


        camScanAcessActivity = new CamScanAcessActivity(this,CSOpenApiFactory.createCSOpenApi(this, "KLPt0gTtYUyP0fTXV8aH44e7" , null));

        loginButton = findViewById(R.id.login_button);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_view);
        camScanButton = findViewById(R.id.camscan_button);

        updateUI(AccessToken.getCurrentAccessToken());

        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions(Arrays.asList("email","public_profile"));

        camScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camScanAcessActivity.goToGallery();
            }
        });


        new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                updateUI(currentAccessToken);
            }
        };


    }

    private void updateUI(AccessToken accessToken){
        if (accessToken==null){
            textView.setText(null);
            imageView.setImageResource(0);
            camScanButton.setVisibility(View.GONE);
        }
        else {
            getData(accessToken);
            camScanButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
        camScanAcessActivity.handleActivityResult(requestCode,resultCode,data);
    }

    private void getData(AccessToken accessToken){
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String name = object.getString("first_name")+ " "+object.getString("last_name");
                    String url = "https://graph.facebook.com/"+object.getString("id")+"/picture?type=normal";
                    textView.setText(name);
                    Glide.with(MainActivity.this).load(url).into(imageView);
                }catch (Exception e){

                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
