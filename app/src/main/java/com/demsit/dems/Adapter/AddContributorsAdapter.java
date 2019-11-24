package com.demsit.dems.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddContributorsAdapter extends FirebaseRecyclerAdapter<User, AddContributorsAdapter.UsersViewHolder> {
    private String teamId;
    private DatabaseReference usersRef, contributorsRef, teamUsersRef;
    private MutableLiveData<List<String>> contributors;
    private Mydb db;
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public AddContributorsAdapter(@NonNull FirebaseRecyclerOptions<User> options, String t) {
        super(options);
        db = Mydb.getInstance();
        teamId = t;
        contributors = new MutableLiveData<List<String>>();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contributorsRef = FirebaseDatabase.getInstance().getReference().child("Teams").child(teamId).child("Contributors");
        teamUsersRef = FirebaseDatabase.getInstance().getReference().child("TeamUsers");
        contributorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List contributorsC = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    contributorsC.add(child.getKey());
                }
                contributors.setValue(contributorsC);
                System.out.println(Arrays.toString(contributors.getValue().toArray()));
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onBindViewHolder(@NonNull final UsersViewHolder usersViewHolder, int i, @NonNull User user) {
        final String userId = getRef(i).getKey(), currentUserId = getRef(i).getParent().getKey();
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final String name = dataSnapshot.child("name").getValue().toString();
                usersViewHolder.name.setText(name);
                usersViewHolder.speciality.setText(dataSnapshot.child("speciality").getValue().toString());
                if(dataSnapshot.child("image").exists()){
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(usersViewHolder.image);
                }
                if(contributors.getValue().contains(userId)){
                    usersViewHolder.isContributor.setVisibility(View.VISIBLE);
                }else{
                    usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Context c = usersViewHolder.itemView.getContext();
                            CharSequence op[] = new CharSequence[]{c.getString(R.string.alert_addC_add)};
                            AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.AlertDialog);
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
                                        contributorsRef.child(userId).child("status").setValue("active").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Map contributorSaved = new HashMap<>();
                                                    contributorSaved.put("status", "active");
                                                    contributorSaved.put("lastMessage", db.d2099 - Calendar.getInstance().getTimeInMillis());
                                                    teamUsersRef.child(userId).child(teamId).setValue(contributorSaved);
                                                }
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_add_contributors_item, parent, false);
        return new UsersViewHolder(view);
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, speciality;
        ImageView isContributor;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.add_contributor_name);
            image = itemView.findViewById(R.id.add_contributor_image);
            speciality = itemView.findViewById(R.id.add_contributor_speciality);
            isContributor = itemView.findViewById(R.id.is_contributor);
        }
    }
}
