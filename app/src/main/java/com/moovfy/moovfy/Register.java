package com.moovfy.moovfy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.regex.Pattern;


public class Register extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private EditText name_t;
    private EditText user_t;
    private EditText pass_t;
    private EditText email_t;
    private EditText pass_t_2;
    private Button regis;
    private ProgressDialog progressDialog;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        name_t = (EditText) findViewById(R.id.name);
        user_t = (EditText) findViewById(R.id.user);
        pass_t = (EditText) findViewById(R.id.pass);
        email_t = (EditText) findViewById(R.id.email);
        pass_t_2 = (EditText) findViewById(R.id.r_pass);
        regis = (Button) findViewById(R.id.register2);
        progressDialog = new ProgressDialog(this);
        queue = Volley.newRequestQueue(this);
        regis.setOnClickListener(e-> registrarUsuario());

    }
    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
    private void registrarUsuario() {

        //Obtenemos el email y la contraseña desde las cajas de texto
        String email = email_t.getText().toString().trim();
        String password = pass_t.getText().toString().trim();
        String password2 = pass_t_2.getText().toString().trim();


        if (!password2.equals("") && !password.equals("") && !password2.equals(password)) {
            Toast.makeText(this, "Contraseñas diferentes", Toast.LENGTH_LONG).show();
            return;
        }
        //Verificamos que las cajas de texto no esten vacías
        if (email.equals("")) {
            Toast.makeText(this, "Se debe ingresar un email", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.equals("")) {
            Toast.makeText(this, "Falta ingresar la contraseña", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener como mínimo 6 carácteres", Toast.LENGTH_LONG).show();
            return;
        }
        if (!validarEmail(email)) {
            Toast.makeText(this, "Error en el formato del email", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Realizando registro en linea...");
        progressDialog.show();
        //registramos un nuevo usuario
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String usern = user_t.getText().toString().trim();
                            String name = name_t.getText().toString().trim();

                            JSONObject json = new JSONObject();

                            try {
                                json.put("email", email);
                                json.put("complete_name", name);
                                json.put("username", usern);
                                json.put("firebase_uid", user.getUid());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            pasar_datos(json, user.getUid());

                            user.sendEmailVerification();

                            /*UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(Uri.parse("C:\\Users\\Andoni\\Desktop\\Nueva carpeta (2)\\andoni\\moovfy_android-master\\moovfy_android-master\\app\\src\\main\\res\\drawable"))
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("profile", "User profile updated.");
                                            }
                                        }
                                    });

                            */

                        } else {
                            Log.w( "createUserWithEmail", task.getException());
                            Toast.makeText(Register.this, "No se pudo registrar el usuario ", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    private void pasar_datos(JSONObject json, String uid) {
        String url = "https://10.4.41.143:3001/users/register";

        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.w("created", String.valueOf(response));
                        //queue = Volley.newRequestQueue(getApplicationContext());
                        String url2 = "https://10.4.41.143:3001/users/updateavatar/" + uid;
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put("avatar", "https://firebasestorage.googleapis.com/v0/b/moovfy.appspot.com/o/default-avatar-2.jpg?alt=media&token=fb78f411-b713-4365-9514-d82e6725cb62");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JsonObjectRequest jsonobj2 = new JsonObjectRequest(Request.Method.PUT, url2, obj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("Response update iamge:", response.toString());
                                    }
                                },
                                new ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("Error.Response", error.toString());
                                    }
                                }
                        );
                        Intent main = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(main);
                        queue.add(jsonobj2);
                    }
                },new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.w("failed", String.valueOf(error));
                        }
                }

        );
        queue.add(jsonobj);
    }


}
