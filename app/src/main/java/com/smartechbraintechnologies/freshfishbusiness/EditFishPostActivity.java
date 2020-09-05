package com.smartechbraintechnologies.freshfishbusiness;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditFishPostActivity extends AppCompatActivity {

    public static final int CAMERA_REQUEST_CODE = 1;
    public static final int PERMISSIONS_REQUEST_CODE = 1;

    private ExtendedFloatingActionButton camera_btn, edit_update_btn, available_btn, oos_btn;
    private CircleImageView fish_pic;
    private EditText fish_name, fish_price, fish_location;

    private Uri fishImage;
    private String fishID;
    private String fishName;
    private String fishPrice = "";
    private String fishLocation = "";
    private String fishAvailability = "";

    private StorageReference mStorageRef;
    private ProgressDialog mProgress;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Map<String, Object> fishPost = new HashMap<>();
    private CollectionReference fishPostRef;
    private CollectionReference sellerRef;
    private boolean photoFlag = false;
    private boolean updateBTN = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_fish_post);

        Intent intent = getIntent();
        fishID = intent.getStringExtra("FISH ID");

        initValues();

        loadData();

        lockFields();

        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera_btn.shrink();
                getPermissions();

            }
        });

        available_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fishAvailability = "Available";

                oos_btn.setTextColor(Color.parseColor("#FF0000"));
                oos_btn.setBackgroundColor(Color.parseColor("#FFFFFF"));

                available_btn.setTextColor(Color.parseColor("#FFFFFF"));
                available_btn.setBackgroundColor(Color.parseColor("#00FF0C"));
            }
        });
        oos_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fishAvailability = "Out of Stock";

                available_btn.setTextColor(Color.parseColor("#00FF0C"));
                available_btn.setBackgroundColor(Color.parseColor("#FFFFFF"));

                oos_btn.setTextColor(Color.parseColor("#FFFFFF"));
                oos_btn.setBackgroundColor(Color.parseColor("#FF0000"));
            }
        });

        edit_update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updateBTN) {
                    verifyDetails();
                } else {
                    edit_update_btn.setIconResource(R.drawable.update);
                    unlockFields();
                    edit_update_btn.extend();
                    updateBTN = true;
                }
            }
        });

    }

    private void updateFishwithoutPic() {
        mProgress.setMessage("Posting Fish on the Market...");
        mProgress.show();
        sellerRef.document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String sellerName = documentSnapshot.getString("sellerName");

                fishPost.put("fishAvailability", fishAvailability);
                fishPost.put("fishLocation", fishLocation);
                fishPost.put("fishName", fishName);
                fishPost.put("fishPrice", fishPrice);
                fishPost.put("fishPostTime", timestamp);
                fishPost.put("sellerName", sellerName);
                fishPost.put("sellerID", currentUser.getUid());

                fishPostRef.document(fishID).update(fishPost).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mProgress.dismiss();
                        Toast.makeText(EditFishPostActivity.this, "Successfully Updated.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditFishPostActivity.this, MainActivity.class));
                    }
                });
            }
        });
    }

    private void updateFishwithPic() {
        mProgress.setMessage("Updating Fish on the Market...");
        mProgress.show();
        final StorageReference filepath = mStorageRef.child(Objects.requireNonNull(fishImage.getLastPathSegment()));
        filepath.putFile(fishImage)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        if (taskSnapshot.getMetadata() != null) {
                            if (taskSnapshot.getMetadata().getReference() != null) {
                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();

                                        sellerRef.document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                                String sellerName = documentSnapshot.getString("sellerName");

                                                fishPost.put("fishAvailability", fishAvailability);
                                                fishPost.put("fishImage", downloadUrl);
                                                fishPost.put("fishLocation", fishLocation);
                                                fishPost.put("fishName", fishName);
                                                fishPost.put("fishPrice", fishPrice);
                                                fishPost.put("fishPostTime", timestamp);
                                                fishPost.put("sellerName", sellerName);
                                                fishPost.put("sellerID", currentUser.getUid());

                                                fishPostRef.document(fishID).update(fishPost).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        mProgress.dismiss();
                                                        Toast.makeText(EditFishPostActivity.this, "Successfully Updated.", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(EditFishPostActivity.this, MainActivity.class));
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(EditFishPostActivity.this, "Update Unsuccessful", Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
                });
    }


    private void verifyDetails() {
        fishName = fish_name.getText().toString();
        fishPrice = fish_price.getText().toString();
        fishLocation = fish_location.getText().toString();

        if (fishPrice.isEmpty()) {
            Toast.makeText(this, "Please enter the fish price", Toast.LENGTH_SHORT).show();
            fish_price.requestFocus();
        } else if (fishLocation.isEmpty()) {
            Toast.makeText(this, "Please enter the fish location", Toast.LENGTH_SHORT).show();
            fish_location.requestFocus();
        } else if (fishAvailability.equals("")) {
            Toast.makeText(this, "Please select the availability of the fish", Toast.LENGTH_SHORT).show();
            available_btn.requestFocus();
            oos_btn.requestFocus();
        } else {

            Dialog dialog = new Dialog(EditFishPostActivity.this);
            dialog.setContentView(R.layout.post_confirmation_pop_up);
            Window window = dialog.getWindow();
            window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            TextView fish_name = (TextView) dialog.findViewById(R.id.pop_up_fish_name);
            TextView fish_price = (TextView) dialog.findViewById(R.id.pop_up_fish_price);
            TextView fish_availability = (TextView) dialog.findViewById(R.id.pop_up_fish_availability);
            TextView fish_location = (TextView) dialog.findViewById(R.id.pop_up_fish_loaction);
            ExtendedFloatingActionButton confirm_btn = (ExtendedFloatingActionButton) dialog.findViewById(R.id.pop_up_confirm_btn);
            ExtendedFloatingActionButton cancel_btn = (ExtendedFloatingActionButton) dialog.findViewById(R.id.pop_up_cancel_btn);
            fish_name.setText(fishName);
            fish_price.setText("Rs." + fishPrice + "/Kg");
            if (fishAvailability.equals("Available")) {
                fish_availability.setTextColor(Color.parseColor("#00FF0C"));
            } else {
                fish_availability.setTextColor(Color.parseColor("#FF0000"));
            }
            fish_availability.setText(fishAvailability);
            fish_location.setText(fishLocation);
            confirm_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (photoFlag) {
                        updateFishwithPic();
                    } else {
                        updateFishwithoutPic();
                    }

                }
            });
            cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            dialog.show();
        }
    }

    private void loadData() {
        mProgress.setMessage("Please wait...");
        mProgress.show();
        fishPostRef.document(fishID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Picasso.get().load(documentSnapshot.getString("fishImage")).into(fish_pic);
                fish_name.setText(documentSnapshot.getString("fishName"));
                fish_price.setText(documentSnapshot.getString("fishPrice"));
                fish_location.setText(documentSnapshot.getString("fishLocation"));
                if (documentSnapshot.getString("fishAvailability").equals("Available")) {
                    fishAvailability = "Available";

                    oos_btn.setTextColor(Color.parseColor("#FF0000"));
                    oos_btn.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    available_btn.setTextColor(Color.parseColor("#FFFFFF"));
                    available_btn.setBackgroundColor(Color.parseColor("#00FF0C"));
                } else {
                    fishAvailability = "Out of Stock";

                    available_btn.setTextColor(Color.parseColor("#00FF0C"));
                    available_btn.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    oos_btn.setTextColor(Color.parseColor("#FFFFFF"));
                    oos_btn.setBackgroundColor(Color.parseColor("#FF0000"));
                }
                mProgress.dismiss();
            }
        });
    }

    private void lockFields() {
        camera_btn.setEnabled(false);
        fish_name.setEnabled(false);
        fish_price.setEnabled(false);
        fish_location.setEnabled(false);
        available_btn.setEnabled(false);
        oos_btn.setEnabled(false);
    }

    private void unlockFields() {
        camera_btn.setEnabled(true);
        fish_name.setEnabled(true);
        fish_price.setEnabled(true);
        fish_location.setEnabled(true);
        available_btn.setEnabled(true);
        oos_btn.setEnabled(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                fishImage = resultUri;
                Toast.makeText(EditFishPostActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                Picasso.get().load(resultUri).into(fish_pic);
                photoFlag = true;
                mProgress.dismiss();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getPermissions() {
        if (ContextCompat.checkSelfPermission(EditFishPostActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(EditFishPostActivity.this, Manifest.permission.CAMERA)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(EditFishPostActivity.this);
                builder.setTitle("Grant this Permission");
                builder.setMessage("Camera");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(EditFishPostActivity.this,
                                new String[]{
                                        Manifest.permission.CAMERA,
                                },
                                PERMISSIONS_REQUEST_CODE
                        );
                    }
                });
                builder.setNegativeButton("Cancel", null);

                builder.create();
                builder.show();
            } else {
                ActivityCompat.requestPermissions(EditFishPostActivity.this,
                        new String[]{
                                Manifest.permission.CAMERA
                        },
                        PERMISSIONS_REQUEST_CODE
                );
            }

        } else {
            //Permissions already granted part.
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (PERMISSIONS_REQUEST_CODE == requestCode) {
            if ((grantResults.length > 0) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } //Permissions Denied part.
        }
    }


    private void initValues() {
        camera_btn = (ExtendedFloatingActionButton) findViewById(R.id.edit_fish_camera_btn);
        camera_btn.shrink();
        edit_update_btn = (ExtendedFloatingActionButton) findViewById(R.id.edit_fish_edit_update_btn);
        edit_update_btn.shrink();
        available_btn = (ExtendedFloatingActionButton) findViewById(R.id.edit_fish_available_btn);
        oos_btn = (ExtendedFloatingActionButton) findViewById(R.id.edit_fish_out_of_stock_btn);
        fish_pic = (CircleImageView) findViewById(R.id.edit_fish_pic);
        fish_name = (EditText) findViewById(R.id.edit_fish_name);
        fish_price = (EditText) findViewById(R.id.edit_fish_price);
        fish_location = (EditText) findViewById(R.id.edit_fish_location);
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Market Fish Photos");
        mProgress = new ProgressDialog(this);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fishPostRef = db.collection("Fish Posts");
        sellerRef = db.collection("Sellers");
    }
}