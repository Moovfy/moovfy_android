package com.moovfy.moovfy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kosalgeek.android.photoutil.ImageLoader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import io.nlopez.smartlocation.location.utils.LocationState;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private RequestQueue queue;
    private LocationGooglePlayServicesProvider provider;
    private final int REQUEST_PERMISSION_PHONE_STATE = 1;
    Button chan;

    /*
     * Tabs
     * */
    TabLayout tabLayout;
    ViewPager viewPager;
    PageAdapter pageAdapter;
    TabItem tabClose;
    TabItem tabFriend;
    //----------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        mAuth = FirebaseAuth.getInstance();


        FirebaseUser currentUser = mAuth.getCurrentUser();
        String firebase_uid = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user_uid", "");

        if (firebase_uid.equals("")) { // || currentUser == null
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(login);
        }

        if (!SmartLocation.with(getApplicationContext()).location().state().isGpsAvailable()) {
            Toast.makeText(MainActivity.this, "Por favor active el GPS del teléfono!", Toast.LENGTH_SHORT).show();
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Bundle bundle = getIntent().getExtras();
        String path_p = null;
        if (bundle!= null) path_p = bundle.getString("path_photo");

        Bitmap bitmap = null;
        try {
           if (path_p!= null) bitmap = ImageLoader.init().from(path_p).getBitmap();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View hview = navigationView.getHeaderView(0);
        TextView correo = hview.findViewById(R.id.correo);
        ImageView prof = hview.findViewById(R.id.profile_image);
        if (bitmap != null) prof.setImageBitmap(bitmap);

        chan = hview.findViewById(R.id.but);
        chan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent change_i = new Intent(getApplicationContext(),choose_image.class);
                startActivity(change_i);
            }
        });


        if (currentUser != null) {
            String us = currentUser.getEmail();
            correo.setText(us);
        }
        navigationView.setNavigationItemSelectedListener(this);



        //----------------------------- TABS
        tabLayout = findViewById(R.id.tablayout);
        tabClose = findViewById(R.id.tabClose);
        tabFriend = findViewById(R.id.tabFriends);
        viewPager = findViewById(R.id.viewPager);

        pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 1) {

                } else {

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //------------------------------------

       /* chan = findViewById(R.id.but);
        chan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent change_i = new Intent(getApplicationContext(),choose_image.class);
                startActivity(change_i);
            }
        });*/
    }

    private void pasar_datos(JSONObject json) {
        String url = "http://10.4.41.143:3000/locations/addLocation";

        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.PUT, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        queue.add(jsonobj);

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_chat) {

            Intent intent = new Intent(this, ChatsActivity.class);
            startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.nav_edit_profile) {

            Intent intent = new Intent(this, EditProfile.class);
            startActivity(intent);
        } else if (id == R.id.nav_invite) {

        } else if (id == R.id.nav_help) {

            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("user_uid", "").commit();
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(login);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}