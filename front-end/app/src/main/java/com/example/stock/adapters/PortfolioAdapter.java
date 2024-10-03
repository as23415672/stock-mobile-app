package com.example.stock.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stock.DetailsActivity;
import com.example.stock.R;
import com.example.stock.data.PurchaseRecord;
import com.example.stock.data.Quote;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder> {

    private final LinkedHashMap<String, ArrayList<PurchaseRecord>> purchaseRecords;
    private final LinkedHashMap<String, Quote> quotes;
    private final ArrayList<String> portfolio;

    public PortfolioAdapter(ArrayList<String> portfolio, LinkedHashMap<String, Quote> quotes, LinkedHashMap<String, ArrayList<PurchaseRecord>> purchaseRecords) {
        this.portfolio = portfolio;
        this.quotes = quotes;
        this.purchaseRecords = purchaseRecords;
    }

    @NonNull
    @Override
    public PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_main, parent, false);
        return new PortfolioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioViewHolder holder, int position) {
        String ticker = portfolio.get(position);
        Quote quote = quotes.get(ticker);
        ArrayList<PurchaseRecord> purchaseRecord = purchaseRecords.get(ticker);
        holder.bind(ticker, quote, purchaseRecord);
    }

    @Override
    public int getItemCount() {
        return portfolio.size();
    }

    public static class PortfolioViewHolder extends RecyclerView.ViewHolder {
        TextView ticker, totalWorth, stockShare, trend;
        ImageView trendIcon, detailButton;

        public PortfolioViewHolder(@NonNull View itemView) {
            super(itemView);
            ticker = itemView.findViewById(R.id.entryTicker);
            totalWorth = itemView.findViewById(R.id.entryData);
            stockShare = itemView.findViewById(R.id.entryAdditionalInfo);
            trend = itemView.findViewById(R.id.entryTrend);
            trendIcon = itemView.findViewById(R.id.entryTrendIcon);
            detailButton = itemView.findViewById(R.id.entryDetailButton);
        }

        public void bind(String ticker, Quote quote, ArrayList<PurchaseRecord> purchaseRecords) {
            int totalShares = purchaseRecords.stream().mapToInt(PurchaseRecord::getQuantity).sum();
            double price = quote.getC();
            double totalCost = purchaseRecords.stream().mapToDouble(p -> p.getPrice() * p.getQuantity()).sum();
            double averageCost = totalCost / totalShares;
            this.ticker.setText(ticker);
            this.totalWorth.setText(String.format(Locale.getDefault(), "$%.2f", totalShares * price));
            this.stockShare.setText(String.format(Locale.getDefault(), "%d shares", totalShares));
            this.trend.setText(String.format(Locale.getDefault(), "$%.2f(%.2f%%)", (price - averageCost) * totalShares, (price - averageCost) * 100 / averageCost));
            if (price > averageCost + 0.0005) {
                this.trend.setTextColor(0xFF00B000);
                this.trendIcon.setImageResource(R.drawable.trending_up);
            } else if (price < averageCost - 0.0005) {
                this.trend.setTextColor(0xFFB00000);
                this.trendIcon.setImageResource(R.drawable.trending_down);
            } else {
                this.trend.setTextColor(Color.BLACK);
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
