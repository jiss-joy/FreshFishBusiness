package com.smartechbraintechnologies.freshfishbusiness;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class FullOrderDetailsActivity extends AppCompatActivity {

    private TextView fishName, status, price, qty, time, date, total, name, building, area, landmark, city, pin;
    private ExtendedFloatingActionButton acceptBTN, declineBTN;
    private ProgressDialog mProgress;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private CollectionReference addressRef;
    private CollectionReference orderRef;

    private String orderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_order_details);

        Intent intent = getIntent();
        orderID = intent.getStringExtra("Order ID");

        initValues();

        mProgress.setMessage("Fetching Order Details...");
        mProgress.setCancelable(false);
        mProgress.show();

        checkStatus();

        loadAddress();

        acceptBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptOrder();
            }
        });

        declineBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineOrder();
            }
        });
    }

    private void declineOrder() {
        orderRef.document(orderID).update("orderStatus", "Declined");
        finish();
    }

    private void acceptOrder() {
        orderRef.document(orderID).update("orderStatus", "Accepted");
        finish();
    }

    private void loadAddress() {

        addressRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    name.setText(documentSnapshot.getString("addressName"));
                    building.setText(documentSnapshot.getString("addressBuilding"));
                    area.setText(documentSnapshot.getString("addressArea"));
                    landmark.setText(documentSnapshot.getString("addressLandmark"));
                    city.setText(documentSnapshot.getString("addressCity"));
                    pin.setText(documentSnapshot.getString("addressPin"));
                    loadOrderSummary();
                }
            }
        });

    }


    private void loadOrderSummary() {
        orderRef.document(orderID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                fishName.setText(value.getString("orderFishName"));
                String fishPrice = value.getString("orderFishPrice");
                String fishQty = value.getString("orderFishQty");
                price.setText("₹" + fishPrice);
                qty.setText(fishQty);
                time.setText(value.getString("orderTime"));
                date.setText(value.getString("orderDate"));
                String orderTotal = String.valueOf(Float.parseFloat(fishPrice) * Float.parseFloat(fishQty));
                total.setText("₹" + orderTotal);
                status.setText(value.getString("orderStatus"));
                mProgress.dismiss();
            }
        });
    }

    private void checkStatus() {
        orderRef.document(orderID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String status = value.getString("orderStatus");
                switch (status) {
                    case "Placed":
                        break;
                    case "Accepted":
                        acceptBTN.setText("ORDER ACCEPTED");
                        acceptBTN.setEnabled(false);
                        declineBTN.setVisibility(View.GONE);
                        break;
                    case "Declined":
                        acceptBTN.setVisibility(View.GONE);
                        declineBTN.setText("ORDER DECLINED");
                        declineBTN.setEnabled(false);
                        break;
                    case "Delivered":
                        declineBTN.setVisibility(View.GONE);
                        acceptBTN.setText("ORDER DELIVERED");
                        acceptBTN.setEnabled(false);
                        break;
                }
            }
        });
    }

    private void initValues() {
        fishName = (TextView) findViewById(R.id.full_order_fish_name);
        status = (TextView) findViewById(R.id.full_order_status);
        price = (TextView) findViewById(R.id.full_order_fish_price);
        qty = (TextView) findViewById(R.id.full_order_fish_qty);
        time = (TextView) findViewById(R.id.full_order_time);
        date = (TextView) findViewById(R.id.full_order_date);
        total = (TextView) findViewById(R.id.full_order_total);
        name = (TextView) findViewById(R.id.full_order_address_name);
        building = (TextView) findViewById(R.id.full_order_address_building);
        area = (TextView) findViewById(R.id.full_order_address_area);
        landmark = (TextView) findViewById(R.id.full_order_address_landmark);
        city = (TextView) findViewById(R.id.full_order_address_city);
        pin = (TextView) findViewById(R.id.full_order_address_pin);
        acceptBTN = (ExtendedFloatingActionButton) findViewById(R.id.full_order_accept_order_btn);
        declineBTN = (ExtendedFloatingActionButton) findViewById(R.id.full_order_decline_order_btn);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        addressRef = db.collection("Orders").document(orderID).collection("Order Address");
        orderRef = db.collection("Orders");

    }
}