package com.moovfy.moovfy.map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.moovfy.moovfy.FriendFragment;
import com.moovfy.moovfy.R;
import com.moovfy.moovfy.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    private LatLngBounds mMapBoundary;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ClusterManager<ClusterMarker> mClusterManager;

    BroadcastReceiver mReceiver;
    BroadcastReceiver mReceiver2;

    public MapFragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mView = inflater.inflate(R.layout.map_fragment, container, false);



        return mView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) mView.findViewById(R.id.map);

        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String uid = "";
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                if (currentFirebaseUser != null) {
                    uid = currentFirebaseUser.getUid();
                } else {
                    Log.d("APIResponse3: ", "> " + "Usuari null");
                }

                if ("cargarNear".equals(intent.getAction())) {

                    String url = "http://10.4.41.143:3000/near/" + uid;
                    JsonTaskUpdateMap t = new JsonTaskUpdateMap();
                    t.execute(url);
                    Log.d("NEEEEARRR", "NEAR");

                }
                /*
                else if ("cargarFriends".equals(intent.getAction())) {
                    Log.d("FFFFFFFFFFFFFFFFFFFF", "friends");
                    String url = "http://10.4.41.143:3000/friends/" + uid;
                    JsonTaskUpdateMap t = new JsonTaskUpdateMap();
                    //t.execute(url);
                }
                */
            }
        };
        mReceiver2 = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String uid = "";
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                if (currentFirebaseUser != null) {
                    uid = currentFirebaseUser.getUid();
                } else {
                    Log.d("APIResponse3: ", "> " + "Usuari null");
                }

                if ("cargarFriends".equals(intent.getAction())) {
                    Log.d("ExecutantFriends: ", "> " + "Usuari null");
                    String url = "http://10.4.41.143:3000/friends/" + uid;
                    JsonTaskUpdateMap t = new JsonTaskUpdateMap();
                    t.execute(url);

                }
                /*
                else if ("cargarFriends".equals(intent.getAction())) {
                    Log.d("FFFFFFFFFFFFFFFFFFFF", "friends");
                    String url = "http://10.4.41.143:3000/friends/" + uid;
                    JsonTaskUpdateMap t = new JsonTaskUpdateMap();
                    //t.execute(url);
                }
                */
            }
        };

        IntentFilter filter2 = new IntentFilter("cargarFriends");
        IntentFilter filter3 = new IntentFilter("cargarNear");

        getContext().registerReceiver(mReceiver,filter3);
        getContext().registerReceiver(mReceiver2,filter2);

    }

    @Override
    public void onStop() {
        getContext().unregisterReceiver(mReceiver);
        getContext().unregisterReceiver(mReceiver2);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;

        addMapMarkers();
        mGoogleMap.getUiSettings().setAllGesturesEnabled(false);


    }

    private void addMapMarkers(){

        if(mGoogleMap != null){

            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), mGoogleMap);
            }
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getActivity(),
                        mGoogleMap,
                        mClusterManager

                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            String uid = "";
            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
            if (currentFirebaseUser != null) {
                uid = currentFirebaseUser.getUid();
            } else {
                Log.d("APIResponse3: ", "> " + "Usuari null");
            }
            String url = "http://10.4.41.143:3000/near/" + uid;
            JsonTaskUpdateMap t = new JsonTaskUpdateMap();
            t.execute(url);
        }
    }


    private class JsonTaskUpdateMap extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            mClusterManager.clearItems();
            String mylat = "";
            String mylng = "";

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("APIResponsebbb: ", "> " + line);
                }
                HttpURLConnection connection2 = null;
                BufferedReader reader2 = null;
                try {
                    //afegir el propi marker
                    String myuid = "";
                    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                    if (currentFirebaseUser != null) {
                        myuid = currentFirebaseUser.getUid();
                    } else {
                        Log.d("APIResponse3222: ", "> " + "Usuari null");
                    }


                    URL url2 = new URL("http://10.4.41.143:3000/users/" + myuid);
                    connection2 = (HttpURLConnection) url2.openConnection();
                    connection2.connect();
                    connection2.setConnectTimeout(5000);
                    connection2.setReadTimeout(5000);
                    InputStream stream2 = connection2.getInputStream();

                    reader2 = new BufferedReader(new InputStreamReader(stream2));

                    StringBuffer buffer2 = new StringBuffer();
                    String line2 = "";

                    while ((line2 = reader2.readLine()) != null) {
                        buffer2.append(line2+"\n");
                        Log.d("APIResponse222: ", "> " + line2);
                    }
                    JSONObject myuser= new JSONObject(buffer2.toString());
                    JSONArray locations = myuser.getJSONArray("locations");
                    if (locations.length() >= 1) { //SI EXISTEIX LOCATIONS EN EL USER
                        JSONObject lastObj = locations.getJSONObject(locations.length() - 1);

                        double lat = lastObj.getDouble("latitude");
                        mylat = lastObj.getString("latitude").toString();
                        Log.d("laaaaaat", String.valueOf(lat));
                        double lng = lastObj.getDouble("longitude");
                        mylng = lastObj.getString("longitude").toString();

                        LatLng myposition = new LatLng(lat, lng);
                        //LatLng position = new LatLng(41.7164, 1.8223);

                        String avatar = "https://firebasestorage.googleapis.com/v0/b/moovfy.appspot.com/o/default-avatar-2.jpg?alt=media&token=fb78f411-b713-4365-9514-d82e6725cb62"; // set the default avatar
                        if (myuser.getString("avatar") != null) {
                            avatar = myuser.getString("avatar");
                        }
                        URL imageurl = new URL(avatar);
                        Bitmap bmp = BitmapFactory.decodeStream(imageurl.openConnection().getInputStream());

                        ClusterMarker newClusterMarker = new ClusterMarker(
                                myposition,
                                myuser.getString("complete_name"),
                                myuser.getString("email"),
                                bmp
                        );
                        mClusterManager.addItem(newClusterMarker);
                    }

                    if (buffer.toString() != null) {

                        JSONArray jsonArray = new JSONArray(buffer.toString());
                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject e = jsonArray.getJSONObject(i);
                                String uid = e.getString("uid");

                                //ELS ALTRES USUARIS
                                HttpURLConnection connection3 = null;
                                BufferedReader reader3 = null;
                                try {

                                    URL url3 = new URL("http://10.4.41.143:3000/users/" + uid);
                                    connection3 = (HttpURLConnection) url3.openConnection();
                                    connection3.connect();
                                    connection3.setConnectTimeout(5000);
                                    connection3.setReadTimeout(5000);
                                    InputStream stream3 = connection3.getInputStream();

                                    reader3 = new BufferedReader(new InputStreamReader(stream3));

                                    StringBuffer buffer3 = new StringBuffer();
                                    String line3 = "";

                                    while ((line3 = reader3.readLine()) != null) {
                                        buffer3.append(line3+"\n");
                                        Log.d("APIResponse222: ", "> " + line2);
                                    }
                                    JSONObject newuser= new JSONObject(buffer3.toString());
                                    JSONArray newlocations = newuser.getJSONArray("locations");
                                    if (newlocations.length() >= 1) { //SI EXISTEIX LOCATIONS EN EL new USER
                                        JSONObject lastObj = newlocations.getJSONObject(newlocations.length() - 1);

                                        double lat = lastObj.getDouble("latitude");
                                        Log.d("laaaaaatuseeeeeer", String.valueOf(lat));
                                        double lng = lastObj.getDouble("longitude");

                                        LatLng newposition = new LatLng(lat, lng);
                                        //LatLng position = new LatLng(41.7164, 1.8223);

                                        Log.d("avatarde", newuser.getString("username"));
                                        String avatar = "https://firebasestorage.googleapis.com/v0/b/moovfy.appspot.com/o/default-avatar-2.jpg?alt=media&token=fb78f411-b713-4365-9514-d82e6725cb62"; // set the default avatar
                                        if (newuser.getString("avatar") != null) {
                                            avatar = newuser.getString("avatar");
                                        }

                                        URL imageurl = new URL(avatar);
                                        Bitmap bmp = BitmapFactory.decodeStream(imageurl.openConnection().getInputStream());


                                        ClusterMarker newClusterMarker = new ClusterMarker(
                                                newposition,
                                                newuser.getString("complete_name"),
                                                newuser.getString("email"),
                                                bmp
                                        );
                                        mClusterManager.addItem(newClusterMarker);
                                    }



                                } finally {
                                    if (connection2 != null) {
                                        connection2.disconnect();
                                    }
                                    try {
                                        if (reader2 != null) {
                                            reader2.close();
                                        }
                                    } catch (IOException e2) {
                                        e2.printStackTrace();
                                    }
                                }

                            }
                        }


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection2 != null) {
                        connection2.disconnect();
                    }
                    try {
                        if (reader2 != null) {
                            reader2.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }




                return mylat + "|" + mylng;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            Log.d("UrlRequestedss: ", "> " + s);
            mClusterManager.cluster();

            String latlng[] = s.split("\\|");
            LatLng position = new LatLng(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1]));
            mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom(position, 18.0f) );

        }


    }
}
