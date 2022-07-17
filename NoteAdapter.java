package android.scroll.tlllllll;

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

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    ArrayList<Note> items = new ArrayList<Note>();

    @NonNull
    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);

        return new ViewHolder(itemView);
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

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layout1;
        ImageView pictureImageView;
        TextView titleTextView, contentsTextView, locationTextView, dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layout1 = itemView.findViewById(R.id.layout1);
            pictureImageView = itemView.findViewById(R.id.pictureImageView);
            contentsTextView = itemView.findViewById(R.id.contentsTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            locationTextView =itemView.findViewById(R.id.locationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);

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
