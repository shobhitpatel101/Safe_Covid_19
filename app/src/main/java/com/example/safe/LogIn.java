package com.example.safe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LogIn extends Activity {
    private static final String TAG = "LOGIN";
    DatabaseReference Rreference;
    EditText UiD;
    EditText Pass;
    TextView status;
    Button next;
    Button next2;
    FirebaseAuth fAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        UiD = findViewById(R.id.UiD);
        //Pass = findViewById(R.id.pass);
        status = findViewById(R.id.login_btn);
        next = findViewById(R.id.l_inv_next);
        next2 = findViewById(R.id.v_next);
        Rreference = FirebaseDatabase.getInstance().getReference("users");
        fAuth = FirebaseAuth.getInstance();
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
        UiD.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                next.setVisibility(v.VISIBLE);
                next2.setVisibility(v.INVISIBLE);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uid = UiD.getText().toString().trim();
                //final String pass = Pass.getText().toString().trim();
                if (fAuth.getCurrentUser() != null) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
                validNo(uid);
                Intent intent = new Intent(LogIn.this,Verify.class);
                intent.putExtra("mobile",uid);
                startActivity(intent);
                Toast.makeText(LogIn.this, uid, Toast.LENGTH_LONG).show();

            }
        });
    }

    private void validNo(String no){
        if(no.isEmpty() || no.length() < 10){
            UiD.setError("Enter a valid mobile");
            UiD.requestFocus();
            return;
        }
    }

}
