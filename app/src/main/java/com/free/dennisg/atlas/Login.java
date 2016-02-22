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

import com.facebook.login.DefaultAudience;
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
                Permission.EMAIL,
                Permission.USER_WEBSITE
        };

        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId("748966228551478")
                .setNamespace("places_")
                .setPermissions(permissions)
                .setDefaultAudience(DefaultAudience.FRIENDS)
                .setAskForAllPermissionsAtOnce(false)
                .build();

        SimpleFacebook.setConfiguration(configuration);

        FacebookLogoutBtn = (Button) findViewById(R.id.logout_button);
        StatusTxt = (TextView) findViewById(R.id.status_text);

        final OnLogoutListener onLogoutListener = new OnLogoutListener() {
            @Override
            public void onLogout() {
                // change the state of the button or do whatever you want
                StatusTxt.setText("Logged out");
                Log.i(TAG, "You are logged out");
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
        mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}