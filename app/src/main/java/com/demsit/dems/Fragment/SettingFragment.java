package com.demsit.dems.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import de.hdodenhof.circleimageview.CircleImageView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.demsit.dems.Adapter.TeamAdapter;
import com.demsit.dems.Model.Team;
import com.demsit.dems.Model.User;
import com.demsit.dems.R;
import com.demsit.dems.ViewModel.TeamVM;
import com.demsit.dems.ViewModel.UserVM;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;


public class SettingFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private Button updateUserButton;
    private String userId;
    private EditText userNameF, userSpecialityF;
    private TextView userNameT, userSpecialityT;
    private String userImageLink;
    private CircleImageView imageUser;
    private View view;
    private UserVM userVM;
    private final int GalleryPick =  1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();
        view = inflater.inflate(R.layout.fragment_setting, container, false);
        userVM = ViewModelProviders.of(this).get(UserVM.class);
        userVM.getUserInfo().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                updateUserInfoView(user);
            }
        });
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateUserButton = view.findViewById(R.id.update_user_button);
        userNameF = view.findViewById(R.id.update_user_name);
        userSpecialityF = view.findViewById(R.id.update_user_speciality);
        imageUser = view.findViewById(R.id.update_user_image);
        updateUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVM.updateUserInfo(userNameF.getText().toString(), userSpecialityF.getText().toString(), userImageLink);
            }
        });
        imageUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");

                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
        userNameT = view.findViewById(R.id.user_name_info);
        userSpecialityT = view.findViewById(R.id.user_speciality_info);

    }


    private void updateUserInfoView(User user){
        userNameT.setText(user.getName());
        userSpecialityT.setText(user.getSpeciality());
        userImageLink = user.getImage();
        Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_account).into(imageUser);

    }

    private void showMessage(int id){
        Toast.makeText(view.getContext().getApplicationContext(), getString(id), Toast.LENGTH_SHORT).show();

    }

    private void showMessage(String s){
        Toast.makeText(view.getContext().getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPick && resultCode == RESULT_OK && data!=null){

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri imageUri = result.getUri();
            Bitmap bitmap = BitmapFactory.decodeStream(getInputStreamFromUri(imageUri));
            //Bitmap bitmap =  result.getBitmap();
            ByteArrayOutputStream baos =  new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imagesBytes = baos.toByteArray();
            if(resultCode == RESULT_OK){
                userVM.storeImage(imagesBytes);
            }else{
                userVM.storeImage(imagesBytes);

            }
        }
    }

    private InputStream getInputStreamFromUri(Uri uri) {
        try {
            File imageFile = new File(uri.getPath());
            return new FileInputStream(imageFile);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "getInputStreamFromUri: exception while opening input stream from file. try from content resolver");
        }

        try {
            return getContext().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getInputStreamFromUri: exception while opening input stream from content resolver. returning");
        }

        return null;
    }
}
