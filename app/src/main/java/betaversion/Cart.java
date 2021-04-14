package betaversion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Common.Common;
import MenuViewHolder.CartAdapter;
import Model.MyResponse;
import Model.Notification;
import Model.Order;
import Model.Sender;
import Model.Token;
import betaversion.Database.Database;
import betaversion.Remote.APIServices;
import betaversion.Remote.IGoogleService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener{

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    EditText edtSize;

   public  TextView txtTotalPrice ,txtDiscount;
    Button btnPlace;

    List<Order> cart =new ArrayList<>();

    CartAdapter adapter;

    APIServices mService;

    String address;


    Place shippingAddress;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private  static final int UPDATE_INTERVAL=5000;
    private  static final int FASTEST_INTERVAL=3000;
    private  static final int DISPLACEMENT=10;

    private  static  final int LOCATION_REQUEST_CODE=9999;
    private static final int PLAY_SERVICES_REQUEST=9997;

    IGoogleService mGoogleMapService;

    PlaceAutocompleteFragment edtAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mGoogleMapService=Common.getGoogleMapAPI();
        setContentView(R.layout.activity_cart);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, LOCATION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        mService = Common.getFCMService();
        //firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //init

        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);



        txtTotalPrice = (TextView) findViewById(R.id.total);
        txtDiscount = (TextView) findViewById(R.id.discount);

        btnPlace = (Button) findViewById(R.id.btnPlaceOrder);
        final View parentLayout = findViewById(R.id.cart);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0)
                    showAlertDialog();
                else

                    Snackbar.make(parentLayout, "Your cart is empty!", Snackbar.LENGTH_SHORT).show();

            }
        });


        loadListFood();




    }
    private void createLocationRequest() {
        mLocationRequest =LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }



    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

        }

    private boolean checkPlayServices() {
        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_REQUEST).show();

            else
            {
                Toast.makeText(this,"This device is not supported",Toast.LENGTH_SHORT).show();
                finish();
            }

            return false;
        }
        return true;


    }

    private  void showAlertDialog()
    {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address");

        LayoutInflater inflater=this.getLayoutInflater();
        final View order_address_comment=inflater.inflate(R.layout
        .order_address_comment,null);

        final PlaceAutocompleteFragment edtAddress=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment) ;

        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        //hint for autocomplete edit text
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Enter your address");

        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(14);

        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress=place;

            }

            @Override
            public void onError(Status status) {
                Log.e("ERROR",status.getStatusMessage());

            }
        });


        // MaterialEditText edtAddress=(MaterialEditText)order_address_comment.findViewById(R.id.edtAddress);
        final MaterialEditText edtComment=(MaterialEditText)order_address_comment.findViewById(R.id.edtComment);
        final RadioButton rdiShipToAddress=(RadioButton) order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress=(RadioButton) order_address_comment.findViewById(R.id.rdiHomeAddress);

        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                if(b) {
                    if (Common.currentUser.getHomeAddress() != null ||

                            !TextUtils.isEmpty(Common.currentUser.getHomeAddress())) {

                        address = Common.currentUser.getHomeAddress();
                        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);

                    } else {
                        Toast.makeText(Cart.this, "Please enter address or select option", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&key=AIzaSyANAm3-L-mGPhY0E_gP0uLExvtRKihVQow"))

                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    try { //fetch API
                                        JSONObject jsonObject=new JSONObject(response.body().toString());


                                        JSONArray resultsArray= jsonObject.getJSONArray("results");

                                        JSONObject firstObject=resultsArray.getJSONObject(0);

                                        address=firstObject.getString("formatted_address");

                                        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);



                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();

                                }
                            });
                }
            }
        });





        alertDialog.setView(order_address_comment);

        alertDialog.setIcon(R.drawable.shoppingcart);

        alertDialog.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                if (!rdiShipToAddress.isChecked() && !rdiHomeAddress.isChecked()) {
                    if (shippingAddress != null)
                        address = shippingAddress.getAddress().toString();
                    else {
                        Toast.makeText(Cart.this, "Please enter address or select option", Toast.LENGTH_SHORT).show();
                        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment)).commit();
                        return;

                    }
                }

                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(Cart.this, "Please enter address or select option", Toast.LENGTH_SHORT).show();
                    getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment)).commit();
                    return;
                }


                        Request request =new Request(Common
                                .currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                edtComment.getText().toString(),
                                "0",
                               // String.format("%s,%s",shippingAddress.getLatLng().latitude,shippingAddress.getLatLng().longitude),
                                cart

                        );

                        String order_number=String.valueOf(System.currentTimeMillis());
                        sendNotificationOrder(order_number);


                        requests.child(order_number)
                                .setValue(request);
                        new Database(getBaseContext()).cleanCart();
                        Toast.makeText(Cart.this,"Order placed!",Toast.LENGTH_SHORT).show();

                        finish();
                        return;

                    }




            });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment)).commit();
            }
        });

        alertDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:
            {
                if(grantResults.length>0&& grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                    break;
                }
            }
        }
    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query data=tokens.orderByChild("serverToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Token serverToken=postSnapshot.getValue(Token.class);


                    Notification notification=new Notification("Alert:New Order "+order_number , "You have a new order : "+order_number);

                    Sender content=new Sender(serverToken.getToken(),notification);



                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.body().success==1)
                                    {

                                        Toast.makeText(Cart.this,"Order placed!",Toast.LENGTH_SHORT).show();

                                        finish();
                                    }

                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                Log.e("ERROR",t.getMessage());
                                }
                            });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

//calc total
        double total=0;
       // double sub=0;
        double discount =0;
        for(Order order:cart)
            total+=(Double.parseDouble(order.getPrice()))*(Double.parseDouble(order.getQuantity()))-(Double.parseDouble(order.getDiscount())*Double.parseDouble(order.getQuantity()));
        //sub=(Double.parseDouble(order.getQuantity())*Double.parseDouble(order.getDiscount());

        Locale locale=new Locale("en","ZA");
        NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);
        txtTotalPrice.setText(fmt.format(total));

        for(Order order:cart)
            discount+=Double.parseDouble(order.getDiscount())*Double.parseDouble(order.getQuantity());
        Locale locales=new Locale("en","ZA");
        NumberFormat fmts=NumberFormat.getCurrencyInstance(locales);
        txtDiscount.setText(fmts.format(discount));

    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());

        return true;
    }

    private void deleteCart(int position) {
        cart.remove(position);

        new Database(this).cleanCart();

        for(Order item:cart)
            new Database(this).addToCart(item);

        loadListFood();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation !=null)
        {
            Log.d("LOCATION","Your location :" + mLastLocation.getLatitude()+" ," + mLastLocation.getLongitude());
        }
        else
        {
            Log.d("LOCATION","Could not get your location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();

    }
}
