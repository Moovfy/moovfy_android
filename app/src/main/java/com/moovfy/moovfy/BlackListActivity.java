package com.moovfy.moovfy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

public class BlackListActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener , SwipeRefreshLayout.OnRefreshListener{

    private RecyclerView recyclerBlackList;
    private BlackListAdapter adapter;
    List<User> userList = new ArrayList<>();
    List<String> uids = new ArrayList<>();

    private RelativeLayout relativeLayout;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean clicked = false;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_list);
        setTitle("Black list");

        relativeLayout = findViewById(R.id.relative_layout_black_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerBlackList = (RecyclerView) findViewById(R.id.recycleBlackList);
        recyclerBlackList.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerBlackList);



        adapter = new BlackListAdapter(this, userList, new BlackListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                Log.d("Listener Activat","Click en l'usuari" + user.getUsername());
            }
        });
        recyclerBlackList.setAdapter(adapter);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                updateList();

            }
        });
    }

    private void updateList() {
        userList.clear();
        uids.clear();
        String url = "https://10.4.41.143:3001/blocked/";
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        if (currentFirebaseUser != null) {
            url += currentFirebaseUser.getUid();
        } else {
            Log.d("APIResponse3: ", "> " + "Usuari null");
        }
        Log.d("UrlRequested: ", "> " + url);
        JsonTask t = new JsonTask();
        t.execute(url);


    }

    @Override
    public void onRefresh() {
        updateList();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();

                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("APIResponse: ", "> " + line);
                }
                try {
                    if (buffer.toString() != null) {

                        JSONArray jsonArray = new JSONArray(buffer.toString());
                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject e = jsonArray.getJSONObject(i);
                                String uid = e.getString("firebase_uid");

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user =  dataSnapshot.getValue(User.class);

                                        userList.add(new User(
                                                user.getEmail(),
                                                user.getUsername(),
                                                user.getAvatar(),
                                                user.getName()
                                        ));
                                        uids.add(uid);
                                        adapter.notifyDataSetChanged();

                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }

                                });


                            }
                        }


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }



                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
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

            mSwipeRefreshLayout.setRefreshing(false);

        }


    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof BlackListAdapter.ItemBlackListViewHolder) {
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
                        //borrar de la llista negra
                        queue = Volley.newRequestQueue(getApplicationContext());
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        String myuid =  currentUser.getUid();
                        JSONObject  obj = new JSONObject();
                        try {
                            obj.put("firebase_uid1",myuid);
                            obj.put("firebase_uid2",uids.get(deletedIndex));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("Unblocking:" , obj.toString());
                        String url = "https://10.4.41.143:3001/relations/unblock";

                        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url, obj,
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
                        queue.add(jsonobj);
                    }
                }
            });
        }
    }
}

class BlackListAdapter extends RecyclerView.Adapter<BlackListAdapter.ItemBlackListViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    private Context mCtx;
    private List<User> userList;
    private final OnItemClickListener listener;

    public BlackListAdapter(Context mCtx, List<User> userList, OnItemClickListener listener) {
        this.mCtx = mCtx;
        this.userList = userList;
        this.listener = listener;
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
    public ItemBlackListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view =  inflater.inflate(R.layout.item_black_list, viewGroup,false);
        return new ItemBlackListViewHolder(view,mCtx);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemBlackListViewHolder itemCloseViewHolder, int i) {

        itemCloseViewHolder.bind(userList.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }




    class ItemBlackListViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername, textViewDesc;
        ImageView imageView;
        public RelativeLayout viewBackground, viewForeground;
        Context mContext;


        public ItemBlackListViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewDesc = itemView.findViewById(R.id.textViewShortDesc);
            imageView = itemView.findViewById(R.id.imageView);

            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            this.mContext = context;
        }

        public void bind(final User user, final OnItemClickListener listener) {
            textViewUsername.setText(user.getName());
            textViewDesc.setText(user.getEmail());
            GlideApp.with(mContext).load(user.getAvatar()).into(imageView);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listener.onItemClick(user);
                }
            });
        }
    }
}