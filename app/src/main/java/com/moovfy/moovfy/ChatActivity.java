package com.moovfy.moovfy;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

import org.w3c.dom.Text;

import static java.lang.System.currentTimeMillis;


public class ChatActivity extends AppCompatActivity {

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private static FirebaseDatabase database;
    private DatabaseReference DatabaseReference;
    private static final int UPLOAD_IMAGE = 1;
    private String Chat_UID;

    private ImageButton btnEnviar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        database = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid1 = "1";

        Intent intent = getIntent();
        uid2 = intent.getStringExtra(CloseFragment.EXTRA_MESSAGE);


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
        txtMensaje  = (EditText) findViewById(R.id.edittext_chatbox);
        fotousuari = (ImageView) findViewById(R.id.conversation_contact_photo);
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

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
                Glide.with(ChatActivity.this).load(usuari2.getAvatar()).into(fotousuari);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Chat", "loadUser1:onCancelled", databaseError.toException());
            }
        };

        Ref_uid2.addValueEventListener(usuari2Listener);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String words = txtMensaje.getText().toString();
                words = words.replace(System.getProperty("line.separator"), "");
                Message mensaje = new Message(words,usuari1.getName(),uid1,currentTimeMillis());
                DatabaseReference.push().setValue(mensaje);
                String m = mensaje.getMessage();
                System.out.println("estoy escribiendo en la base de datos el mensaje " + m);
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
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("12345").child(uri.getLastPathSegment());
                    putImageInStorage(storageReference, uri);
                }
            }
        }
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri) {
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



    public String get_chat_uid(String uid1, String uid2){
        if(uid1.compareTo(uid2) < 0 ){
            return uid1+uid2;
        }
        else return uid2+uid1;
    }

}
