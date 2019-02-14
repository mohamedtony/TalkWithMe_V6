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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import dev.mohamed.tony.talkwithme.R;
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

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static dev.mohamed.tony.talkwithme.MyConstants.NAME;
import static dev.mohamed.tony.talkwithme.MyConstants.TASK;
import static dev.mohamed.tony.talkwithme.MyConstants.USERS;

public class SignUpActivity extends AppCompatActivity {

    public final String TAG = SignUpActivity.class.getName();
    @BindView(R.id.sign_up_Button)
    Button signUp;
    @BindView(R.id.name)
    EditText name_editText;
    @BindView(R.id.email)
    EditText email_editText;
    @BindView(R.id.password)
    EditText password_editText;
    @BindView(R.id.toolbar_signup)
    Toolbar toolbar;
    private FirebaseAuth mAuth;
    private String name, password, email;
    private FirebaseDatabase database;
    private String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sign_up);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = name_editText.getText().toString();
                email = email_editText.getText().toString();
                password = password_editText.getText().toString();

                createNewAccount();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == android.R.id.home) {
            finish();
        }


        return true;
    }

    private void createNewAccount() {

        if (name.matches("")) {
            Toast.makeText(SignUpActivity.this, getString(R.string.empty_name), Toast.LENGTH_SHORT).show();
        } else if (email.matches("")) {
            Toast.makeText(SignUpActivity.this, getString(R.string.empty_email), Toast.LENGTH_SHORT).show();
        } else if (password.matches("")) {
            Toast.makeText(SignUpActivity.this, getString(R.string.empty_pass), Toast.LENGTH_SHORT).show();

        } else {

            final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                    R.style.MyAlertDialogStyle);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.auth_text));
            progressDialog.show();

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = null;
            if (cm != null) {
                activeNetwork = cm.getActiveNetworkInfo();
            }
            Boolean stateChanged = activeNetwork != null && activeNetwork.isConnected();

            if (stateChanged) {
                if (mAuth != null) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        String uid = "";
                                        if (user != null) {
                                            uid = user.getUid();
                                        }
                                        // Write a message to the database
                                        database = FirebaseDatabase.getInstance();

                                        final String finalUid = uid;
                                        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignUpActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                            @Override
                                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                               deviceToken=instanceIdResult.getToken();
                                                Log.d("device_token",deviceToken);
                                                DatabaseReference myRef = database.getReference().child(USERS).child(finalUid);
                                                HashMap<String, String> userData = new HashMap<>();
                                                userData.put("device_token",deviceToken);
                                                userData.put(NAME, name);
                                                userData.put("image", "null");
                                                myRef.setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(SignUpActivity.this, getString(R.string.signup_succes),
                                                                    Toast.LENGTH_SHORT).show();
                                                            progressDialog.dismiss();

                                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }
                                        });




                                    } else {
                                        // If sign in fails, display a message to the user.
                                        progressDialog.hide();
                                        if (task.getException() != null) {
                                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, TASK + task.getException().getMessage());
                                        }

                                    }


                                }
                            });
                }
            } else {
                Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
            }

        }
    }
}
