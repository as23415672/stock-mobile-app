package com.example.stock.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.stock.R;

public class ChartFragment extends Fragment {
    private static final String ARG_URL = "url";

    public static ChartFragment newInstance(String url) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        WebView webView = view.findViewById(R.id.tabChart);
        webView.getSettings().setJavaScriptEnabled(true);
        String url = getArguments().getString(ARG_URL);
        webView.loadUrl(url);
        return view;
    }
}