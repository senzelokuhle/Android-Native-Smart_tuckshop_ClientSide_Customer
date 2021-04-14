package betaversion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Common.Common;
import Model.User;
import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
    Button loginbutton;
    Button registerbutton;
    EditText username_editText;
    EditText password_editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginbutton=(Button)findViewById(R.id.loginbutton);
        registerbutton=(Button)findViewById(R.id.registerbutton);
        username_editText=(EditText)findViewById(R.id.username_editText);
        password_editText=(EditText)findViewById(R.id.password_editText);


        //init firebase
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        final DatabaseReference table_user =database.getReference("User");


        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    final android.app.AlertDialog waitingDialog=new SpotsDialog(MainActivity.this);
                    waitingDialog.show();


                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(username_editText.getText().toString().equals(""))
                            {
                                Toast.makeText(MainActivity.this, "Input Text Is Empty.. Please Enter Some Text", Toast.LENGTH_SHORT).show();
                                waitingDialog.dismiss();
                            }
                            else
                            //check if user does not exist in database
                            if (dataSnapshot.child(username_editText.getText().toString()).exists()) {


                                // get user information
                                waitingDialog.dismiss();
                                User user = dataSnapshot.child(username_editText.getText().toString()).getValue(User.class);
                                user.setPhone(username_editText.getText().toString());

                                if (user.getPassword().equals(password_editText.getText().toString())) {
                                    Intent categories = new Intent(MainActivity.this, Navigation.class);
                                    Common.currentUser = user;
                                    startActivity(categories);
                                    finish();

                                    table_user.removeEventListener(this);
                                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();


                                } else {

                                    Toast.makeText(MainActivity.this,"Incorrect Password",Toast.LENGTH_SHORT).show();




                                }
                            } else {
                                waitingDialog.dismiss();
                                Toast.makeText(MainActivity.this,"User does not exist",Toast.LENGTH_SHORT).show();
                                }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
                else
                {
                    View parentLayout = findViewById(R.id.main);

                    Snackbar.make(parentLayout, "Please check your connection", Snackbar.LENGTH_SHORT).show();

                    return;
                }
            }

        });
        registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signup=new Intent(MainActivity.this,SignUp.class);
                startActivity(signup);




            }
        });




    }
}
