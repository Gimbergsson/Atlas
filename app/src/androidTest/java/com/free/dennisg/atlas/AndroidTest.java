package com.free.dennisg.atlas;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static java.lang.Thread.sleep;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AndroidTest {

    private String mEmail, mPassword;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void initValidString() {
        mEmail = "tester01@test.se";
        mPassword = "Tester123";
    }

    @Test
    public void loginClickMarker() throws UiObjectNotFoundException {
        waitTime(1000);
        onView(withId(R.id.email)).perform(typeText(mEmail), closeSoftKeyboard());
        waitTime(1000);
        onView(withId(R.id.password)).perform(typeText(mPassword), closeSoftKeyboard());
        waitTime(1000);
        onView(withId(R.id.login_button)).perform(click());
        waitTime(1000);
        for (int i = 0; i < 5 ; i++){
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject marker = device.findObject(new UiSelector().descriptionContains("148"));
            marker.pinchIn(100, 5);
            waitTime(1000);
            marker.click();
            //onView(withId(R.id.map)).perform(doubleClick());
            waitTime(1000);
        }
        waitTime(2500);
    }

    public void waitTime(long timeInMs){
        try{
            sleep(timeInMs, 0);
        }catch (Exception e){
            Log.e("TAG", e.toString());
        }
    }
}