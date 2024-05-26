package com.ka.roadsafetysystem;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ka.roadsafetysystem.DataRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class DisplayDataActivity extends AppCompatActivity implements DataRecyclerViewAdapter.OnDeleteClickListener {

    private RecyclerView recyclerView;
    private DataRecyclerViewAdapter adapter;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDatabase = FirebaseDatabase.getInstance().getReference();

        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<AccidentData> dataList = new ArrayList<>();
                for (DataSnapshot districtSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot accidentZoneSnapshot : districtSnapshot.getChildren()) {

                        for (DataSnapshot accidentZoneKey : accidentZoneSnapshot.getChildren()) {
                            String rootId = accidentZoneKey.getKey();

                            String district = districtSnapshot.getKey();
                            String accidentZone = accidentZoneSnapshot.getKey();
                            Double latitude = accidentZoneKey.child("latitude").getValue(Double.class);
                            Double longitude = accidentZoneKey.child("longitude").getValue(Double.class);
                            AccidentData data = new AccidentData(district, accidentZone, latitude, longitude,rootId);
                            dataList.add(data);
                        }
                    }
                }
                adapter = new DataRecyclerViewAdapter(dataList);
                recyclerView.setAdapter(adapter);
                adapter.setOnDeleteClickListener(DisplayDataActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DisplayDataActivity.this, "Failed to read data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(String district,String accidentZone ,String RootID) {
        deleteData(district,accidentZone,RootID);
        System.out.println("RootID"+RootID);
    }
    private void deleteData(String district,String accidentZone, String rootId) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child(district).child(accidentZone).child(rootId);
        rootRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data deletion successful
                        Toast.makeText(getApplicationContext(), "Data deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Data deletion failed
                        Toast.makeText(getApplicationContext(), "Failed to delete data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
