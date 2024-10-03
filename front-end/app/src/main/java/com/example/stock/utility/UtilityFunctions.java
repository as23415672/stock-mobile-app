package com.example.stock.utility;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class UtilityFunctions {
    public static void adjustDialogSize(Dialog dialog) {
        Window window = dialog.getWindow();
        Display display = window.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (width * 0.95); // Set dialog width to 90% of the screen width
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Adjust height as needed
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public static void OpenUrl(String url, Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    public static void PostToTwitter(String url, String headline, Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?url=" + url + "&text=" + headline));
        context.startActivity(browserIntent);
    }

    public static void PostToFacebook(String url, Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/sharer/sharer.php?u=" + url));
        context.startActivity(browserIntent);
    }
}
