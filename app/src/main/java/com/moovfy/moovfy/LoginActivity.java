package com.moovfy.moovfy;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.shape.TriangleEdgeTreatment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.Authenticator;
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


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth mAuth;
    Button log;
    Button regist;
    Button forg;
    SignInButton google;
    EditText user_t;
    EditText pas_t;
    private ProgressDialog progressDialog;
    private static final int SIGN_IN_CODE = 777;
    private GoogleApiClient googleApiClient;
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
                Intent reset = new Intent(getApplicationContext(),PasswordReset.class);
                startActivity(reset);
            }
        });




    }
    public void showLocationPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(LoginActivity.this, "No se tiene permiso para obtener la ubicación", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE}, 225);
        }
        /*else {
            Toast.makeText(LoginActivity.this, "Se tiene permiso!", Toast.LENGTH_SHORT).show();
        }*/
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
            Toast.makeText(this,"Introduzca el email", Toast.LENGTH_LONG).show();
            return;
        }
        if (pas.equals("")) {
            Toast.makeText(this,"Introduzca el password", Toast.LENGTH_LONG).show();
            return;
        }
        if (!validarEmail(email)) {
            Toast.makeText(this, "Error en el formato del email", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(LoginActivity.this,"Correo electrónico no verificado", Toast.LENGTH_LONG).show();
                                return;
                            }
                            progressDialog.setMessage("Iniciando sessión...");
                            progressDialog.show();
                            getSharedPreferences("PREFERENCE",MODE_PRIVATE).edit().putString("user_uid",user.getUid()).commit();
                            Intent main = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(main);
                        } else {
                            Log.w( "signInUserWithEmail", task.getException());
                            Toast.makeText(LoginActivity.this, "No se pudo iniciar sesión", Toast.LENGTH_LONG).show();
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
                // Google Sign In failed, update UI appropriately

                // [START_EXCLUDE]
                // [END_EXCLUDE]
            }
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            firebaseAuthWithGoogle(result.getSignInAccount());
        } else {
            Toast.makeText(this, "Not log!", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount signInAccount) {
        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {


                if (task.isSuccessful()) {
                    //mAuth.getCurrentUser().sendEmailVerification();
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


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}