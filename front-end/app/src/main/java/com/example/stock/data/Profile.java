package com.example.stock.data;

import androidx.annotation.NonNull;

public class Profile {
    private final String country;
    private final String currency;
    private final String estimateCurrency;
    private final String exchange;
    private final String finnhubIndustry;
    private final String ipo;
    private final String logo;
    private final String marketCapitalization;
    private final String name;
    private final String phone;
    private final String shareOutstanding;
    private final String ticker;
    private final String weburl;

    public Profile(String country, String currency, String estimateCurrency, String exchange, String finnhubIndustry, String ipo, String logo, String marketCapitalization, String name, String phone, String shareOutstanding, String ticker, String weburl) {
        this.country = country;
        this.currency = currency;
        this.estimateCurrency = estimateCurrency;
        this.exchange = exchange;
        this.finnhubIndustry = finnhubIndustry;
        this.ipo = ipo;
        this.logo = logo;
        this.marketCapitalization = marketCapitalization;
        this.name = name;
        this.phone = phone;
        this.shareOutstanding = shareOutstanding;
        this.ticker = ticker;
        this.weburl = weburl;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrency() {
        return currency;
    }

    public String getEstimateCurrency() {
        return estimateCurrency;
    }

    public String getExchange() {
        return exchange;
    }

    public String getFinnhubIndustry() {
        return finnhubIndustry;
    }

    public String getIpo() {
        return ipo;
    }

    public String getLogo() {
        return logo;
    }

    public String getMarketCapitalization() {
        return marketCapitalization;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getShareOutstanding() {
        return shareOutstanding;
    }

    public String getTicker() {
        return ticker;
    }

    public String getWeburl() {
        return weburl;
    }

    @NonNull
    @Override
    public String toString() {
        return "Profile{" +
                "country='" + country + '\'' +
                ", currency='" + currency + '\'' +
                ", estimateCurrency='" + estimateCurrency + '\'' +
                ", exchange='" + exchange + '\'' +
                ", finnhubIndustry='" + finnhubIndustry + '\'' +
                ", ipo='" + ipo + '\'' +
                ", logo='" + logo + '\'' +
                ", marketCapitalization='" + marketCapitalization + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", shareOutstanding='" + shareOutstanding + '\'' +
                ", ticker='" + ticker + '\'' +
                ", weburl='" + weburl + '\'' +
                '}';
    }
}
