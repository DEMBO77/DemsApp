package com.demsit.dems.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.demsit.dems.ChatActivity;
import com.demsit.dems.Model.User;
import com.demsit.dems.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends FirebaseRecyclerAdapter<User, ContactAdapter.UsersViewHolder> {
    private DatabaseReference usersRef, contactsRef;
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ContactAdapter(@NonNull FirebaseRecyclerOptions<User> options) {
        super(options);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
    }

    @Override
    protected void onBindViewHolder(@NonNull final UsersViewHolder usersViewHolder,final int i, @NonNull User user) {
        final String userId = getRef(i).getKey(), currentUserId = getRef(i).getParent().getKey();
        contactsRef.child(currentUserId).child(userId).child("seen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getValue().toString().equals("not_seen")){
                    usersViewHolder.newMessage.setVisibility(View.VISIBLE);
                }else if(dataSnapshot.getValue().toString().equals("seen")){
                    usersViewHolder.newMessage.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String name = dataSnapshot.child("name").getValue().toString();
                final String[] image = {"image"};
                usersViewHolder.name.setText(dataSnapshot.child("name").getValue().toString());
                usersViewHolder.speciality.setText(dataSnapshot.child("speciality").getValue().toString());
                if(dataSnapshot.child("image").exists()){
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(usersViewHolder.image);
                    image[0] = dataSnapshot.child("image").getValue().toString();
                }
                if(dataSnapshot.child("State").child("status").getValue().toString().equals("online")){
                    usersViewHolder.state.setBackgroundResource(R.drawable.online_bg);
                }
                usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contactsRef.child(currentUserId).child(userId).child("seen").setValue("seen");
                        Context c = usersViewHolder.itemView.getContext();
                        Intent chatIntent = new Intent(c, ChatActivity.class);
                        chatIntent.putExtra("SENDER_UID", currentUserId);
                        chatIntent.putExtra("RECEIVER_UID", userId);
                        chatIntent.putExtra("RECEIVER_NAME", name);
                        chatIntent.putExtra("RECEIVER_IMAGE", image[0]);
                        c.startActivity(chatIntent);
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
        View view = inflater.inflate(R.layout.list_contact_item, parent, false);
        return new UsersViewHolder(view);
    }


    public class UsersViewHolder extends RecyclerView.ViewHolder {
        TextView name, speciality, state, newMessage;
        CircleImageView image;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contact_name);
            speciality = itemView.findViewById(R.id.contact_speciality);
            image = itemView.findViewById(R.id.contact_image);
            state = itemView.findViewById(R.id.contact_state);
            newMessage = itemView.findViewById(R.id.new_message);
        }
    }
}
