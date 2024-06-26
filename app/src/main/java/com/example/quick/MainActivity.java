package com.example.quick;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.quick.Fragments.HomeFragment;
import com.example.quick.Fragments.NotificationFragment;
import com.example.quick.Fragments.ProfileFragment;
import com.example.quick.Fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        Context context = this;

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int menuItemId = item.getItemId();

                if (menuItemId == R.id.nav_home) {
                    selectorFragment = new HomeFragment();
                } else if (menuItemId == R.id.nav_search) {
                    selectorFragment = new SearchFragment();
                } else if (menuItemId == R.id.nav_add) {
                    selectorFragment = null;
                    startActivity(new Intent(MainActivity.this, PostActivity.class));
                } else if (menuItemId == R.id.nav_heart) {
                    selectorFragment = new NotificationFragment();
                } else if (menuItemId == R.id.nav_profile) {


                    SharedPreferences.Editor editor = getBaseContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    editor.apply();

                    Log.d("AFTER INTENT", getBaseContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE).getString("profileid", "none"));
                    selectorFragment = new ProfileFragment();
                }
                if (selectorFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectorFragment).commit();
                }
                return true;
            }
        });

        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile); // This Line should before
            // sharedpreferences.
            String profileId = intent.getString("publisherId");
            Log.d("INTENT PROFILE", "Not null " + profileId);

            // PROFILE
            SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS", MODE_PRIVATE);
            sharedPreferences.edit().putString("profileid", profileId).apply();

            Log.d("MAIN ACTIVITY INSIDE BUNDLE CHECK", getSharedPreferences("PREFS", MODE_PRIVATE).getString("profileid", "none"));
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();


        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

    }
}