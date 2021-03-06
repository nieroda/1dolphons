package com.example.btcpro.dolphons;

import android.content.DialogInterface;
import android.content.Intent;
//import android.database.Cursor;
//import android.graphics.BitmapFactory;
import android.net.Uri;
//import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.EditText;
import android.widget.EditText;
import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.TextView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.EmailAuthCredential;
//import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
//import java.util.HashMap;
//import java.util.Map;

public class editUserProfile extends AppCompatActivity {

    private ImageButton profilePicture;
    private EditText editName;
    private TextView userEmail;
    private FirebaseFirestore FireStore;
    private FirebaseUser user;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    private static int RESULT_LOAD_IMAGE = 1;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        profilePicture = findViewById(R.id.imagebuttonProfilePicture);
        editName = findViewById(R.id.edittextName);
        userEmail = findViewById(R.id.textviewEmail);
        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        databaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        user = FirebaseAuth.getInstance().getCurrentUser();
        FireStore = FirebaseFirestore.getInstance();
        if(user != null){
            if(user.getDisplayName() != null){
                editName.setText(user.getDisplayName());
            }
            if(user.getPhotoUrl() != null){
                //profilePicture.setImageURI(user.getPhotoUrl()); //works but is slow
                Picasso.with(editUserProfile.this).load(user.getPhotoUrl()).into(profilePicture);
            }
            if(user.getEmail() != null){
                userEmail.setText(user.getEmail());
            }
        }
        else{
            //User is not signed in, return to Login screen.
            openLogin();
        }

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(i, RESULT_LOAD_IMAGE);
                openFileChooser();
            }
        });
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(editName.getText().toString())
                        .build();

                user.updateProfile(profileUpdates);
            }
        });
    }

    private void openFileChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadFile()
    {
        if (user.getPhotoUrl() != null)
        {
            //StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            final StorageReference fileReference = storageRef.child(user.getPhotoUrl().toString());

            fileReference.putFile(user.getPhotoUrl())
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            //imageUri = Uri.parse(fileReference.getDownloadUrl().toString());
                            Toast.makeText(editUserProfile.this, "Profile Picture successfully updated.", Toast.LENGTH_LONG).show();
                            Upload upload = new Upload(taskSnapshot.getDownloadUrl().toString());
                            String uploadId = databaseRef.push().getKey();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(taskSnapshot.getDownloadUrl())
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //Log.d(TAG, "User profile picture updated.");
                                                //writeUserData(name);
                                                //updateUI(true);
                                            }
                                        }
                                    });

                            databaseRef.child(uploadId).setValue(upload);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            //pop up notification needed
                            Toast.makeText(editUserProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else
        {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    public void openWelcome(View v){
        //uploadFile();
        Intent intent = new Intent(this, welcome.class);
        startActivity(intent);
    }
    public void openChangePassword(View v){
        Intent intent = new Intent(this, changeUserPassword.class);
        startActivity(intent);
    }
    public void deleteUserAccount(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle("Delete Account?").setMessage("You won't be able to undo this.").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(editUserProfile.this, "User successfully deleted.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(editUserProfile.this, "User could not be deleted- try re-signing in.", Toast.LENGTH_SHORT).show();
                    }
                });
                FirebaseAuth.getInstance().signOut();
                openLogin();
            }
        }).setNegativeButton("Don't Delete", null).show();
    }
    public void openLogin(){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            //Uri selectedImage = data.getData();
            //String[] filePathColumn = { MediaStore.Images.Media.DATA };

            //Cursor cursor = getContentResolver().query(selectedImage,
                    //filePathColumn, null, null, null);
            //cursor.moveToFirst();

            //int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            //String picturePath = cursor.getString(columnIndex);
            //cursor.close();

            //ImageView imageView = findViewById(R.id.imagebuttonProfilePicture);
            //imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(data.getDataString())).build();
            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //Toast.makeText(editUserProfile.this, "Successfully changed picture!", Toast.LENGTH_SHORT).show();
                    uploadFile();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(editUserProfile.this, "Could not change picture.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
