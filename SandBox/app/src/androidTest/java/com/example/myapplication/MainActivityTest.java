package com.example.myapplication;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.myapplication.media.VideoTrimmer.VideoTrimmerActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public IntentsTestRule<MainActivity> mMainActivityTestRule = new IntentsTestRule<>(MainActivity.class);

    @Test
    public void testClickItemOpensNewActivity(){
        onView(withText("Video Trimmer")).perform(click());
        intended(hasComponent(VideoTrimmerActivity.class.getName()));
    }

}
