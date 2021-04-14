package MenuViewHolder;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Common.Common;
import Interface.ItemClickListener;
import Model.Food;
import Model.Order;
import Model.Rating;
import betaversion.Cart;
import betaversion.Database.Database;
import betaversion.R;
import betaversion.foodList;

class cartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener {
    public TextView txt_cart_name,txt_price;
    public ElegantNumberButton btn_quantity;
    public ImageView cart_image;



    private ItemClickListener itemClickListener;


    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public cartViewHolder(View itemView) {
        super(itemView);
        txt_cart_name=(TextView)itemView.findViewById(R.id.cart_item_name);
        txt_price=(TextView)itemView.findViewById(R.id.cart_item_price);
        btn_quantity=(ElegantNumberButton) itemView.findViewById(R.id.btn_quantity);
       // cart_image=(ImageView)itemView.findViewById(R.id.cart_image);

        itemView.setOnCreateContextMenuListener(this);


    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         menu.setHeaderTitle("Select action");
         menu.add(0,0,getAdapterPosition(),Common.DELETE);

    }
}

public class CartAdapter extends  RecyclerView.Adapter<cartViewHolder>{

public List<Order> listData=new ArrayList<>();
public Cart cart;


    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @NonNull
    @Override
    public cartViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(cart);
        View itemView= inflater.inflate(R.layout.cart_layout,parent,false);
        return new cartViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder( cartViewHolder holder,final int position) {

       // Picasso.with(cart.getBaseContext())
        //        .load(listData.get(position).getImage())
        //        .resize(50,50)
       //         .into(holder.cart_image);





       //TextDrawable drawable = TextDrawable.builder()
         //   .buildRound(""+listData.get(position).getQuantity(),Color.RED);
           //     holder.img_cart_count.setImageDrawable(drawable);


        holder.btn_quantity.setNumber(listData.get(position).getQuantity());

        holder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order=listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //update txttotal

                //calc total
                double total=0;
                List<Order> orders =new Database(cart).getCarts();

                // double sub=0;
                double discount =0;
                for(Order item:orders)
                    total+=(Double.parseDouble(item.getPrice()))*(Double.parseDouble(item.getQuantity()))-(Double.parseDouble(item.getDiscount())*Double.parseDouble(item.getQuantity()));
                //sub=(Double.parseDouble(order.getQuantity())*Double.parseDouble(order.getDiscount());

                Locale locale=new Locale("en","ZA");
                NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);
               cart.txtTotalPrice.setText(fmt.format(total));

                for(Order item:orders)
                    discount+=Double.parseDouble(item.getDiscount())*Double.parseDouble(item.getQuantity());
                Locale locales=new Locale("en","ZA");
                NumberFormat fmts=NumberFormat.getCurrencyInstance(locales);
                cart.txtDiscount.setText(fmts.format(discount));


            }
        });

        Locale locale=new Locale("en","ZA");
        NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);
        double price=(Double.parseDouble(listData.get(position).getPrice()))*
                (Double.parseDouble(listData.get(position).getQuantity()))-(Double.parseDouble(listData.get(position).getDiscount())*Double.parseDouble(listData.get(position).getQuantity()));
        holder.txt_price.setText(fmt.format(price));
        holder.txt_cart_name.setText(listData.get(position).getProductName());




    }

    @Override
    public int getItemCount() {
        return listData.size();
    }



}
