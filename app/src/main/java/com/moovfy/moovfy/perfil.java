package com.moovfy.moovfy;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
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
}
