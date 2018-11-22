package com.moovfy.moovfy;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
import java.util.Random;

import static android.provider.AlarmClock.EXTRA_MESSAGE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CloseFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerListClose;
    private ListCloseAdapter adapter;
    List<Usuario> userList = new ArrayList<>();

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View layout = inflater.inflate(R.layout.fragment_close, container, false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListClose = (RecyclerView) layout.findViewById(R.id.recycleListClose);
        recyclerListClose.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        String url = "http://10.4.41.143:3000/near/";
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        if (currentFirebaseUser != null) {
            url += currentFirebaseUser.getUid();
        } else {
            Log.d("APIResponse3: ", "> " + "Usuari null");
        }
        Log.d("UrlRequested: ", "> " + url);
        new JsonTask().execute(url);

        adapter = new ListCloseAdapter(getContext(), userList, new ListCloseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Usuario user) {
                Log.d("Listener Activat","Click en l'usuari" + user.getUsername());
                Intent intent = new Intent(getContext(),ChatActivity.class);
                startActivity(intent);
            }
        });
        recyclerListClose.setAdapter(adapter);
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
            updateList();
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onRefresh() {
        updateList();
    }

    private void updateList() {
        String url = "http://10.4.41.143:3000/near/";
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        if (currentFirebaseUser != null) {
            url += currentFirebaseUser.getUid();
        } else {
            Log.d("APIResponse3: ", "> " + "Usuari null");
        }
        Log.d("UrlRequested: ", "> " + url);
        new JsonTask().execute(url);
        adapter.notifyDataSetChanged();
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
            userList.clear();
            try {
                if (s != null) {
                    JSONArray jsonArray = new JSONArray(s);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject e = jsonArray.getJSONObject(i);
                        userList.add(new Usuario(
                                "Usuario con uid " + e.getString("uid"),
                                "Descripcion: Relacio: " + e.getString("relation"),
                                R.drawable.icono
                        ));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("APIResponse2: ", "> " + s);



        }
    }
}

class ListCloseAdapter extends RecyclerView.Adapter<ListCloseAdapter.ItemCloseViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Usuario user);
    }

    private Context mCtx;
    private List<Usuario> userList;
    private final OnItemClickListener listener;

    public ListCloseAdapter(Context mCtx, List<Usuario> userList, OnItemClickListener listener) {
        this.mCtx = mCtx;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemCloseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view =  inflater.inflate(R.layout.item_friend, viewGroup,false);
        return new ItemCloseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemCloseViewHolder itemCloseViewHolder, int i) {

        itemCloseViewHolder.bind(userList.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    class ItemCloseViewHolder extends RecyclerView.ViewHolder {

        TextView textViewUsername, textViewDesc;
        ImageView imageView;

        public ItemCloseViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewDesc = itemView.findViewById(R.id.textViewShortDesc);
            imageView = itemView.findViewById(R.id.imageView);
        }

        public void bind(final Usuario user, final OnItemClickListener listener) {
            textViewUsername.setText(user.getUsername());
            textViewDesc.setText(user.getDesc());
            imageView.setImageDrawable(mCtx.getResources().getDrawable(user.getIcon()));

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listener.onItemClick(user);
                }
            });
        }
    }
}

