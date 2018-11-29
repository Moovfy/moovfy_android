package com.moovfy.moovfy;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String EXTRA_MESSAGE = "";
    private RecyclerView recyclerListFriends;
    private ListFriendsAdapter adapter;

    List<User> userList = new ArrayList<>();
    List<String> uids = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View layout = inflater.inflate(R.layout.fragment_friends, container, false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListFriends = (RecyclerView) layout.findViewById(R.id.recycleListFriends);
        recyclerListFriends.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        adapter = new ListFriendsAdapter(getContext(), userList, uids,new ListFriendsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String uid) {
                Log.d("UIDagafat: ", "> " + uid);
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(EXTRA_MESSAGE, uid);
                startActivity(intent);
            }
        });
        recyclerListFriends.setAdapter(adapter);
        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                updateList();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflater.inflate(R.menu.menu_calls, menu);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_refresh) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    updateList();
                }
            });
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onRefresh() {
        updateList();
    }

    private void updateList() {
        userList.clear();
        uids.clear();
        String url = "http://10.4.41.143:3000/friends/";
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
    private class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
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
                                String uid = e.getString("uid");


                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user =  dataSnapshot.getValue(User.class);


                                        if(user != null) {
                                            userList.add(new User(
                                                    user.getEmail(),
                                                    user.getUsername(),
                                                    user.getAvatar(),
                                                    user.getName()
                                            ));
                                            uids.add(uid);
                                        }
                                        adapter.notifyDataSetChanged();

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
}

class ListFriendsAdapter extends RecyclerView.Adapter<ListFriendsAdapter.ItemFriendsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String uid);
    }

    private Context mCtx;
    private List<User> userList;
    private List<String> uids;
    private final ListFriendsAdapter.OnItemClickListener listener;

    public ListFriendsAdapter(Context mCtx, List<User> userList, List<String> uids,ListFriendsAdapter.OnItemClickListener listener) {
        this.mCtx = mCtx;
        this.userList = userList;
        this.listener = listener;
        this.uids = uids;
    }
    @NonNull
    @Override
    public ItemFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view =  inflater.inflate(R.layout.item_friend, viewGroup,false);
        return new ItemFriendsViewHolder(view, mCtx);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemFriendsViewHolder itemFriendsViewHolder, int i) {
        itemFriendsViewHolder.bind(userList.get(i),uids.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    class ItemFriendsViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername, textViewDesc;
        ImageView imageView;
        Context mContext;

        public ItemFriendsViewHolder(@NonNull View itemView ,Context context) {
            super(itemView);
            mContext = context;
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewDesc = itemView.findViewById(R.id.textViewShortDesc);
            imageView = itemView.findViewById(R.id.imageView);
        }

        public void bind(final User user, String uid,final OnItemClickListener listener) {
            textViewUsername.setText(user.getName());
            textViewDesc.setText(user.getEmail());
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

