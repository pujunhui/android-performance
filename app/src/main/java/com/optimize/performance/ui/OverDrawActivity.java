package com.optimize.performance.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.optimize.performance.R;

/**
 * 演示解决OverDraw的Activity
 */
public class OverDrawActivity extends AppCompatActivity {
    public static final String TAG = "droid-cards-activity";

    // For this sample, we simply hard code the size of the droid image. A real app might prefer
    // to dynamically calculate this value based on the activity's dimensions.
    protected static final float DROID_IMAGE_WIDTH = 420f;

    // The distance between the left edges of two adjacent cards. The cards overlap horizontally.
    protected static final float CARD_SPACING = DROID_IMAGE_WIDTH / 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Droid[] droids = {
                new Droid("Joanna", R.color.joanna_color, R.mipmap.joanna),
                new Droid("Shailen", R.color.shailen_color, R.mipmap.shailen),
                new Droid("Chris", R.color.chris_color, R.mipmap.chris)
        };

        DroidCardsView droidCardView = new DroidCardsView(
                this,
                droids,
                DROID_IMAGE_WIDTH,
                CARD_SPACING);

        setContentView(droidCardView);
    }
}
