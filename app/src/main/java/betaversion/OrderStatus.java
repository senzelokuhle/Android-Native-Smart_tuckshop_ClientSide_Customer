package betaversion;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import Common.Common;
import MenuViewHolder.OrderViewHolder;

public class  OrderStatus extends Activity {
    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //firebase

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView) findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrder(Common.currentUser.getPhone());

        //if (getIntent() == null)
          //  loadOrder(Common.currentUser.getPhone());
        //else
          //  loadOrder(getIntent().getStringExtra("userPhone"));


    }

    private void loadOrder(String phone) {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.activity_order__layout,
                OrderViewHolder.class,
                requests.orderByChild("phone").equalTo(phone)

                //        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                // .setQuery(query, Request.class)
                // .setLayout(R.layout.activity_order_layout).build();

                /* Query query=requests.orderByChild("phone").equalTo(phone);
                    FirebaseRecyclerOptions<Request> options=new FirebaseRecyclerOptions.Builder<Request>()
                            .setQuery(query,Request.class)
                            .build();
                            */


       ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtOrderComment.setText(model.getComment());

            }
        };
        recyclerView.setAdapter(adapter);
    }

}

