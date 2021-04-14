package betaversion;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

import Common.Common;
import Model.Food;
import Model.Order;
import Model.Rating;
import betaversion.Database.Database;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener{

    TextView food_name,food_price,food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCarts,btn_rating;
    Button btnCart, btnShowComment;
    ElegantNumberButton numberButton;

    RatingBar ratingBar;


    String foodId=" ";
    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingtbl;



    Food currentFood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        //firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Food");
        ratingtbl=database.getReference("Rating");

        //init View

        numberButton = (ElegantNumberButton) findViewById(R.id.number_button);
        btnCart = (Button) findViewById(R.id.btnCart);
        btn_rating = (FloatingActionButton) findViewById(R.id.btn_rating);
        ratingBar=(RatingBar)findViewById(R.id.ratingBar);

        btnShowComment=(Button)findViewById(R.id.btnShowComment);

        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodDetail.this,ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID,foodId);
                startActivity(intent);
            }
        });

        btn_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });


        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database (getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()


                        ));
                View parentLayout = findViewById(R.id.FoodDetail);

                Snackbar.make(parentLayout, "Added to cart", Snackbar.LENGTH_SHORT).show();



            }
        });

        food_description = (TextView) findViewById(R.id.food_description);
        food_name = (TextView) findViewById(R.id.food_name);
        food_price = (TextView) findViewById(R.id.food_price);
        food_image = (ImageView) findViewById(R.id.img_food);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapseAppbar);

        //get food id from intent

        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty()) {
            if(Common.isConnectedToInternet(getBaseContext())) {
                getDetailFood(foodId);
                getRatinfFood(foodId);
            }
            else
            {
                View parentLayout = findViewById(R.id.FoodDetail);

                Snackbar.make(parentLayout, "Please check your connection", Snackbar.LENGTH_LONG).show();

                return;
            }
        }
    }

    private void getRatinfFood(String foodId) {
        com.google.firebase.database.Query foodRating=ratingtbl.orderByChild("foodId").equalTo(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item=postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }
                if(count!=0)
                {
                    float average=sum/count;
                    ratingBar.setRating(average);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very bad","Not Good","Quite OK","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Give your feedback")
                .setTitleTextColor(R.color.black)
                .setHint("comment here...")
                .setHintTextColor(R.color.black)
                .setCommentTextColor(android.R.color.white)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();

    }

    private void getDetailFood(String foodId){
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood=dataSnapshot.getValue(Food.class);

                //set image
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());

                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());

                food_description.setText(currentFood.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        final Rating rating=new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comments);


        ratingtbl.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetail.this,"Thank you for your feedback!",Toast.LENGTH_SHORT).show();

                    }


                });

        /*ratingtbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(Common.currentUser.getPhone()).exists()) {
                    ratingtbl.child(Common.currentUser.getPhone()).removeValue();

                    ratingtbl.child(Common.currentUser.getPhone()).setValue(rating);
                } else
                    {

                        ratingtbl.child(Common.currentUser.getPhone()).setValue(rating);


                }
                View parentLayout = findViewById(R.id.FoodDetail);
                Toast.makeText(FoodDetail.this,"Thank you for your feedback!",Toast.LENGTH_SHORT).show();





            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); */






    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
