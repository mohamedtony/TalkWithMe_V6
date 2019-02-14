package dev.mohamed.tony.talkwithme.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import dev.mohamed.tony.talkwithme.MyConstants;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.mohamed.tony.talkwithme.R;

import static dev.mohamed.tony.talkwithme.MyConstants.TAG_EDU;
import static dev.mohamed.tony.talkwithme.MyConstants.TAG_EMAIL;
import static dev.mohamed.tony.talkwithme.MyConstants.TAG_IMAGE;
import static dev.mohamed.tony.talkwithme.MyConstants.TAG_NAME;

public class AboutMe extends AppCompatActivity {

    @BindView(R.id.toolbar_AboutME)
    Toolbar toolbar;
    @BindView(R.id.my_name)
    TextView myName;
    @BindView(R.id.my_email)
    TextView myEmail;
    @BindView(R.id.request_image)
    ImageView myEmage;
    @BindView(R.id.my_education)
    TextView myEdu;
    TextView txtString;
    private ProgressDialog pDialog;
    private DatabaseReference mFirebasedatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser cureentUser;
    //OkHttpClient client;
    private String myNameText, myEmailText, myEduText, MyImageURl;

    // public String url= "https://reqres.in/api/users/2";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.about_me));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebasedatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();
        cureentUser=mAuth.getCurrentUser();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TAG_NAME)) {
                myNameText = savedInstanceState.getString(TAG_NAME);
                myName.setText(myNameText);
            }
            if (savedInstanceState.containsKey(TAG_EMAIL)) {
                myEmailText = savedInstanceState.getString(TAG_EMAIL);
                myEmail.setText(myEmailText);
            }
            if (savedInstanceState.containsKey(TAG_EDU)) {
                myEduText = savedInstanceState.getString(TAG_EDU);
                myEdu.setText(myEduText);
            }
            if (savedInstanceState.containsKey(TAG_IMAGE)) {
                MyImageURl = savedInstanceState.getString(TAG_IMAGE);
                Picasso.with(AboutMe.this).load(MyImageURl).into(myEmage);
            }

        } else {


            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = null;
            if (cm != null) {
                activeNetwork = cm.getActiveNetworkInfo();
            }
            Boolean stateChanged = activeNetwork != null && activeNetwork.isConnected();

            if (stateChanged) {

                OkHttpHandler okHttpHandler = new OkHttpHandler();
                // client = new OkHttpClient();
                okHttpHandler.execute(MyConstants.URL);


            } else {
                Toast.makeText(AboutMe.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();


            }

        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is signed in (non-null) and update UI accordingly.

        if(mAuth!=null) {
            cureentUser = mAuth.getCurrentUser();
            if(cureentUser!=null) {
                HashMap<String,Object>map=new HashMap<>();
                map.put("online","true");
                mFirebasedatabase.child(cureentUser.getUid()).updateChildren(map);
            }
        }
    }

   @Override
    protected void onPause() {
       super.onPause();
       if (mAuth != null) {
           cureentUser = mAuth.getCurrentUser();
           if (cureentUser != null) {
               mFirebasedatabase.child(cureentUser.getUid()).child("online").setValue("false");
           }
       }
   }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TAG_NAME, myNameText);
        outState.putString(TAG_EMAIL, myEmailText);
        outState.putString(TAG_EDU, myEduText);
        outState.putString(TAG_IMAGE, MyImageURl);

    }


    public class OkHttpHandler extends AsyncTask<String, Void, String> {

        //geting object of OkHttpClient to making a connection
        OkHttpClient client = new OkHttpClient();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(AboutMe.this);
            pDialog.setMessage(getString(R.string.wait_plz));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            // making http get request using OkHttp - you need to create Request object by using Request.Builder
            Request.Builder builder = new Request.Builder();
            //setting the targeted url to api
            builder.url(params[0]);
            //building the request
            Request request = builder.build();

            try {
                //making a new connection and executing the request to get the response
                Response response = client.newCall(request).execute();
                //response containing object of the body and converting it to string
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String stringResponse) {
            super.onPostExecute(stringResponse);
            // hide th progress bar
            pDialog.dismiss();

            try {
                // JSON Parsing of data
                if (stringResponse != null) {
                    //converting the string response to JSONObject
                    JSONObject jsonObject = new JSONObject(stringResponse);

                    //  JSONObject oneObject = jsonArray.getJSONObject(0);
                    // Pulling items from the array
                    JSONObject main = jsonObject.getJSONObject(MyConstants.TAG_ABOUTME);

                    //get developer name
                    myNameText = main.getString(MyConstants.TAG_NAME);
                    //get developer email
                    myEmailText = main.getString(MyConstants.TAG_EMAIL);
                    //get developer education
                    myEduText = main.getString(MyConstants.TAG_EDU);
                    //get developer image url
                    MyImageURl = main.getString(MyConstants.TAG_IMAGE);
                    // set developer name
                    myName.setText(myNameText);
                    // set developer email
                    myEmail.setText(myEmailText);
                    // set developer edu
                    myEdu.setText(myEduText);
                    // set developer image
                    Picasso.with(AboutMe.this).load(MyImageURl).into(myEmage);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}


