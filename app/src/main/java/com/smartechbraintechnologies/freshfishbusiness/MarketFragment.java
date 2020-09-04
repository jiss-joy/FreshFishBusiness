package com.smartechbraintechnologies.freshfishbusiness;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MarketFragment extends Fragment implements ShortFishDetailsAdapter.OnFishSelectedListener {

    private FloatingActionButton addNewFishBTN;
    private RecyclerView marketRecycler;
    private Toolbar toolbar;


    private FirebaseFirestore db;
    private CollectionReference fishPostRef;
    private ArrayList<ShortFishDetailsModel> fishDetailsList = new ArrayList<>();
    private ShortFishDetailsAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market, container, false);

        initValues(view);

        setUpRecycler();

        setUpToolbar();

        addNewFishBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SelectNewFishActivity.class));
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    private void setUpToolbar() {
        toolbar.inflateMenu(R.menu.menu_market_toolbar);

        Menu menu = toolbar.getMenu();

        MenuItem menuItem = menu.findItem(R.id.toolbar_search);

        SearchView searchView = (SearchView) menuItem.getActionView();

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
        fishPostRef.orderBy("fishPostTime", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                fishDetailsList.clear();
                for (DocumentSnapshot fishDetails : value.getDocuments()) {
                    String fishID = fishDetails.getId();
                    String fishImage = fishDetails.getString("fishImage");
                    String fishName = fishDetails.getString("fishName");
                    String fishPrice = fishDetails.getString("fishPrice");
                    String fishAvailability = fishDetails.getString("fishAvailability");
                    String fishLocation = fishDetails.getString("fishLocation");

                    ShortFishDetailsModel fishDetail = new ShortFishDetailsModel(fishID, fishImage, fishName, fishPrice, fishAvailability, fishLocation);
                    fishDetailsList.add(fishDetail);
                }
                mAdapter = new ShortFishDetailsAdapter(getContext(), fishDetailsList, MarketFragment.this);
                marketRecycler.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

        });
    }


    private void initValues(View view) {
        addNewFishBTN = (FloatingActionButton) view.findViewById(R.id.add_new_fish_btn);
        marketRecycler = (RecyclerView) view.findViewById(R.id.market_recycler);
        toolbar = (Toolbar) view.findViewById(R.id.market_toolbar);

        db = FirebaseFirestore.getInstance();
        fishPostRef = db.collection("Fish Posts");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        marketRecycler.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void onFishClick(int position) {

    }
}
