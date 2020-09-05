package com.smartechbraintechnologies.freshfishbusiness;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyFishesFragment extends Fragment implements ShortFishDetailsAdapter.OnFishSelectedListener {

    private RecyclerView marketRecycler;
    private Toolbar toolbar;
    private FloatingActionButton addBTN;
    private ProgressDialog mProgress;


    private FirebaseFirestore db;
    private CollectionReference fishPostRef;
    private ArrayList<ShortFishDetailsModel> fishDetailsList = new ArrayList<>();
    private ShortFishDetailsAdapter mAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_fishes, container, false);

        initValues(view);

        setUpRecycler();

        setUpToolbar();

        addBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SelectNewFishActivity.class));
            }
        });

        return view;
    }

    private void setUpToolbar() {
        toolbar.inflateMenu(R.menu.menu_toolbar_market);
        toolbar.setTitle("My Fishes");
        Menu menu = toolbar.getMenu();
        MenuItem menuItem = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setIconifiedByDefault(false);
        searchView.setIconified(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void setUpRecycler() {
        mProgress.setMessage("Please wait...");
        mProgress.show();
        fishPostRef.orderBy("fishPostTime", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                fishDetailsList.clear();
                for (DocumentSnapshot fishDetails : value.getDocuments()) {
                    String sellerID = fishDetails.getString("sellerID");
                    if (sellerID.equals(currentUser.getUid())) {
                        String fishID = fishDetails.getId();
                        String fishImage = fishDetails.getString("fishImage");
                        String fishName = fishDetails.getString("fishName");
                        String fishPrice = fishDetails.getString("fishPrice");
                        String fishAvailability = fishDetails.getString("fishAvailability");
                        String fishLocation = fishDetails.getString("fishLocation");

                        ShortFishDetailsModel fishDetail = new ShortFishDetailsModel(fishID, fishImage, fishName, fishPrice, fishAvailability, fishLocation);
                        fishDetailsList.add(fishDetail);
                    }
                }
                mAdapter = new ShortFishDetailsAdapter(getContext(), fishDetailsList, MyFishesFragment.this);
                marketRecycler.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

        });
        mProgress.dismiss();
    }


    private void initValues(View view) {
        marketRecycler = (RecyclerView) view.findViewById(R.id.market_recycler);
        toolbar = (Toolbar) view.findViewById(R.id.my_fish_toolbar);
        addBTN = (FloatingActionButton) view.findViewById(R.id.my_fishes_add_btn);
        mProgress = new ProgressDialog(getContext());


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        fishPostRef = db.collection("Fish Posts");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        marketRecycler.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void onFishClick(int position) {
        String fishID = fishDetailsList.get(position).getFishID();
        Intent intent = new Intent(getContext(), EditFishPostActivity.class);
        intent.putExtra("FISH ID", fishID);
        startActivity(intent);
    }
}
