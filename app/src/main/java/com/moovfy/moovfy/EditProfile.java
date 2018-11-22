package com.moovfy.moovfy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.*;
public class EditProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button mod_b;
    EditText name_t;
    EditText username_t;
    EditText email_t;
    EditText pass_t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();

        mod_b =  findViewById(R.id.cambiar);
        name_t = findViewById(R.id.editName);
        username_t = findViewById(R.id.editUsername);
        email_t = findViewById(R.id.editEmail);
        pass_t = findViewById(R.id.editPassword);

        mod_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_t.getText().toString().trim();
                String username = username_t.getText().toString().trim();
                String email = email_t.getText().toString().trim();
                String pass = pass_t.getText().toString().trim();

                FirebaseUser user = mAuth.getCurrentUser();

                if (!name.equals("")) {

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    }
                                }
                            });
                }

                if(!email.equals("")) {
                    user.updateEmail(email);
                    user.sendEmailVerification();
                }

                if(!pass.equals("")) {
                    user.updatePassword(pass);
                }

                Toast.makeText(EditProfile.this, "Su perfil se ha modificado correctamente", Toast.LENGTH_SHORT).show();

                if(!email.equals("")) {
                    Intent main = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(main);
                }
            }
        });
    }
}
