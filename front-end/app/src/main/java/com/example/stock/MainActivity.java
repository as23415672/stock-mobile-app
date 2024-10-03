package com.example.stock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.stock.adapters.FavoritesAdapter;
import com.example.stock.adapters.PortfolioAdapter;
import com.example.stock.data.Profile;
import com.example.stock.data.PurchaseRecord;
import com.example.stock.data.Quote;
import com.example.stock.singleton.RequestQueueSingleton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    private boolean isLoading, initialLoad;
    private ProgressBar progressBar;
    private LinearLayout linearLayout;
    private TextView dateTextView, cashBalance, netWorth;
    private double cashBalanceValue;
    private RequestQueue requestQueue;
    private static final String AUTOCOMPLETE_TAG = "autocomplete";
    private final String baseUrl = "https://assignment3-service-lrby6jnbia-uw.a.run.app/api";
    private final Gson gson = new Gson();
    private final ArrayList<String> portfolio = new ArrayList<>();
    private final ArrayList<String> favorites = new ArrayList<>();
    private final LinkedHashMap<String, Quote> quotes = new LinkedHashMap<>();
    private final LinkedHashMap<String, Profile> profiles = new LinkedHashMap<>();
    private final LinkedHashMap<String, ArrayList<PurchaseRecord>> purchaseRecords = new LinkedHashMap<>();
    private final PortfolioAdapter portfolioAdapter = new PortfolioAdapter(portfolio, quotes, purchaseRecords);
    private final FavoritesAdapter favoritesAdapter = new FavoritesAdapter(favorites, quotes, profiles);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoUpdater = new Runnable() {
        @Override
        public void run() {
            fetchNewQuotes();
            handler.postDelayed(this, 15000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Stocks");

        initialLoad = true;

        progressBar = findViewById(R.id.progressBar);

        progressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.BLUE));

        linearLayout = findViewById(R.id.mainLayout);

        dateTextView = findViewById(R.id.dateTextView);

        cashBalance = findViewById(R.id.cashBalanceValue);

        netWorth = findViewById(R.id.netWorthValue);

        RecyclerView portfolioView = findViewById(R.id.portfolioView);
        portfolioView.setAdapter(portfolioAdapter);
        portfolioView.setLayoutManager(new LinearLayoutManager(this));
        getPortfolioTouchHelper().attachToRecyclerView(portfolioView);

        RecyclerView favoritesView = findViewById(R.id.favoriteView);
        favoritesView.setAdapter(favoritesAdapter);
        favoritesView.setLayoutManager(new LinearLayoutManager(this));
        getFavoritesTouchHelper().attachToRecyclerView(favoritesView);

        TextView attribution = findViewById(R.id.attribution);
        attribution.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://finnhub.io/"));
            startActivity(i);
        });

        requestQueue = RequestQueueSingleton.getInstance(this).getRequestQueue();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startingConfiguration();
        parseInitialRequests();
        handler.postDelayed(autoUpdater, 15000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(autoUpdater);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.actionBarSearch).getActionView();
        final AutoCompleteTextView autoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        autoComplete.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{}));
        autoComplete.setThreshold(1);
        autoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String ticker = parent.getItemAtPosition(position).toString().split(" ")[0].trim();
            searchView.setQuery(ticker, true);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("ticker", query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ((ArrayAdapter<String>) autoComplete.getAdapter()).clear();
                if (newText.isEmpty()) {
                    return true;
                }

                RequestQueueSingleton.getInstance(MainActivity.this).getRequestQueue().cancelAll(AUTOCOMPLETE_TAG);

                RequestQueueSingleton.getInstance(MainActivity.this).addToRequestQueue(new JsonArrayRequest(Request.Method.GET, baseUrl + "/auto-complete/" + newText, null, response -> {
                    Log.i("MainActivity", response.toString());
                    try {
                        ArrayList<String> suggestions = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            String ticker = response.getJSONObject(i).getString("symbol");
                            if (ticker.contains(".") || !ticker.contains(newText.toUpperCase())) {
                                continue;
                            }
                            String name = response.getJSONObject(i).getString("description");
                            suggestions.add(ticker + " | " + name);
                        }
                        autoComplete.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, suggestions));
                        autoComplete.showDropDown();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }, null).setTag(AUTOCOMPLETE_TAG));
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.actionBarSearch);
        item.setVisible(!isLoading);
        return true;
    }

    private void startingConfiguration() {
        if(initialLoad) {
            isLoading = true;
            invalidateOptionsMenu();
            linearLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
        Date currentDate = new Date();
        dateTextView.setText(new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(currentDate));
    }

    private void parseInitialRequests() {

        CountDownLatch latch = new CountDownLatch(3);

        AtomicReference<JSONArray> portfolioArray = new AtomicReference<>(), favoritesArray = new AtomicReference<>();

        requestQueue.add(new JsonArrayRequest(Request.Method.GET, baseUrl + "/money", null, response -> {
            try {
                cashBalanceValue = response.getJSONObject(0).getDouble("money");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            cashBalance.setText(String.format(Locale.getDefault(), "$%.2f", cashBalanceValue));
            latch.countDown();
        }, null));

        requestQueue.add(new JsonArrayRequest(Request.Method.GET, baseUrl + "/portfolio", null, response -> {
            portfolioArray.set(response);
            latch.countDown();
        }, null));

        requestQueue.add(new JsonArrayRequest(Request.Method.GET, baseUrl + "/watchlist", null, response -> {
            favoritesArray.set(response);
            latch.countDown();
        }, null));

        new Thread(() -> {
            try {
                latch.await();
                parseStartingData(portfolioArray.get(), favoritesArray.get());
            } catch (InterruptedException | NullPointerException | JSONException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void parseStartingData(JSONArray portfolioArray, JSONArray favoritesArray) throws JSONException, InterruptedException {
        ArrayList<String> tickers = new ArrayList<>();

        portfolio.clear();
        favorites.clear();
        quotes.clear();
        profiles.clear();

        for (int i = 0; i < portfolioArray.length(); i++) {
            String ticker = portfolioArray.getJSONObject(i).getString("ticker");
            tickers.add(ticker);
            ArrayList<PurchaseRecord> purchaseRecord = new ArrayList<>();
            int quantity = portfolioArray.getJSONObject(i).getInt("quantity");
            double price = portfolioArray.getJSONObject(i).getDouble("price");
            purchaseRecord.add(new PurchaseRecord(quantity, price));
            if (portfolio.contains(ticker)) {
                purchaseRecords.get(ticker).add(new PurchaseRecord(quantity, price));
            } else {
                portfolio.add(ticker);
                purchaseRecords.put(ticker, purchaseRecord);
            }
        }

        for (int i = 0; i < favoritesArray.length(); i++) {
            String ticker = favoritesArray.getJSONObject(i).getJSONObject("profile").getString("ticker");
            if (!tickers.contains(ticker)) {
                tickers.add(ticker);
            }
            favorites.add(ticker);
        }

        CountDownLatch latch = new CountDownLatch(tickers.size() * 2);

        for (String ticker : tickers) {
            requestQueue.add(new JsonObjectRequest(Request.Method.GET, baseUrl + "/quote/" + ticker, null, response -> {
                Quote quote = gson.fromJson(response.toString(), Quote.class);
                quotes.put(ticker, quote);
                latch.countDown();
            }, null));

            requestQueue.add(new JsonObjectRequest(Request.Method.GET, baseUrl + "/profile/" + ticker, null, response -> {
                Profile profile = gson.fromJson(response.toString(), Profile.class);
                profiles.put(ticker, profile);
                latch.countDown();
            }, null));
        }

        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(this::updateView);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void updateView() {

        updateData();

        if(initialLoad) {
            isLoading = false;
            invalidateOptionsMenu();
            linearLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        initialLoad = false;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData() {
        double netWorthValue = cashBalanceValue;

        for (String ticker : portfolio) {
            int quantity = purchaseRecords.get(ticker).stream().mapToInt(PurchaseRecord::getQuantity).sum();
            double price = Objects.requireNonNull(quotes.get(ticker)).getC();
            netWorthValue += quantity * price;
        }

        netWorth.setText(String.format(Locale.getDefault(), "$%.2f", netWorthValue));

        portfolioAdapter.notifyDataSetChanged();
        favoritesAdapter.notifyDataSetChanged();
    }

    @NonNull
    private ItemTouchHelper getPortfolioTouchHelper() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                final int from = viewHolder.getBindingAdapterPosition();
                final int to = target.getBindingAdapterPosition();

                if (from < to) {
                    for (int i = from; i < to; i++) {
                        Collections.swap(portfolio, i, i + 1);
                    }
                } else {
                    for (int i = from; i > to; i--) {
                        Collections.swap(portfolio, i, i - 1);
                    }
                }

                portfolioAdapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        };

        return new ItemTouchHelper(callback);
    }

    @NonNull
    private ItemTouchHelper getFavoritesTouchHelper() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                final int from = viewHolder.getBindingAdapterPosition();
                final int to = target.getBindingAdapterPosition();

                if (from < to) {
                    for (int i = from; i < to; i++) {
                        Collections.swap(favorites, i, i + 1);
                    }
                } else {
                    for (int i = from; i > to; i--) {
                        Collections.swap(favorites, i, i - 1);
                    }
                }

                favoritesAdapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                try {
                    final int position = viewHolder.getBindingAdapterPosition();
                    final String ticker = favorites.get(position);
                    favorites.remove(position);
                    favoritesAdapter.notifyItemRemoved(position);
                    RequestQueueSingleton.getInstance(MainActivity.this).addToRequestQueue(new JsonObjectRequest(Request.Method.DELETE, baseUrl + "/watchlist/" + ticker, null, null, null));
                } catch (Exception e) {
                    Log.e("MainActivity", e.getMessage());
                }
            }

            @Override
            public void onChildDraw(@NonNull android.graphics.Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(Color.RED)
                        .addSwipeLeftActionIcon(R.drawable.delete)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        return new ItemTouchHelper(callback);
    }

    public void fetchNewQuotes() {
        CountDownLatch latch = new CountDownLatch(quotes.size())  ;
        for (String ticker : quotes.keySet()) {
            requestQueue.add(new JsonObjectRequest(Request.Method.GET, baseUrl + "/quote/" + ticker, null, response -> {
                Quote quote = gson.fromJson(response.toString(), Quote.class);
                quotes.put(ticker, quote);
                latch.countDown();
            }, null));
        }
        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(this::updateData);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}