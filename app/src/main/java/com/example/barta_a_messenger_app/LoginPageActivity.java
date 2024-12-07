package com.example.barta_a_messenger_app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginPageActivity extends AppCompatActivity {

    Button loginButton, signupButton;
    ImageView googleButton;

    EditText email, password;

    TextView forgetPass;

    ProgressDialog progressDialog;

    FirebaseAuth mAuth;

    DatabaseReference databaseReference;

    SignInClient oneTapClient;
    BeginSignInRequest signUpRequest;

    ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    Button googleSignInButton;
    private static final int RC_SIGN_IN = 9001;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        /*

1. `GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)`: This initializes a `GoogleSignInOptions.Builder`
object with the default sign-in options.
`DEFAULT_SIGN_IN` includes basic profile information such as the user's ID and basic profile information.

2. `.requestIdToken(getString(R.string.web_client_id))`: This requests an ID token for the authenticated user.
The `getString(R.string.web_client_id)` retrieves the web client ID from the `strings.xml` resource file.
This ID token is used to authenticate the user on your backend server.
ফায়ারবেসে গিয়ে প্রজেক্টের সেটিংসে গুগল সাইন ইন এনাবল করে নতুন json file নামালে web_client_id auto add  হয় string.xml file এ।

3. `.requestEmail()`: This requests the user's email address as part of the sign-in process.

this code configures the Google Sign-In options to request an ID token and the user's email address,
which are necessary for authenticating the user and accessing their profile information.
*/
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
        // gsc -> GoogleSignInClient object provides the getSignInIntent() which returns an Intent
        // to start the Google Sign-in activity. It's required for Google sign-in flow, including initiating sign-in
        // handling sign-out and managing user sessions.
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        loginButton = findViewById(R.id.loginbutton);
        signupButton = findViewById(R.id.signupbutton);
        googleSignInButton = findViewById(R.id.googleSignInButton);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        forgetPass = findViewById(R.id.forgetpasstext);


        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPageActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });

        oneTapClient = Identity.getSignInClient(this);
        signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        mAuth = FirebaseAuth.getInstance();

        activityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            try {
                                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                                String idToken = credential.getGoogleIdToken();
                                if (idToken != null) {
                                    navigateToSecondActivity();
                                }
                            } catch (ApiException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                if (email.getText().toString().isEmpty() == true) {
                    progressDialog.cancel();
                    email.setError("required");
                }
                if (password.getText().toString().isEmpty() == true) {
                    progressDialog.cancel();
                    password.setError("password empty");
                }
                if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                    signInwithEmailPassword(email.getText().toString(), password.getText().toString());
                }
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginPageActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        googleSignInButton.setOnClickListener(view -> signIn());

    }

    private void signIn() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    /*The signIn method initiates Google Sign-in process. It's part of the Google Sign-in flow which allows users
     * to sign in to the app using Google account. startActivityForResult নতুন একটা activity launch করে। নতুন activity থেকে
     * exit হয়ে গেলে onActivityResult method automatically call হবে। RC_SIGN_IN যেকোন integer value হত পারে।  */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed for : " + e.getMessage(), e);
            }
        }
    }
    /*This method is called when the acivity which was launched to connect to google acoount exits and provides the request
     * it was started with , result code returned and any additional data returned.The `Task` class in the Google Play services library
     *  represents an asynchronous operation. It is part of the `com.google.android.gms.tasks` package and is used to handle the result
     *  of an asynchronous operation, such as a network request or a database query.
     *  The `getSignedInAccountFromIntent` method returns a `Task` object that represents the asynchronous operation of retrieving the signed-in account from the intent.
     **Asynchronous Operation Handling**: It allows you to perform operations asynchronously and handle the result or error when the
     *  operation completes.*/

    //    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        // Sign in success
//                        Log.d(TAG, "signInWithCredential:success");
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        Log.d(TAG, user.getDisplayName() + " " + user.getEmail());
//                        updateUI(user);
//                    } else {
//                        // Sign in fails
//                        Log.w(TAG, "signInWithCredential:failure", task.getException());
//                        updateUI(null);
//                    }
//                });
//    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkAndStoreUserInDatabase(user);
                        }
                    } else {
                        // Sign in fails
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }
                });
    }

    //    private void checkAndStoreUserInDatabase(FirebaseUser user) {
//        String uid = user.getUid();
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(uid);
//
//        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()) {
//                    String name = user.getDisplayName();
//                    String email = user.getEmail();
//                    String profilePicture = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
//                    Intent intent = new Intent(LoginPageActivity.this, SendOTPActivity.class);
//                    intent.putExtra("uid", uid);
//                    intent.putExtra("email", email);
//                    intent.putExtra("name", name);
//                    intent.putExtra("password", "");
//                    intent.putExtra("profilePicture", profilePicture);
//                    startActivity(intent);
//                }
//            } else
//            {
//                // User exists, proceed to home
//                updateUI(user);
//            }
//        }
//    }
//}
    private void checkAndStoreUserInDatabase(FirebaseUser user) {
        String uid = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // User doesn't exist, so redirect to SendOTPActivity to store new user
                    String name = user.getDisplayName();
                    String email = user.getEmail();
                    String profilePicture = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

                    // Create an intent to pass data to SendOTPActivity
                    Intent intent = new Intent(LoginPageActivity.this, SendOTPActivity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("email", email);
                    intent.putExtra("name", name);
                    intent.putExtra("password", "");
                    intent.putExtra("profilePicture", profilePicture);
                    startActivity(intent);
                } else {
                    // User exists, proceed to home activity
                    updateUI(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error in case database operation fails
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }


//    private void checkAndStoreUserInDatabase(FirebaseUser user) {
//        String uid = user.getUid();
//        String name = user.getDisplayName();
//        String email = user.getEmail();
//        String profilePicture = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
//
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(uid);
//
//        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()) {
////                     User does not exist, create new user entry
//                    userRef.setValue(new User(name, email, "0183" ,  profilePicture , "active"))
//                            .addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    Log.d(TAG, "User added to database.");
//                                    updateUI(user);
//                                } else {
//                                    Log.e(TAG, "Failed to add user to database.");
//                                }
//                            });
//
//                } else {
//                    // User exists, proceed to home
//                    updateUI(user);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e(TAG, "Database error: " + databaseError.getMessage());
//            }
//        });
//    }


    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mAuth = FirebaseAuth.getInstance();
            String uid = user.getUid();

//            databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(uid);
//
//            databaseReference.child("status").setValue("active");
            // temproary
            Intent intent = new Intent(LoginPageActivity.this, HomeScreen.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
//            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(String.valueOf(currentUser));

//            databaseReference.child("status").setValue("active");
            Intent intent = new Intent(LoginPageActivity.this, HomeScreen.class);
            startActivity(intent);
        }
    }


    private void signOut() {
        // Implement sign out logic here
    }

    private void signInwithGoogle() {
        oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        IntentSenderRequest intentSenderRequest =
                                new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();

                        activityResultLauncher.launch(intentSenderRequest);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                });
    }


    void signInwithEmailPassword(String mail, String pass) {
        mAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithEmail:success");

                    mAuth = FirebaseAuth.getInstance();
                    String uid = mAuth.getCurrentUser().getUid();

                    databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(uid);

                    databaseReference.child("status").setValue("active");

                    Intent intent = new Intent(LoginPageActivity.this, HomeScreen.class);
                    progressDialog.cancel();

                    startActivity(intent);
                } else {
                    progressDialog.cancel();
                    Log.w(TAG, "signInWithEmail:Failed", task.getException());
                    Toast.makeText(LoginPageActivity.this, "Email Or Password is Wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToSecondActivity() {
        finish();
        Intent intent = new Intent(LoginPageActivity.this, HomeScreen.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }
}
