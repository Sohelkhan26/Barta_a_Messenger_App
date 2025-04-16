package com.example.barta_a_messenger_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import androidx.annotation.NonNull;

public class ContactDiscoveryActivity extends AppCompatActivity {

    private static final int CONTACTS_PERMISSION_REQUEST = 1001;
    private Button btnGrantPermission;
    private TextView tvPermissionStatus;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ContactDiscoveryAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_discovery);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        btnGrantPermission = findViewById(R.id.btn_grant_permission);
        tvPermissionStatus = findViewById(R.id.tv_permission_status);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactDiscoveryAdapter();
        recyclerView.setAdapter(adapter);

        checkAndRequestPermissions();
        btnGrantPermission.setOnClickListener(v -> checkAndRequestPermissions());
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    CONTACTS_PERMISSION_REQUEST);
        } else {
            updatePermissionStatus(true);
        }
    }

    private void updatePermissionStatus(boolean granted) {
        if (granted) {
            tvPermissionStatus.setText("Permission granted");
            btnGrantPermission.setVisibility(View.GONE);
            startContactDiscovery();
        } else {
            tvPermissionStatus.setText("Permission required");
            btnGrantPermission.setVisibility(View.VISIBLE);
        }
    }

    private void startContactDiscovery() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        // Get phone contacts
        List<String> hashedNumbers = ContactDiscoveryHelper.getHashedPhoneNumbers(this);
        List<DiscoveredContact> discoveredContacts = new ArrayList<>();

        // First get current user's contacts
        String currentUserId = mAuth.getCurrentUser().getUid();
        database.getReference().child("Contacts").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot existingContactsSnapshot) {
                        // Create a set of existing contact phone numbers
                        Set<String> existingPhoneNumbers = new HashSet<>();
                        for (DataSnapshot contactSnapshot : existingContactsSnapshot.getChildren()) {
                            String phoneNumber = contactSnapshot.child("phone_number").getValue(String.class);
                            if (phoneNumber != null) {
                                existingPhoneNumbers.add(phoneNumber);
                                Log.d("ContactDiscovery", "Added existing contact: " + phoneNumber);
                            }
                        }

                        // Query Firebase for All Accounts
                        database.getReference("All Accounts")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        int totalAccounts = (int) dataSnapshot.getChildrenCount();
                                        final int[] processedAccounts = {0};
                                        Log.d("ContactDiscovery", "Total accounts found: " + totalAccounts);
                                        Log.d("ContactDiscovery", "Initial discovered contacts size: " + discoveredContacts.size());

                                        if (totalAccounts == 0) {
                                            updateUI(discoveredContacts);
                                            return;
                                        }

                                        for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                                            String phoneNumber = accountSnapshot.child("phone_no").getValue(String.class);
                                            String uid = accountSnapshot.child("uid").getValue(String.class);

                                            if (phoneNumber != null && uid != null) {
                                                Log.d("ContactDiscovery", "Processing All Accounts number: " + phoneNumber);
                                                String hashedUserNumber = ContactDiscoveryHelper.hashPhoneNumber(phoneNumber);
                                                Log.d("ContactDiscovery", "All Accounts hashed number: " + hashedUserNumber);
                                                Log.d("ContactDiscovery", "Checking against " + hashedNumbers.size() + " contact hashes");

                                                // Check if number exists in phone contacts AND is not already in user's contacts
                                                boolean found = false;
                                                for (String hash : hashedNumbers) {
                                                    if (hash.equals(hashedUserNumber) && !existingPhoneNumbers.contains(phoneNumber)) {
                                                        found = true;
                                                        Log.d("ContactDiscovery", "Hash match found: " + hash);
                                                        break;
                                                    }
                                                }

                                                if (found) {
                                                    if (!uid.equals(mAuth.getCurrentUser().getUid())) {
                                                        Log.d("ContactDiscovery", "Found matching hash for number: " + phoneNumber);
                                                        // Get user details from user node
                                                        database.getReference("user").child(uid)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot userSnapshot) {
                                                                        processedAccounts[0]++;
                                                                        Log.d("ContactDiscovery", "Processing account " + processedAccounts[0] + " of " + totalAccounts);

                                                                        String username = userSnapshot.child("username").getValue(String.class);
                                                                        if (username != null) {
                                                                            Log.d("ContactDiscovery", "Adding contact: " + username + " - " + phoneNumber);
                                                                            discoveredContacts.add(new DiscoveredContact(
                                                                                    username,
                                                                                    phoneNumber
                                                                            ));
                                                                            Log.d("ContactDiscovery", "Current discovered contacts size: " + discoveredContacts.size());
                                                                        }

                                                                        // Update UI when all accounts are processed
                                                                        if (processedAccounts[0] >= totalAccounts) {
                                                                            Log.d("ContactDiscovery", "All accounts processed. Updating UI with " + discoveredContacts.size() + " contacts");
                                                                            updateUI(discoveredContacts);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {
                                                                        processedAccounts[0]++;
                                                                        Log.e("ContactDiscovery", "Error getting user details: " + databaseError.getMessage());
                                                                        if (processedAccounts[0] >= totalAccounts) {
                                                                            updateUI(discoveredContacts);
                                                                        }
                                                                    }
                                                                });
                                                    } else {
                                                        processedAccounts[0]++;
                                                        Log.d("ContactDiscovery", "Hash matched but same user: " + phoneNumber + ". Processed: " + processedAccounts[0] + "/" + totalAccounts);
                                                        if (processedAccounts[0] >= totalAccounts) {
                                                            updateUI(discoveredContacts);
                                                        }
                                                    }
                                                } else {
                                                    processedAccounts[0]++;
                                                    Log.d("ContactDiscovery", "No hash match found for: " + phoneNumber + ". Processed: " + processedAccounts[0] + "/" + totalAccounts);
                                                    if (processedAccounts[0] >= totalAccounts) {
                                                        updateUI(discoveredContacts);
                                                    }
                                                }
                                            } else {
                                                processedAccounts[0]++;
                                                Log.d("ContactDiscovery", "Invalid account data. Processed: " + processedAccounts[0] + "/" + totalAccounts);
                                                if (processedAccounts[0] >= totalAccounts) {
                                                    updateUI(discoveredContacts);
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        updateUI(discoveredContacts);
                                        Toast.makeText(ContactDiscoveryActivity.this,
                                                "Error: " + databaseError.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ContactDiscoveryActivity.this,
                                "Error fetching existing contacts: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        updateUI(discoveredContacts);
                    }
                });
    }

    private void updateUI(List<DiscoveredContact> contacts) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            Log.d("ContactDiscovery", "UpdateUI called with " + contacts.size() + " contacts");
            if (contacts.isEmpty()) {
                tvPermissionStatus.setText("No contacts found using Barta");
                Log.d("ContactDiscovery", "No contacts to display");
            } else {
                tvPermissionStatus.setText("Found " + contacts.size() + " contacts");
                Log.d("ContactDiscovery", "Setting adapter with " + contacts.size() + " contacts");
                for (DiscoveredContact contact : contacts) {
                    Log.d("ContactDiscovery", "Contact in list: " + contact.getName() + " - " + contact.getPhoneNumber());
                }
                adapter.setContacts(contacts);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updatePermissionStatus(true);
            } else {
                updatePermissionStatus(false);
            }
        }
    }
}
