package com.example.stock.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stock.DetailsActivity;
import com.example.stock.R;
import com.example.stock.data.Profile;
import com.example.stock.data.Quote;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>{
    private final ArrayList<String> favorites;
    private final LinkedHashMap<String, Quote> quotes;
    private final LinkedHashMap<String, Profile> profiles;

    public FavoritesAdapter(ArrayList<String> favorites, LinkedHashMap<String, Quote> quotes, LinkedHashMap<String, Profile> profiles) {
        this.favorites = favorites;
        this.quotes = quotes;
        this.profiles = profiles;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_main, parent, false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        String ticker = favorites.get(position);
        Quote quote = quotes.get(ticker);
        Profile profile = profiles.get(ticker);
        holder.bind(ticker, quote, profile);
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        TextView ticker, price, name, trend;
        ImageView trendIcon, detailButton;
        public FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            ticker = itemView.findViewById(R.id.entryTicker);
            price = itemView.findViewById(R.id.entryData);
            name = itemView.findViewById(R.id.entryAdditionalInfo);
            trend = itemView.findViewById(R.id.entryTrend);
            trendIcon = itemView.findViewById(R.id.entryTrendIcon);
            detailButton = itemView.findViewById(R.id.entryDetailButton);
        }

        public void bind(String ticker, Quote quote, Profile profile) {
            this.ticker.setText(ticker);
            this.price.setText(String.format(Locale.getDefault(), "$%.2f", quote.getC()));
            this.name.setText(profile.getName());
            this.trend.setText(String.format(Locale.getDefault(), "$%.2f(%.2f%%)", quote.getD(), quote.getDp()));
            if (quote.getD() > 0.0005) {
                this.trend.setTextColor(0xFF00B000);
                this.trendIcon.setImageResource(R.drawable.trending_up);
            } else if(quote.getD() < -0.0005){
                this.trend.setTextColor(0xFFB00000);
                this.trendIcon.setImageResource(R.drawable.trending_down);
            } else {
                this.trend.setTextColor(0xFF000000);
                this.trendIcon.setImageBitmap(null);
            }
            this.detailButton.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                intent.putExtra("ticker", ticker);
                v.getContext().startActivity(intent);
            });
        }
    }
}
