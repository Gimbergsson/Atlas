package com.free.dennisg.atlas;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("deprecation")
public class PrivateAddPlace extends Activity {

    String title_text = null;
    String description_text = null;
    //String lat_text = null;
    //String lng_text = null;
    String result = null;

    String lat_double = null;
    String lng_double = null;
    String URL;

    InputStream is = null;

    String line = null;
    int code;

    EditText title = null;
    EditText description = null;
    TextView lat = null;
    TextView lng = null;
    Button submit = null;

    Context context = null;
    Location location;

    SharedPreferences sharedPrefs;
    SharedPreferences.Editor sharedPrefEdit;
    String signedInUser;
    Boolean isSingedIn;
    String createdTimestamp;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Define XML layout
        setContentView(R.layout.add_place);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        isSingedIn = sharedPrefs.getBoolean("isSingedIn", false);
        signedInUser = sharedPrefs.getString("signedInUser", null);
        userId = sharedPrefs.getInt("userId", 0);

        context = this;

        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        lat = (TextView) findViewById(R.id.lat);
        lng = (TextView) findViewById(R.id.lng);
        submit = (Button) findViewById(R.id.submit);

        TextView time = (TextView) findViewById(R.id.time);
        time.setText(String.valueOf(new Date().getTime()));

        //RelativeTimeTextView v = (RelativeTimeTextView)findViewById(R.id.timestamp);
        //v.setReferenceTime(getCurrentTime - (10*60*1000));

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PrivateAddPlace.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                location = locationManager.getLastKnownLocation(provider);
                return;
            }
        }

        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                title_text = title.getText().toString();
                description_text = description.getText().toString();

                createdTimestamp = String.valueOf(new Date().getTime());

                lat_double = String.valueOf(location.getLatitude());
                lng_double = String.valueOf(location.getLongitude());

                lat.setText(lat_double);
                lng.setText(lng_double);

                URL = "http://dennisgimbergsson.se/places_temp/user_locations/" + String.valueOf(userId) +"/private_add_location_id_" + String.valueOf(userId) + ".php";

                //lat_text = lat.getText().toString();
                //lng_text = lng.getText().toString();
                new addPlaceTask().execute();
            }
        });

    }

    class addPlaceTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();

            values.add(new BasicNameValuePair("UserID", String.valueOf(userId)));
            values.add(new BasicNameValuePair("ByUsername", signedInUser));
            values.add(new BasicNameValuePair("Title", title_text));
            values.add(new BasicNameValuePair("Description", description_text));
            values.add(new BasicNameValuePair("DetailedDescription", "TODO-detailedDescription_string"));
            values.add(new BasicNameValuePair("Picture", "TODO-picture_string"));
            values.add(new BasicNameValuePair("Type", "1"));
            values.add(new BasicNameValuePair("Created", createdTimestamp));
            values.add(new BasicNameValuePair("Latitude", lat_double));
            values.add(new BasicNameValuePair("Longitude", lng_double));

            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL);
                httppost.setEntity(new UrlEncodedFormEntity(values));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();

                Log.i("TAG", "Connection Successful!");
            } catch (Exception e) {
                Log.i("TAG", e.toString());
                // Invalid Address
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                Log.i("TAG", "Result Retrieved");
            } catch (Exception e) {
                Log.i("TAG", e.toString());
            }
            return null;
        }

        protected void onPostExecute(String onpost) {
            super.onPostExecute(onpost);

            try {
                JSONObject json = new JSONObject(result);
                code = (json.getInt("code"));
                if (code == 1) {
                    // Data Successfully Inserted
                    //finish();
                } else {
                    new AlertDialog.Builder(PrivateAddPlace.this)
                            .setTitle("Error " + code)
                            .setMessage("Something went wrong. Have you filled in every field?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            } catch (Exception e) {
                Log.i("TAG", e.toString());
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_place, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_save:
                new addPlaceTask().execute("");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
