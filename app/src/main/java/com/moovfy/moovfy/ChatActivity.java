package com.moovfy.moovfy;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.System.currentTimeMillis;


public class ChatActivity extends AppCompatActivity {


    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private static FirebaseDatabase database;
    private DatabaseReference DatabaseReference;
    private static final int UPLOAD_IMAGE = 1;
    private String Chat_UID;

    private ImageButton btnEnviar;
    private ImageButton btnFriend;
    private Button btnEnviarFoto;
    private EditText txtMensaje;
    private User usuari;
    private ImageView fotousuari;

    DatabaseReference Ref_uid1;
    DatabaseReference Ref_uid2;
    User usuari1;
    User usuari2;
    String uid1;
    String uid2;
    private TextView nomuser;
    private RequestQueue queue;
    private boolean first;

    Menu optionsMenu;

    String relation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        database = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid1 = currentUser.getUid();
        queue = Volley.newRequestQueue(this);
        first = true;
        //uid1 = "2";

        Intent intent = getIntent();
        uid2 = intent.getStringExtra("uid");
        relation = intent.getStringExtra("relation");

        Chat_UID = get_chat_uid(uid1, uid2);

        DatabaseReference = database.getReference("messages").child(Chat_UID);
        Ref_uid1 = FirebaseDatabase.getInstance().getReference("users").child(uid1);
        Ref_uid2 = FirebaseDatabase.getInstance().getReference("users").child(uid2);



        Toolbar my_toolbar = findViewById(R.id.toolbar_chats);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));
        nomuser = (TextView) findViewById(R.id.action_bar_title_1);
        btnEnviar = (ImageButton) findViewById(R.id.button_chatbox_send);
        btnEnviarFoto = (Button) findViewById(R.id.btnEnviarFoto);
        //btnFriend = (ImageButton) findViewById(R.id.addfriends);
        txtMensaje  = (EditText) findViewById(R.id.edittext_chatbox);
        fotousuari = (ImageView) findViewById(R.id.conversation_contact_photo);
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);


        updateUsers();
        nomuser = (TextView) findViewById(R.id.action_bar_title_1);
        nomuser.setText(intent.getStringExtra("name"));
        fotousuari = (ImageView) findViewById(R.id.conversation_contact_photo);
        Glide.with(getApplicationContext()).load(intent.getStringExtra("urlAvatar")).into(fotousuari);
/*
        ValueEventListener usuari1Listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                usuari1 = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Chat", "loadUser1:onCancelled", databaseError.toException());
            }
        };
        Ref_uid1.addValueEventListener(usuari1Listener);

        ValueEventListener usuari2Listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                usuari2 = dataSnapshot.getValue(User.class);
                Log.w("Chat22", usuari2.toString());
                nomuser.setText(usuari2.getName());
                Glide.with(getApplicationContext()).load(usuari2.getAvatar()).into(fotousuari);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Chat", "loadUser1:onCancelled", databaseError.toException());
            }
        };

        Ref_uid2.addValueEventListener(usuari2Listener);
*/
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String words = txtMensaje.getText().toString();
                words = words.replace(System.getProperty("line.separator"), "");

                Message mensaje = new Message(words,usuari1.getName(),uid1,currentTimeMillis());

                DatabaseReference.push().setValue(mensaje);
                String m = mensaje.getMessage();
                Log.d("Chat", "estoy escribiendo en la base de datos el mensaje " + m);
                txtMensaje.setText("");
                usuari1.AddtoList(Chat_UID);
                usuari2.AddtoList(Chat_UID);
                Ref_uid1.child("ChatsOberts").setValue(usuari1.getChatsOberts());
                Ref_uid2.child("ChatsOberts").setValue(usuari2.getChatsOberts());
            }
        });

        btnEnviarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, UPLOAD_IMAGE);
            }
        });
/*
        btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(first) {
                    Toast.makeText(getApplicationContext(), "Added to friends", Toast.LENGTH_LONG).show();
                    AddFriends(uid1, uid2);
                    first = false;
                }
                else {
                    Toast.makeText(getApplicationContext(), "You are his friend already", Toast.LENGTH_LONG).show();
                }
            }
        });
*/

                mMessageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        setScrollbar();
                    }
                });

        DatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message mensajeRecibido = dataSnapshot.getValue(Message.class);
                String mens= mensajeRecibido.getMessage();
                Log.d("Chat", "El mensaje es: " + mens);
                mMessageAdapter.addMessage(mensajeRecibido);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updateUsers() {
        String url = "https://10.4.41.143:3001/users/";
        JsonTask t = new JsonTask();
        t.execute(url);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection con = null;
            BufferedReader rd = null;
            try {
                URL purl = new URL(params[0] + uid1);
                con = (HttpURLConnection) purl.openConnection();
                StringBuffer buff = new StringBuffer();
                con.connect();

                InputStream strm = con.getInputStream();

                rd = new BufferedReader(new InputStreamReader(strm));

                String line2 = "";

                while ((line2 = rd.readLine()) != null) {
                    buff.append(line2+"\n");
                    Log.d("APIResCloseList: ", "> " + line2);
                }

                JSONObject jsonArrayUser = new JSONObject(buff.toString());
                usuari1 = new User(
                        jsonArrayUser.getString("email"),
                        jsonArrayUser.getString("username"),
                        jsonArrayUser.getString("avatar"),
                        jsonArrayUser.getString("complete_name")
                );



                purl = new URL(params[0] + uid2);
                con = (HttpURLConnection) purl.openConnection();
                buff = new StringBuffer();
                con.connect();

                strm = con.getInputStream();

                rd = new BufferedReader(new InputStreamReader(strm));

                line2 = "";

                while ((line2 = rd.readLine()) != null) {
                    buff.append(line2+"\n");
                    Log.d("APIResCloseList: ", "> " + line2);
                }

                jsonArrayUser = new JSONObject(buff.toString());
                usuari2 = new User(
                        jsonArrayUser.getString("email"),
                        jsonArrayUser.getString("username"),
                        jsonArrayUser.getString("avatar"),
                        jsonArrayUser.getString("complete_name")
                );


            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (con != null) {
                    con.disconnect();
                }
                try {
                    if (rd != null) {
                        rd.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        optionsMenu = menu;
        if (relation.equals("ok")) {
            optionsMenu.getItem(0).setTitle("Delete Friend");
        } else if (relation.equals("donotshow")) {
            optionsMenu.removeItem(R.id.action_add);
        } else {
            optionsMenu.getItem(0).setTitle("Add to Friends");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //afegir a Friends

        if (id == R.id.action_add && relation.equals("no")) { //afegir amic
            Toast.makeText(getApplicationContext(), "Added to Friends", Toast.LENGTH_LONG).show();
            AddFriends(uid1,uid2);
            return true;
        }
        else if (id == R.id.action_add && relation.equals("ok")) { //borrar amic
            Toast.makeText(getApplicationContext(), "Friend Removed", Toast.LENGTH_LONG).show();
            RemoveFriends(uid1,uid2);
            return true;
        }

        else if (id == R.id.action_block){ //afegir a bloquejats
            Toast.makeText(getApplicationContext(), "Added to Black List", Toast.LENGTH_LONG).show();
            AddFBlackList(uid1,uid2);
        }
      
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setScrollbar(){
        mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount()-1);
    }

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }*/

    private void setScrollbar(){
        mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount()-1);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Chat", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == UPLOAD_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d("Chat", "Uri: " + uri.toString());
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference(Chat_UID).child(uri.getLastPathSegment());
                    putImageInStorage(storageReference, uri);
                }
            }
        }
    }

    public void putImageInStorage(StorageReference storageReference, Uri uri) {
        final StorageReference ref = storageReference.child("david");
        ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Message message = new Message(uri.toString(),usuari1.getName(),uid1,System.currentTimeMillis());
                        DatabaseReference.push().setValue(message);



                    }
                });
            }
        });
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
//!!!!!!!!!!!!!!!!!!!!!!!!
    private void RemoveFriends(String uid1,String uid2) {

        String url = "https://10.4.41.143:3001/relations";
        Log.d("inside remove:", url);

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

        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response delete:", response.toString());
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

    private void AddFBlackList(String uid1, String uid2) {
        String url = "https://10.4.41.143:3001/relations/block";


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

        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url,json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response block:", response.toString());
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



    public String get_chat_uid(String uid1, String uid2){
        if(uid1.compareTo(uid2) < 0 ){
            return uid1+ "|" + uid2;
        }
        else return uid2+ "|" + uid1;
    }

}
