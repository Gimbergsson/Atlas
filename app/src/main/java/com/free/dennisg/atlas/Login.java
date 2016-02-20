package com.free.dennisg.atlas;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.SessionDefaultAudience;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.listeners.OnLogoutListener;

public class Login extends Activity {

    private SimpleFacebook mSimpleFacebook;

    Button FacebookLogoutBtn;
    TextView StatusTxt;
    String TAG = "ATLAS";

    SharedPreferences sharedPrefs;
    SharedPreferences.Editor sharedPrefEdit;
    String signedInUser;
    Boolean isSingedIn;
    int userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        isSingedIn = sharedPrefs.getBoolean("isSingedIn", false);
        signedInUser = sharedPrefs.getString("signedInUser", null);
        userId = sharedPrefs.getInt("userId", 0);

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

        FacebookLogoutBtn = (Button) findViewById(R.id.logout_button);
        StatusTxt = (TextView) findViewById(R.id.status_text);

        setLogout();
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

        FacebookLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSimpleFacebook.logout(onLogoutListener);

                sharedPrefEdit = sharedPrefs.edit();
                sharedPrefEdit.putBoolean("isSingedIn", false);
                sharedPrefEdit.putString("signedInUser", null);
                sharedPrefEdit.putInt("userId", 0);
                sharedPrefEdit.commit();

                finish();
            }
        });
    }

    private void setUIState() {
        if (mSimpleFacebook.isLogin()) {
            loggedInUIState();
        } else {
            loggedOutUIState();
        }
    }

    private void loggedInUIState() {
        FacebookLogoutBtn.setEnabled(true);
        StatusTxt.setText("Logged in!");
    }

    private void loggedOutUIState() {
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