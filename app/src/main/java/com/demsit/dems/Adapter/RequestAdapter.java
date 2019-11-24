
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends FirebaseRecyclerAdapter<User, RequestAdapter.UsersViewHolder> {

    private DatabaseReference usersRef, requestRef, contactsRef;
    private Mydb db;
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public RequestAdapter(@NonNull FirebaseRecyclerOptions<User> options) {
        super(options);
        db = Mydb.getInstance();
        usersRef = db.ref.child("Users");
        contactsRef = db.ref.child("Contacts");
        requestRef = db.ref.child("Requests");
    }

    @Override
    protected void onBindViewHolder(@NonNull final UsersViewHolder usersViewHolder, int i, @NonNull User user) {
        final String userId = getRef(i).getKey(), currentUserId = getRef(i).getParent().getKey();
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                final String name = dataSnapshot.child("name").getValue().toString();
                usersViewHolder.userName.setText(name);
                usersViewHolder.userSpeciality.setText(dataSnapshot.child("speciality").getValue().toString());
                if(dataSnapshot.child("image").exists()){
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.ic_account).into(usersViewHolder.image);
                }else{
                    Picasso.get().load("t").placeholder(R.drawable.ic_account).into(usersViewHolder.image);
                }
                usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Context c = usersViewHolder.itemView.getContext();
                        CharSequence op[] = new CharSequence[]{c.getString(R.string.alert_request_accept), c.getString(R.string.alert_request_decline)};
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        //builder.setTitle(name);
                        TextView title = new TextView(c);
                        title.setText(name);
                        title.setPadding(0,0,3, 3);
                        title.setGravity(Gravity.CENTER);
                        builder.setCustomTitle(title);
                        builder.setItems(op, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    Map contactSaved = new HashMap<>();
                                    contactSaved.put("contact_status", "saved");
                                    contactSaved.put("lastMessage", db.d2099 - Calendar.getInstance().getTimeInMillis());
                                    contactSaved.put("seen", "not_seen");
                                    contactsRef.child(currentUserId).child(userId).setValue(contactSaved).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                requestRef.child(currentUserId).child(userId).removeValue();
                                                Toast.makeText(c, name+" add to contacts", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    contactsRef.child(userId).child(currentUserId).setValue(contactSaved);
                                }
                                if(which == 1){
                                    requestRef.child(currentUserId).child(userId).removeValue();
                                    Toast.makeText(c, "contact cancel", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                        builder.show();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_request_item, parent, false);
        return new UsersViewHolder(view);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userSpeciality;
        CircleImageView image;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            this.userName = itemView.findViewById(R.id.user_request_name);
            this.userSpeciality = itemView.findViewById(R.id.user_request_speciality);
            this.image = itemView.findViewById(R.id.user_request_image);

        }
    }
}
