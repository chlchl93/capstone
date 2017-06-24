package com.example.leejaeyun.bikenavi2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Hashtable;

/**
 * Created by 161116 on 2017-06-08.
 */

public class RegisterActivity extends AppCompatActivity {

    String TAG = "RegisterActivity";

    EditText etEmail1;
    EditText etPassword1;
    EditText etPassword2;
    EditText etNumber;
    String stEmail1;
    String stPassword1;
    String stPassword2;
    String stNumber;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference myRef;
    FirebaseUser user;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        etEmail1 = (EditText) findViewById(R.id.etEmail1);
        etPassword1 = (EditText) findViewById(R.id.etPassword1);
        etPassword2 = (EditText) findViewById(R.id.etPassword2);
        etNumber = (EditText) findViewById(R.id.number);



        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                   Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                   Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        Button btnRegister1 =  (Button)findViewById(R.id.btnRegister);
        btnRegister1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                stEmail1 = etEmail1.getText().toString();
                stPassword1 = etPassword1.getText().toString();
                stPassword2 = etPassword2.getText().toString();

                if (stEmail1.isEmpty() || stEmail1.equals("") || stPassword1.isEmpty() || stPassword1.equals("") || stPassword1.isEmpty() || stPassword1.equals("")) {
                    Toast.makeText(RegisterActivity.this, "모두 입력해 주세요", Toast.LENGTH_SHORT).show();
                } else if (!stPassword1.equals(stPassword2)) {
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(stEmail1,stPassword1);
                }
            }
        });

        Button btnLogingo =  (Button)findViewById(R.id.btnlogingo);
        btnLogingo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }







            });

    }

    private void registerUser(String email, String password){

        stNumber = etNumber.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();

                            if (user != null) {
                                Hashtable<String, String> profile = new Hashtable<String, String>();
                                profile.put("email", user.getEmail());
                                profile.put("photo", "");
                                profile.put("key", user.getUid());
                                profile.put("distance", "");
                                if(etNumber != null) {
                                    profile.put("number", stNumber);
                                }
                                myRef.child(user.getUid()).setValue(profile);
                            }
                        }
                }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
