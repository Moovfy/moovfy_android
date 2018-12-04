package com.moovfy.moovfy.map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.google.maps.android.clustering.ClusterManager;
import com.moovfy.moovfy.R;

import java.util.Random;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    private LatLngBounds mMapBoundary;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ClusterManager<ClusterMarker> mClusterManager;

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
        BroadcastReceiver mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if ("action_location_updated".equals(intent.getAction())) {
                    // update fragment here
                    LatLng position = new LatLng(41.7164, 1.8226);
                    Log.w("UUUUUUaaaaaaaaaa", "Near");
                    try{

                        Random r = new Random();
                        Bundle b = intent.getExtras();
                        String j = "BUIT";
                        if(b!=null)
                        {
                            j =(String) b.get("name");

                        }
                        String snippet = "Desaaaaaaaaaaaaaaaaaa" + Integer.toString(r.nextInt());
                        int avatar = R.drawable.icono; // set the default avatar
                        ClusterMarker newClusterMarker = new ClusterMarker(
                                position,
                                j,
                                snippet,
                                avatar
                        );
                       // mClusterManager.clearItems();
                        mClusterManager.addItem(newClusterMarker);


                    }catch (NullPointerException e){
                        Log.e("Errorrrrr", "addMapMarkers: NullPointerException: " + e.getMessage() );
                    }


                    mClusterManager.cluster();

                    mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom(position, 15.0f) );
                }
            }
        };
        IntentFilter filter = new IntentFilter("action_location_updated");
        getContext().registerReceiver(mReceiver, filter);

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
            LatLng position = new LatLng(41.7164, 1.8221);
            try{

                Random r = new Random();

                String snippet = "Descripcio de l'usuari" + Integer.toString(r.nextInt());
                int avatar = R.drawable.icono; // set the default avatar
                ClusterMarker newClusterMarker = new ClusterMarker(
                        position,
                        "USERNAME",
                        snippet,
                        avatar
                );
                mClusterManager.addItem(newClusterMarker);


            }catch (NullPointerException e){
                Log.e("Errorrrrr", "addMapMarkers: NullPointerException: " + e.getMessage() );
            }


            mClusterManager.cluster();


            mGoogleMap.moveCamera( CameraUpdateFactory.newLatLngZoom(position, 15.0f) );
        }
    }

}
