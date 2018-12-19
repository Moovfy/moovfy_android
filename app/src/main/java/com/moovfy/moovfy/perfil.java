package com.moovfy.moovfy;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kosalgeek.android.photoutil.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;

public class perfil extends AppCompatActivity {
    TextView nam,use,ema;
    ImageView imag;
    Button addfriend;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        nam = (TextView) findViewById(R.id.name_u);
        use = (TextView) findViewById(R.id.user_u);
        ema = (TextView) findViewById(R.id.email_u);
        imag = (ImageView) findViewById(R.id.image_per);
        queue = Volley.newRequestQueue(perfil.this);
        addfriend = (Button) findViewById(R.id.addfri);


        Bundle bundle = getIntent().getExtras();
        String na = null;
        if (bundle != null) na = bundle.getString("name_c");

        if (na != null) {
            String aux = null;
            int k = 0;
            while (k < na.length() && na.charAt(k) != (' '))
            {
                k++;
            }
            aux = na.substring(0,k);

            String url = "https://10.4.41.143:3001/users/search/" + aux;

            JsonArrayRequest jsonobj = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            System.out.println(response.toString());

                                try {
                                    JSONObject objeto = response.getJSONObject(0);
                                    nam.setText(objeto.getString("complete_name"));
                                    ema.setText(objeto.getString("email"));
                                    use.setText(objeto.getString("username"));

                                    String ava = objeto.getString("avatar");
                                    Glide.with(perfil.this).load(ava).into(imag);

                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    addfriend.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try {
                                                AddFriends(currentUser.getUid(),objeto.getString("firebase_uid"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            Toast.makeText(getApplicationContext(), "Added to Friends", Toast.LENGTH_LONG).show();

                                        }
                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
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
    }

    public void AddFriends(String uid1,String uid2) {

        String url = "https://10.4.41.143:3001/relations/add";


        JSONObject json = new JSONObject();

        try {
            json.put("firebase_uid1", uid1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            json.put("firebase_uid2", uid2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            json.put("status", "ok");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response add:", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){

        };
        queue.add(jsonobj);

    }
}
