package com.example.quick;

import static com.google.common.io.Files.getFileExtension;
import static java.security.AccessController.getContext;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;
import com.hendraanggrian.appcompat.socialview.widget.SocialTextView;

import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;

    private ImageView close;
    private ImageView imageAdded;
    private TextView post;
    private EditText description;

    private String imageUrl;
    StorageTask uploadTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
//                uploadImage();
            }
        });

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    imageUri = data.getData();
                    imageAdded.setImageURI(imageUri);
                } else {
                    Toast.makeText(PostActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK); // opens the interface to pick the image from the gallery.
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

//        ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
//            if (result.isSuccessful()) {
//                Bitmap cropped = BitmapFactory.decodeFile(result.getUriFilePath(getApplicationContext(), true));
//            }
//        });

//        CropImageOptions cropImageOptions = new CropImageOptions();
//        cropImageOptions.imageSourceIncludeGallery = false;
//        cropImageOptions.imageSourceIncludeCamera = true;
//        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions("", cropImageOptions);
//        cropImage.launch(cropImageContractOptions);


    }

    private void saveData(){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images")
                .child(imageUri.getLastPathSegment());

        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete()) ;
                Uri urlImage = uriTask.getResult();
                imageUrl = urlImage.toString().isEmpty() ? "" : urlImage.toString();
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
        String message = description.getText().toString().trim();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("PostsAndroid");

        String postid = reference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("postid", postid);
        hashMap.put("postimage", imageUrl);
        hashMap.put("description", description.getText().toString());
        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.child(postid).setValue(hashMap);
        startActivity(new Intent(PostActivity.this, MainActivity.class));
        finish();
    }

//    private void uploadImage() {
//        final ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Posting");
//        progressDialog.show();
//
//        if (imageUri != null) {
//            final StorageReference filereference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
//
//            uploadTask = filereference.putFile(imageUri);
//            uploadTask.continueWithTask(new Continuation() {
//                @Override
//                public Object then(@NonNull Task task) throws Exception {
//                    if (!task.isSuccessful()) {
//                        throw task.getException();
//                    }
//
//                    return filereference.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        Uri downloadUri = task.getResult();
//                        imageUrl = downloadUri.toString();
//
//                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("PostsAndroid");
//
//                        String postid = reference.push().getKey();
//
//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("postid", postid);
//                        hashMap.put("postimage", imageUrl);
//                        hashMap.put("description", description.getText().toString());
//                        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
//
//                        reference.child(postid).setValue(hashMap);
//
////                        DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
////                        List<String> hashTags = description.getHashtags();
////                        if (!hashTags.isEmpty()){
////                            for (String hashTag : hashTags){
////                                hashMap.clear();
////
////                                hashMap.put("tag" , hashTag.toLowerCase());
////                                hashMap.put("postid" , postid);
////
////                                mHashTagRef.child(hashTag.toLowerCase()).child(postid).setValue(hashMap);
////                            }
////                        }
//
//                        progressDialog.dismiss();
//
//                        startActivity(new Intent(PostActivity.this, MainActivity.class));
//                        finish();
//                    } else {
//                        Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
//        }
//    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}