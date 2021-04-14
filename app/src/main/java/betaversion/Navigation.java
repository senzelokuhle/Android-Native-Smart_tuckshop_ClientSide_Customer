package betaversion;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import Common.Common;
import Interface.ItemClickListener;
import MenuViewHolder.MenuViewHolder;
import Model.Banner;
import Model.Category;
import Model.Rating;
import Model.Token;
import betaversion.Database.Database;
import dmax.dialog.SpotsDialog;


public class Navigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseDatabase database;
    DatabaseReference category;

    TextView txtFullName;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;
    CounterFab fab;

    String categoryId=" ";

    HashMap<String,String> image_list;
    SliderLayout nSlider;



    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        nSlider.stopAutoCycle();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);



       Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Smart Tuckshop");
        setSupportActionBar(toolbar);

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(Navigation.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(Navigation.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });



        //init firebase
        database=FirebaseDatabase.getInstance();
        category=database.getReference("Category");

        fab = (CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent cartIntent= new Intent(Navigation.this,Cart.class);
               startActivity(cartIntent);

            }
        });

        fab.setCount(new Database(this).getCountCart());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set name for user
        View headerView=navigationView.getHeaderView(0);
        txtFullName= (TextView)headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());

        //load menu
        recycler_menu = (RecyclerView) findViewById(R.id.recycler_menu);
       // recycler_menu.setLayoutManager(new LinearLayoutManager(this, LinearLayout.HORIZONTAL,false));
        //recycler_menu.setHasFixedSize(true);
       layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);

       // recycler_menu.setLayoutManager(new GridLayoutManager(this,2));


         updateToken(FirebaseInstanceId.getInstance().getToken());

         setupSlider();




    }

    private void setupSlider() {
        nSlider=(SliderLayout)findViewById(R.id.slider);
        image_list=new HashMap<>();

        final DatabaseReference banners=database.getReference("Banner");

        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Banner banner=postSnapShot.getValue(Banner.class);

                    image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());

                }
                for(String key:image_list.keySet())
                {
                    String[]keySplit=key.split("@@@");
                    String nameOfFood=keySplit[0];
                    String idOfFood=keySplit[1];

                    //create slider

                    final TextSliderView textSliderView =new TextSliderView(getBaseContext());
                    textSliderView.description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent=new Intent(Navigation.this,FoodDetail.class);

                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);


                                }
                            });

                            textSliderView.bundle(new Bundle());
                            textSliderView.getBundle().putString("FoodId",idOfFood);

                            nSlider.addSlider(textSliderView);
                            //Remove event after finish

                            banners.removeEventListener(this);



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        nSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        nSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        nSlider.setCustomAnimation(new DescriptionAnimation());
        nSlider.setDuration(4000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart());

    }

    private void updateToken(String token) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token data =new Token(token,false);
        tokens.child(Common.currentUser.getPhone()).setValue(data);


    }

    private void loadMenu() {


     /*  Query query =category.orderByChild("CategoryId").equalTo(categoryId);

        FirebaseRecyclerOptions<Category> options=new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(query,Category.class)
                .build();

         adapter=new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.get().load(model.getImage()).into(viewHolder.imageView);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //get category id and send to new activity

                        Intent foodList = new Intent(Navigation.this, foodList.class);

                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());

                        startActivity(foodList);

                        Toast.makeText(Navigation.this, "" + clickItem.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }


            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item, parent, false);

                return new MenuViewHolder(view);
            }
        };

            recycler_menu.setAdapter(adapter);
            recycler_menu.setLayoutManager(layoutManager);
*/



         adapter=new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class,R.layout.menu_item,MenuViewHolder.class,category) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.imageView);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //get category id and send to new activity

                        Intent foodList=new Intent(Navigation.this,foodList.class);

                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());

                        startActivity(foodList);

                        Toast.makeText(Navigation.this, "" + clickItem.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }


        };
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);





    }

   /* private void loadCategory(String categoryId) {

        recycler_menu.setAdapter(adapter);

        swipeRefreshLayout.setRefreshing(false);


    }
    */

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId()==R.id.registerbutton);
        {
            loadMenu();
        }

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_orders) {
            Intent orderIntent=new Intent(Navigation.this,OrderStatus.class);
            startActivity(orderIntent);

        } else if (id == R.id.nav_logout) {

            Intent signIn=new Intent(Navigation.this,MainActivity.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);

        } else if (id == R.id.nav_cart) {

            Intent cartIntent=new Intent(Navigation.this,Cart.class);
            startActivity(cartIntent);


        }

    else if (id == R.id.nav_delivery) {

            Intent deliveryIntent = new Intent(Navigation.this, delivery.class);
            startActivity(deliveryIntent);
        }
        else if (id == R.id.nav_change_owd)
        {
            showChangePasswordDialog();
        }

        else if (id == R.id.nav_support)
    {
        Intent supportIntent=new Intent(Navigation.this,Main2Activity.class);
        startActivity(supportIntent);
    }

        else if (id == R.id.nav_home_address)
        {
            showHomeAddressDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Navigation.this);
        alertDialog.setTitle("Change home address");
        alertDialog.setMessage("Fill in information");


        LayoutInflater inflater=this.getLayoutInflater();
        View layout_home=inflater.inflate(R.layout.home_address_layout,null);

        final MaterialEditText edtHomeAddress=(MaterialEditText)layout_home.findViewById(R.id.edtHomeAddress);

        alertDialog.setView(layout_home);

        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                dialog.dismiss();

                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Navigation.this,"Address Updated",Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
        alertDialog.show();


    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Navigation.this);
        alertDialog.setTitle("Change password");
        alertDialog.setMessage("Fill in information");


        LayoutInflater inflater=this.getLayoutInflater();
        View layout_pwd=inflater.inflate(R.layout.change_password_layout,null);

        final MaterialEditText edtPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtnewPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtReapeatPassword);
        alertDialog.setView(layout_pwd);

        alertDialog.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final android.app.AlertDialog waitingDialog=new SpotsDialog(Navigation.this);
                waitingDialog.show();

                if(edtPassword.getText().toString().equals(Common.currentUser.getPassword()))
                {
                    if(edtnewPassword.getText().toString().equals(edtRepeatPassword.getText().toString()))
                    {
                        Map<String,Object> passwordUpdate=new HashMap<>();
                        passwordUpdate.put("Password",edtnewPassword.getText().toString());


                        DatabaseReference user=FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                        View parentLayout = findViewById(R.id.drawer_layout);
                                        Snackbar.make(parentLayout, "Password Changed!", Snackbar.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Navigation.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                    else
                    {
                        waitingDialog.dismiss();
                        View parentLayout = findViewById(R.id.drawer_layout);
                        Snackbar snackbar = Snackbar.make(parentLayout, "Password does not match !", Snackbar.LENGTH_LONG)
                                .setAction("Retry", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                      showChangePasswordDialog();

                                    }
                                });
                            snackbar.show();


                    }
                }
                else{
                    waitingDialog.dismiss();
                    View parentLayout = findViewById(R.id.drawer_layout);
                    Snackbar snackbar = Snackbar.make(parentLayout, "Incorrect old password !", Snackbar.LENGTH_LONG)
                            .setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showChangePasswordDialog();

                                }
                            });
                    snackbar.show();

                }

            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }
}
