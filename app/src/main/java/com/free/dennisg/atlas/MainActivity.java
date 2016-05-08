package com.free.dennisg.atlas;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.DefaultAudience;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.listeners.OnLoginListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
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
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import se.simbio.encryption.Encryption;

public class MainActivity extends Activity {

    // Declare variables
    NodeList nodelist;
    ProgressDialog pDialog;

    //URL to get the location
    String URL = "https://dennisgimbergsson.se/atlas-backend/places.php";
    //URL to send the login info to
    String LoginURL = "https://dennisgimbergsson.se/atlas-backend/login.php";

    String lng_string, lat_string;

    Double lng_double, lat_double;

    private GoogleMap map;

    Button FacebookLoginBtn, LoginButton;
    EditText email, password;
    ProgressBar FacebookLoadingSpinner;

    SimpleFacebook mSimpleFacebook;
    Encryption encryption;

    InputStream is = null;
    String line = null;
    String result = null;
    int code;
    String username;
    String id_string;
    int id_int;

    SharedPreferences sharedPrefs;
    SharedPreferences.Editor sharedPrefEdit;
    String signedInUser;
    Boolean isSingedIn;
    int userId;

    int MY_PERMISSIONS_REQUEST_GET_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // Explain to the user why we need to read the contacts
                    Toast.makeText(this, "Allow, or some features might be disabled.", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_GET_LOCATION);
                return;
            }
        }

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        isSingedIn = sharedPrefs.getBoolean("isSingedIn", false);
        signedInUser = sharedPrefs.getString("signedInUser", null);
        userId = sharedPrefs.getInt("userId", 0);

        if (isSingedIn.equals(true)){
            Intent mapOverviewIntent = new Intent(MainActivity.this, MapOverview.class);
            startActivity(mapOverviewIntent);
            finish();
        }else if(isSingedIn.equals(false)){
            //Do nothing
        }

        //Define XML layout
        setContentView(R.layout.main_activity);

        TextView createAccount = (TextView) findViewById(R.id.create_one);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapOverviewIntent = new Intent(MainActivity.this, CreateAccount.class);
                startActivity(mapOverviewIntent);
            }
        });

        /**
         **TODO NOT USE THIS KEY AND SALT FOR A RELEASE BUILD
         **/
        encryption = Encryption.getDefault("MyKey", "MySalt", new byte[16]);

        mSimpleFacebook = SimpleFacebook.getInstance(MainActivity.this);

        //Check if already singed in to FB
        if (mSimpleFacebook.isLogin()){
            Intent mapOverviewIntent = new Intent(MainActivity.this, MapOverview.class);
            startActivity(mapOverviewIntent);
            finish();
        }

        //Set map position and get markers
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        LatLng coordinate = new LatLng(59.329444, 18.068611);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 8);
        map.animateCamera(yourLocation);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        new downloadLocationList().execute(URL);

        //Set FB Permission and configure it
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

        FacebookLoadingSpinner = (ProgressBar) findViewById(R.id.fb_progressbar);
        FacebookLoadingSpinner.setVisibility(View.GONE);
        FacebookLoginBtn = (Button) findViewById(R.id.fb_login_button);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        LoginButton = (Button) findViewById(R.id.login_button);
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Checks if the email is valid
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                    new loginToApp().execute();
                } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                    email.setError("Invalid e-mail");
                }
            }
        });

        setLogin();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Nice, all features can be used now", Toast.LENGTH_SHORT).show();
                // permission was granted, yay!
                Intent mapOverviewIntent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(mapOverviewIntent);
                finish();
            } else {
                Toast.makeText(this, "Quitting, all of the features can't be used.", Toast.LENGTH_SHORT).show();
                finish();
            }
    }

    //Login using facebook
    private void setLogin() {
        // Login listener
        final OnLoginListener onLoginListener = new OnLoginListener() {

            public void onFail(String reason) {
                Log.w("TAG", "Failed to login");
                FacebookLoadingSpinner.setVisibility(View.GONE);
                FacebookLoginBtn.setEnabled(true);
            }

            public void onException(Throwable throwable) {
                Log.e("TAG", "Bad thing happened", throwable);
                FacebookLoadingSpinner.setVisibility(View.GONE);
                FacebookLoginBtn.setEnabled(true);
            }

            /*public void onThinking() {
                FacebookLoadingSpinner.setVisibility(View.VISIBLE);
                FacebookLoginBtn.setEnabled(false);
            }*/

            public void onLogin(String accessToken, List<Permission> acceptedPermissions, List<Permission> declinedPermissions) {
                FacebookLoadingSpinner.setVisibility(View.GONE);
                Intent createAccountIntent = new Intent(MainActivity.this, CreateAccount.class);
                startActivity(createAccountIntent);
                finish();
            }

            /*public void onNotAcceptingPermissions(PermissionType type) {
                FacebookLoadingSpinner.setVisibility(View.GONE);
                FacebookLoginBtn.setEnabled(true);
                Toast.makeText(MainActivity.this, String.format("You didn't accept %s permissions", type.name()), Toast.LENGTH_SHORT).show();
            }*/

            public void onCancel(){

            }
        };

        FacebookLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                FacebookLoadingSpinner.setVisibility(View.VISIBLE);
                FacebookLoginBtn.setEnabled(false);
                mSimpleFacebook.login(onLoginListener);
            }
        });
    }

    //Login using a existing user
    class loginToApp extends AsyncTask<String, String, String> {

        String encrypted_password = encryption.encryptOrNull(password.getText().toString());
        String getEmailString = email.getText().toString();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setTitle("Login in");
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();

            values.add(new BasicNameValuePair("Email", getEmailString));
            values.add(new BasicNameValuePair("EncryptedPassword", encrypted_password));

            try {

                HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

                DefaultHttpClient client = new DefaultHttpClient();

                SchemeRegistry registry = new SchemeRegistry();
                SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
                Log.e("HOSTNAME", socketFactory.getHostnameVerifier().toString());
                socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
                SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
                DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());

                HttpPost httppost = new HttpPost(LoginURL);
                httppost.setEntity(new UrlEncodedFormEntity(values));
                HttpResponse response = httpClient.execute(httppost);
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
                Log.e("TAG", "Result Retrieved");
            } catch (Exception e) {
                Log.e("TAG", e.toString());
            }
            return null;
        }

        protected void onPostExecute(String onpost) {
            super.onPostExecute(onpost);

            try {
                JSONObject json = new JSONObject(result);
                code = (json.getInt("code"));
                username = (json.getString("username"));
                id_string = (json.getString("id"));

                if (code == 5) {
                    // Data Success
                    // Do login stuff here...
                    username = username.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
                    id_string = id_string.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
                    Log.e("TAG", id_string);

                    id_int = Integer.parseInt(id_string);

                    Toast.makeText(MainActivity.this, "Welcome back, " + username, Toast.LENGTH_SHORT).show();

                    sharedPrefEdit = sharedPrefs.edit();
                    sharedPrefEdit.putBoolean("isSingedIn", true);
                    sharedPrefEdit.putString("signedInUser", username);
                    sharedPrefEdit.putInt("userId", id_int);
                    sharedPrefEdit.commit();

                    Intent mapOverviewIntent = new Intent(MainActivity.this, MapOverview.class);
                    startActivity(mapOverviewIntent);
                    finish();
                }else if (code == 4){
                    Toast.makeText(MainActivity.this, "Wrong Email or Password", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Wrong " + code, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.i("TAG", e.toString());
            }
            pDialog.dismiss();
        }
    }

    // DownloadXML AsyncTask
    private class downloadLocationList extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
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
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();
                nodelist = doc.getElementsByTagName("item");

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            if (nodelist != null) {
                for (int temp = 0; temp < nodelist.getLength(); temp++) {
                    Node nNode = nodelist.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        lat_string = getNode("Latitude", eElement);
                        lat_double = Double.parseDouble(lat_string);

                        lng_string = getNode("Longitude", eElement);
                        lng_double = Double.parseDouble(lng_string);

                        map.addMarker(new MarkerOptions().position(new LatLng(lat_double, lng_double)));
                    }
                }
                pDialog.dismiss();
            }
        }
    }

    // getNode function
    private static String getNode(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}