package com.example.stock;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.stock.adapters.ChartFragmentAdapter;
import com.example.stock.adapters.NewsAdapter;
import com.example.stock.data.Insider;
import com.example.stock.data.News;
import com.example.stock.data.Profile;
import com.example.stock.data.PurchaseRecord;
import com.example.stock.data.Quote;
import com.example.stock.singleton.RequestQueueSingleton;
import com.example.stock.utility.TimeConstants;
import com.example.stock.utility.UtilityFunctions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class DetailsActivity extends AppCompatActivity {

    private String ticker;
    private ProgressBar progressBar;
    private LinearLayout detailLayout;
    private Profile profile;
    private Quote quote;
    private boolean isLoading, initialLoad = true;
    private RequestQueue requestQueue;
    private double cashBalance;
    private Toast currentToast;
    private final ArrayList<PurchaseRecord> purchaseRecords = new ArrayList<>();
    private final ArrayList<Insider> insiders = new ArrayList<>();
    private final ArrayList<String> favorites = new ArrayList<>(), peers = new ArrayList<>();
    private final ArrayList<News> news = new ArrayList<>();
    private final NewsAdapter newsAdapter = new NewsAdapter(news);
    private final String baseUrl = "https://assignment3-service-lrby6jnbia-uw.a.run.app/api";
    private final Gson gson = new Gson();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoUpdate = new Runnable() {
        @Override
        public void run() {
            fetchNewQuote(null);
            handler.postDelayed(this, 15000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.details), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);

        initialLoad = true;

        ticker = getIntent().getStringExtra("ticker");
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle(ticker);
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBarDetail);
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.BLUE));

        detailLayout = findViewById(R.id.detailLayout);

        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        RecyclerView newsView = findViewById(R.id.detailNewsView);
        newsView.setAdapter(newsAdapter);
        newsView.setLayoutManager(new LinearLayoutManager(this));
    }

    protected void onStart() {
        super.onStart();
        if(initialLoad) {
            progressBar.setVisibility(View.VISIBLE);
            detailLayout.setVisibility(View.GONE);
            isLoading = true;
            invalidateOptionsMenu();
        }
        fetchDetailData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestQueue.add(new JsonArrayRequest(Request.Method.GET, baseUrl + "/portfolio/" + ticker, null, response -> {
            purchaseRecords.clear();
            for (int i = 0; i < response.length(); i++) {
                try {
                    int quantity = response.getJSONObject(i).getInt("quantity");
                    double price = response.getJSONObject(i).getDouble("price");
                    purchaseRecords.add(new PurchaseRecord(quantity, price));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            updatePortfolioSection();
        }, null));

        handler.postDelayed(autoUpdate, 15000);

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(autoUpdate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_activity_menu, menu);

        MenuItem favorite = menu.findItem(R.id.actionBarFavorite);

        favorite.setOnMenuItemClickListener(getOnMenuItemClickListener());
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem favorite = menu.findItem(R.id.actionBarFavorite);
        favorite.setVisible(!isLoading);
        if (favorites != null && favorites.contains(ticker)) {
            favorite.setChecked(true);
            favorite.setIcon(R.drawable.full_star);
        } else {
            favorite.setChecked(false);
            favorite.setIcon(R.drawable.star_border);
        }
        return true;
    }

    @NonNull
    private MenuItem.OnMenuItemClickListener getOnMenuItemClickListener() {
        return item -> {
            item.setOnMenuItemClickListener(null);
            if (item.isChecked()) {
                requestQueue.add(new JsonObjectRequest(Request.Method.DELETE, baseUrl + "/watchlist/" + ticker, null, response -> {
                    favorites.remove(ticker);
                    item.setOnMenuItemClickListener(getOnMenuItemClickListener());
                }, null));
            } else {
                JSONObject body = new JSONObject();
                try {
                    body.put("profile", new JSONObject(gson.toJson(profile)));
                    body.put("quote", new JSONObject(gson.toJson(quote)));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                requestQueue.add(new JsonObjectRequest(Request.Method.POST, baseUrl + "/watchlist", body, response -> {
                    favorites.add(ticker);
                    item.setOnMenuItemClickListener(getOnMenuItemClickListener());
                }, null));
            }
            item.setChecked(!item.isChecked());
            item.setIcon(item.isChecked() ? R.drawable.full_star : R.drawable.star_border);
            showToast(String.format(Locale.getDefault(), "%s is %s", ticker, (item.isChecked() ? " added to favorites" : " removed from favorites")));
            return true;
        };
    }


    private void fetchDetailData() {
        CountDownLatch latch = new CountDownLatch(8);

        requestQueue.add(new JsonArrayRequest(Request.Method.GET, baseUrl + "/money", null, response -> {
            try {
                cashBalance = response.getJSONObject(0).getDouble("money");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            latch.countDown();
        }, volleyError -> {
            Log.e("DetailsActivity", "Failed to fetch cash balance", volleyError);
        }));

        requestQueue.add(new JsonArrayRequest(Request.Method.GET, baseUrl + "/watchlist", null, response -> {
            favorites.clear();
            for (int i = 0; i < response.length(); i++) {
                try {
                    favorites.add(response.getJSONObject(i).getJSONObject("profile").getString("ticker"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            latch.countDown();
        }, null));

        requestQueue.add(new JsonObjectRequest(Request.Method.GET, baseUrl + "/quote/" + ticker, null, response -> {
            quote = gson.fromJson(response.toString(), Quote.class);
            latch.countDown();
        }, null));

        requestQueue.add(new JsonObjectRequest(Request.Method.GET, baseUrl + "/profile/" + ticker, null, response -> {
            profile = gson.fromJson(response.toString(), Profile.class);
            latch.countDown();
        }, null));

        requestQueue.add(new JsonArrayRequest(Request.Method.GET, baseUrl + "/portfolio/" + ticker, null, response -> {
            purchaseRecords.clear();
            for (int i = 0; i < response.length(); i++) {
                try {
                    int quantity = response.getJSONObject(i).getInt("quantity");
                    double price = response.getJSONObject(i).getDouble("price");
                    purchaseRecords.add(new PurchaseRecord(quantity, price));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            latch.countDown();
        }, null));

        requestQueue.add(new JsonArrayRequest(Request.Method.GET, baseUrl + "/peer/" + ticker, null, response -> {
            peers.clear();
            for (int i = 0; i < response.length(); i++) {
                try {
                    peers.add(response.getString(i));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            latch.countDown();
        }, null));

        requestQueue.add(new JsonObjectRequest(Request.Method.GET, baseUrl + "/insider/" + ticker, null, response -> {
            insiders.clear();
            try {
                JSONArray data = response.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject insiderData = data.getJSONObject(i);
                    insiders.add(gson.fromJson(insiderData.toString(), Insider.class));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            latch.countDown();
        }, null));

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(System.currentTimeMillis()));
        String fromDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(System.currentTimeMillis() - TimeConstants.week));

        requestQueue.add(new JsonArrayRequest(Request.Method.GET, baseUrl + "/news/" + ticker + "/" + fromDate + "/" + currentDate, null, response -> {
            news.clear();

            int counter = 0;
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject newsData = response.getJSONObject(i);
                    if (newsData.getString("image").isEmpty()) {
                        continue;
                    }
                    news.add(gson.fromJson(newsData.toString(), News.class));
                    counter++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                if (counter == 15) {
                    break;
                }
            }
            latch.countDown();
        }, null));

        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(this::updateView);
            } catch (InterruptedException e) {
                Log.e("DetailsActivity", "Interrupted while waiting for data to load", e);
            }
        }).start();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateView() {

        renderRecentChart();

        updateSummarySection();

        updatePortfolioSection();

        updateStatsSection();

        updateAboutSection();

        updateInsightsSection();

        newsAdapter.notifyDataSetChanged();

        if(initialLoad) {
            isLoading = false;
            invalidateOptionsMenu();
            progressBar.setVisibility(View.GONE);
            detailLayout.setVisibility(View.VISIBLE);
            initialLoad = false;
        }

    }

    private void updateSummarySection() {
        TextView detailTicker = findViewById(R.id.detailTicker),
                detailPrice = findViewById(R.id.detailPrice),
                detailName = findViewById(R.id.detailName),
                detailTrend = findViewById(R.id.detailTrend);
        ImageView detailTrendIcon = findViewById(R.id.detailTrendIcon);
        detailTicker.setText(ticker);
        detailPrice.setText(String.format(Locale.getDefault(), "$%.2f", quote.getC()));
        detailName.setText(profile.getName());
        detailTrend.setText(String.format(Locale.getDefault(), "$%.2f(%.2f%%)", quote.getD(), quote.getDp()));
        if (quote.getD() > 0.0005) {
            detailTrend.setTextColor(0xFF00B000);
            detailTrendIcon.setImageResource(R.drawable.trending_up);
        } else if (quote.getD() < -0.0005) {
            detailTrend.setTextColor(0xFFB00000);
            detailTrendIcon.setImageResource(R.drawable.trending_down);
        } else {
            detailTrend.setTextColor(0xFF000000);
            detailTrendIcon.setImageBitmap(null);
        }

        ImageView detailLogo = findViewById(R.id.detailLogo);

        Picasso.get().load(profile.getLogo()).into(detailLogo);
    }

    private void updatePortfolioSection() {
        TextView detailShare = findViewById(R.id.detailPortfolioShare),
                detailAverage = findViewById(R.id.detailPortfolioAverage),
                detailCost = findViewById(R.id.detailPortfolioCost),
                detailChange = findViewById(R.id.detailPortfolioChange),
                detailMarketValue = findViewById(R.id.detailPortfolioMarket);

        Button detailTrade = findViewById(R.id.detailPortfolioTrade);

        int totalShares = 0;
        double totalCost = 0;
        double averageCost = 0;
        double change = 0;
        double marketValue = 0;

        if (purchaseRecords != null && !purchaseRecords.isEmpty()) {
            totalShares = purchaseRecords.stream().mapToInt(PurchaseRecord::getQuantity).sum();
            totalCost = purchaseRecords.stream().mapToDouble(record -> record.getPrice() * record.getQuantity()).sum();
            averageCost = totalCost / totalShares;
            change = quote.getC() - averageCost;
            marketValue = totalShares * quote.getC();
        }

        detailShare.setText(String.valueOf(totalShares));
        detailAverage.setText(String.format(Locale.getDefault(), "$%.2f", averageCost));
        detailCost.setText(String.format(Locale.getDefault(), "$%.2f", totalCost));
        detailChange.setText(String.format(Locale.getDefault(), "$%.2f", change));
        detailMarketValue.setText(String.format(Locale.getDefault(), "$%.2f", marketValue));

        if (change > 0) {
            detailChange.setTextColor(0xFF00B000);
            detailMarketValue.setTextColor(0xFF00B000);
        } else if (change < 0) {
            detailChange.setTextColor(0xFFB00000);
            detailMarketValue.setTextColor(0xFFB00000);
        } else {
            detailChange.setTextColor(0xFF000000);
            detailMarketValue.setTextColor(0xFF000000);
        }

        detailTrade.setOnClickListener(v -> fetchNewQuote(this::showTradeDialog));

    }

    private void updateStatsSection() {
        TextView detailOpen = findViewById(R.id.detailStatsOpen),
                detailHigh = findViewById(R.id.detailStatsHigh),
                detailLow = findViewById(R.id.detailStatsLow),
                detailPrev = findViewById(R.id.detailStatsPrev);

        detailOpen.setText(String.format(Locale.getDefault(), "$%.2f", quote.getO()));
        detailHigh.setText(String.format(Locale.getDefault(), "$%.2f", quote.getH()));
        detailLow.setText(String.format(Locale.getDefault(), "$%.2f", quote.getL()));
        detailPrev.setText(String.format(Locale.getDefault(), "$%.2f", quote.getPc()));
    }

    private void updateAboutSection() {
        TextView detailStart = findViewById(R.id.detailAboutIPO),
                detailIndustry = findViewById(R.id.detailAboutIndustry),
                detailWebpage = findViewById(R.id.detailAboutWebpage),
                detailPeers = findViewById(R.id.detailAboutPeers);

        detailStart.setText(profile.getIpo());
        detailIndustry.setText(profile.getFinnhubIndustry());

        detailWebpage.setText(profile.getWeburl());
        detailWebpage.setTextColor(Color.BLUE);
        detailWebpage.setPaintFlags(detailWebpage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        detailWebpage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(profile.getWeburl()));
            startActivity(intent);
        });

        String peersString = String.join(", ", peers);
        SpannableString spannablePeers = getSpannableString(peersString);
        detailPeers.setText(spannablePeers);
        detailPeers.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void updateInsightsSection() {
        TextView detailInsightsName = findViewById(R.id.detailInsightsTableName),
                detailTotalMSPR = findViewById(R.id.detailInsightsTotalMSPR),
                detailTotalChange = findViewById(R.id.detailInsightsTotalChange),
                detailPositiveMSPR = findViewById(R.id.detailInsightsPositiveMSPR),
                detailPositiveChange = findViewById(R.id.detailInsightsPositiveChange),
                detailNegativeMSPR = findViewById(R.id.detailInsightsNegativeMSPR),
                detailNegativeChange = findViewById(R.id.detailInsightsNegativeChange);

        WebView recommendationChart = findViewById(R.id.detailInsightsRecommendationChart),
                epsChart = findViewById(R.id.detailInsightsEPSChart);

        detailInsightsName.setText(profile.getName());

        double positiveMSPR = insiders.stream().filter(insider -> insider.getMSPR() > 0).mapToDouble(Insider::getMSPR).sum(),
                negativeMSPR = insiders.stream().filter(insider -> insider.getMSPR() < 0).mapToDouble(Insider::getMSPR).sum(),
                totalMSPR = positiveMSPR + negativeMSPR,
                positiveChange = insiders.stream().filter(insider -> insider.getChange() > 0).mapToDouble(Insider::getChange).sum(),
                negativeChange = insiders.stream().filter(insider -> insider.getChange() < 0).mapToDouble(Insider::getChange).sum(),
                totalChange = positiveChange + negativeChange;

        detailTotalMSPR.setText(String.format(Locale.getDefault(), "%.2f", totalMSPR));
        detailTotalChange.setText(String.format(Locale.getDefault(), "%.2f", totalChange));
        detailPositiveMSPR.setText(String.format(Locale.getDefault(), "%.2f", positiveMSPR));
        detailPositiveChange.setText(String.format(Locale.getDefault(), "%.2f", positiveChange));
        detailNegativeMSPR.setText(String.format(Locale.getDefault(), "%.2f", negativeMSPR));
        detailNegativeChange.setText(String.format(Locale.getDefault(), "%.2f", negativeChange));

        recommendationChart.getSettings().setJavaScriptEnabled(true);
        recommendationChart.loadUrl("file:///android_asset/recommendationChart.html?ticker=" + ticker);

        epsChart.getSettings().setJavaScriptEnabled(true);
        epsChart.loadUrl("file:///android_asset/epsChart.html?ticker=" + ticker);
    }

    private void renderRecentChart() {
        ArrayList<String> urls = calculateUrls();

        ViewPager2 detailPager = findViewById(R.id.viewPagerDetail);
        detailPager.setAdapter(new ChartFragmentAdapter(getSupportFragmentManager(), getLifecycle(), urls));

        TabLayout tabs = findViewById(R.id.tabLayoutDetail);
        new TabLayoutMediator(tabs, detailPager, (tab, position) -> {
            if (position == 0) {
                tab.setIcon(R.drawable.chart_hour);
            } else {
                tab.setIcon(R.drawable.chart_historical);
            }
        }).attach();
    }

    @NonNull
    private ArrayList<String> calculateUrls() {

        long lastUpdate = quote.getT() * 1000;

        ArrayList<String> urls = new ArrayList<>();
        urls.add("file:///android_asset/recentChart.html?ticker=" + ticker + "&from=" + (lastUpdate - TimeConstants.day) + "&to=" + lastUpdate + "&change=" + quote.getD());
        urls.add("file:///android_asset/historicalChart.html?ticker=" + ticker + "&from=" + (lastUpdate - 2 * TimeConstants.year) + "&to=" + lastUpdate);
        return urls;
    }

    @NonNull
    private SpannableString getSpannableString(String peersString) {
        SpannableString spannablePeers = new SpannableString(peersString);
        int currentPos = 0;
        for (String peer : peers) {
            spannablePeers.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
                    intent.putExtra("ticker", peer);
                    startActivity(intent);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.BLUE);
                    ds.setUnderlineText(true);
                }
            }, currentPos, currentPos + peer.length(), 0);
            currentPos += peer.length() + 2;
        }
        return spannablePeers;
    }

    private void showTradeDialog() {
        final Dialog dialog = new Dialog(DetailsActivity.this);
        dialog.setContentView(R.layout.dialog_trade);

        UtilityFunctions.adjustDialogSize(dialog);

        TextView dialogTitle = dialog.findViewById(R.id.tradeDialogTitle),
                dialogCost = dialog.findViewById(R.id.tradeDialogCost),
                dialogBalance = dialog.findViewById(R.id.tradeDialogBalance),
                dialogInput = dialog.findViewById(R.id.tradeDialogInput),
                successMessage = dialog.findViewById(R.id.successMessage);

        dialogInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                dialogInput.setText("");
            }
        });

        int input = dialogInput.getText().toString().isEmpty() ? 0 : Integer.parseInt(dialogInput.getText().toString());

        dialogTitle.setText(String.format(Locale.getDefault(), "Trade %s shares", profile.getName()));
        updateDialogCost(dialogCost, input);
        dialogBalance.setText(String.format(Locale.getDefault(), "$%.2f to buy %s", cashBalance, ticker));

        Button dialogBuy = dialog.findViewById(R.id.tradeDialogBuy),
                dialogSell = dialog.findViewById(R.id.tradeDialogSell),
                successReturn = dialog.findViewById(R.id.successReturn);

        dialogInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int input;
                try {
                    input = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    input = 0;
                }
                dialogCost.setText(String.format(Locale.getDefault(), "%d*%.2f/shares = %.2f", input, quote.getC(), input * quote.getC()));
            }
        });

        dialogBuy.setOnClickListener(v -> {
            try {
                int value = dialogInput.getText().toString().isEmpty() ? 0 : Integer.parseInt(dialogInput.getText().toString());
                fetchNewQuote(() -> {
                    if (value * quote.getC() > cashBalance) {
                        showToast("Not enough balance to buy");
                        updateDialogCost(dialogCost, value);
                    } else if (value <= 0) {
                        showToast("Cannot buy non-positive shares");
                        updateDialogCost(dialogCost, value);
                    } else {
                        buyStock(value, dialog, successMessage);
                    }
                });
            } catch (NumberFormatException e) {
                showToast("Invalid input");
            }
        });

        dialogSell.setOnClickListener(v -> {
            try {
                int value = dialogInput.getText().toString().isEmpty() ? 0 : Integer.parseInt(dialogInput.getText().toString());
                fetchNewQuote(() -> {
                    if (value > purchaseRecords.stream().mapToInt(PurchaseRecord::getQuantity).sum()) {
                        showToast("Not enough shares to sell");
                        updateDialogCost(dialogCost, value);
                    } else if (value <= 0) {
                        showToast("Cannot sell non-positive shares");
                        updateDialogCost(dialogCost, value);
                    } else {
                        sellStock(value, dialog, successMessage);
                    }
                });
            } catch (NumberFormatException e) {
                showToast("Invalid input");
            }
        });

        successReturn.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();

        dialog.setOnDismissListener(dialogInterface -> onResume());
    }

    private void updateDialogCost(TextView dialogCost, int input) {
        dialogCost.setText(String.format(Locale.getDefault(), "%d*%.2f/shares = %.2f", input, quote.getC(), input * quote.getC()));
    }

    private void sellStock(int value, Dialog dialog, TextView successMessage) {
        requestQueue.add(new JsonArrayRequest(Request.Method.DELETE, baseUrl + "/portfolio" + "?ticker=" + ticker + "&quantity=" + value, null, responseTrade -> {

            double newBalance = cashBalance + value * quote.getC();

            JSONObject moneyBody = new JSONObject();
            try {
                moneyBody.put("money", newBalance);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            requestQueue.add(new JsonObjectRequest(Request.Method.POST, baseUrl + "/money", moneyBody, responseMoney -> {
                View tradeDialog = dialog.findViewById(R.id.tradeDialog),
                        successDialog = dialog.findViewById(R.id.successDialog);

                cashBalance = newBalance;

                successMessage.setText(String.format(Locale.getDefault(), "You have successfully sold %d shares of %s", value, ticker));
                tradeDialog.setVisibility(View.GONE);
                successDialog.setVisibility(View.VISIBLE);
            }, null));
        }, error -> {
            Log.e("DetailsActivity", "Failed to sell shares", error);
        }));
    }

    private void buyStock(int value, Dialog dialog, TextView successMessage) {
        JSONObject body = new JSONObject();
        try {
            body.put("ticker", ticker);
            body.put("quantity", value);
            body.put("price", quote.getC());
            body.put("time", System.currentTimeMillis());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        requestQueue.add(new JsonObjectRequest(Request.Method.POST, baseUrl + "/portfolio", body, responseTrade -> {

            double newBalance = cashBalance - value * quote.getC();

            JSONObject moneyBody = new JSONObject();
            try {
                moneyBody.put("money", newBalance);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            requestQueue.add(new JsonObjectRequest(Request.Method.POST, baseUrl + "/money", moneyBody, responseMoney -> {
                View tradeDialog = dialog.findViewById(R.id.tradeDialog),
                        successDialog = dialog.findViewById(R.id.successDialog);

                cashBalance = newBalance;

                successMessage.setText(String.format(Locale.getDefault(), "You have successfully bought %d shares of %s", value, ticker));
                tradeDialog.setVisibility(View.GONE);
                successDialog.setVisibility(View.VISIBLE);
            }, null));
        }, null));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    public void fetchNewQuote(Runnable runnable) {
        requestQueue.add(new JsonObjectRequest(Request.Method.GET, baseUrl + "/quote/" + ticker, null, response -> {
            quote = gson.fromJson(response.toString(), Quote.class);
            updateSummarySection();
            updatePortfolioSection();
            updateStatsSection();
            if (runnable != null) {
                runnable.run();
            }
        }, null));
    }
}
