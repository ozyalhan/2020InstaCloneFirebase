package com.ozy.a2020instaclonefirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    ImageView imageView;
    EditText commentText;
    Bitmap selectedImage;
    Uri imageData;

    private FirebaseStorage firebaseStorage;

    private StorageReference storageReference;

    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_activiy);

        imageView = findViewById(R.id.imageView);
        commentText = findViewById(R.id.commentText);

        firebaseStorage = FirebaseStorage.getInstance();

        storageReference = firebaseStorage.getReference();

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();

    }


    public void uploadClick(View view){
        //görselleri storege'a ekleyeceğiz //
        //  storage'a referans vererek kaydedeceği yeri belirtiyoruz.

        if(imageData != null){
            //uu id universial unique id kullanacağız / her resme aynı isim vermekten bizleri kurtaracak adım.

            UUID uuid = UUID.randomUUID();
            final String imageName = "images/" + uuid + ".jpg";
            //istenilen dizine unique olarak resmi kaydetme.

            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // veri tabanına url'yi kaydedeceğiz !!!!!!!!!!!!!!! mükemmel
                    // download url
                    StorageReference newReference = FirebaseStorage.getInstance().getReference(imageName);
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl =uri.toString(); // ve veriyi db ye kaydedilebilecek hale getirdik.

                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            String userEmail =firebaseUser.getEmail();

                            String comment = commentText.getText().toString();
                            if(comment == null) {
                                comment = " ";
                                //tost vs de yapılabilir ama gerekli gözükmüyor
                            }


                            HashMap<String, Object> postData = new HashMap<>();
                            postData.put("useremail",userEmail);
                            postData.put("usercomment",comment);
                            postData.put("downloadurl",downloadUrl);
                            postData.put("date", FieldValue.serverTimestamp()); // zamanı da kaydedeceğiz.

                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Intent intent = new Intent(UploadActivity.this,FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // gidilen aktivite haricinde diğer aktiviteleri silmemizi sağlar .. Pretty Cool!!!
                                    startActivity(intent);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(getApplicationContext(),"Select Image!",Toast.LENGTH_SHORT).show();
        }
    }




    public void selectImage(View view){
        //izin durumu api23 sonrası için
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //izin verilmedi ise
            //yapı bu şekilde // izin kodu ile pek çok izinler olduğunda kontrol için önemli.

            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            //izin verilmiş ise galeri açıyoruz
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //dosyanın kayıtlı olduğu uri a gideceğiz.
            startActivityForResult(intentToGallery,2); // ayrıştırmak için reqcod a 2 dedik.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //izin verildi ise ne yapılacağını gösterir , bu metod sadece 1 kere izin verildikten sonra çağrılacak
        if (requestCode == 1){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intentToGallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            imageData = data.getData(); //alınan datayı uri olarak kaydediyoruz.

            try {
                if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source); //28 üzerinde getbitmap yerine decode bitmap'i kullanıyoruz.
                    imageView.setImageBitmap(selectedImage);
                } else {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    imageView.setImageBitmap(selectedImage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
