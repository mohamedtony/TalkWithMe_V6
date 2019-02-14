package dev.mohamed.tony.talkwithme.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import static dev.mohamed.tony.talkwithme.MyConstants.IMAGE;
import static dev.mohamed.tony.talkwithme.MyConstants.IMAGES;
import static dev.mohamed.tony.talkwithme.MyConstants.JPG;
import static dev.mohamed.tony.talkwithme.MyConstants.NAME;
import static dev.mohamed.tony.talkwithme.MyConstants.USERS;

public class ProfileSettingsActivity extends AppCompatActivity {

    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.profile_image)
    ImageView profileImage;

    @BindView(R.id.change_img_btn)
    Button change_image;
    @BindView(R.id.change_name)
    Button change_name;

    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private StorageReference mStorageRef;
    private String newUserName, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        ButterKnife.bind(this);


    }

    @Override
    protected void onStart() {
        super.onStart();


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
            mDatabase = FirebaseDatabase.getInstance().getReference().child(USERS).child(mUser.getUid());
            mDatabase.child("online").setValue("true");
            mDatabase.keepSynced(true);
            loadProfile();
        } else {
            Toast.makeText(ProfileSettingsActivity.this, getString(R.string.no_user), Toast.LENGTH_SHORT).show();
            gotoStartActivity();

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mUser!=null){
            mDatabase.child("online").setValue("false");
        }
    }

    private void loadProfile() {
        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(dataSnapshot.child(NAME).getValue()!=null) {
                    name = dataSnapshot.child(NAME).getValue().toString();
                }
                 String image = "";
                if(dataSnapshot.child("image").getValue()!=null) {
                    image = dataSnapshot.child("image").getValue().toString();
                }
                final String imageFinal=image;

                if (!TextUtils.isEmpty(name)) {
                    userName.setText(name);
                }
                if (!image.matches("null")) {
                    Picasso.with(ProfileSettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(profileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            /// success in ofline mode
                        }

                        @Override
                        public void onError() {
                            Picasso.with(ProfileSettingsActivity.this).load(imageFinal).into(profileImage);
                        }
                    });
                }


                // Log.d(TAG, "Value is: " + value);
                // Toast.makeText(ProfileSettingsActivity.this, "value : "+value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        change_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pickImageFromGallary();


            }
        });

        change_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeLangDialog();

            }
        });

    }

    private void gotoStartActivity() {
        Intent intent = new Intent(ProfileSettingsActivity.this, StartActivity.class);
        startActivity(intent);
    }

    private void pickImageFromGallary() {

        Intent getImageIntent = new Intent();
        getImageIntent.setType(IMAGE);
        getImageIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(getImageIntent, getString(R.string.pick_image)), 0);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                final Uri resultUri = data.getData();
                mStorageRef = FirebaseStorage.getInstance().getReference();

                final ProgressDialog progressDialog = new ProgressDialog(ProfileSettingsActivity.this,
                        R.style.MyAlertDialogStyle);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage(getString(R.string.wait_msg));
                progressDialog.show();

                // Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
                File f = new File(String.valueOf(resultUri));
                String imageName = f.getName();
                final StorageReference riversRef = mStorageRef.child(IMAGES + imageName + JPG);

                riversRef.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                String downloadUrl = riversRef.getDownloadUrl().toString();
                                Log.d("downloadUrl",downloadUrl);
                                mDatabase.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            Picasso.with(ProfileSettingsActivity.this).load(resultUri).into(profileImage);
                                            Toast.makeText(ProfileSettingsActivity.this, getString(R.string.image_changed), Toast.LENGTH_SHORT).show();
                                        } else {

                                        }
                                    }
                                });
                                Picasso.with(ProfileSettingsActivity.this).load(resultUri).into(profileImage);
                                Toast.makeText(ProfileSettingsActivity.this, getString(R.string.image_changed), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                                progressDialog.hide();
                                Toast.makeText(ProfileSettingsActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

            }
        }
    }

    public void showChangeLangDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.my_edit_text, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);
        edt.setText(name);

        dialogBuilder.setTitle(getString(R.string.profile_setting_titles));
        dialogBuilder.setMessage(getString(R.string.enter_your_name));
        dialogBuilder.setPositiveButton(MyConstants.DONE, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                newUserName = edt.getText().toString();

                //to write to the database
                final ProgressDialog progressDialog = new ProgressDialog(ProfileSettingsActivity.this,
                        R.style.MyAlertDialogStyle);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage(getString(R.string.wait_msg));
                progressDialog.show();

                mDatabase.child(NAME).setValue(newUserName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            userName.setText(newUserName);
                            progressDialog.dismiss();

                        } else {
                            progressDialog.hide();
                            if (task.getException() != null) {
                                Toast.makeText(ProfileSettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });


            }
        });
        dialogBuilder.setNegativeButton(MyConstants.CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}
