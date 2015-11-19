package com.stoutner.privacybrowser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Webview extends AppCompatActivity {

    static String formattedUrlString;
    static WebView mainWebView;
    static ProgressBar progressBar;
    static EditText urlTextBox;
    static ImageView favoriteIcon;
    static final String homepage = "https://www.duckduckgo.com/";

    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.
    @SuppressLint("SetJavaScriptEnabled")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mainWebView = (WebView) findViewById(R.id.mainWebView);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Remove the title from the action bar.
            actionBar.setDisplayShowTitleEnabled(false);

            // Add the custom app_bar layout, which shows the favoriteIcon, urlTextBar, and progressBar.
            actionBar.setCustomView(R.layout.app_bar);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            // Initialize the variables for favoriteIcon, urlTextBox, and progressBar
            favoriteIcon = (ImageView) actionBar.getCustomView().findViewById(R.id.favoriteIcon);
            urlTextBox = (EditText) actionBar.getCustomView().findViewById(R.id.urlTextBox);
            progressBar = (ProgressBar) actionBar.getCustomView().findViewById(R.id.progressBar);
        }

        mainWebView.setWebViewClient(new WebViewClient() {

            // setWebViewClient makes this WebView the default handler for URLs inside the app, so that links are not kicked out to other apps.
            // Save the URL to formattedUrlString and update urlTextBox before loading mainWebView.
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mainWebView.loadUrl(url);
                return true;
            }

            // Update the URL in urlTextBox when the page starts to load.
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                urlTextBox.setText(url);
            }

            // Update formattedUrlString and urlTextBox.  It is necessary to do this after the page finishes loading because the final URL can change during load.
            @Override
            public void onPageFinished(WebView view, String url) {
                formattedUrlString = url;
                urlTextBox.setText(formattedUrlString);
            }
        });

        mainWebView.setWebChromeClient(new WebChromeClient() {

            // Update the progress bar when a page is loading.
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            // Set the favorite icon when it changes.
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                favoriteIcon.setImageBitmap(Bitmap.createScaledBitmap(icon, 64, 64, true));
            }
        });

        // Set the "go" button on the keyboard to load the URL.
        urlTextBox.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // If the event is a key-down event on the "enter" button, load the URL.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Load the URL into the mainWebView and consume the event.
                    try {
                        loadUrlFromTextBox(mainWebView);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    // If the enter key was pressed, consume the event.
                    return true;
                }
                // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // Allow pinch to zoom.
        mainWebView.getSettings().setBuiltInZoomControls(true);

        // Hide zoom controls.
        mainWebView.getSettings().setDisplayZoomControls(false);

        // Enable JavaScript.
        mainWebView.getSettings().setJavaScriptEnabled(true);

        // Enable DOM Storage.
        mainWebView.getSettings().setDomStorageEnabled(true);

        // Get the intent information that started the app.
        final Intent intent = getIntent();

        if (intent.getData() != null) {
            // Get the intent data and convert it to a string.
            final Uri intentUriData = intent.getData();
            formattedUrlString = intentUriData.toString();
        }

        // If formattedUrlString is null assign the homepage to it.
        if (formattedUrlString == null) {
            formattedUrlString = homepage;
        }

        // Load the initial website.
        mainWebView.loadUrl(formattedUrlString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        return true;
    }

    // @TargetApi(11) turns off the errors regarding copy and paste, which are removied from view in menu_webview.xml for lower version of Android.
    @Override
    @TargetApi(11)
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Sets the commands that relate to the menu entries.
        switch (menuItemId) {
            case R.id.home:
                mainWebView.loadUrl(homepage);
                break;

            case R.id.refresh:
                mainWebView.loadUrl(formattedUrlString);
                break;

            case R.id.back:
                mainWebView.goBack();
                break;

            case R.id.forward:
                mainWebView.goForward();
                break;

            case R.id.copyURL:
                clipboard.setPrimaryClip(ClipData.newPlainText("URL", urlTextBox.getText()));
                break;

            case R.id.pasteURL:
                ClipData.Item clipboardData = clipboard.getPrimaryClip().getItemAt(0);
                urlTextBox.setText(clipboardData.coerceToText(this));
                try {
                    loadUrlFromTextBox(mainWebView);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.shareURL:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, urlTextBox.getText().toString());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share URL"));
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    // Override onBackPressed so that if mainWebView can go back it does when the system back button is pressed.
    @Override
    public void onBackPressed() {
        if (mainWebView.canGoBack()) {
            mainWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public void loadUrlFromTextBox(View view) throws UnsupportedEncodingException {
        // Get the text from urlTextInput and convert it to a string.
        String unformattedUrlString = urlTextBox.getText().toString();
        URL unformattedUrl = null;
        Uri.Builder formattedUri = new Uri.Builder();

        // Check to see if unformattedUrlString is a valid URL.  Otherwise, convert it into a Duck Duck Go search.
        if (Patterns.WEB_URL.matcher(unformattedUrlString).matches()) {

            // Add http:// at the beginning if it is missing.  Otherwise the app will segfault.
            if (!unformattedUrlString.startsWith("http")) {
                unformattedUrlString = "http://" + unformattedUrlString;
            }

            // Convert unformattedUrlString to a URL, then to a URI, and then back to a string, which sanitizes the input and adds in any missing components.
            try {
                unformattedUrl = new URL(unformattedUrlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            // The ternary operator (? :) makes sure that a null pointer exception is not thrown, which would happen if .get was called on a null value.
            final String scheme = unformattedUrl != null ? unformattedUrl.getProtocol() : null;
            final String authority = unformattedUrl != null ? unformattedUrl.getAuthority() : null;
            final String path = unformattedUrl != null ? unformattedUrl.getPath() : null;
            final String query = unformattedUrl != null ? unformattedUrl.getQuery() : null;
            final String fragment = unformattedUrl != null ? unformattedUrl.getRef() : null;

            formattedUri.scheme(scheme).authority(authority).path(path).query(query).fragment(fragment);
            formattedUrlString = formattedUri.build().toString();

        } else {
            // Sanitize the search input.
            final String encodedUrlString = URLEncoder.encode(unformattedUrlString, "UTF-8");
            formattedUrlString = "https://duckduckgo.com/?q=" + encodedUrlString;
        }

        mainWebView.loadUrl(formattedUrlString);

        // Hides the keyboard so we can see the webpage.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);
    }
}
