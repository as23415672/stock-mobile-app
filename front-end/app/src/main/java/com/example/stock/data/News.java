package com.example.stock.data;

import androidx.annotation.NonNull;

public class News {
    final String category, headline, image, source, summary, url, related;

    final long datetime, id;

    public News(String category, String headline, String image, String source, String summary, String url, String related, long datetime, long id) {
        this.category = category;
        this.headline = headline;
        this.image = image;
        this.source = source;
        this.summary = summary;
        this.url = url;
        this.related = related;
        this.datetime = datetime;
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public String getHeadline() {
        return headline;
    }

    public String getImage() {
        return image;
    }

    public String getSource() {
        return source;
    }

    public String getSummary() {
        return summary;
    }

    public String getUrl() {
        return url;
    }

    public String getRelated() {
        return related;
    }

    public long getDatetime() {
        return datetime;
    }

    public long getId() {
        return id;
    }

    @NonNull
    @Override
    public String toString() {
        return "News{" +
                "category='" + category + '\'' +
                ", headline='" + headline + '\'' +
                ", image='" + image + '\'' +
                ", source='" + source + '\'' +
                ", summary='" + summary + '\'' +
                ", url='" + url + '\'' +
                ", related='" + related + '\'' +
                ", datetime=" + datetime +
                ", id=" + id +
                '}';
    }
}
