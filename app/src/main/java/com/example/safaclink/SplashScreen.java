package com.example.safaclink;

import static android.app.ActivityOptions.makeSceneTransitionAnimation;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {
    private static final int SPLASH_SCREEN = 5000;
    Animation TopAnimation, BottomAnimation;
    ImageView image;
    TextView logo, slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TopAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        BottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        image = findViewById(R.id.imglogo);
        logo = findViewById(R.id.txtlogo);
        slogan = findViewById(R.id.txtslogan);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);

                Pair[] pairs = new Pair[2];
                pairs[0] = new Pair<View,String>(image, "logo_image");
                pairs[1] = new Pair<View,String>(logo, "logo_text");

                ActivityOptions options = makeSceneTransitionAnimation(SplashScreen.this, pairs);
                startActivity(intent,options.toBundle());
            }
        }, SPLASH_SCREEN);
    }
}