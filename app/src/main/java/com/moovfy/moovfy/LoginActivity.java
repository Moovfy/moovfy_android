package com.moovfy.moovfy;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.shape.TriangleEdgeTreatment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Authenticator;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.signin.SignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;
import com.google.firebase.auth.GoogleAuthProvider;

import com.google.android.gms.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth mAuth;
    Button log;
    Button regist;
    Button forg;
    SignInButton google;
    EditText user_t;
    EditText pas_t;
    private ProgressDialog progressDialog;
    private RequestQueue queue;
    private static final int SIGN_IN_CODE = 777;
    private GoogleSignInClient mGoogleSignInClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        showLocationPermission();

        log = findViewById(R.id.login);
        regist = findViewById(R.id.register);
        forg = findViewById(R.id.forgot);
        google = findViewById(R.id.sign_in_button);


        user_t = findViewById(R.id.email);
        pas_t = findViewById(R.id.password);

        progressDialog = new ProgressDialog(this);
        queue = Volley.newRequestQueue(this);

       GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = user_t.getText().toString().trim();
                String pas = pas_t.getText().toString().trim();
                login(email,pas);
            }
        });

        regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(getApplicationContext(),Register.class);
                startActivity(register);
            }
        });

        forg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Put email");

                final EditText input = new EditText(LoginActivity.this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS );
                builder.setView(input);

                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth = FirebaseAuth.getInstance();
                        mAuth.sendPasswordResetEmail(input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();


            }
        });

    }
    public void showLocationPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(LoginActivity.this, "No se tiene permiso para obtener la ubicación", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);
        }
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 225:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }else {
                }
                return;
        }
    }
    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
    private void login(String email, String pas) {

        if (email.equals("")) {
            user_t.setError("Email can not be empty!");
            return;
        }
        if (pas.equals("")) {
            pas_t.setError("Password can not be empty!");
            return;
        }
        if (!validarEmail(email)) {
            user_t.setError("Format error on email!");
            return;
        }


        mAuth.signInWithEmailAndPassword(email,pas)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (!user.isEmailVerified()) {
                                user_t.setError("Email not verified! Please go to your email tray!");
                                return;
                            }
                            progressDialog.setMessage("Iniciando sessión...");
                            progressDialog.show();
                            getSharedPreferences("PREFERENCE",MODE_PRIVATE).edit().putString("user_uid",user.getUid()).commit();
                            Intent main = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(main);
                        } else {
                            Log.w( "signInUserWithEmail", task.getException());
                            pas_t.setError("Are you sure this information is correct?");
                        }
                        progressDialog.dismiss();
                    }
                });


    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,SIGN_IN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed
            }
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            firebaseAuthWithGoogle(result.getSignInAccount());
        } else {
            Toast.makeText(this, "Ya registrado!", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount signInAccount) {
        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {


                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    String usern = user.getEmail();
                    String email = user.getEmail();
                    String name = user.getDisplayName();

                    JSONObject json = new JSONObject();


                    DatabaseReference mDatabase;
                    mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.child("avatar").exists()) {

                            }
                            else {
                                String uri = "https://firebasestorage.googleapis.com/v0/b/moovfy.appspot.com/o/default-avatar-2.jpg?alt=media&token=fb78f411-b713-4365-9514-d82e6725cb62";
                                User usuari = new User(email,usern,uri,name);
                                mDatabase.setValue(usuari);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("GoogleError: " ,"Error en login amb Google");
                        }
                    });


                    try {
                        json.put("email", email);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        json.put("complete_name", name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        json.put("username", usern);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        json.put("firebase_uid", user.getUid());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    pasar_datos(json);
                    getSharedPreferences("PREFERENCE",MODE_PRIVATE).edit().putString("user_uid",mAuth.getCurrentUser().getUid()).commit();
                    Intent main = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(main);
                }
                else {

                    Toast.makeText(getApplicationContext(), "Not firebase auth!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void pasar_datos(JSONObject json) {
        String url = "http://10.4.41.143:3000/users/register";

        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}