package com.moovfy.moovfy;

import android.arch.persistence.room.Database;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageLoader;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class choose_image extends AppCompatActivity {
    Button galeria;
    Button camara;
    private static final int Activity_select_image = 100;
    private static final int Activity_cam = 101;

    ImageView ivImage;

    CameraPhoto cameraPhoto;
    GalleryPhoto galleryPhoto;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);


        cameraPhoto = new CameraPhoto(getApplicationContext());
        galleryPhoto = new GalleryPhoto(getApplicationContext());
        ivImage = (ImageView) findViewById(R.id.profile_image);

        galeria = findViewById(R.id.galeria_b);
        camara = findViewById(R.id.camara_b);

        galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent gal = new Intent(Intent.ACTION_GET_CONTENT);
                gal.setType("image/*");
                startActivityForResult(gal, Activity_select_image);*/
                startActivityForResult(galleryPhoto.openGalleryIntent(), Activity_select_image);

            }
        });

        camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(cam);*/
                try {
                    startActivityForResult(cameraPhoto.takePhotoIntent(), Activity_cam);
                cameraPhoto.addToGallery();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),
                        "Something Wrong while taking photos", Toast.LENGTH_SHORT).show();
            }
        }
         });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == Activity_cam){
                String photoPath = cameraPhoto.getPhotoPath();

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("path_photo", photoPath);
                putImageInStorage(data);
                startActivity(intent);

            }
            else if(requestCode == Activity_select_image){
                Uri uri = data.getData();

                galleryPhoto.setPhotoUri(uri);
                String photoPath = galleryPhoto.getPath();

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("path_photo", photoPath);
                putImageInStorage(data);
                startActivity(intent);
            }
        }
    }
    protected void putImageInStorage(Intent data) {
        if (data != null) {
            Uri uri;

            final Uri selectedImage = data.getData();

            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(
                        selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 20, stream);
            byte[] byteArray = stream.toByteArray();
            try {
                stream.close();
                stream = null;
            } catch (IOException e) {

                e.printStackTrace();
            }

            uri = getImageUri(this,bmp);

            Log.d("Chat", "Uri: " + uri.toString());
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("Perfil").child(uri.getLastPathSegment());

            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            String uid = currentUser.getUid();
                            //uid = "2"; //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                            DatabaseReference Ref_uid1 = FirebaseDatabase.getInstance().getReference("users").child(uid).child("avatar");
                            Ref_uid1.setValue(uri.toString());
                            //acces api per guardar el avatar
                            queue = Volley.newRequestQueue(getApplicationContext());
                            String url2 = "http://10.4.41.143:3000/users/updateavatar/" + uid;
                            JSONObject obj = new JSONObject();
                            try {
                                obj.put("avatar", uri.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            JsonObjectRequest jsonobj2 = new JsonObjectRequest(Request.Method.PUT, url2, obj,
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
                            queue.add(jsonobj2);

                        }
                    });
                }
            });
        }
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


}
