package com.demsit.dems.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.demsit.dems.Model.Message;
import com.demsit.dems.Model.Team;
import com.demsit.dems.Mydb;
import com.demsit.dems.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TeamChatAdapter  extends RecyclerView.Adapter<TeamChatAdapter.TeamChatViewHolder> {

    private DatabaseReference teamChatRef;
    private String teamId;
    private String userId;
    private Mydb db;
    private List<Message> messages;

    public TeamChatAdapter(String teamId, List<Message> messages) {
        db = Mydb.getInstance();
        this.teamId = teamId;
        this.messages = messages;
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        teamChatRef = FirebaseDatabase.getInstance().getReference().child("Teams").child(teamId).child("Messages");
    }

    @Override
    public void onBindViewHolder(@NonNull final TeamChatViewHolder teamChatViewHolder, final int i) {
        //final String chatId = getRef(i).getKey();
        final Context context = teamChatViewHolder.itemView.getContext();

        final Message chat = messages.get(i);
        if(chat!=null){
            final String chatId = chat.getUid();
            String date = chat.getDate().split("[,]")[0];
            teamChatViewHolder.time.setText(date+" "+chat.getTime());
            if(chat.getSender().equals(db.user.getUid())){
                teamChatViewHolder.itemView.findViewById(R.id.team_chat_container_child).setBackgroundResource(R.drawable.chat_mess_s_bg);
                teamChatViewHolder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                teamChatViewHolder.content.setTextColor(context.getResources().getColor(R.color.white));
                teamChatViewHolder.fileName.setTextColor(context.getResources().getColor(R.color.white));
                teamChatViewHolder.time.setTextColor(context.getResources().getColor(R.color.white));
                teamChatViewHolder.isCurrentUser();
            }else{
                teamChatViewHolder.content.setTextColor(context.getResources().getColor(R.color.dark));
                teamChatViewHolder.time.setTextColor(context.getResources().getColor(R.color.dark));
                teamChatViewHolder.fileName.setTextColor(context.getResources().getColor(R.color.dark));
                teamChatViewHolder.senderName.setText(chat.getSenderName());
            }
            if(chat.getType().equals("text")){
                teamChatViewHolder.content.setText(chat.getContent());
            }else if(chat.getType().equals("image")){

                teamChatViewHolder.content.setText("");
                teamChatViewHolder.content.setVisibility(View.GONE);
                teamChatViewHolder.image.setVisibility(View.VISIBLE);
                Picasso.get().load(chat.getContent()).placeholder(R.drawable.ic_insert_photo).fit().into(teamChatViewHolder.image);
            }else if (chat.getType().equals("pdf") || chat.getType().equals("docx")){
                teamChatViewHolder.content.setVisibility(View.GONE);
                teamChatViewHolder.messageFile.setVisibility(View.VISIBLE);
                teamChatViewHolder.fileName.setText(chat.getInfo());
                teamChatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewFile  = new Intent(Intent.ACTION_VIEW, Uri.parse(chat.getContent()));
                        teamChatViewHolder.itemView.getContext().startActivity(viewFile);
                    }
                });
            }
            else if(chat.getType().equals("deleted")){
                teamChatViewHolder.content.setText(context.getString(R.string.message_deleted));
                teamChatViewHolder.content.setTextColor(context.getResources().getColor(R.color.deleteMessage));
            }

            if(!chat.getType().equals("deleted") && chat.getSender().equals(userId)){
                teamChatViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder builder = createBuilder(teamChatViewHolder);
                        final Context c = teamChatViewHolder.itemView.getContext();
                        CharSequence op[];
                        op = new CharSequence[]{c.getString(R.string.delete_message)};
                        builder.setItems(op, new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    if(!chat.getType().equals("text")){
                                        if(chat.getType().equals("image")){
                                            db.storageRef.child("Images/"+chatId+".jpg").delete();
                                        }else if(chat.getType().equals("pdf")){
                                            db.storageRef.child("DocFiles/"+chatId+".pdf").delete();
                                        }else if(chat.getType().equals("docx")){
                                            db.storageRef.child("DocFiles/"+chatId+".docx").delete();
                                        }
                                    }
                                    teamChatRef.child(chatId).child("type").setValue("deleted");
                                    teamChatViewHolder.content.setVisibility(View.VISIBLE);
                                    teamChatViewHolder.image.setVisibility(View.INVISIBLE);
                                    teamChatViewHolder.messageFile.setVisibility(View.GONE);
                                    teamChatViewHolder.content.setText(context.getString(R.string.message_deleted));
                                    teamChatViewHolder.content.setTextColor(context.getResources().getColor(R.color.deleteMessage));
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




    @NonNull
    @Override
    public TeamChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_team_chat_item, parent, false);
        return new TeamChatViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public AlertDialog.Builder createBuilder(TeamChatViewHolder messageViewHolder){
        Context c = messageViewHolder.itemView.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.AlertDialog);
        return builder;
    }

    class TeamChatViewHolder extends RecyclerView.ViewHolder{
        TextView content, date, time, senderName, fileName;
        ImageView image;
        LinearLayout messageFile;

        TeamChatViewHolder(View itemView){
            super(itemView);
            this.content = itemView.findViewById(R.id.team_chat_content);
            this.senderName = itemView.findViewById(R.id.team_chat_sender);
            this.time = itemView.findViewById(R.id.team_chat_time);
            this.date = itemView.findViewById(R.id.message_date);
            this.image = itemView.findViewById(R.id.message_image);
            this.messageFile = itemView.findViewById(R.id.message_file);
            this.fileName = itemView.findViewById(R.id.file_name);
        }

        public void isCurrentUser() {
            itemView.findViewById(R.id.team_chat_container_child).setBackgroundResource(R.drawable.chat_mess_s_bg);
            itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            senderName.setText("");
        }
    }

}
