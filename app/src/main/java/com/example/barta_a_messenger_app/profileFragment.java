package com.example.barta_a_messenger_app;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.Manifest;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.util.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class profileFragment extends Fragment {

    private String uid;
    private RecyclerView recyclerView;
    private TextView username, phone_nmbr;
    private ImageView imgProfile;
    private Uri imagePath;
    private ArrayList<Contact> list;
    private ContactAdapter adapter;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, userRef;

    private Button editStatus;
    private static final int MAX_CONTACTS_FOR_PSI = 500; // Limit to prevent performance issues
    private static final double FALSE_POSITIVE_PROBABILITY = 0.01;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private String sharedSecret;

    private String currentUserName;
    private String currentUserPhone;
    private String currentUserProfilePic;
    private String currentUserStatus;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        androidx.constraintlayout.utils.widget.MotionButton btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logout();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        username = view.findViewById(R.id.username);
        imgProfile = view.findViewById(R.id.profilePicture);
        recyclerView = view.findViewById(R.id.recyclerView);
        editStatus = view.findViewById(R.id.updateStatusBtn);
        phone_nmbr = view.findViewById(R.id.phone);

        editStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileUpdateActivity.class);
                startActivity(intent);
            }
        });


        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ContactAdapter(requireContext(), list);
        recyclerView.setAdapter(adapter);

        userRef = FirebaseDatabase.getInstance().getReference().child("user").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("username").getValue(String.class);
                    currentUserPhone = dataSnapshot.child("phone").getValue(String.class);
                    currentUserProfilePic = dataSnapshot.child("profilePicture").getValue(String.class);
                    currentUserStatus = dataSnapshot.child("status").getValue(String.class);

                    username.setText(currentUserName);
                    phone_nmbr.setText(currentUserPhone);

                    if (currentUserProfilePic != null && !currentUserProfilePic.isEmpty()) {
                        Picasso.get().load(currentUserProfilePic).into(imgProfile);
                    }
                }
            }

            // sohel
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                Log.e("FirebaseErrorInProfileFragment", "Error: " + databaseError.getMessage());

                switch (databaseError.getCode()) {
                    case DatabaseError.DISCONNECTED:
                        Log.e("FirebaseError", "The operation had to be aborted due to a network disconnect.");
                        break;
                    case DatabaseError.PERMISSION_DENIED:
                        Log.e("FirebaseError", "The client doesn't have permission to access the database.");
                        break;
                    case DatabaseError.NETWORK_ERROR:
                        Log.e("FirebaseError", "The operation could not be performed due to a network error.");
                        break;
                    case DatabaseError.OPERATION_FAILED:
                        Log.e("FirebaseError", "The server indicated that this operation failed.");
                        break;
                    default:
                        Log.e("FirebaseError", "An unknown error occurred.");
                        break;
                }
            }
        });

        userRef = FirebaseDatabase.getInstance().getReference().child("user").child(uid);
        databaseReference = FirebaseDatabase.getInstance().getReference("Contacts").child(uid);

        loadContacts();

//        imgProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent photoIntent = new Intent(Intent.ACTION_PICK);
//                photoIntent.setType("image/*");
//                startActivityForResult(photoIntent, 1);
//            }
//        });



//        Button btnDiscoverContacts = view.findViewById(R.id.btnDiscoverContacts);
//        btnDiscoverContacts.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // startContactDiscovery();
//                // onRetrieveContactsClick();
//
//                // Check for READ_CONTACTS permission
//                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    // Permission is not granted, request it
//                    ActivityCompat.requestPermissions(requireActivity(),
//                            new String[]{Manifest.permission.READ_CONTACTS},
//                            PERMISSIONS_REQUEST_READ_CONTACTS);
//                } else {
//                    // Permission has already been granted
//                    fetchSharedSecret(secret -> {
//                        if (secret != null && !secret.isEmpty()) {
//                            startContactDiscovery(secret);
//                        } else {
//                            Toast.makeText(requireContext(), "Failed to retrieve shared secret.", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            }
//        });

        // Handle Contact Discovery Button
        Button btnDiscoverContacts = view.findViewById(R.id.btnDiscoverContacts);
        btnDiscoverContacts.setOnClickListener(v -> {
            // Check for READ_CONTACTS permission
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, check if we should show a rationale
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.READ_CONTACTS)) {
                    // Show an explanation to the user
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Permission Needed")
                            .setMessage("This app requires access to your contacts to discover mutual contacts.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                // Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(requireActivity(),
                                        new String[]{Manifest.permission.READ_CONTACTS},
                                        PERMISSIONS_REQUEST_READ_CONTACTS);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                                Toast.makeText(requireContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
                            })
                            .create()
                            .show();
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            } else {
                // Permission has already been granted
                retrieveAndProcessContacts();
            }
        });

        return view;
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, proceed with contact discovery
                retrieveAndProcessContacts();
            } else {
                // Permission denied, disable the functionality that depends on this permission.
                Toast.makeText(requireContext(), "Permission denied to read contacts.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void retrieveAndProcessContacts() {
        // Directly process local contacts without hashing
        List<String> normalizedLocalContacts = getLocalContacts();
        Log.e("ContactDiscovery", "Local contacts: " + normalizedLocalContacts.size());

        if (normalizedLocalContacts.isEmpty()) {
            Toast.makeText(requireContext(), "No contacts to process.", Toast.LENGTH_SHORT).show();
            return;
        }

        retrieveMatchedContacts(normalizedLocalContacts);
    }

    private void loadContacts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Contact contact = dataSnapshot.getValue(Contact.class);
                    if (contact != null) {
                        list.add(contact);
                        String uid2 = contact.getUid();

                        if (uid2 != null) {
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(uid2);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String profilePictureUrl = dataSnapshot.child("profilePicture").getValue(String.class);
                                        String status = dataSnapshot.child("status").getValue(String.class);
                                        contact.setProfilePic(profilePictureUrl != null ? profilePictureUrl : "");
                                        contact.setStatus(status != null ? status : "");
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle errors
                                }
                            });
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }

    private void retrieveMatchedContacts(List<String> normalizedLocalContacts) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("All Accounts");
        List<String> matchedUids = new ArrayList<>();

        for (String phone : normalizedLocalContacts) {
            ref.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String matchedUid = snapshot.child("uid").getValue(String.class);
                        Log.d("ContactDiscovery", "Matched UID for " + phone + ": " + matchedUid);
                        if (matchedUid != null && !matchedUid.equals(uid)) {
                            matchedUids.add(matchedUid);
                            addMatchedContact(matchedUid);
                        }
                    } else {
                        Log.d("ContactDiscovery", "No account found for phone: " + phone);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ContactDiscovery", "Error accessing Firebase for phone: " + phone, error.toException());
                }
            });
        }

        // Optional: Handle the case where no mutual contacts are found
        // For example, using a counter to check after all queries
    }

    private void addMatchedContact(String matchedUid) {
        // Reference to the current user's contacts
        DatabaseReference currentUserContactsRef = FirebaseDatabase.getInstance().getReference("Contacts").child(uid).child(matchedUid);

        // Check if the contact already exists
        currentUserContactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Fetch matched user's details
                    DatabaseReference matchedUserRef = FirebaseDatabase.getInstance().getReference("user").child(matchedUid);
                    matchedUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot matchedSnapshot) {
                            if (matchedSnapshot.exists()) {
                                String matchedName = matchedSnapshot.child("username").getValue(String.class);
                                String matchedPhone = matchedSnapshot.child("phone").getValue(String.class);
                                String matchedProfilePic = matchedSnapshot.child("profilePicture").getValue(String.class);
                                String matchedStatus = matchedSnapshot.child("status").getValue(String.class);

                                // Create Contact object for the matched user
                                Contact matchedContact = new Contact(
                                        matchedName != null ? matchedName : "",
                                        matchedPhone != null ? matchedPhone : "",
                                        matchedUid,
                                        matchedProfilePic != null ? matchedProfilePic : "",
                                        matchedStatus != null ? matchedStatus : "",
                                        "", // Additional fields if any
                                        new Date().getTime(),
                                        "", // Last message
                                        ""  // Message time
                                );

                                // Create Contact object for the current user
                                Contact currentUserContact = new Contact(
                                        currentUserName != null ? currentUserName : "",
                                        currentUserPhone != null ? currentUserPhone : "",
                                        uid,
                                        currentUserProfilePic != null ? currentUserProfilePic : "",
                                        currentUserStatus != null ? currentUserStatus : "",
                                        "", // Additional fields if any
                                        new Date().getTime(),
                                        "", // Last message
                                        ""  // Message time
                                );

                                // Prepare updates for both users
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("Contacts/" + uid + "/" + matchedUid, matchedContact);
                                updates.put("Contacts/" + matchedUid + "/" + uid, currentUserContact);

                                // Update the database
                                FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(requireContext(), "Mutual contact added: " + matchedContact.getFull_name(), Toast.LENGTH_SHORT).show();
                                                Log.d("ContactDiscovery", "Mutual contact added: " + matchedContact.getFull_name());

                                                // Optionally, update the RecyclerView
                                                list.add(matchedContact);
                                                adapter.notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(requireContext(), "Failed to add mutual contact.", Toast.LENGTH_SHORT).show();
                                                Log.e("ContactDiscovery", "Failed to add mutual contact", task.getException());
                                            }
                                        });
                            } else {
                                Log.d("ContactDiscovery", "Matched user does not exist.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("ContactDiscovery", "Error fetching matched user details", error.toException());
                        }
                    });
                } else {
                    Log.d("ContactDiscovery", "Contact already exists: " + matchedUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ContactDiscovery", "Error checking existing contact", error.toException());
            }
        });
    }

    private List<String> getLocalContacts() {
        List<String> contacts = new ArrayList<>();
        ContentResolver contentResolver = requireContext().getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if (phone != null && !phone.isEmpty()) {
                    String normalizedPhone = normalizePhoneNumber(phone);
                    contacts.add(normalizedPhone);
                    Log.d("PhoneNumber", "Normalized Phone number: " + normalizedPhone);
                }
            }
            cursor.close();
        }

        return contacts;
    }

    private String normalizePhoneNumber(String phoneNumber) {
        // Remove all characters except digits and '+'
        phoneNumber = phoneNumber.replaceAll("[^\\d+]", "");

        if (phoneNumber.startsWith("+880")) {
            // Already in correct format
        } else if (phoneNumber.startsWith("880")) {
            phoneNumber = "+" + phoneNumber;
        } else if (phoneNumber.startsWith("0")) {
            phoneNumber = "+880" + phoneNumber.substring(1);
        } else {
            // Handle unexpected formats if necessary
            Log.e("NormalizationError", "Unexpected phone number format: " + phoneNumber);
        }

        return phoneNumber;
    }


//    private void loadContacts() {
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                list.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    Contact contact = dataSnapshot.getValue(Contact.class);
//                    if (contact != null) {
//                        list.add(contact);
//                        String uid2 = contact.getUid();
//
//                        if (uid2 != null) {
//                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(uid2);
//                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                    if (dataSnapshot.exists()) {
//                                        String profilePictureUrl = dataSnapshot.child("profilePicture").getValue(String.class);
//                                        String status = dataSnapshot.child("status").getValue(String.class);
//                                        contact.setProfilePic(profilePictureUrl != null ? profilePictureUrl : "");
//                                        contact.setStatus(status != null ? status : "");
//                                        adapter.notifyDataSetChanged();
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError databaseError) {
//                                    // Handle errors
//                                }
//                            });
//                        }
//                    }
//                }
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Handle errors
//            }
//        });
//    }
//
//    // Example method to fetch the shared secret securely
//    private void fetchSharedSecret(final OnSharedSecretFetchedListener listener) {
//        // TODO: Implement secure retrieval of the shared secret from your server
//        // For demonstration, we'll use a hardcoded secret (NOT SECURE)
//        // Replace this with your secure retrieval logic
//        sharedSecret = "your_secure_shared_secret";
//        listener.onFetched(sharedSecret);
//    }
//
//    private interface OnSharedSecretFetchedListener {
//        void onFetched(String secret);
//    }
//
//    private void startContactDiscovery(String secret) {
//        Toast.makeText(requireContext(), "Starting contact discovery...", Toast.LENGTH_SHORT).show();
//        Log.d("ContactDiscovery", "Initiating PSI contact discovery...");
//
//        // Step 1: Fetch all registered users' hashed phone numbers from the server
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("All Accounts");
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                List<String> serverHashedContacts = new ArrayList<>();
//                for (DataSnapshot accountSnapshot : snapshot.getChildren()) {
//                    String hashedPhone = accountSnapshot.child("hashed_phone_no").getValue(String.class);
//                    if (hashedPhone != null) {
//                        serverHashedContacts.add(hashedPhone);
//                    }
//                }
//                processLocalContacts(serverHashedContacts, secret); // Proceed to process local contacts
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(requireContext(), "Error fetching accounts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                Log.e("FirebaseError", error.getMessage());
//            }
//        });
//    }
//
//    private void processLocalContacts(List<String> serverHashedContacts, String secret) {
//        List<String> localContacts = getLocalContacts();
//        Log.e("ContactDiscovery", "Local contacts: " + localContacts.size());
//        if (localContacts.size() > MAX_CONTACTS_FOR_PSI) {
//            Toast.makeText(requireContext(), "Too many contacts for processing.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        List<String> hashedLocalContacts = new ArrayList<>();
//        for (String phone : localContacts) {
//            String cleanedPhone = phone.replaceAll("\\s+", ""); // Remove whitespace
//            Log.e("ContactDiscovery", "Phone number: " + cleanedPhone);
//            String hashedPhone = hashPhoneNumber(cleanedPhone, secret);
//            if (!hashedPhone.isEmpty()) {
//                hashedLocalContacts.add(hashedPhone);
//            }
//        }
//
//        // Create Bloom Filter from server hashed contacts
//        BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8),
//                serverHashedContacts.size(), FALSE_POSITIVE_PROBABILITY);
//
//        for (String hash : serverHashedContacts) {
//            bloomFilter.put(hash);
//        }
//
//        // Find mutual contacts by checking if local hashed contacts are in server Bloom Filter
//        List<String> matchedHashes = new ArrayList<>();
//        for (String localHash : hashedLocalContacts) {
//            if (bloomFilter.mightContain(localHash)) {
//                matchedHashes.add(localHash);
//            }
//        }
//
//        if (matchedHashes.isEmpty()) {
//            Toast.makeText(requireContext(), "No mutual contacts found.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Now, retrieve UIDs of matched contacts
//        retrieveMatchedContacts(matchedHashes, secret);
//    }
//
//    private void retrieveMatchedContacts(List<String> matchedHashes, String secret) {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("All Accounts");
//        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        for (String hash : matchedHashes) {
//            ref.orderByChild("hashed_phone_no").equalTo(hash).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
//                        String matchedUid = userSnapshot.child("uid").getValue(String.class);
//                        if (matchedUid != null && !matchedUid.equals(currentUid)) {
//                            // Fetch user details and add to Contacts
//                            addMatchedContact(matchedUid);
//                            Toast.makeText(requireContext(), "Contact added: " + matchedUid, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.e("ContactDiscovery", "Error finding matched accounts", error.toException());
//                }
//            });
//        }
//    }
//
//    private void addMatchedContact(String matchedUid) {
//        DatabaseReference matchedUserRef = FirebaseDatabase.getInstance().getReference("user").child(matchedUid);
//        matchedUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot matchedSnapshot) {
//                if (matchedSnapshot.exists()) {
//                    // Fetch matched user's details
//                    String matchedName = matchedSnapshot.child("username").getValue(String.class);
//                    String matchedPhone = matchedSnapshot.child("phone").getValue(String.class);
//                    String matchedProfilePic = matchedSnapshot.child("profilePicture").getValue(String.class);
//                    String matchedStatus = matchedSnapshot.child("status").getValue(String.class);
//
//                    // Create Contact object for the matched user to add to current user's Contacts
//                    Contact matchedContact = new Contact(
//                            matchedName != null ? matchedName : "",
//                            matchedPhone != null ? matchedPhone : "",
//                            matchedUid,
//                            matchedProfilePic != null ? matchedProfilePic : "",
//                            matchedStatus != null ? matchedStatus : "",
//                            "", // other details if any
//                            new Date().getTime(),
//                            "", // last message
//                            "" // message time
//                    );
//
//                    // Create Contact object for the current user to add to matched user's Contacts
//                    Contact currentUserContact = new Contact(
//                            currentUserName != null ? currentUserName : "",
//                            currentUserPhone != null ? currentUserPhone : "",
//                            uid,
//                            currentUserProfilePic != null ? currentUserProfilePic : "",
//                            currentUserStatus != null ? currentUserStatus : "",
//                            "", // other details if any
//                            new Date().getTime(),
//                            "", // last message
//                            "" // message time
//                    );
//
//                    // Prepare updates map for multi-location update
//                    Map<String, Object> updates = new HashMap<>();
//                    updates.put("Contacts/" + uid + "/" + matchedUid, matchedContact);
//                    updates.put("Contacts/" + matchedUid + "/" + uid, currentUserContact);
//
//                    // Perform the multi-location update
//                    FirebaseDatabase.getInstance().getReference().updateChildren(updates)
//                            .addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    Toast.makeText(requireContext(), "Mutual contact added: " + matchedContact.getFull_name(), Toast.LENGTH_SHORT).show();
//                                    // Update the local RecyclerView
//                                    list.add(matchedContact);
//                                    adapter.notifyDataSetChanged();
//                                } else {
//                                    Toast.makeText(requireContext(), "Failed to add mutual contact: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                    Log.e("ContactDiscovery", "Failed to add mutual contact", task.getException());
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("ContactDiscovery", "Error fetching matched user details", error.toException());
//            }
//        });
//    }
//
//    private List<String> getLocalContacts() {
//        List<String> contacts = new ArrayList<>();
//        ContentResolver contentResolver = requireContext().getContentResolver();
//        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
//
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                if (phone != null && !phone.isEmpty()) {
//                    String normalizedPhone = normalizePhoneNumber(phone);
//                    contacts.add(normalizedPhone);
//                    Log.d("PhoneNumber", "Normalized Phone number: " + normalizedPhone);
//                }
//            }
//            cursor.close();
//        }
//
//        return contacts;
//    }
//
//    private String normalizePhoneNumber(String phoneNumber) {
//        // Remove all characters except digits and '+'
//        phoneNumber = phoneNumber.replaceAll("[^\\d+]", "");
//
//        if (phoneNumber.startsWith("+880")) {
//            // Already in correct format
//        } else if (phoneNumber.startsWith("880")) {
//            phoneNumber = "+" + phoneNumber;
//        } else if (phoneNumber.startsWith("0")) {
//            phoneNumber = "+880" + phoneNumber.substring(1);
//        } else {
//            // Handle unexpected formats if necessary
//            Log.e("NormalizationError", "Unexpected phone number format: " + phoneNumber);
//        }
//
//        return phoneNumber;
//    }


//    private String hashPhoneNumber(String phoneNumber, String secret) {
//        try {
//            Mac mac = Mac.getInstance("HmacSHA256");
//            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//            mac.init(keySpec);
//            byte[] hashBytes = mac.doFinal(phoneNumber.getBytes(StandardCharsets.UTF_8));
//            return Base64.encodeToString(hashBytes, Base64.NO_WRAP);
//        } catch (Exception e) {
//            Log.e("HashingError", "Error hashing phone number", e);
//            return "";
//        }
//    }



    //sohel
    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Uploading....");
        progressDialog.show();

        FirebaseStorage.getInstance().getReference("images/" + UUID.randomUUID().toString()).putFile(imagePath)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        updateProfilePicture(task.getResult().toString());
                                    }
                                }
                            });
                            Toast.makeText(requireContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void updateProfilePicture(String url) {
        FirebaseDatabase.getInstance().getReference("user/" + uid + "/profilePicture").setValue(url);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("username").getValue(String.class);
                    String profilePictureUrl = dataSnapshot.child("profilePicture").getValue(String.class);

                    username.setText(name);

                    if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                        Picasso.get().load(profilePictureUrl).into(imgProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null) {
            imagePath = data.getData();
            getImageInImageView();
            uploadImage();
        }
    }

    private void getImageInImageView() {
        Bitmap bitmap = null;
        try {
            if (getContext() != null) {
                bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imagePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgProfile.setImageBitmap(bitmap);
    }

    //    private void logout() {
//
//        FirebaseAuth.getInstance().signOut();
//
//        Intent intent = new Intent(getActivity(), LoginPageActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        getActivity().finish();
//    }
    private void logout() {
        FirebaseAuth.getInstance().signOut();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            googleSignInClient.revokeAccess().addOnCompleteListener(task2 -> {
                Intent intent = new Intent(getActivity(), LoginPageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            });
        });
    }

}
