package com.free.dennisg.atlas.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.free.dennisg.atlas.R;

public class Settings extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        final Preference userCountry = this.findPreference("pref_user_country");
        userCountry.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(Settings.this, "hej", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

/*
    @Override
    public void onBackPressed() {
        Toast.makeText(Settings.this, "Settings ", Toast.LENGTH_SHORT).show();
    }*/
}