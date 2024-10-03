package com.example.stock.adapters;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stock.R;
import com.example.stock.data.News;
import com.example.stock.utility.TimeConstants;
import com.example.stock.utility.UtilityFunctions;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private final ArrayList<News> news;

    public NewsAdapter(ArrayList<News> news) {
        this.news = news;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_news_first, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_news, parent, false);
        }

        if (viewType == 0) {
            return new FirstNewsViewHolder(view);
        } else {
            return new NewsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = this.news.get(position);
        holder.bind(news);
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView title, source, date;
        ImageView thumbnail;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.detailNewsTitle);
            source = itemView.findViewById(R.id.detailNewsSource);
            date = itemView.findViewById(R.id.detailNewsDate);
            thumbnail = itemView.findViewById(R.id.detailNewsThumbnail);
        }

        public void bind(News news) {
            title.setText(news.getHeadline());
            source.setText(news.getSource());

            String dateStr = getDateStr(news);
            date.setText(dateStr);
            Picasso.get().load(news.getImage()).fit().centerCrop().into(thumbnail);

            itemView.setOnClickListener(v -> {
                Dialog dialog = new Dialog(itemView.getContext());

                dialog.setContentView(R.layout.dialog_news);

                TextView dialogTitle = dialog.findViewById(R.id.dialogNewsTitle),
                        dialogSource = dialog.findViewById(R.id.dialogNewsSource),
                        dialogDate = dialog.findViewById(R.id.dialogNewsDate),
                        dialogSummary = dialog.findViewById(R.id.dialogNewsSummary);

                dialogSource.setText(news.getSource());
                dialogDate.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(news.getDatetime() * 1000));
                dialogTitle.setText(news.getHeadline());
                dialogSummary.setText(news.getSummary());

                UtilityFunctions.adjustDialogSize(dialog);

                ImageButton chromeButton = dialog.findViewById(R.id.chromeButton),
                        twitterButton = dialog.findViewById(R.id.twitterButton),
                        facebookButton = dialog.findViewById(R.id.facebookButton);

                chromeButton.setOnClickListener(v1 -> UtilityFunctions.OpenUrl(news.getUrl(), v1.getContext()));
                twitterButton.setOnClickListener(v1 -> UtilityFunctions.PostToTwitter(news.getUrl(), news.getHeadline(), v1.getContext()));
                facebookButton.setOnClickListener(v1 -> UtilityFunctions.PostToFacebook(news.getUrl(), v1.getContext()));

                dialog.show();
            });
        }

        protected static String getDateStr(News news) {
            long currentTime = System.currentTimeMillis();

            return (currentTime - news.getDatetime() * 1000) < TimeConstants.day ?
                    (currentTime - news.getDatetime() * 1000) < TimeConstants.hour ?
                            (currentTime - news.getDatetime() * 1000) / TimeConstants.minute + "minutes ago" :
                            (currentTime - news.getDatetime() * 1000) / TimeConstants.hour + "hours ago"
                    : (currentTime - news.getDatetime() * 1000) < TimeConstants.year ?
                    new SimpleDateFormat("MMMM dd", Locale.getDefault()).format(news.getDatetime() * 1000) :
                    new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(news.getDatetime() * 1000);
        }
    }

    public static class FirstNewsViewHolder extends NewsViewHolder {

        public FirstNewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.detailNewsFirstTitle);
            source = itemView.findViewById(R.id.detailNewsFirstSource);
            date = itemView.findViewById(R.id.detailNewsFirstDate);
            thumbnail = itemView.findViewById(R.id.detailNewsFirstThumbnail);
        }
    }


}
