package com.moovfy.moovfy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
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
import com.moovfy.moovfy.map.MapFragment;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class CloseFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String EXTRA_MESSAGE = "uid";
    public static final String RELATION = "relation";
    private User me = new User();

    private RecyclerView recyclerListClose;
    private ListCloseAdapter adapter;

    List<User> userList = new ArrayList<>();
    List<String> uids = new ArrayList<>();
    List<String> friends = new ArrayList<>(); //lista dels uids dels friends
    private SwipeRefreshLayout mSwipeRefreshLayout1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View layout = inflater.inflate(R.layout.fragment_close, container, false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListClose = (RecyclerView) layout.findViewById(R.id.recycleListClose);
        recyclerListClose.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout1 = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout1);
        mSwipeRefreshLayout1.setOnRefreshListener(this);
/*
        Registrar_usuari_BD("homer@simpson.com", "homersimpson", "3","http://firebasestorage.googleapis.com/v0/b/moovfy.appspot.com/o/default-avatar-2.jpg?alt=media&token=fb78f411-b713-4365-9514-d82e6725cb62", "Homer Simpson");
        Registrar_usuari_BD("marge@simpson.com", "margesimpson", "4","http://firebasestorage.googleapis.com/v0/b/moovfy.appspot.com/o/default-avatar-2.jpg?alt=media&token=fb78f411-b713-4365-9514-d82e6725cb62", "Marge Simpson");
        Registrar_usuari_BD("vallsortizpol@gmail.com", "polvallsortiz", "sIGgaYLgSxSXUelMuj7KqLle6FX2","http://firebasestorage.googleapis.com/v0/b/moovfy.appspot.com/o/default-avatar-2.jpg?alt=media&token=fb78f411-b713-4365-9514-d82e6725cb62", "Pol Valls");
*/
        adapter = new ListCloseAdapter(getContext(), userList,uids, new ListCloseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String uid) {
                Log.d("UIDagafat: ", "> " + uid);

                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(EXTRA_MESSAGE, uid);
                if (friends.contains(uid)) {
                    intent.putExtra(CloseFragment.RELATION, "ok");
                } else {
                    intent.putExtra(CloseFragment.RELATION, "no");
                }
                int idx = uids.indexOf(uid);
                User u = userList.get(idx);
                intent.putExtra("urlAvatar", u.getAvatar());
                intent.putExtra("name", u.getName());

                startActivity(intent);
            }
        });

        recyclerListClose.setAdapter(adapter);
        mSwipeRefreshLayout1.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout1.setRefreshing(true);
                Log.d("AAAAAAAAAAAAAAAAA", "porque");
                updateList();
            }
        });
        Log.d("Cargant:", "CloseFragment");
        return layout;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       // inflater.inflate(R.menu.menu_calls, menu);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_refresh) {
            mSwipeRefreshLayout1.post(new Runnable() {
                @Override
                public void run() {

                    mSwipeRefreshLayout1.setRefreshing(true);
                    Log.d("aaaaaa222aa","manual refrehs");
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
        String url = "http://10.4.41.143:3000/near/";
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        if (currentFirebaseUser != null) {
            url += currentFirebaseUser.getUid();
        } else {
            Log.d("APIResponse3: ", "> " + "Usuari null");
        }
        Log.d("UrlRequested222aa: ", "> " + url);
        JsonTask t = new JsonTask();
        t.execute(url);


    }

    public void Registrar_usuari_BD(String email,String usern,String firebase_uid,String urlfoto, String name){
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        User usuari = new User(email,usern,urlfoto,name);
        mDatabase.child("users").child(firebase_uid).setValue(usuari);
        //User usuari = new User(email, usern,urlfoto);

    }
    private class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuffer buffer = new StringBuffer();
            Log.d("Aquiiii22", "hol2a");
            try {

                URL url = new URL(params[0]);
                Log.d("URLCOnection", url.toString());
                connection = (HttpURLConnection) url.openConnection();

                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));


                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Aquiiii22", "hola");
                    Log.d("APIResponse222zzz: ", "> " + line);
                }
                try {
                    if (buffer.toString() != null) {

                        JSONArray jsonArray = new JSONArray(buffer.toString());

                        //-------Buscar la info del propi usuari.
                        JSONObject obj = new JSONObject();
                        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                        String current_uid = "";
                        if (currentFirebaseUser != null) {
                            current_uid = currentFirebaseUser.getUid();
                        } else {
                            Log.d("UIDNULL: ", "> " + "Usuari null");
                        }
                        obj.put("uid", current_uid);
                        obj.put("relation", "myself");
                        //-----------------------------------------------------

                        jsonArray.put(obj);
                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject e = jsonArray.getJSONObject(i);
                                String uid = e.getString("uid");

                                //afegeix a la llista de friends tots els friends
                                if (e.getString("relation").equals("ok")){
                                    friends.add(uid);
                                }

                                HttpURLConnection con = null;
                                BufferedReader rd = null;
                                try {
                                    URL purl = new URL("http://10.4.41.143:3000/users/" + uid);
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
                                    if (!e.getString("relation").equals("myself")) {

                                        userList.add(new User(
                                                jsonArrayUser.getString("email"),
                                                jsonArrayUser.getString("username"),
                                                jsonArrayUser.getString("avatar"),
                                                jsonArrayUser.getString("complete_name")
                                        ));
                                        uids.add(uid);

                                    }
                                    else {
                                        me = new User(
                                                jsonArrayUser.getString("email"),
                                                jsonArrayUser.getString("username"),
                                                jsonArrayUser.getString("avatar"),
                                                jsonArrayUser.getString("complete_name")
                                        );
                                    }
                                } finally {
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

/*
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
                                                Log.d("Avatarurl", user.getEmail() + user.getAvatar());
                                                //--------------------------------------------
                                                /*
                                                Intent ii = new Intent("action_location_updated");
                                                ii.putExtra("name", user.getName());
                                                ii.putExtra("avatar", user.getAvatar());
                                               // ii.putExtra("loc", )
                                                getActivity().sendBroadcast(ii);
                                                */
                                                //-------------------------------------
                                /*
                                            }
                                            adapter.notifyDataSetChanged();
                                            mSwipeRefreshLayout.setRefreshing(false);

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }

                                    });
*/

                            }
                        }


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.d("BufferTo", buffer.toString());
                return buffer.toString();

            } catch (Exception e) {
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
            Log.d("BufferTo2", buffer.toString());
            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("BufferTo233", s);
            Log.d("UrlRequestedss: ", "> " + s);
            adapter.notifyDataSetChanged();

            mSwipeRefreshLayout1.setRefreshing(false);

            getActivity().sendBroadcast(new Intent("cargarNear"));
        }


    }


}

class ListCloseAdapter extends RecyclerView.Adapter<ListCloseAdapter.ItemCloseViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String uid);
    }

    private Context mCtx;
    private List<User> userList;
    private List<String> uids;
    private final OnItemClickListener listener;

    public ListCloseAdapter(Context mCtx, List<User> userList,List<String> uids, OnItemClickListener listener) {
        this.mCtx = mCtx;
        this.userList = userList;
        this.listener = listener;
        this.uids = uids;
    }

    @NonNull
    @Override
    public ItemCloseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view =  inflater.inflate(R.layout.item_friend, viewGroup,false);
        return new ItemCloseViewHolder(view, mCtx);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemCloseViewHolder itemCloseViewHolder, int i) {

        itemCloseViewHolder.bind(userList.get(i),uids.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    class ItemCloseViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername, textViewDesc;
        ImageView imageView;
        Context mContext;

        public ItemCloseViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            mContext = context;
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewDesc = itemView.findViewById(R.id.textViewShortDesc);
            imageView = itemView.findViewById(R.id.imageView);
        }

        public void bind(final User user, String uid, final OnItemClickListener listener) {
            textViewUsername.setText(user.getName());
            textViewDesc.setText(user.getEmail());
            GlideApp.with(mContext).load(user.getAvatar()).thumbnail(0.1f).into(imageView);


            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listener.onItemClick(uid);
                }
            });
        }
    }
}

