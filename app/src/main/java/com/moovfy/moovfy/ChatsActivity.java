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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
import java.util.regex.Pattern;

import static com.moovfy.moovfy.MessageListAdapter.formatDateTime;

public class ChatsActivity extends AppCompatActivity implements RecyclerItemTouchHelperChats.RecyclerItemTouchHelperChatsListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String EXTRA_MESSAGE = "uid";
    public static final String RELATION = "relation";
    private RecyclerView recyclerListChats;
    private ListChatsAdapter adapter;
    List<User> userList = new ArrayList<>();
    List<String> uids = new ArrayList<>();
    private RelativeLayout relativeLayout;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    DatabaseReference Ref;
    User chatsOberts;
    List<String> listChatsOberts;

    private boolean clicked = false;

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
                intent.putExtra(RELATION, "donotshow");
                intent.putExtra(EXTRA_MESSAGE, uid);
                int idx = uids.indexOf(uid);

                User u = userList.get(idx);
                intent.putExtra("urlAvatar", u.getAvatar());
                intent.putExtra("name", u.getName());
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
                    .make(relativeLayout, name + " removed from Chats!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clicked = true;
                    // undo is selected, restore the deleted item
                    adapter.restoreItem(deletedItem, deletedIndex);

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
            snackbar.addCallback(new Snackbar.Callback(){
                @Override
                public void onShown(Snackbar sb) {
                    super.onShown(sb);
                }

                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);

                    if (!clicked) {
                        borrarDB(deletedIndex, deletedItem);
                    }

                }
            });
        }
    }



    public void borrarDB(int deletedIndex, User deletedItem) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String myuid =  currentUser.getUid();
        String altreuid = uids.get(deletedIndex);
        String chatuid = get_chat_uid(myuid,altreuid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("messages");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d("BOORRAT","element: " + deletedIndex + " " + deletedItem.getName() + uids.get(deletedIndex));
                //Borra els missatges
                //dataSnapshot.getRef().child(chatuid).removeValue();


                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("users").child(myuid).child("ChatsOberts");
                ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("ChatsOberts", dataSnapshot.toString());
                        List<String> co = (List<String>) dataSnapshot.getValue();
                        int index = co.indexOf(chatuid);
                        co.remove(index);
                        ref2.setValue(co);
                        /*
                        User user =  dataSnapshot.getValue(User.class);
                        int index = user.getChatsOberts().indexOf(chatuid);
                        if (index != -1) {
                            user.deleteListItem(index);
                        }
                        ref2.setValue(user);
                        */

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });
/*
                DatabaseReference ref3 = FirebaseDatabase.getInstance().getReference("users").child(altreuid);
                ref3.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user =  dataSnapshot.getValue(User.class);
                        int index = user.getChatsOberts().indexOf(chatuid);
                        if (index != -1) {
                            user.deleteListItem(index);
                        }
                        ref3.setValue(user);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });
                */
                userList.clear();
                uids.clear();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
    public String get_chat_uid(String uid1, String uid2){
        if(uid1.compareTo(uid2) < 0 ){
            return uid1+ "|" + uid2;
        }
        else return uid2+ "|" + uid1;
    }

    @Override
    public void onRefresh() {
        updateList();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void updateList() {

        userList.clear();
        uids.clear();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

        String uid = currentFirebaseUser.getUid();//currentFirebaseUser.getUid();


        Ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
        ValueEventListener usuari1Listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Log.d("ChatsOOOO", dataSnapshot.toString());
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

                    for (String s : ids) {
                        if (!s.equals(currentuid)) {

                            String url = "http://10.4.41.143:3000/users/" + s;
                            uids.add(s);
                            String[] params = {url, chatid};
                            JsonTask t = new JsonTask();
                            t.execute(params);

/*
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(s);
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d("USSSSSS", "> " + dataSnapshot.toString());

                                    User user =  dataSnapshot.getValue(User.class);


                                    uids.add(s);

                                    Query ref2 = FirebaseDatabase.getInstance().getReference("messages").child(chatid).orderByKey().limitToLast(1);
                                    ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            String mm = "";
                                            String time = "";
                                            for (DataSnapshot child : dataSnapshot.getChildren()){

                                                mm = child.child("message").getValue().toString();
                                                time =  child.child("time").getValue().toString();

                                            }
                                            user.setEmail(mm); //ultim missatge guardat en el email
                                            user.setDesc(time);

                                            userList.add(user);

                                            adapter.notifyDataSetChanged();
                                            mSwipeRefreshLayout.setRefreshing(false);

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }

                                    });


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }

                            });
*/

                        }
                    }

                }
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Chat", "loadUser1:onCancelled", databaseError.toException());
            }

        };
        Ref.addValueEventListener(usuari1Listener);

    }



    private class restaurarDB {
        List<Message> msgs;
        User u1;
        User u2;

        public restaurarDB(List<Message> msgs, User u1, User u2) {
            this.msgs = msgs;
            this.u1 = u1;
            this.u2 = u2;
        }

    }

    private class JsonTask extends AsyncTask<String, String, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            HttpURLConnection con = null;
            BufferedReader rd = null;
            try {
                URL purl = new URL(params[0]);
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
                ArrayList<String> list = new ArrayList<>();
                list.add(jsonArrayUser.toString());
                list.add(params[1]);

                return list;


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

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {

            try {
                JSONObject obj = new JSONObject(s.get(0));
                User u = new User(
                        obj.getString("email"),
                        obj.getString("username"),
                        obj.getString("avatar"),
                        obj.getString("complete_name")
                );

                Query ref2 = FirebaseDatabase.getInstance().getReference("messages").child(s.get(1)).orderByKey().limitToLast(1);
                ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String mm = "";
                        String time = "";
                        for (DataSnapshot child : dataSnapshot.getChildren()){

                            mm = child.child("message").getValue().toString();
                            String[] parts = mm.split(Pattern.quote("."));
                           if (parts.length > 0 && parts[0].equals("https://firebasestorage")) {
                               mm = "[Image]";
                           }
                            time =  child.child("time").getValue().toString();
                            u.setEmail(mm); //ultim missatge guardat en el email
                            u.setDesc(time);

                            userList.add(u);

                            adapter.notifyDataSetChanged();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

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
        notifyItemRangeChanged(position, getItemCount() - position);
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

        TextView textViewUsername, textViewLastMsg, textViewLastTime;
        ImageView imageView;
        Context mContext;

        public RelativeLayout viewBackground, viewForeground;

        public ItemChatViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            mContext = context;
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewLastMsg = itemView.findViewById(R.id.textViewLastMsg);
            textViewLastTime = itemView.findViewById(R.id.textViewLastTime);
            imageView = itemView.findViewById(R.id.imageView);

            viewBackground = itemView.findViewById(R.id.view_background_chats);
            viewForeground = itemView.findViewById(R.id.view_foreground_chats);
        }

        public void bind(final User user, String uid,final OnItemClickListener listener) {
            textViewUsername.setText(user.getName());
            textViewLastMsg.setText(user.getEmail());
            Long time = Long.parseLong(user.getDesc(),10);
            textViewLastTime.setText(formatDateTime(time));
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