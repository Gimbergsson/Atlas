package com.free.dennisg.atlas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.SessionDefaultAudience;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnLogoutListener;

public class About extends Activity {

    private SimpleFacebook mSimpleFacebook;

    Button FacebookLoginBtn, FacebookLogoutBtn;
    TextView StatusTxt;
    String TAG = "Places Debug TAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Permission[] permissions = new Permission[] {
                Permission.PUBLIC_PROFILE,
                Permission.EMAIL
        };

        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId("748966228551478")
                .setNamespace("places_")
                .setPermissions(permissions)
                .setDefaultAudience(SessionDefaultAudience.FRIENDS)
                .setAskForAllPermissionsAtOnce(false)
                .build();

        SimpleFacebook.setConfiguration(configuration);

        FacebookLoginBtn = (Button) findViewById(R.id.fb_login_button);
        FacebookLogoutBtn = (Button) findViewById(R.id.logout_button);
        StatusTxt = (TextView) findViewById(R.id.status_text);

        setLogin();
        setLogout();
    }

    private void setLogin() {
        // Login listener
        final OnLoginListener onLoginListener = new OnLoginListener() {

            @Override
            public void onFail(String reason) {
                StatusTxt.setText(reason);
                Log.w(TAG, "Failed to login");
            }

            @Override
            public void onException(Throwable throwable) {
                StatusTxt.setText("Exception: " + throwable.getMessage());
                Log.e(TAG, "Bad thing happened", throwable);
            }

            @Override
            public void onThinking() {
                // show progress bar or something to the user while login is
                // happening
                StatusTxt.setText("Thinking...");
            }

            @Override
            public void onLogin() {
                // change the state of the button or do whatever you want
                StatusTxt.setText("Logged in");
                loggedInUIState();
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                StatusTxt.setText("Logged out");
                Toast.makeText(About.this, String.format("You didn't accept %s permissions", type.name()), Toast.LENGTH_SHORT).show();
            }
        };

        FacebookLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSimpleFacebook.login(onLoginListener);
            }
        });
    }

    /**
     * Logout example
     */
    private void setLogout() {
        final OnLogoutListener onLogoutListener = new OnLogoutListener() {

            @Override
            public void onFail(String reason) {
                StatusTxt.setText(reason);
                Log.w(TAG, "Failed to login");
            }

            @Override
            public void onException(Throwable throwable) {
                StatusTxt.setText("Exception: " + throwable.getMessage());
                Log.e(TAG, "Bad thing happened", throwable);
            }

            @Override
            public void onThinking() {
                // show progress bar or something to the user while login is
                // happening
                StatusTxt.setText("Thinking...");
            }

            @Override
            public void onLogout() {
                // change the state of the button or do whatever you want
                StatusTxt.setText("Logged out");
                loggedOutUIState();
            }

        };
    }

    private void setUIState() {
        if (mSimpleFacebook.isLogin()) {
            loggedInUIState();
        } else {
            loggedOutUIState();
        }
    }

    private void loggedInUIState() {
        FacebookLoginBtn.setEnabled(false);
        FacebookLogoutBtn.setEnabled(true);
        StatusTxt.setText("Logged in");
    }

    private void loggedOutUIState() {
        FacebookLoginBtn.setEnabled(true);
        FacebookLogoutBtn.setEnabled(false);
        StatusTxt.setText("Logged out");
    }

    @Override
    public void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}