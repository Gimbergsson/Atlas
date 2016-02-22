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

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONObject;
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

public class PrivateMapOverview extends Activity implements OnMarkerClickListener{

    // Declare variables
    NodeList nodelist;
    ProgressDialog pDialog;

    String LocationListURL, UniqueLocationURL;

    String title_string, description_string, lng_string, lat_string, detailed_description_string, info_image_string, unique_id_string;

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
    String signedInUser;
    Boolean isSingedIn;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_map_overview);

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
                Animation slideDown = AnimationUtils.loadAnimation(PrivateMapOverview.this, R.anim.slide_down);

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

                Toast.makeText(PrivateMapOverview.this, "Location enabled!", Toast.LENGTH_SHORT).show();

                /*LatLng coordinate = new LatLng(myLatitude, myLongitude);
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
                map.animateCamera(yourLocation);*/
            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(PrivateMapOverview.this, "No Location Data!", Toast.LENGTH_SHORT).show();
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PrivateMapOverview.this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                return;
            }
        }

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);


        String userId_string = String.valueOf(userId);
        LocationListURL     = "http://dennisgimbergsson.se/places_temp/user_locations/" + userId_string + "/private_location_list_id_" + userId_string + ".php";
        UniqueLocationURL   = "http://dennisgimbergsson.se/places_temp/user_locations/" + userId_string + "/private_get_unique_location_id_" + userId_string + ".php";

        new downloadLocationList().execute(LocationListURL);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(PrivateMapOverview.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }

    // DownloadXML AsyncTask
    private class downloadLocationList extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PrivateMapOverview.this);
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

            values.add(new BasicNameValuePair("UserID", String.valueOf(userId)));
            values.add(new BasicNameValuePair("ID", marker_snippet_string));

            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(UniqueLocationURL);
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
                        Animation slideUp = AnimationUtils.loadAnimation(PrivateMapOverview.this, R.anim.slide_up);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.private_map_overview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new downloadLocationList().execute(LocationListURL);
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
                Intent addPlaceIntent = new Intent(PrivateMapOverview.this, PrivateAddPlace.class);
                startActivity(addPlaceIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}