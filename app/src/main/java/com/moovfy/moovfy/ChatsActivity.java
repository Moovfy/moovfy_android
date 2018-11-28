package com.moovfy.moovfy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatsActivity extends AppCompatActivity implements RecyclerItemTouchHelperChats.RecyclerItemTouchHelperChatsListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String EXTRA_MESSAGE = "";
    private RecyclerView recyclerListChats;
    private ListChatsAdapter adapter;
    List<User> userList = new ArrayList<>();
    List<String> lastMsgList = new ArrayList<>();
    List<String> uids = new ArrayList<>();
    private RelativeLayout relativeLayout;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    DatabaseReference Ref;
    User chatsOberts;
    List<String> listChatsOberts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
        setTitle("Chats");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerListChats = (RecyclerView) findViewById(R.id.recycleListChats);
        recyclerListChats.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        relativeLayout = findViewById(R.id.relative_layout_cahts);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperChats(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerListChats);



        adapter = new ListChatsAdapter(this, userList, uids, new ListChatsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String uid) {
                Log.d("UIDagafat: ", "> " + uid);
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra(EXTRA_MESSAGE, uid);
                startActivity(intent);
            }
        });
        recyclerListChats.setAdapter(adapter);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                updateList();
            }
        });
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ListChatsAdapter.ItemChatViewHolder) {
            // get the removed item name to display it in snack bar
            String name = userList.get(viewHolder.getAdapterPosition()).getUsername();

            // backup of removed item for undo purpose
            final User deletedItem = userList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            adapter.removeItem(viewHolder.getAdapterPosition());

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(relativeLayout, name + " removed from black list!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    adapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public void onRefresh() {
        updateList();
    }

    private void updateList() {

        userList.clear();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;


//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11substituir child("2) per la uid actual del usuari
        Ref = FirebaseDatabase.getInstance().getReference("users").child("2");
        ValueEventListener usuari1Listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                chatsOberts = dataSnapshot.getValue(User.class);
                listChatsOberts = chatsOberts.getChatsOberts();
                for (int i = 0; i < listChatsOberts.size(); i++) {
                    Log.w("ChatObert", listChatsOberts.get(i));
                    String chatid = listChatsOberts.get(i);
                    String ids[] = chatid.split("\\|");
                    String currentuid = "";
                    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                    if (currentFirebaseUser != null) {
                        currentuid = currentFirebaseUser.getUid();
                        Log.d("Current UId: ", "> " + currentuid);
                    } else {
                        Log.d("Current UId: ", "> " + "Usuari null");
                    }
                    currentuid = "2"; //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    for (String s : ids) {
                        if (!s.equals(currentuid)) {


                            Log.d("Executant s: ", "> " + s);
                            JsonTask t = new JsonTask();
                            t.execute("http://10.4.41.143:3000/users/" + s);


                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Chat", "loadUser1:onCancelled", databaseError.toException());
            }
        };
        Ref.addValueEventListener(usuari1Listener);

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);

                HttpURLConnection connection2 = null;
                BufferedReader reader2 = null;



                connection2 = (HttpURLConnection) url.openConnection();
                connection2.connect();

                InputStream stream2 = connection2.getInputStream();

                reader2 = new BufferedReader(new InputStreamReader(stream2));

                StringBuffer buffer2 = new StringBuffer();
                String line2 = "";

                while ((line2 = reader2.readLine()) != null) {
                    buffer2.append(line2+"\n");
                    Log.d("APIResponse2: ", "> " + line2);
                }

                String s = buffer2.toString();
                Log.d("buffer2: ", "> " + s);
                JSONObject user = null;

                user = new JSONObject(s);
                String avatar ="";
                                /*
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("avatar");
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String value = (String) dataSnapshot.getValue();
                                        Log.d("avatarurl: ", "> " + value);


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }

                                });
                                */
                avatar = "https://firebasestorage.googleapis.com/v0/b/moovfy.appspot.com/o/default-avatar-2.jpg?alt=media&token=fb78f411-b713-4365-9514-d82e6725cb62";
                userList.add(new User(
                        user.getString("email"),
                        user.getString("username"),
                        avatar,
                        user.getString("complete_name")
                ));
                uids.add(user.getString("firebase_uid"));





                return buffer2.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            Log.d("UrlRequestedss: ", "> " + s);
            adapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);

        }


    }


}

class ListChatsAdapter extends RecyclerView.Adapter<ListChatsAdapter.ItemChatViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String uid);
    }

    private Context mCtx;
    private List<User> userList;
    private List<String> uids;

    private final OnItemClickListener listener;

    public ListChatsAdapter(Context mCtx, List<User> userList, List<String> uids,OnItemClickListener listener) {
        this.mCtx = mCtx;
        this.userList = userList;
        this.listener = listener;
        this.uids = uids;
    }

    public void removeItem(int position) {
        userList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreItem(User item, int position) {
        userList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    @NonNull
    @Override
    public ItemChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view =  inflater.inflate(R.layout.item_chat, viewGroup,false);
        return new ItemChatViewHolder(view, mCtx);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemChatViewHolder itemCloseViewHolder, int i) {

        itemCloseViewHolder.bind(userList.get(i),uids.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    class ItemChatViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername, textViewLastMsg;
        ImageView imageView;
        Context mContext;

        public RelativeLayout viewBackground, viewForeground;

        public ItemChatViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            mContext = context;
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewLastMsg = itemView.findViewById(R.id.textViewLastMsg);
            imageView = itemView.findViewById(R.id.imageView);

            viewBackground = itemView.findViewById(R.id.view_background_chats);
            viewForeground = itemView.findViewById(R.id.view_foreground_chats);
        }

        public void bind(final User user, String uid,final OnItemClickListener listener) {
            textViewUsername.setText(user.getName());
            textViewLastMsg.setText(user.getDesc());
            GlideApp.with(mContext).load(user.getAvatar()).into(imageView);


            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listener.onItemClick(uid);
                }
            });
        }
    }
}