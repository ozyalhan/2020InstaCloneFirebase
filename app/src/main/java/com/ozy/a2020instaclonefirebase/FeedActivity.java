package com.ozy.a2020instaclonefirebase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    ArrayList<String> userEmailFromFB;
    ArrayList<String> userCommentFromFB;
    ArrayList<String> userImageFromFB;

    Intent intentToUpload;

    FeedRecyclerAdapter feedRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        userCommentFromFB = new ArrayList<>(); // initialize ettik.
        userEmailFromFB = new ArrayList<>();
        userImageFromFB = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        getDataFromFireStore();

        //Recycler View

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedRecyclerAdapter  = new FeedRecyclerAdapter(userEmailFromFB,userCommentFromFB,userImageFromFB);
        recyclerView.setAdapter(feedRecyclerAdapter);

    }



    public void getDataFromFireStore(){

        //snapshot veri tabanı güncellendiğinde uygulamada da içeriği günceller... Pretty Cool !!!
        CollectionReference collectionReference = firebaseFirestore.collection("Posts");
        //filtreleme : collectionReference.whereEqualTo().add.. // azalarak tarihe göre sıralama istedik ... en güncel olanlar en yukarıda kalacak.
        collectionReference.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(FeedActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
                }

                if(queryDocumentSnapshots != null){
                    for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                        Map<String,Object> data = snapshot.getData(); //map olarak veriyor. // bu şekilde kaydettiğimiz dataya ulaşmış olduk.
                        String comment = (String) data.get("usercomment"); //objeyi string olrak cast ettik // string olarak atadık.!!!!
                        userCommentFromFB.add(comment);

                        String email = (String) data.get("useremail");
                        userEmailFromFB.add(email);
                        String url = (String) data.get("downloadurl");
                        userImageFromFB.add(url);

                        feedRecyclerAdapter.notifyDataSetChanged();

                        //System.out.println(comment);
                    }

                }



            }
        });




    }











    //menüyü bağlama
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.insta_options_menu,menu);

        return super.onCreateOptionsMenu(menu);

    }



    //menüde seçim yapma
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.add_post){
            intentToUpload = new Intent(FeedActivity.this,UploadActivity.class);
            startActivity(intentToUpload);

        }else if (item.getItemId() == R.id.sign_out){

            try{
                firebaseAuth.signOut();
                Intent intentToSignUp = new Intent(FeedActivity.this,SignUpActivity.class);
                startActivity(intentToSignUp);
                finish();

            }catch (Exception e){
                Toast.makeText(FeedActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
            }

        }

        return super.onOptionsItemSelected(item);

    }

}
