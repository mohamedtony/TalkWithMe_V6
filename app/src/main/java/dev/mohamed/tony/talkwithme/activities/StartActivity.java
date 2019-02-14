package dev.mohamed.tony.talkwithme.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

import dev.mohamed.tony.talkwithme.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static dev.mohamed.tony.talkwithme.MyConstants.NAME;
import static dev.mohamed.tony.talkwithme.MyConstants.USERS;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 9001;
    @BindView(R.id.sign_upButton)
    Button signUp;

    @BindView(R.id.login_btn)
    Button loginBtn;

    public static GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference mDataBaseToken;
    private String deviceToken;
    private ProgressDialog progressDialog;

    //facebook login
    private CallbackManager mCallbackManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        mAuth=FirebaseAuth.getInstance();
        mDataBaseToken= FirebaseDatabase.getInstance().getReference().child("Users");
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //facebook login
        // [START initialize_fblogin]
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FacebookLogin", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("FacebookLogin", "facebook:onCancel");
                // [START_EXCLUDE]
              //  updateUI(null);
                // [END_EXCLUDE]
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("FacebookLogin", "facebook:onError", error);
                // [START_EXCLUDE]
                //updateUI(null);
                // [END_EXCLUDE]
            }
        });
        // [END initialize_fblogin]
    }

    private void handleFacebookAccessToken(final AccessToken accessToken) {
        Log.d("FacebookLogin", "handleFacebookAccessToken:" + accessToken);
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                           // if(task.getResult()!=null && task.getResult().getAdditionalUserInfo().isNewUser()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("FacebookLogin", "signInWithCredential:success");
                                final FirebaseUser user = mAuth.getCurrentUser();
                                // updateUI(user);
                                //================= to save token id to notification =========================
                                 String uid = null;
                                if (user != null) {
                                    uid = user.getUid();
                                }
                                final String finalUid = uid;
                                // Write a message to the database
                               final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(StartActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        deviceToken = instanceIdResult.getToken();
                                      //  String photoUrl = "https://graph.facebook.com/" + accessToken.getUserId() + "/picture?height=250";
                                        String photoUrl =user.getPhotoUrl()+ "?type=large";

                                        DatabaseReference myRef = database.getReference().child(USERS).child(finalUid);
                                        HashMap<String, String> userData = new HashMap<>();
                                        userData.put("device_token", deviceToken);
                                        userData.put(NAME, user.getDisplayName());
                                        userData.put("image", photoUrl);
                                        myRef.setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(StartActivity.this, getString(R.string.signup_succes),
                                                            Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();

                                                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                });




                           /* }else{
                                Toast.makeText(StartActivity.this, getString(R.string.signin_success),
                                        Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }*/


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FacebookLogin", "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartActivity.this, "Authentication failed."+task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            //updateUI(null);
                        }

                        // [START_EXCLUDE]
                        progressDialog.hide();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(StartActivity.this,
                R.style.MyAlertDialogStyle);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.auth_text));
        progressDialog.show();
    }

/*    @Override
    protected void onStart() {
        super.onStart();
        if(){

        }
    }*/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            showProgressDialog();
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("StartActivity", "signInResult:failed code=" + e.getStatusCode());
            //firebaseAuthWithGoogle(null);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        Log.d("StartActivity", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult()!=null && task.getResult().getAdditionalUserInfo().isNewUser()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("StartActivity", "signInWithCredential:success");
                                final FirebaseUser user = mAuth.getCurrentUser();
                                // updateUI(user);
                                //================= to save token id to notification =========================
                                 String uid =null;
                                if (user != null) {
                                    uid = user.getUid();
                                }
                               final String finalUid=uid;
                                // Write a message to the database
                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(StartActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        deviceToken = instanceIdResult.getToken();
                                        String photoURL = null;
                                        if (user != null) {
                                            photoURL = user.getPhotoUrl().toString();
                                        }
                                        String resultPhoto = photoURL.replace("s96-c", "s400-c");
                                        DatabaseReference myRef = database.getReference().child(USERS).child(finalUid);
                                        HashMap<String, String> userData = new HashMap<>();
                                        userData.put("device_token", deviceToken);
                                        userData.put(NAME, user.getDisplayName());
                                        userData.put("image", resultPhoto);
                                        myRef.setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(StartActivity.this, getString(R.string.signup_succes),
                                                            Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();

                                                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                });

                            }else {
                                Toast.makeText(StartActivity.this, getString(R.string.signin_success),
                                        Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("StartActivity", "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                         //   updateUI(null);
                        }

                        // ...
                    }
                });
    }
}
