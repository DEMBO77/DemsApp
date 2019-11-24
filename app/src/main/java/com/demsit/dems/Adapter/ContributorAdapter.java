package com.demsit.dems.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.demsit.dems.Model.User;
import com.demsit.dems.Mydb;
import com.demsit.dems.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContributorAdapter extends FirebaseRecyclerAdapter<User,ContributorAdapter.UsersViewHolder> {

    private DatabaseReference usersRef, teamRef, teamUsersRef, contactsRef, requestsRef;
    private String teamId, currentUserId; Mydb db;
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ContributorAdapter(@NonNull FirebaseRecyclerOptions<User> options, String t) {
        super(options);
        db = Mydb.getInstance();
        teamId = t;
        currentUserId = db.user.getUid();
        usersRef = db.ref.child("Users");
        teamRef = db.ref.child("Teams").child(teamId);
        teamUsersRef = db.ref.child("TeamUsers");
        requestsRef = db.ref.child("Requests");
        contactsRef = db.ref.child("Contacts").child(currentUserId);

    }

    @Override
    protected void onBindViewHolder(@NonNull final ContributorAdapter.UsersViewHolder usersViewHolder, int i, @NonNull final User user) {
        final String userId = getRef(i).getKey();
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String name = dataSnapshot.child("name").getValue().toString();
                usersViewHolder.userName.setText(name);
                if(dataSnapshot.child("image").exists()){
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.ic_account).into(usersViewHolder.image);
                }
                teamRef.child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!currentUserId.equals(userId)){
                            if(dataSnapshot.getValue().toString().equals(currentUserId)){
                                usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        AlertDialog.Builder builder = createBuilder(usersViewHolder, name);
                                        final Context c = usersViewHolder.itemView.getContext();
                                        CharSequence op[] = new CharSequence[]{c.getString(R.string.alert_remove_contributor)};
                                        builder.setItems(op, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if(which == 0){
                                                    teamRef.child("Contributors").child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                teamUsersRef.child(userId).child(teamId).removeValue();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });
                            }else{
                                usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        AlertDialog.Builder builder = createBuilder(usersViewHolder, name);
                                        final Context c = usersViewHolder.itemView.getContext();
                                        CharSequence op[] = new CharSequence[]{c.getString(R.string.alert_add_contributor_to_contact)};
                                        builder.setItems(op, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if(which == 0){
                                                    contactsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if(!dataSnapshot.exists()){
                                                                requestsRef.child(userId).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        if(!dataSnapshot.exists()){
                                                                            requestsRef.child(userId).child(currentUserId).child("request_status").setValue("received");
                                                                            showMessage(c, R.string.request_sent);
                                                                        }else{
                                                                            showMessage(c, R.string.request_alr_sent);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            }else{
                                                                showMessage(c, R.string.alr_contact);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });
                            }
                        }

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

    }

    public AlertDialog.Builder createBuilder(UsersViewHolder usersViewHolder, String titleName){
        Context c = usersViewHolder.itemView.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.AlertDialog);
        //builder.setTitle(name);
        TextView title = new TextView(c);
        title.setText(titleName);
        title.setPadding(0,0,3, 3);
        title.setGravity(Gravity.CENTER);
        builder.setCustomTitle(title);
        return builder;
    }

    @NonNull
    @Override
    public ContributorAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_team_contributors_item, parent, false);
        return new UsersViewHolder(view);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        TextView userName;
        CircleImageView image;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            this.userName = itemView.findViewById(R.id.contributor_name);
            this.image = itemView.findViewById(R.id.contributor_image);

        }
    }

    private void showMessage(Context c, int id){
        Toast.makeText(c, c.getString(id), Toast.LENGTH_SHORT).show();

    }

    private void showMessage(Context c, String s){
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }
}
