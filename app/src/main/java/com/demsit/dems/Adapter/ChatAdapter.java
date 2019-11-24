package com.demsit.dems.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.demsit.dems.Model.Message;
import com.demsit.dems.Mydb;
import com.demsit.dems.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private DatabaseReference chatRef;
    private Mydb db;
    private String senderId, receiverId;
    private List messageDate = new ArrayList();
    private List<Message> messages;


    public ChatAdapter(String receiverId, List messages) {
        db = Mydb.getInstance();
        this.receiverId = receiverId;
        this.senderId = db.user.getUid();
        this.chatRef = db.ref.child("Messages");
        this.messages = messages;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatAdapter.MessageViewHolder messageViewHolder, final int i) {
        //final String messageId = getRef(i).getKey(), receiverId = getRef(i).getParent().getKey(), senderId = getRef(i).getParent().getParent().getKey();
        final Context context = messageViewHolder.itemView.getContext();
        final Message message = messages.get(i);
        final String messageId = message.getUid();
        if(message!=null){
            String date = message.getDate().split("[,]")[0];
            messageViewHolder.time.setText(date+" "+message.getTime());
            if(message.getSender().equals(senderId)){
                messageViewHolder.itemView.findViewById(R.id.chat_container_child).setBackgroundResource(R.drawable.chat_mess_s_bg);
                messageViewHolder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                messageViewHolder.content.setTextColor(context.getResources().getColor(R.color.white));
                messageViewHolder.fileName.setTextColor(context.getResources().getColor(R.color.white));
            }else{
                messageViewHolder.content.setTextColor(context.getResources().getColor(R.color.dark));
                messageViewHolder.fileName.setTextColor(context.getResources().getColor(R.color.dark));
            }
            if(message.getType().equals("text")){
                messageViewHolder.content.setText(message.getContent());
            }else if(message.getType().equals("image")){
                messageViewHolder.content.setText("");
                messageViewHolder.content.setVisibility(View.GONE);
                messageViewHolder.image.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getContent()).placeholder(R.drawable.ic_insert_photo).fit().into(messageViewHolder.image);
            }else if(!message.getType().equals("deleted") && (message.getType().equals("pdf") || message.getType().equals("docx"))){
                messageViewHolder.content.setText("");
                messageViewHolder.content.setVisibility(View.GONE);
                messageViewHolder.file.setVisibility(View.VISIBLE);
                messageViewHolder.fileName.setText(message.getInfo());
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewFile  = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getContent()));
                        messageViewHolder.itemView.getContext().startActivity(viewFile);
                    }
                });
            }else if(message.getType().equals("deleted")){
                messageViewHolder.content.setText(context.getString(R.string.message_deleted));
                messageViewHolder.content.setTextColor(context.getResources().getColor(R.color.deleteMessage));

            }

            if(!message.getType().equals("deleted")){
                messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder builder = createBuilder(messageViewHolder);
                        final Context c = messageViewHolder.itemView.getContext();
                        CharSequence op[];
                        if(message.getSender().equals(senderId)){
                            op = new CharSequence[]{c.getString(R.string.delete_for_me), c.getString(R.string.delete_for_evr)};
                        }else{
                            op = new CharSequence[]{c.getString(R.string.delete_for_me)};
                        }
                        builder.setItems(op, new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!message.getType().equals("text")){
                                    if(message.getType().equals("image")){
                                        db.storageRef.child("Images").child(messageId+".jpg").delete();
                                    }else if(message.getType().equals("pdf")){
                                        db.storageRef.child("DocFiles").child(messageId+".pdf").delete();
                                    }else if(message.getType().equals("docx")){
                                        db.storageRef.child("DocFiles").child(messageId+".docx").delete();
                                    }
                                }
                                if(which == 0){
                                    chatRef.child(senderId).child(receiverId).child(messageId).child("type").setValue("deleted");
                                    messageViewHolder.content.setVisibility(View.VISIBLE);
                                    messageViewHolder.image.setVisibility(View.INVISIBLE);
                                    messageViewHolder.file.setVisibility(View.GONE);
                                    messageViewHolder.content.setText(context.getString(R.string.message_deleted));
                                    messageViewHolder.content.setTextColor(context.getResources().getColor(R.color.deleteMessage));
                                }else{
                                    chatRef.child(senderId).child(receiverId).child(messageId).child("type").setValue("deleted");
                                    chatRef.child(receiverId).child(senderId).child(messageId).child("type").setValue("deleted");
                                    messageViewHolder.content.setVisibility(View.VISIBLE);
                                    messageViewHolder.image.setVisibility(View.INVISIBLE);
                                    messageViewHolder.file.setVisibility(View.GONE);
                                    messageViewHolder.content.setText(context.getString(R.string.message_deleted));
                                    messageViewHolder.content.setTextColor(context.getResources().getColor(R.color.deleteMessage));
                                }
                            }
                        });
                        builder.show();
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @NonNull
    @Override
    public ChatAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_chat_item, parent, false);
        return new MessageViewHolder(view);
    }

    public AlertDialog.Builder createBuilder(MessageViewHolder messageViewHolder){
        Context c = messageViewHolder.itemView.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.AlertDialog);
        return builder;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView content, time, messageDate, fileName;
        ImageView image;
        LinearLayout file;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            content = itemView.findViewById(R.id.chat_content);
            time = itemView.findViewById(R.id.chat_time);
            messageDate = itemView.findViewById(R.id.message_date);
            image = itemView.findViewById(R.id.message_image);
            file = itemView.findViewById(R.id.message_file);
        }
    }
}
