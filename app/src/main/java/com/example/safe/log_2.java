package com.example.safe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;


public class log_2 extends Activity {
    private EditText Fullname, email;
    private ImageView dob;
    private String gend;
    String no;
    private TextView dob_t;
    private RadioGroup rg;
    DatabaseReference mreference;
    private ProgressBar progressBar;
    private Button btn,gnddr;
    FirebaseAuth fAuth;
    String userid;
    private static final String TAG = "signup";
    private DatePickerDialog.OnDateSetListener mdl;
    @SuppressLint({"ClickableViewAccessibility", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_2);
        Fullname = (EditText) findViewById(R.id.Fname);
        email = (EditText)findViewById(R.id.email);
        dob = (ImageView)findViewById(R.id.dob);
        btn = findViewById(R.id.button2);
        progressBar = findViewById(R.id.progressBar);
        dob_t = (TextView) findViewById(R.id.dob_t);
        no = getIntent().getStringExtra("ID");
        mreference = FirebaseDatabase.getInstance().getReference("users");
        fAuth = FirebaseAuth.getInstance();
        rg = findViewById(R.id.gndr);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup Group, int checkedid) {
                RadioButton gender = findViewById(checkedid);
                gend = gender.getText().toString();

            }
        });
        dob.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(log_2.this, "Select DOB", Toast.LENGTH_SHORT).show();
                Calendar c = Calendar.getInstance();
                int day = c.get(DAY_OF_MONTH);
                int month = c.get(MONTH);
                int year = c.get(YEAR);
                DatePickerDialog date = new DatePickerDialog(log_2.this,
                        android.R.style.Theme_DeviceDefault_Dialog_MinWidth, mdl, year, month, day);
                date.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLUE));
                date.show();
            }
        });

        mdl = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month += 1;
                Log.d(TAG, "onDateSet: mm/dd/yy: " + month + "/" + day + "/" + year);
                String dat = month + "/" + day + "/" + year;
                dob_t.setText(dat);
            }
        };

        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final String lemail = email.getText().toString().trim();
                final String fullname = Fullname.getText().toString().trim();
                final String date = dob_t.getText().toString().trim();
                if (TextUtils.isEmpty(lemail)) {
                    email.setError("Email is Mandatory");
                    return;
                }

                if(TextUtils.isEmpty(date)){
                    dob_t.setError("Enter Valid DOB");
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    HashMap<String, String>map = new HashMap<>();
                    map.put("UID", no);
                    map.put("FullName", fullname);
                    map.put("Email", lemail);
                    map.put("DOB", date);
                    map.put("Gender", gend);
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(fAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(log_2.this, "User Created Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), Map.class));
                        }
                    });
                }
            }
        });
    }

}