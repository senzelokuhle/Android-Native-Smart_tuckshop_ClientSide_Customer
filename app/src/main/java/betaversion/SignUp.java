package betaversion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

import Common.Common;
import Model.User;

public class SignUp extends AppCompatActivity {
    EditText name_edittext, phone_edittext, RegPassword_editText,
            verificationcode;
    Button Regregisterbutton, sendBtn;
    String OtpverificationCode,otp;
    FirebaseAuth auth;

    private  int BtnType=0;


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        verificationcode = (EditText) findViewById(R.id.verificationcode);
        sendBtn = (Button) findViewById(R.id.sendBtn);


        name_edittext = (EditText) findViewById(R.id.name_edittext);
        phone_edittext = (EditText) findViewById(R.id.phone_edittext);
        RegPassword_editText = (EditText) findViewById(R.id.RegPassword_editText);

       // Regregisterbutton = (Button) findViewById(R.id.Regregisterbutton);

        //init firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        auth = FirebaseAuth.getInstance();



        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BtnType == 0) {


                    String phoneNumber = phone_edittext.getText().toString();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            SignUp.this,
                            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                                    Toast.makeText(SignUp.this, "Check Messages", Toast.LENGTH_SHORT).show();


                                }

                                @Override
                                public void onVerificationFailed(FirebaseException e) {
                                    Toast.makeText(SignUp.this, "OTP ERROR!,Contact developer:0712422070", Toast.LENGTH_SHORT).show();


                                }

                                @Override
                                public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    super.onCodeSent(s, forceResendingToken);
                                    OtpverificationCode = s;
                                    BtnType=1;
                                    sendBtn.setEnabled(true);
                                    Toast.makeText(SignUp.this, "Code sent", Toast.LENGTH_SHORT).show();

                                }


                            }

                    );
                }
                else
                {
                    sendBtn.setEnabled(false);
                    otp = verificationcode.getText().toString();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OtpverificationCode, otp);
                    SigninWithPhone(credential);

                }



            }

            private void SigninWithPhone(PhoneAuthCredential phoneAuthCredential) {
                auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (Common.isConnectedToInternet(getBaseContext())) {
                                final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                                mDialog.setMessage("Please wait... ");

                                mDialog.show();

                                table_user.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //check if already user phone exist
                                        if (dataSnapshot.child(phone_edittext.getText().toString()).exists()) {
                                            mDialog.dismiss();
                                            View parentLayout = findViewById(R.id.signup);
                                            Snackbar.make(parentLayout, "Phone Number already exist", Snackbar.LENGTH_LONG).show();

                                        } else {
                                            mDialog.dismiss();
                                            User user = new User(name_edittext.getText().toString(), RegPassword_editText.getText().toString());
                                            table_user.child(phone_edittext.getText().toString()).setValue(user);
                                            View parentLayout = findViewById(R.id.signup);
                                            Snackbar.make(parentLayout, "Registration successful!!", Snackbar.LENGTH_LONG).show();
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            } else {
                                View parentLayout = findViewById(R.id.signup);

                                Snackbar.make(parentLayout, "Please check your connection", Snackbar.LENGTH_LONG).show();

                                return;
                            }
                        }
                    }
                });


            }
        });


            }


}

