package com.free.dennisg.atlas;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.sromku.simple.fb.utils.Attributes;
import com.sromku.simple.fb.utils.PictureAttributes;

import java.io.InputStream;
import java.net.URL;

public class TestProfile extends Activity {

    SimpleFacebook mSimpleFacebook;
    String TAG = "Places Debug TAG";

    TextView status, fb_id, fb_name, fb_email;
    ImageView fb_img;
    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_profile);

        status = (TextView) findViewById(R.id.status);
        fb_id = (TextView) findViewById(R.id.fb_id);
        fb_email = (TextView) findViewById(R.id.fb_email);
        fb_name = (TextView) findViewById(R.id.fb_name);
        fb_img = (ImageView) findViewById(R.id.fb_img);

        PictureAttributes pictureAttributes = Attributes.createPictureAttributes();
        pictureAttributes.setHeight(500);
        pictureAttributes.setWidth(500);
        pictureAttributes.setType(PictureAttributes.PictureType.LARGE);

        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.ID)
                .add(Profile.Properties.EMAIL)
                .add(Profile.Properties.FIRST_NAME)
                .add(Profile.Properties.LAST_NAME)
                .add(Profile.Properties.PICTURE, pictureAttributes)
                .build();

        SimpleFacebook.getInstance(TestProfile.this).getProfile(properties, new OnProfileListener() {
            @Override
            public void onFail(String reason) {
                status.setText(reason);
                Log.w(TAG, "Failed to login");
            }

            @Override
            public void onException(Throwable throwable) {
                status.setText("Exception: " + throwable.getMessage());
                Log.e(TAG, "Bad thing happened", throwable);
            }

            @Override
            public void onThinking() {
                // show progress bar or something to the user while login is
                // happening
                status.setText("Thinking...");
            }

            @Override
            public void onComplete(Profile profile) {

                fb_id.setText(profile.getId());
                fb_email.setText(profile.getEmail());
                fb_name.setText(profile.getFirstName() + " " + profile.getLastName());

                String FBProfilePic = profile.getPicture();
                new getProfilePicture().execute(FBProfilePic);
            }
        });
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
                fb_img.setImageBitmap(bmp);
            }
        }
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