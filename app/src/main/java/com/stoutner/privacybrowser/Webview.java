package com.stoutner.privacybrowser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;

import java.net.URL;


public class Webview extends ActionBarActivity {

    static String formattedUrlString;
    static WebView mainWebView;
    static final String homepage = "https://www.duckduckgo.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        final EditText urlTextBox = (EditText) findViewById(R.id.urlTextBox);
        mainWebView = (WebView) findViewById(R.id.mainWebView);

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
            // Get the intent data.
            final Uri intentUriData = intent.getData();

            // Try to parse the intent data and store it in urlData.
            URL urlData = null;
            try {
                urlData = new URL(intentUriData.getScheme(), intentUriData.getHost(), intentUriData.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Webview.formattedUrlString = urlData.toString();
        }

        // If formattedUrlString is null assign the homepage to it.
        if (formattedUrlString == null) {
            formattedUrlString = homepage;
        }

        // Place the formattedUrlString in the address bar and load the website.
        urlTextBox.setText(formattedUrlString);
        mainWebView.loadUrl(formattedUrlString);

        // Set the "go" button on the keyboard to load the URL.
        urlTextBox.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Load the URL into the mainWebView and consume the event.
                    loadUrl(mainWebView);
                    return true;
                }
                // Do not consume the event.
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();
        final WebView mainWebView = (WebView) findViewById(R.id.mainWebView);

        // Use the menu items to go forward or back.
        switch (menuItemId) {
            case R.id.back:
                mainWebView.goBack();
                break;
            case R.id.forward:
                mainWebView.goForward();
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

    public void loadUrl(View view) {
        // Get the text from urlTextInput and convert it to a string.
        final EditText urlTextBox = (EditText) findViewById(R.id.urlTextBox);
        final String unformattedUrlString = urlTextBox.getText().toString();

        // Don't do anything unless unformattedUrlString is at least 6 characters long.
        if (unformattedUrlString.length() < 6) { return; }

        // Add correct protocol formatting to the beginning of the URL if needed.
        final String firstSixCharacters = unformattedUrlString.substring(0, 6);

        switch (firstSixCharacters) {
            case "http:/":
                formattedUrlString = unformattedUrlString;
                break;
            case "https:":
                formattedUrlString = unformattedUrlString;
                break;
            case "ftp://":
                formattedUrlString = unformattedUrlString;
                break;
            default:
                formattedUrlString = "http://" + unformattedUrlString;
        }

        /*
        // Parse the unformattedURLString into a Uri.

        final Uri uriData = Uri.parse(unformattedUrlString);

        // Convert the Uri to a URL, chicking for any problems with the formatting.
        URL urlData = null;

        try {
            urlData = new URL(uriData.getScheme(), uriData.getHost(), uriData.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  Convert urlData to a string, which is reqauired by loadUrl method.
        formattedUrlString = urlData.toString();
        */

        final WebView mainWebView = (WebView) findViewById(R.id.mainWebView);

        // Place the URL text back in the address bar and load the website.
        urlTextBox.setText(formattedUrlString);
        mainWebView.loadUrl(formattedUrlString);

        // Hides the keyboard so we can see the webpage.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);
    }
}
