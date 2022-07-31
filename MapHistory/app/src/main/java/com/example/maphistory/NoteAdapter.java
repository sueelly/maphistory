package com.example.maphistory;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements OnNoteItemClickListener {

    ArrayList<Note> items = new ArrayList<Note>();
    OnNoteItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.note_item, viewGroup, false);

        return new ViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.ViewHolder viewHolder, int position) {

        Note item = items.get(position);
        viewHolder.setItem(item);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Note item) {
        items.add(item);
    }

    public void setItems(ArrayList<Note> items) {
        this.items = items;
    }

    public Note getItem(int position) {
        return items.get(position);
    }

    public void setOnItemClickListener(OnNoteItemClickListener listener) {
        this.listener = listener;
    }

    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }



    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layout1;
        ImageView pictureImageView;
        TextView titleTextView, contentsTextView, locationTextView, dateTextView;

        public ViewHolder(@NonNull View itemView, final OnNoteItemClickListener listener) {
            super(itemView);

            layout1 = itemView.findViewById(R.id.layout1);
            pictureImageView = itemView.findViewById(R.id.pictureImageView);
            contentsTextView = itemView.findViewById(R.id.contentsTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            locationTextView =itemView.findViewById(R.id.locationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if( listener != null) {

                        listener.onItemClick(ViewHolder.this, v, position);
                    }
                }
            });

        }

        public void setItem(Note item) {

            String picturePath = item.getPicture();
            if (picturePath != null && !picturePath.equals("")) {
                pictureImageView.setVisibility(View.VISIBLE);
                pictureImageView.setImageResource(R.drawable.ic_info);
            }

            contentsTextView.setText(item.getContents());
            locationTextView.setText((item.getAddress()));
            dateTextView.setText(item.getCreateDateStr());

        }
    }

}