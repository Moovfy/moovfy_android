package com.moovfy.moovfy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;

    private Context mContext;
    private List<Message> mMessageList = new ArrayList<>();

    public MessageListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determina el tipus de ViewType depenent de qui envii el missatge
    @Override
    public int getItemViewType(int position) {
        Message message = (Message) mMessageList.get(position);
        String[] parts = message.getMessage().split(Pattern.quote(".")); // Split on period.
        if(parts[0].equals("https://firebasestorage")) {
            Log.d("Chat", "foto");
            Random rand = new Random();

            int  n = rand.nextInt(4) + 2;
            return 3;
        }
        /*
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        if (message.getSender().getFirebase_uid().equals(uid)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
        */
        else {
            Log.d("Chat", "entro en el getItemviewType");
            Random rand = new Random();

            int n = rand.nextInt(2) + 1;
            return 2;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        Log.d("Chat", "Entro en el onCreateViewHolder");
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
        else if (viewType == VIEW_TYPE_IMAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_received, parent, false);
            return new ReceivedImageHolder(view);
        }
        else if (viewType == VIEW_TYPE_IMAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_sent, parent, false);
            return new SentImageHolder(view);
        }
        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) mMessageList.get(position);
        Log.d("Chat", "Entro en el onBindViewHolder");
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_RECEIVED:
                ((ReceivedImageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_SENT:
                ((SentImageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;


        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        }




        void bind(Message message) {
            Log.d("Chat", "Entro a escriure SentMEssageHolder");
            messageText.setText(message.getMessage());
            // Format the stored timestamp into a readable String using method.
            timeText.setText(formatDateTime(message.getTime()));
        }
    }

    private class SentImageHolder extends RecyclerView.ViewHolder {
        TextView  timeText;
        ImageView imageView;
        ProgressBar progressBar;

        SentImageHolder(View itemView) {
            super(itemView);

            progressBar = (ProgressBar) itemView.findViewById(R.id.loading);
            imageView = itemView.findViewById(R.id.mensajeFoto);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get position
                    int pos = getAdapterPosition();
                    Message message = (Message) mMessageList.get(pos);
                    String mensaje = message.getMessage();
                    Log.d("chat", "MESSAGE: " + mensaje);
                    new PhotoFullPopupWindow(mContext, R.layout.view_image, imageView, mensaje, null);
                }
            });
        }




        void bind(Message message) {
            Log.d("Chat", "Entro a mostrar imatge");
            progressBar.setVisibility(View.VISIBLE);

            GlideApp.with(mContext)
                    .load(message.getMessage())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(600, 200)
                    .into(imageView);
            // Format the stored timestamp into a readable String using method.
            timeText.setText(formatDateTime(message.getTime()));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;


        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
            }


        void bind(Message message) {
            Log.d("Chat", "Entro a escriure ReceivedMessageHolder"+ message.getMessage());
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(formatDateTime(message.getTime()));
            nameText.setText(message.getSender().getUsername());
        }
    }

    private class ReceivedImageHolder extends RecyclerView.ViewHolder {
        TextView  timeText, nameText;
        ImageView imageView;
        ProgressBar progressBar;


        ReceivedImageHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.loading);
            imageView = itemView.findViewById(R.id.mensajeFoto);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get position
                    int pos = getAdapterPosition();
                    Message message = (Message) mMessageList.get(pos);
                    String mensaje = message.getMessage();
                    Log.d("chat", "MESSAGE: " + mensaje);
                    new PhotoFullPopupWindow(mContext, R.layout.view_image, imageView, mensaje, null);

                }
            });
        }


        void bind(Message message) {
            Log.d("Chat", "Entro a mostrar imatge rebuda"+ message.getMessage());
            progressBar.setVisibility(View.VISIBLE);
            GlideApp.with(mContext)
                    .load(message.getMessage())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .override(600, 200)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
            // Format the stored timestamp into a readable String using method.
            timeText.setText(formatDateTime(message.getTime()));

            nameText.setText(message.getSender().getUsername());
        }
    }

    public static String formatTime(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    public static String formatDateTime(long timeInMillis) {
        if(isToday(timeInMillis)) {
            return formatTime(timeInMillis);
        } else {
            return formatDate(timeInMillis);
        }
    }

    public static String formatDate(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return dateFormat.format(timeInMillis);
    }

    public static boolean isToday(long timeInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = dateFormat.format(timeInMillis);
        return date.equals(dateFormat.format(System.currentTimeMillis()));
    }

    public void addMessage(Message m){
        Log.d("Chat", "Arribo al añadir mensaje");
        mMessageList.add(m);
        notifyItemInserted( mMessageList.size());
    }
}