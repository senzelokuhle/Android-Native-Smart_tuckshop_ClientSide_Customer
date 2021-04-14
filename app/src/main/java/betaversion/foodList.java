package betaversion;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import Common.Common;
import Interface.ItemClickListener;
import MenuViewHolder.FoodViewHolder;
import Model.Food;
import Model.Rating;

public class foodList extends AppCompatActivity {
    RecyclerView recycler_food;
    RecyclerView.LayoutManager layoutManager;


    FirebaseDatabase database;
    DatabaseReference foodList;
    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //search func

    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchadapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Food");

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.foodlist);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else {
                        View parentLayout = findViewById(R.id.foodlist);

                        Snackbar.make(parentLayout, "Please check your connection", Snackbar.LENGTH_LONG).show();

                        return;
                    }
                }

            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet(getBaseContext()))
                        loadListFood(categoryId);
                    else {
                        View parentLayout = findViewById(R.id.foodlist);

                        Snackbar.make(parentLayout, "Please check your connection", Snackbar.LENGTH_LONG).show();

                        return;
                    }
                }
            }
        });
        recycler_food = (RecyclerView) findViewById(R.id.recycler_food);
        recycler_food.setHasFixedSize(true);
        //layoutManager = new LinearLayoutManager(this);
        //recycler_food.setLayoutManager(layoutManager);
        //get intent here
        recycler_food.setLayoutManager(new GridLayoutManager(this,2));



    }

    private void loadListFood (String categoryId) {


        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_items,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)) //like:select all from food where menuid=catid

        {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.food_name.setText(model.getName());
                viewHolder.food_price.setText(String.format("R %s", model.getPrice().toString()));
                viewHolder.food_discount.setText(String.format("SAVE: R %s", model.getDiscount().toString()));
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);


                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //start new activity
                        Intent foodDetail = new Intent(foodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey()); //send food id to new activity
                        startActivity(foodDetail);


                        Toast.makeText(foodList.this, "" + local.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        //set adapter
        recycler_food.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    }



