package com.ka.roadsafetysystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DataRecyclerViewAdapter extends RecyclerView.Adapter<DataRecyclerViewAdapter.ViewHolder> {

    private List<AccidentData> dataList;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(String District,String AcidentZone,String position);
    }

    // Method to set the delete click listener
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public DataRecyclerViewAdapter(List<AccidentData> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccidentData data = dataList.get(position);
        holder.districtTextView.setText(data.getDistrict());
        holder.accidentZoneTextView.setText(data.getAccidentZone());
        holder.latitudeTextView.setText(String.valueOf(data.getLatitude()));
        holder.longitudeTextView.setText(String.valueOf(data.getLongitude()));
        holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteClickListener != null) {
                    AccidentData data1=dataList.get(position);
                    deleteClickListener.onDeleteClick(data1.getDistrict(),data1.getAccidentZone(),data1.getRootId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView districtTextView;
        TextView accidentZoneTextView;
        TextView latitudeTextView;
        TextView longitudeTextView;
        ImageView deleteImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            districtTextView = itemView.findViewById(R.id.textViewDistrict);
            accidentZoneTextView = itemView.findViewById(R.id.textViewAccidentZone);
            latitudeTextView = itemView.findViewById(R.id.textViewLatitude);
            longitudeTextView = itemView.findViewById(R.id.textViewLongitude);
            deleteImageView = itemView.findViewById(R.id.imageViewDelete);

        }
    }
}
