package com.moovfy.moovfy;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.kosalgeek.android.photoutil.ImageLoader;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import io.nlopez.smartlocation.location.utils.LocationState;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private FirebaseAuth mAuth;
    private RequestQueue queue;
    private LocationGooglePlayServicesProvider provider;
    private final int REQUEST_PERMISSION_PHONE_STATE = 1;
    Button chan;
    final static int REQUEST_LOCATION = 199;
    SearchView se;
   ListView list;
    ArrayList<String> arrayList;
   ArrayAdapter<String> adapter;


    /*
     * Tabs
     * */
    TabLayout tabLayout;
    ViewPager viewPager;
    PageAdapter pageAdapter;
    TabItem tabClose;
    TabItem tabFriend;
    //----------------------

    ImageView ivImage; // avatar
    DatabaseReference Ref_uid1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Boolean firstRun = getSharedPreferences("PREFERENCE",MODE_PRIVATE).getBoolean("firstRun",true);
        /*
        if(firstRun) {
            Intent intro = new Intent(getApplicationContext(),MoovfyIntro.class);
            startActivity(intro);
        }
*/


        mAuth = FirebaseAuth.getInstance();

        queue = Volley.newRequestQueue(MainActivity.this);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String firebase_uid = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user_uid", "");

        if (firebase_uid.equals("")) { // || currentUser == null
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(login);
        }




        SmartLocation.with(getApplicationContext()).location().start(locationListener);
        if (!SmartLocation.with(getApplicationContext()).location().state().isGpsAvailable()) {
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5 * 1000);
            locationRequest.setFastestInterval(2 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult result) {
                    final Status status = result.getStatus();
//                final LocationSettingsStates state = result.getLocationSettingsStates();

                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:


                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            try {

                                status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
                            } catch (IntentSender.SendIntentException e) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//coment

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




        FirebaseUser currentUser2 = mAuth.getCurrentUser();


       ///

       list = (ListView) findViewById(R.id.listV);
        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,arrayList);
       list.setAdapter(adapter);
        se = (SearchView) findViewById(R.id.searchView);
        se.setOnQueryTextListener(this);
        se.setOnCloseListener(this::onClose);
        Toast.makeText(this,"h" , Toast.LENGTH_SHORT).show();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String a = adapter.getItem(position);
                System.out.println(a);
                Intent i=new Intent(MainActivity.this, perfil.class);
                i.putExtra("name_c",a);
                startActivity(i);

            }
        });



    }
    private void buscar(String s) {
        String url = "http://10.4.41.143:3000/users/search/" + s;

        JsonArrayRequest jsonobj = new JsonArrayRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println(response.toString());
                        Log.d("Respuesta", response.toString());
                        adapter.clear();
                        for (int i = 0; i < response.length(); ++i){
                            try {
                                JSONObject objeto = response.getJSONObject(i);
                                String usr = objeto.getString("complete_name");


                                arrayList.add(usr);
                                adapter.notifyDataSetChanged();
                                setListViewHeightBasedOnItems(list);

                                System.out.println(usr);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

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
       // getMenuInflater().inflate(R.menu.main, menu);
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

        } else if (id == R.id.nav_black_list) {
           Intent intent = new Intent(this, BlackListActivity.class);
           startActivity(intent);
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

    //LOCATION FUNCTIONS
    OnLocationUpdatedListener locationListener = new OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            System.out.println("LOCATION CHANGED: latitude " + lat + " longitude " + lng);
            Toast.makeText(getApplicationContext(),"LOCATION CHANGED: latitude " + lat + " longitude " + lng,Toast.LENGTH_LONG).show();
            String firebase_uid = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("user_uid","");
            if(firebase_uid != "") {
                FirebaseUser user = mAuth.getCurrentUser();
                String userUid = user.getUid();
                JSONObject json = new JSONObject();
                try {
                    json.put("userUID", userUid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    json.put("latitude", location.getLatitude());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    json.put("longitude", location.getLongitude());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pasar_datos(json);
            }
        }
    };

    Runnable locationRunnable = new Runnable(){
        @Override
        public void run() {
            SmartLocation.with(getApplicationContext()).location().start(locationListener);
        }
    };

    //callback method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Toast.makeText(getApplicationContext(), "Gps enabled", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(getApplicationContext(), "Gps Canceled", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        buscar(s);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        buscar(s);
        return true;
    }

    public boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;

            if (numberOfItems <= 10) {
                for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                    View item = listAdapter.getView(itemPos, null, listView);
                    float px = 500 * (listView.getResources().getDisplayMetrics().density);
                    item.measure(View.MeasureSpec.makeMeasureSpec((int) px, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    totalItemsHeight += item.getMeasuredHeight();
                }
            }
            else {
                for (int itemPos = 0; itemPos < 10; itemPos++) {
                    View item = listAdapter.getView(itemPos, null, listView);
                    float px = 500 * (listView.getResources().getDisplayMetrics().density);
                    item.measure(View.MeasureSpec.makeMeasureSpec((int) px, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    totalItemsHeight += item.getMeasuredHeight();
                }
            }


            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);
            // Get padding
            int totalPadding = listView.getPaddingTop() + listView.getPaddingBottom();

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();


               params.height = totalItemsHeight + totalDividersHeight + totalPadding;


            listView.setLayoutParams(params);
            listView.requestLayout();
            return true;

        } else {
            return false;
        }

    }


    @Override
    public boolean onClose() {
        adapter.clear();
        adapter.notifyDataSetChanged();
        setListViewHeightBasedOnItems(list);
        return false;
    }
}