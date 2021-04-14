package MenuViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import Interface.ItemClickListener;
import betaversion.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView food_name, food_price ,food_discount;
    public ImageView food_image;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }




    public FoodViewHolder(View itemView) {
        super(itemView);
        food_name=(TextView)itemView.findViewById(R.id.food_name);
        food_image=(ImageView)itemView.findViewById(R.id.food_image);

        food_price=(TextView)itemView.findViewById(R.id.food_price);
        food_discount=(TextView)itemView.findViewById(R.id.food_discount);


        itemView.setOnClickListener(this);




    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);


    }
}
