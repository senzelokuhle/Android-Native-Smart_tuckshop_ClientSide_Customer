package betaversion;

import android.content.Context;
import android.graphics.RadialGradient;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import Common.Common;
import MenuViewHolder.FoodViewHolder;
import MenuViewHolder.ShowCommentViewHolder;
import Model.Food;
import Model.Rating;
import betaversion.R;

public class ShowComment extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;


    DatabaseReference ratingTbl;

    SwipeRefreshLayout swipeRefreshLayout;


    FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder> adapter;

    String foodId = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_comment);


        database = FirebaseDatabase.getInstance();
        ratingTbl = database.getReference("Rating");

        recyclerView = (RecyclerView) findViewById(R.id.recycleComment);



       // LinearLayoutManager layoutManager
         //       = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

       // recyclerView.setLayoutManager(layoutManager);
        final LinearLayoutManager llm = new LinearLayoutManager(this);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getIntent() != null)
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if (!foodId.isEmpty() && foodId != null) {

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(Rating.class,
                            R.layout.show_comment_layout,
                            ShowCommentViewHolder.class,
                            ratingTbl.orderByChild("foodId").equalTo(foodId)) {
                        @Override
                        protected void populateViewHolder(ShowCommentViewHolder holder, Rating model, int position) {
                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.txtComment.setText(model.getComment());
                            holder.txtUserPhone.setText(model.getUserPhone());
                        }

                    };
                    recyclerView.setAdapter(adapter);

                    llm.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(llm);
                }
            }

                });

                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {

                        swipeRefreshLayout.setRefreshing(true);
                        if (getIntent() != null)
                            foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                        if (!foodId.isEmpty() && foodId != null) {
                            adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(Rating.class,
                                    R.layout.show_comment_layout,
                                    ShowCommentViewHolder.class,
                                    ratingTbl.orderByChild("foodId").equalTo(foodId)) {
                                @Override
                                protected void populateViewHolder(ShowCommentViewHolder holder, Rating model, int position) {
                                    holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                                    holder.txtComment.setText(model.getComment());
                                    holder.txtUserPhone.setText(model.getUserPhone());
                                }
                            };
                            recyclerView.setAdapter(adapter);
                            swipeRefreshLayout.setRefreshing(false);
                            //layoutManager=new LinearLayoutManager(this);


                        }


                    }
                });
        recyclerView.setAdapter(adapter);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        swipeRefreshLayout.setRefreshing(false);






    }
}


   /* private void loadComment(String foodId) {


        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }
    */
