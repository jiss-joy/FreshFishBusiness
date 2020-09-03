package com.smartechbraintechnologies.freshfishbusiness;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SelectNewFishActivity extends AppCompatActivity implements SelectFishAdapter.OnFishSelectedListener {

    private RecyclerView selectFishRecycler;
    private FloatingActionButton selectFishBTN;

    private FirebaseFirestore db;
    private CollectionReference fishRef;
    private ArrayList<SelectFishModel> fishList = new ArrayList<>();
    private ArrayList<Integer> fishCounter = new ArrayList<>();
    private SelectFishAdapter mAdapter;
    private String selectedFish = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_new_fish);

        initValues();

        setUpRecycler();

        selectFishBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFish.equals("")) {
                    Toast.makeText(SelectNewFishActivity.this, "Please select a fish type.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(SelectNewFishActivity.this, AddNewFishActivity.class);
                    intent.putExtra("SELECTED FISH", selectedFish);
                    startActivity(intent);
                }
            }
        });
    }

    private void setUpRecycler() {
        fishRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                fishList.clear();
                for (DocumentSnapshot fishes : value.getDocuments()) {
                    String fishName = fishes.getString("fishName");
                    SelectFishModel fish = new SelectFishModel(fishName);
                    fishList.add(fish);
                    fishCounter.add(0);
                }
                fishList.sort(new Comparator<SelectFishModel>() {
                    @Override
                    public int compare(SelectFishModel t1, SelectFishModel t2) {
                        return t1.getFishName().compareTo(t2.getFishName());
                    }
                });

                mAdapter = new SelectFishAdapter(SelectNewFishActivity.this, fishList, fishCounter, SelectNewFishActivity.this);
                selectFishRecycler.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initValues() {
        selectFishRecycler = (RecyclerView) findViewById(R.id.select_fish_recycler);
        selectFishBTN = (FloatingActionButton) findViewById(R.id.select_fish_btn);

        db = FirebaseFirestore.getInstance();
        fishRef = db.collection("Fishes");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SelectNewFishActivity.this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        selectFishRecycler.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onFishClick(int position) {
        Parcelable recyclerViewState = selectFishRecycler.getLayoutManager().onSaveInstanceState();
        String fishName = fishList.get(position).getFishName();
        Collections.fill(fishCounter, 0);
        fishCounter.set(position, 1);
        Toast.makeText(this, fishName + " Selected", Toast.LENGTH_SHORT).show();
        selectedFish = fishName;
        mAdapter = new SelectFishAdapter(SelectNewFishActivity.this, fishList, fishCounter, SelectNewFishActivity.this);
        selectFishRecycler.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        selectFishRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SelectNewFishActivity.this, MainActivity.class));
    }
}