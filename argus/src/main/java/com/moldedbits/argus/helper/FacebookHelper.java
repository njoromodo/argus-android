package com.moldedbits.argus.helper;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.moldedbits.argus.listener.LoginListener;
import com.moldedbits.argus.model.ArgusUser;

import org.json.JSONObject;

import java.util.List;

public class FacebookHelper {
    private AccessToken token;
    private CallbackManager callbackManager;
    private LoginListener loginListener;

    public FacebookHelper(LoginListener loginListener) {
        this.loginListener = loginListener;
        callbackManager = CallbackManager.Factory.create();
    }

    public void initialize() {
        LoginManager.getInstance()
                .registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        GraphRequest graphRequest = GraphRequest
                                .newMeRequest(loginResult.getAccessToken()
                                        , new GraphRequest.GraphJSONObjectCallback() {
                                            @Override
                                            public void onCompleted(JSONObject object,
                                                                    GraphResponse response) {
                                                token = AccessToken.getCurrentAccessToken();
                                                if (loginListener != null) {
                                                    loginListener.onSuccess(
                                                            new ArgusUser("Facebook"));
                                                }
                                            }
                                        });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "public_profile");
                        graphRequest.setParameters(parameters);
                        graphRequest.executeAsync();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {
                        loginListener.onFailure(error.getMessage());
                        Log.d("FACEBOOK", error.getMessage());
                    }
                });
    }


    public void logout() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return;
            // already logged out
        }
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/",
                         null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                LoginManager.getInstance().logOut();
            }
        }).executeAsync();
    }

    public CallbackManager getCallbackManager() {
        return callbackManager;
    }

    public void initiateLogin(Fragment fragment, List<String> faceBookPermissions) {
        LoginManager.getInstance()
                .logInWithReadPermissions(fragment, faceBookPermissions);
    }
}

