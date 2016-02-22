package com.free.dennisg.atlas;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.free.dennisg.atlas.settings.EditProfile;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLogoutListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MapOverview extends Activity implements OnMarkerClickListener{

    // Declare variables
    NodeList nodelist;
    ProgressDialog pDialog;

    String URL = "http://dennisgimbergsson.se/places_temp/places.php";
    String URL2 = "http://dennisgimbergsson.se/places_temp/get_unique_location.php";

    //all location strings
    String title_string, description_string, lng_string, lat_string, detailed_description_string, info_image_string, unique_id_string;

    //unique location strings
    String unique_title_string, unique_description_string, unique_lng_string, unique_lat_string, unique_detailed_description_string, unique_info_image_string, unique_unique_id_string;

    Double lng_double, lat_double;
    Location location;

    RelativeLayout marker_info;
    Button info_close;

    TextView info_title, info_description, info_created_date, info_username, info_type;
    ImageView info_image;

    private GoogleMap map;
    private Bitmap bmp;

    Double myLatitude = 0.00;
    Double myLongitude = 0.00;


    String marker_snippet_string;

    String result = null;
    InputStream is = null;

    String line = null;
    String picture, detailed_description, title, by_username, created_date;
    String type;
    int code;

    SharedPreferences sharedPrefs;
    SharedPreferences.Editor sharedPrefEdit;
    String signedInUser;
    Boolean isSingedIn;
    int userId;
    OnLogoutListener onLogoutListener;

    private SimpleFacebook mSimpleFacebook;
    Button FacebookLogoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_overview);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        isSingedIn = sharedPrefs.getBoolean("isSingedIn", false);
        signedInUser = sharedPrefs.getString("signedInUser", null);
        userId = sharedPrefs.getInt("userId", 0);

        marker_info = (RelativeLayout) findViewById(R.id.marker_info);
        info_title = (TextView) findViewById(R.id.info_title);
        info_description = (TextView) findViewById(R.id.info_description);
        info_username = (TextView) findViewById(R.id.info_username);
        info_type = (TextView) findViewById(R.id.info_type);
        info_image = (ImageView) findViewById(R.id.info_image);

        info_close = (Button) findViewById(R.id.info_close);
        info_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Animation slideDown = AnimationUtils.loadAnimation(MapOverview.this, R.anim.slide_down);

                marker_info.startAnimation(slideDown);
                marker_info.setVisibility(View.GONE);
            }
        });

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                myLatitude = location.getLatitude();
                myLongitude = location.getLongitude();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                myLatitude = location.getLatitude();
                myLongitude = location.getLongitude();
            }

            public void onProviderEnabled(String provider) {
                /*myLatitude = location.getLatitude();
                myLongitude = location.getLongitude();*/

                Toast.makeText(MapOverview.this, "Location enabled!", Toast.LENGTH_SHORT).show();

                /*LatLng coordinate = new LatLng(myLatitude, myLongitude);
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                map.animateCamera(yourLocation);*/
            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(MapOverview.this, "No Location Data!", Toast.LENGTH_SHORT).show();
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                MapOverview.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return;
            }
        }

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);

        new downloadLocationList().execute(URL);

        onLogoutListener = new OnLogoutListener() {

            @Override
            public void onLogout() {
                // change the state of the button or do whatever you want
                Intent loginIntent = new Intent(MapOverview.this, MainActivity.class);
                startActivity(loginIntent);
                finish();
            }

        };
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(MapOverview.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }

    // DownloadXML AsyncTask
    private class downloadLocationList extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapOverview.this);
            pDialog.setTitle("Loading locations...");
            pDialog.setMessage("Loading..");
            pDialog.setIndeterminate(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... Url) {
            try {
                URL url = new URL(Url[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                // Download the XML file
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
                // Locate the Tag Name
                nodelist = doc.getElementsByTagName("item");

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {

            for (int temp = 0; temp < nodelist.getLength(); temp++) {

                Node nNode = nodelist.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    lat_string = getNode("Latitude", eElement);
                    lat_double = Double.parseDouble(lat_string);

                    lng_string = getNode("Longitude", eElement);
                    lng_double = Double.parseDouble(lng_string);

                    title_string = getNode("Title", eElement);
                    description_string = getNode("Description", eElement);
                    detailed_description_string = getNode("DetailedDescription", eElement);
                    info_image_string = getNode("Picture", eElement);
                    unique_id_string = getNode("ID", eElement);

                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(lat_double, lng_double))
                            .title(title_string)
                            .snippet(unique_id_string)/*description_string*/
                            .flat(true));

                    map.setOnMarkerClickListener(new OnMarkerClickListener(){
                        @Override
                        public boolean onMarkerClick(Marker marker) {

                            marker.showInfoWindow();
                            marker_snippet_string = marker.getSnippet();

                            //Starts to download the json file for that specific location
                            new downloadUniqueLocation().execute();

                            return true;
                        }
                    });
                }
            }
            pDialog.dismiss();
        }
    }

    class downloadUniqueLocation extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
            values.add(new BasicNameValuePair("ID", marker_snippet_string));

            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL2);
                httppost.setEntity(new UrlEncodedFormEntity(values, HTTP.UTF_8));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(is);
                doc.getDocumentElement().normalize();
                is.close();
                Log.i("TAG", "Result Retrieved");

                // Locate the Tag Name
                nodelist = doc.getElementsByTagName("item");
                Log.e("TAG", "Connection Successful!");
            } catch (Exception e) {
                Log.e("TAG", e.toString());
                // Invalid Address
            }
            return null;

        }

        protected void onPostExecute(String onpost) {
            super.onPostExecute(onpost);

            for (int temp = 0; temp < nodelist.getLength(); temp++) {

                Node nNode = nodelist.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    title = getNode2("Title", eElement);
                    Log.e("TAG", "balle:" + title);
                    detailed_description = getNode("DetailedDescription", eElement);
                    picture = getNode2("Picture", eElement);
                    by_username = getNode2("ByUsername", eElement);
                    type = getNode2("Type", eElement);
                    created_date = getNode2("Created", eElement);

                    info_title.setText(title);
                    info_description.setText("Description: " + detailed_description);
                    info_username.setText("By: " + by_username);
                    RelativeTimeTextView v = (RelativeTimeTextView)findViewById(R.id.timestamp);
                    v.setReferenceTime(Long.parseLong(created_date));
                    info_type.setText("Type: " + type);
                    new getInfoPicture().execute(picture);
                    if (marker_info.getVisibility() == View.GONE) {
                        Animation slideUp = AnimationUtils.loadAnimation(MapOverview.this, R.anim.slide_up);
                        marker_info.startAnimation(slideUp);
                        marker_info.setVisibility(View.VISIBLE);
                    } else if (marker_info.getVisibility() == View.VISIBLE) {
                        //Do nothing
                        TimeZone timeZone = TimeZone.getDefault();
                        Log.e("TAG", ": " + timeZone.getDisplayName(true, TimeZone.SHORT, Locale.getDefault()));
                    }
                }
            }
        }
    }

    /*
    class downloadUniqueLocation extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();

            values.add(new BasicNameValuePair("ID", marker_snippet_string));

            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL2);
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
                title = (json.getString("title"));
                detailed_description = (json.getString("detailed_description"));
                picture = (json.getString("picture"));
                by_username = (json.getString("by_username"));
                type = (json.getString("type"));
                created_date = (json.getString("created_date"));

                if (code == 1) {
                    Toast.makeText(getApplicationContext(), "Error: 1", Toast.LENGTH_SHORT).show();
                }else {

                    title = title.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
                    info_title.setText(title);

                    detailed_description = detailed_description.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
                    info_description.setText("Description: " + detailed_description);

                    by_username = by_username.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
                    info_username.setText("By: " + by_username);

                    created_date = created_date.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
                    RelativeTimeTextView v = (RelativeTimeTextView)findViewById(R.id.timestamp);
                    v.setReferenceTime(Long.parseLong(created_date));

                    type = type.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
                    info_type.setText("Type: " + type);

                    picture = picture.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "").replace("\\", "");
                    new getInfoPicture().execute(picture);

                    if (marker_info.getVisibility() == View.GONE) {
                        Animation slideUp = AnimationUtils.loadAnimation(MapOverview.this, R.anim.slide_up);
                        marker_info.startAnimation(slideUp);
                        marker_info.setVisibility(View.VISIBLE);
                    } else if (marker_info.getVisibility() == View.VISIBLE) {
                        //Do nothing
                        TimeZone timeZone = TimeZone.getDefault();
                        Log.e("TAG IS HERE", timeZone.getDisplayName(true, TimeZone.SHORT, Locale.getDefault()));
                    }
                }

            } catch (Exception e) {
                Log.i("TAG", e.toString());
            }
        }
    }
    */

    private class getInfoPicture extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... url) {
            try {
                InputStream in = new URL(url[0]).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                // log error
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (bmp != null) {
                info_image.setImageBitmap(bmp);
            }
        }
    }

    private static String getNode(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }

    private static String getNode2(String sTag, Element eElement) {
        NodeList nlList2 = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue2 = (Node) nlList2.item(0);
        return nValue2.getNodeValue();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_overview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new downloadLocationList().execute(URL);
                return true;
            case R.id.action_zoom_to_me:
                LatLng coordinate = new LatLng(myLatitude, myLongitude);
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 17);
                map.animateCamera(yourLocation);
                return true;
            case R.id.action_clear:
                map.clear();
                return true;
            case R.id.action_add_place:
                Intent addPlaceIntent = new Intent(MapOverview.this, AddPlace.class);
                startActivity(addPlaceIntent);
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(MapOverview.this, EditProfile.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_sign_out:
                mSimpleFacebook.logout(onLogoutListener);

                sharedPrefEdit = sharedPrefs.edit();
                sharedPrefEdit.putBoolean("isSingedIn", false);
                sharedPrefEdit.putString("signedInUser", null);
                sharedPrefEdit.putInt("userId", 0);
                sharedPrefEdit.commit();
                return true;
            case R.id.action_get_profile:
                Intent getProfileIntent = new Intent(MapOverview.this, TestProfile.class);
                startActivity(getProfileIntent);
                return true;
            case R.id.action_private_map:
                Intent private_map_intent = new Intent(MapOverview.this, PrivateMapOverview.class);
                startActivity(private_map_intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}