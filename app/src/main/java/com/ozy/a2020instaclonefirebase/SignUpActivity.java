    package com.ozy.a2020instaclonefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

    public class SignUpActivity extends AppCompatActivity {

    EditText emailText, passText;
    private FirebaseAuth firebaseAuth;
    String email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailText = findViewById(R.id.emailText);
        passText = findViewById(R.id.passText);

        firebaseAuth = FirebaseAuth.getInstance(); //initialize


        //daha önceden kullanıcı girişi yapılmış ile sürekli longin durumundan bizi kurtaran adım.
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null){

            Toast.makeText(SignUpActivity.this,"Welcome Back",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignUpActivity.this, FeedActivity.class);
            startActivity(intent);
            finish(); //be şekilde activiteyi kapatarak kullanıcının buraya geri gelmesini engelliyoruz.

        }
    }


    public void signIn(View view) {
        email = emailText.getText().toString();
        password = passText.getText().toString();
        if (!email.matches("") && !password.matches("")){
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                    Toast.makeText(SignUpActivity.this, "Welcome", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(SignUpActivity.this, FeedActivity.class);
                    startActivity(intent);
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignUpActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_LONG).show();
                }
            });
        }else{
            Toast.makeText(getApplicationContext(),"Empty email or password",Toast.LENGTH_SHORT).show();
        }
    }



    public void signUp(View view){

        email = emailText.getText().toString();
        password = passText.getText().toString();

        if (!email.matches("") && !password.matches("")){

            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                    Toast.makeText(SignUpActivity.this,"User is created succesfuly",Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(SignUpActivity.this, FeedActivity.class);
                    startActivity(intent);
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignUpActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
            });

        }else{
            Toast.makeText(getApplicationContext(),"Empty email or password",Toast.LENGTH_SHORT).show();
        }


    }

}
