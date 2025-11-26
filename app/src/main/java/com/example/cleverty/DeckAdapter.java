package com.example.cleverty;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private Context context;
    private ArrayList<Deck> deckList;

    // Constructor to initialize the adapter with data
    public DeckAdapter(Context context, ArrayList<Deck> deckList) {
        this.context = context;
        this.deckList = deckList;
    }

    // This method is called by the RecyclerView to create a new row layout
    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single item (your item_deck.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_deck, parent, false);
        return new DeckViewHolder(view);
    }

    // This method is called for each row to bind the data to the views
    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        // Get the deck object for the current row
        Deck deck = deckList.get(position);

        // Set the data to the TextViews in the row
        holder.deckName.setText(deck.getDeckName());
        holder.cardCount.setText(deck.getCardCount() + " cards");

        // Set a click listener for the entire row
        holder.itemView.setOnClickListener(v -> {
            // TODO: Add logic to open the selected deck for studying
            Toast.makeText(context, "Clicked on " + deck.getDeckName(), Toast.LENGTH_SHORT).show();
        });
    }

    // This method tells the RecyclerView how many items are in the list
    @Override
    public int getItemCount() {
        return deckList.size();
    }

    // The ViewHolder class holds the views for a single row
    public static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView deckName;
        TextView cardCount;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the TextViews within the item_deck.xml layout
            deckName = itemView.findViewById(R.id.deck_name_text);
            cardCount = itemView.findViewById(R.id.deck_card_count_text);
        }
    }
}
