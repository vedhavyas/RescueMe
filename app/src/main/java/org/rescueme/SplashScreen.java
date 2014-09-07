package org.rescueme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;




public class SplashScreen extends Activity {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = RescueMeConstants.SPLASH_SCREEN_TIMEOUT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent intent = new Intent(SplashScreen.this, RescueMe.class);
                startActivity(intent);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
