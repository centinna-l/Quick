package com.example.quick;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {

    private Uri mImageUri;
    String myUrl = "";
    private StorageTask storageTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    mImageUri = data.getData();
//                    imageAdded.setImageURI(imageUri);
                    saveData();

                } else {

                    Toast.makeText(AddStoryActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
                    finish();

                }
            }
        });

        Intent photoPicker = new Intent(Intent.ACTION_PICK); // opens the interface to pick the image from the gallery.
        photoPicker.setType("image/*");
        activityResultLauncher.launch(photoPicker);
    }

    private void saveData(){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images")
                .child(mImageUri.getLastPathSegment());

        AlertDialog.Builder builder = new AlertDialog.Builder(AddStoryActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        storageReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete()) ;
                Uri urlImage = uriTask.getResult();
                myUrl = urlImage.toString().isEmpty() ? "" : urlImage.toString();
                uploadData();
                dialog.dismiss();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
            }
        });
    }

    private void uploadData(){
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userID);

        String storyid = reference.push().getKey();
        long timeend = System.currentTimeMillis() + 86400000; //1 day

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("imageurl" , myUrl);
        hashMap.put("timestart" , System.currentTimeMillis());
        hashMap.put("timeend" , timeend);
        hashMap.put("storyid" , storyid);
        hashMap.put("userid" , userID);
        startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
        finish();
    }
}