package com.moovfy.moovfy;

import android.content.Context;
import android.content.Intent;
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
    List<Usuario> userList;

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

        userList = new ArrayList<>();
        userList.add(new Usuario(
                "Usuario1",
                "Descripcion del user1",
                R.drawable.icono
        ));
        userList.add(new Usuario(
                "Usuario2",
                "Descripcion del user2",
                R.drawable.icono
        ));
        userList.add(new Usuario(
                "Usuario3",
                "Descripcion del user3",
                R.drawable.icono
        ));
        userList.add(new Usuario(
                "Usuario4",
                "Descripcion del user4",
                R.drawable.icono
        ));
        userList.add(new Usuario(
                "Usuario5",
                "Descripcion del user5",
                R.drawable.icono
        ));
        userList.add(new Usuario(
                "Usuario6",
                "Descripcion del user6",
                R.drawable.icono
        ));
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
        userList.clear();
        Random rand = new Random();
        int n = rand.nextInt(10) + 1;
        for (int i = 0; i < n; i++) {
            userList.add(new Usuario(
                    "Usuario " + i,
                    "Descripcion del user " + i,
                    R.drawable.icono
            ));
        }
        adapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
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

