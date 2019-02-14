package dev.mohamed.tony.talkwithme.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.mohamed.tony.talkwithme.R;

import static dev.mohamed.tony.talkwithme.MyConstants.SIGN_IN_WITH_EMAIL_FAILURE;
import static dev.mohamed.tony.talkwithme.MyConstants.SIGN_IN_WITH_EMAIL_SUCCESS;

public class LoginActivity extends AppCompatActivity {

    public final String TAG = LoginActivity.class.getName();
    @BindView(R.id.login_btn2)
    Button login_btn;
    @BindView(R.id.enter_email)
    EditText email_editText;
    @BindView(R.id.enter_password)
    EditText password_editText;
    @BindView(R.id.toolbar_LoginActivity)
    Toolbar toolbar;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference mDataBaseToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mDataBaseToken= FirebaseDatabase.getInstance().getReference().child("Users");


        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = email_editText.getText().toString();
                String password = password_editText.getText().toString();

                if (email.matches("")) {
                    Toast.makeText(LoginActivity.this, getString(R.string.empty_email), Toast.LENGTH_SHORT).show();

                } else if (password.matches("")) {
                    Toast.makeText(LoginActivity.this, getString(R.string.empty_pass), Toast.LENGTH_SHORT).show();

                } else {
                    progressDialog = new ProgressDialog(LoginActivity.this,
                            R.style.MyAlertDialogStyle);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(getString(R.string.wait_msg));
                    progressDialog.show();


                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = null;
                    if (cm != null) {
                        activeNetwork = cm.getActiveNetworkInfo();
                    }
                    Boolean stateChanged = activeNetwork != null && activeNetwork.isConnected();

                    if (stateChanged) {

                        signInTouser(email, password);

                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();


                    }


                }


            }
        });
    }

    private void signInTouser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, SIGN_IN_WITH_EMAIL_SUCCESS);
                            FirebaseUser user = mAuth.getCurrentUser();

                            //================= to save token id to notification =========================
                            final String currentUserId=mAuth.getCurrentUser().getUid();
                             FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                 @Override
                                 public void onSuccess(InstanceIdResult instanceIdResult) {
                                     String deviceToken=instanceIdResult.getToken();
                                     mDataBaseToken.child(currentUserId).child("device_token").setValue(deviceToken);
                                     Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                     startActivity(intent);
                                     finish();
                                 }
                             });

                          /*   mDataBaseToken.child(currentUserId).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                 @Override
                                 public void onSuccess(Void aVoid) {
                                     Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                     startActivity(intent);
                                     finish();
                                     //  updateUI(user);
                                 }
                             });*/


                        } else {
                            progressDialog.hide();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, SIGN_IN_WITH_EMAIL_FAILURE, task.getException());
                            if (task.getException() != null) {
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }


                    }
                });

    }
}
