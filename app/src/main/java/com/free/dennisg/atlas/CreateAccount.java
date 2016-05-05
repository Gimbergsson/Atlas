package com.free.dennisg.atlas;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.lib.recaptcha.ReCaptcha;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.sromku.simple.fb.utils.Attributes;
import com.sromku.simple.fb.utils.PictureAttributes;

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
import java.net.URL;
import java.util.ArrayList;

import se.simbio.encryption.Encryption;

@SuppressWarnings("deprecation")
public class CreateAccount extends Activity implements ReCaptcha.OnShowChallengeListener, ReCaptcha.OnVerifyAnswerListener {

    ProgressDialog pDialog;

    SimpleFacebook mSimpleFacebook;

    String URL = "http://dennisgimbergsson.se/atlas-backend/create_account.php";

    InputStream is = null;
    String line = null;
    String result = null;
    int code;

    EditText username = null, email = null, password = null;
    CheckBox skip_password;
    String fb_id = null, fb_first_name = null, fb_last_name = null, fb_email = null, fb_profile_pic = null;
    TextView fb_id_txt, fb_name_txt, fb_email_txt, fb_using_txt, fb_only_pw;
    ImageView fb_profile_img;
    Button submit_details;
    Encryption encryption;
    private Bitmap bmp;

    OnLogoutListener onLogoutListener;

    //Chaptcha stuff below
    EditText chaptchaAnswer;
    ReCaptcha reCaptcha;
    Dialog captchaDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);

        encryption = Encryption.getDefault("MyKey", "MySalt", new byte[16]);

        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        skip_password = (CheckBox) findViewById(R.id.skip_password);
        submit_details = (Button) findViewById(R.id.submit_details);

        fb_id_txt = (TextView) findViewById(R.id.fb_id);
        fb_email_txt = (TextView) findViewById(R.id.fb_email);
        fb_name_txt = (TextView) findViewById(R.id.fb_name);
        fb_profile_img = (ImageView) findViewById(R.id.fb_img);
        fb_using_txt = (TextView) findViewById(R.id.fb_using_txt);
        fb_only_pw = (TextView) findViewById(R.id.fb_only_pw_txt);

        PictureAttributes pictureAttributes = Attributes.createPictureAttributes();
        pictureAttributes.setHeight(250);
        pictureAttributes.setWidth(250);
        pictureAttributes.setType(PictureAttributes.PictureType.LARGE);

        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.ID)
                .add(Profile.Properties.EMAIL)
                .add(Profile.Properties.FIRST_NAME)
                .add(Profile.Properties.LAST_NAME)
                .add(Profile.Properties.PICTURE, pictureAttributes)
                .build();

        SimpleFacebook.getInstance(CreateAccount.this).getProfile(properties, new OnProfileListener() {
            @Override
            public void onFail(String reason) {
                Log.e("TAG", "Failed to login " + reason);
                fb_id_txt.setVisibility(View.GONE);
                fb_email_txt.setVisibility(View.GONE);
                fb_name_txt.setVisibility(View.GONE);
                fb_profile_img.setVisibility(View.GONE);
                skip_password.setVisibility(View.GONE);
                fb_using_txt.setVisibility(View.GONE);
                fb_only_pw.setVisibility(View.GONE);
            }

            @Override
            public void onException(Throwable throwable) {
                Log.e("TAG", "Bad thing happened", throwable);
            }

            @Override
            public void onThinking() {
                // show progress bar or something to the user while login is happening
            }

            @Override
            public void onComplete(Profile profile) {
                fb_id_txt.setText(profile.getId());
                fb_email_txt.setText(profile.getEmail());
                fb_name_txt.setText(profile.getFirstName() + " " + profile.getLastName());

                fb_id = profile.getId();
                fb_email = profile.getEmail();
                fb_first_name = profile.getFirstName();
                fb_last_name = profile.getLastName();
                fb_profile_pic = profile.getPicture();

                new getProfilePicture().execute(profile.getPicture());

                if (fb_email.equals("null")) {
                    email.setEnabled(true);
                } else {
                    email.setEnabled(false);
                    email.setText(fb_email);
                }
            }

        });

        skip_password.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (password.isEnabled()) {
                    password.setEnabled(false);
                } else if (password.isEnabled() == false) {
                    password.setEnabled(true);
                }
            }
        });

        submit_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //Checks if the email is valid
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {

                    //Chaptcha dialog
                    captchaDialog = new Dialog(CreateAccount.this);
                    captchaDialog.setContentView(R.layout.captcha_dialog);
                    captchaDialog.setTitle("Please enter captcha");

                    reCaptcha = (ReCaptcha) captchaDialog.findViewById(R.id.recaptcha);
                    reCaptcha.showChallengeAsync("6LfhQwsTAAAAAL-OKNTUPccd13dyQtwwd09uWFJb", CreateAccount.this);

                    chaptchaAnswer = (EditText) captchaDialog.findViewById(R.id.answer);

                    Button captchaVerify = (Button) captchaDialog.findViewById(R.id.verify);
                    captchaVerify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //dialog.dismiss();
                            reCaptcha.verifyAnswerAsync("6LfhQwsTAAAAALJvjUqiurjAdzi4dztuUIGq-NyZ", chaptchaAnswer.getText().toString(), CreateAccount.this);
                        }
                    });
                    Button captchaReload = (Button) captchaDialog.findViewById(R.id.reload);
                    captchaReload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            reCaptcha.showChallengeAsync("6LfhQwsTAAAAAL-OKNTUPccd13dyQtwwd09uWFJb", CreateAccount.this);
                        }
                    });
                    captchaDialog.show();

                } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                    email.setError("Invalid e-mail");
                }
            }
        });

        onLogoutListener = new OnLogoutListener() {

            @Override
            public void onLogout() {
                // change the state of the button or do whatever you want
                Intent loginIntent = new Intent(CreateAccount.this, MainActivity.class);
                startActivity(loginIntent);
                finish();
            }

        };
    }


    @Override
    public void onChallengeShown(final boolean shown) {
        //Do stuff when the captcha is visible
    }

    @Override
    public void onAnswerVerified(final boolean success) {
        if (success) {
            Toast.makeText(CreateAccount.this, "Verification success", Toast.LENGTH_SHORT).show();
            if (skip_password.isChecked() == false && password.length() >= 4) {

                String skip_password_boolean = "false";
                String email_string = email.getText().toString();
                String username_string = username.getText().toString();
                String unencrypted_password = password.getText().toString();


                new createAccount().execute(skip_password_boolean, email_string, username_string, unencrypted_password);
                captchaDialog.dismiss();

            } else if (skip_password.isChecked() == true) {

                new createAccount().execute();
                captchaDialog.dismiss();

            } else if (skip_password.isChecked() == false && password.length() < 4) {

                password.setError("At least 4 characters");

            }
        } else {
            Toast.makeText(CreateAccount.this, "Verification failed", Toast.LENGTH_SHORT).show();
            // (Optional) Shows the next CAPTCHA
            reCaptcha.showChallengeAsync("6LfhQwsTAAAAAL-OKNTUPccd13dyQtwwd09uWFJb", CreateAccount.this);
        }
    }

    private class getProfilePicture extends AsyncTask<String, String, String> {

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
                fb_profile_img.setImageBitmap(bmp);
            }
        }
    }

    private class createAccount extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CreateAccount.this);
            pDialog.setTitle("Loading locations...");
            pDialog.setMessage("Loading..");
            pDialog.setIndeterminate(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();

            //Test
            String skip_password_checkbox = args[0];
            String email = args[1];
            String username = args[2];
            String unencrypted_password = args[3];

            String encrypted_password = encryption.encryptOrNull(unencrypted_password).toString();

            if (fb_id == null || fb_first_name == null || fb_last_name == null || fb_profile_pic == null){
                values.add(new BasicNameValuePair("FacebookID", "not_applicable"));
                values.add(new BasicNameValuePair("FirstName", "not_applicable"));
                values.add(new BasicNameValuePair("LastName", "not_applicable"));
                values.add(new BasicNameValuePair("Picture", "not_applicable"));
            }else{
                values.add(new BasicNameValuePair("FacebookID", fb_id));
                values.add(new BasicNameValuePair("FirstName", fb_first_name));
                values.add(new BasicNameValuePair("LastName", fb_last_name));
                values.add(new BasicNameValuePair("Picture", fb_profile_pic));
            }
            values.add(new BasicNameValuePair("Email", email));
            if (skip_password_checkbox.equals("true")){
                values.add(new BasicNameValuePair("EncryptedPassword", "not_applicable"));
            }else if(skip_password_checkbox.equals("false")){
                values.add(new BasicNameValuePair("EncryptedPassword", encrypted_password));
            }
            values.add(new BasicNameValuePair("Username", username));
            values.add(new BasicNameValuePair("AcceptedToS", "1"));

            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL);
                httppost.setEntity(new UrlEncodedFormEntity(values));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();

                Log.e("TAG", "Connection Successful");
            } catch (Exception e) {
                Log.e("TAG", e.toString() + "line 238");
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
                Log.e("TAG", e.toString() + "line 252");
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
                    Log.e("TAG", "Data inserted");
                }else if (code == 5) {
                    Toast.makeText(CreateAccount.this, "Your account has been created!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(CreateAccount.this, "You may login now.", Toast.LENGTH_LONG).show();
                    Intent mainActivityIntent = new Intent(CreateAccount.this, MainActivity.class);
                    startActivity(mainActivityIntent);
                    finish();
                } else if (code == 2) {
                    Log.i("TAG", "Data NOT Successfully Inserted, email already in use");
                    new AlertDialog.Builder(CreateAccount.this)
                            .setTitle("Email already in use!")
                            .setMessage("The email you entered is already in use please change it and try again.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Dismiss
                                }
                            })
                            .show();
                } else if (code == 3) {
                    Log.i("TAG", "Data NOT Successfully Inserted, username already in use");
                    new AlertDialog.Builder(CreateAccount.this)
                            .setTitle("Username already in use!")
                            .setMessage("The username you entered is already in use please change it and try again.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Dismiss
                                }
                            })
                            .show();
                } else {
                    Log.i("TAG", "Data NOT Successfully Inserted");
                    new AlertDialog.Builder(CreateAccount.this)
                            .setTitle("Error " + code)
                            .setMessage("An error occurred with the error code " + code)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Dismiss
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            } catch (Exception e) {
                Log.e("TAG", e.toString());
            }
            pDialog.dismiss();
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            mSimpleFacebook.logout(onLogoutListener);
            super.onBackPressed();
            return;
        }

    this.doubleBackToExitPressedOnce = true;
    Toast.makeText(this, "Click back again to cancel sign up", Toast.LENGTH_SHORT).show();

    new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    }, 2000);
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