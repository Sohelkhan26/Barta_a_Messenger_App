package com.example.barta_a_messenger_app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.ArrayList;
import java.util.UUID;

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

<<<<<<< HEAD
=======

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ContactAdapter(requireContext(), list);
        recyclerView.setAdapter(adapter);

<<<<<<< HEAD
        // Add click listener to adapter
        adapter.setOnItemClickListener(new ContactAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Contact contact) {
                // Start InboxActivity with contact info
                Intent intent = new Intent(getActivity(), InboxActivity.class);
                intent.putExtra("uid", contact.getUid());
                intent.putExtra("full_name", contact.getFull_name());
                intent.putExtra("profilePic", contact.getProfilePic());
                intent.putExtra("phone", contact.getPhone_number());
                startActivity(intent);
            }
        });

=======
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
        userRef = FirebaseDatabase.getInstance().getReference().child("user").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("username").getValue(String.class);
                    String number = dataSnapshot.child("phone").getValue(String.class);
                    String profilePictureUrl = dataSnapshot.child("profilePicture").getValue(String.class);

                    username.setText(name);
                    phone_nmbr.setText(number);

                    if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                        Picasso.get().load(profilePictureUrl).into(imgProfile);
                    }
                }
            }

<<<<<<< HEAD
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error: " + databaseError.getMessage());
=======
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
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Contacts").child(uid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Contact contact = dataSnapshot.getValue(Contact.class);
                    list.add(contact);
                    String uid2 = contact.getUid();

                    if (uid2 != null) {
<<<<<<< HEAD
                        DatabaseReference contactUserRef = FirebaseDatabase.getInstance()
                                .getReference("user").child(uid2);

                        contactUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
=======
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(uid2);

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String profilePictureUrl = dataSnapshot.child("profilePicture").getValue(String.class);
<<<<<<< HEAD
                                    databaseReference.child(uid2).child("profilePic").setValue(profilePictureUrl);
=======

//                                contact.setProfilePic(profilePictureUrl);
                                    databaseReference.child(uid2).child("profilePic").setValue(profilePictureUrl);

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
<<<<<<< HEAD
                                Toast.makeText(getContext(), "Error loading profile picture", Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Status listener
                        contactUserRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String status = dataSnapshot.child("status").getValue(String.class);
=======
                                // Handle errors
                            }
                        });


                        DatabaseReference userRef2 = FirebaseDatabase.getInstance().getReference("user").child(uid2);

                        userRef2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    String status = dataSnapshot.child("status").getValue(String.class);
//                                contact.setProfilePic(profilePictureUrl);

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
                                    databaseReference.child(uid2).child("status").setValue(status);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
<<<<<<< HEAD
                                Toast.makeText(getContext(), "Error loading status", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
=======
                                // Handle errors
                            }
                        });
                    }


                }

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
<<<<<<< HEAD
                Toast.makeText(getContext(), "Error loading contacts", Toast.LENGTH_SHORT).show();
            }
        });

=======
                // Handle errors
            }
        });

//        imgProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent photoIntent = new Intent(Intent.ACTION_PICK);
//                photoIntent.setType("image/*");
//                startActivityForResult(photoIntent, 1);
//            }
//        });

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
        return view;
    }

    private void uploadImage() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
<<<<<<< HEAD
        progressDialog.setMessage("Uploading....");
=======
        progressDialog.setTitle("Uploading....");
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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
<<<<<<< HEAD

                        progressDialog.dismiss();
                    }

=======
                        progressDialog.dismiss();
                    }
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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

<<<<<<< HEAD
=======
    //    private void logout() {
//
//        FirebaseAuth.getInstance().signOut();
//
//        Intent intent = new Intent(getActivity(), LoginPageActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        getActivity().finish();
//    }
>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
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
<<<<<<< HEAD
=======

>>>>>>> 2a9cdb6f17dc4b4bb22e37f17df83c07534c202c
}
