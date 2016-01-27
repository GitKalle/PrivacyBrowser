/**
 * Copyright 2015-2016 Soren Stoutner <soren@stoutner.com>.
 *
 * This file is part of Privacy Browser.
 *
 * Privacy Browser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Browser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Browser.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stoutner.privacybrowser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Webview extends AppCompatActivity implements CreateHomeScreenShortcut.CreateHomeScreenSchortcutListener {
    // favoriteIcon is public static so it can be accessed from CreateHomeScreenShortcut.
    public static Bitmap favoriteIcon;

    // mainWebView is used in onCreate and onOptionsItemSelected.
    private WebView mainWebView;
    // formattedUrlString is used in onCreate, onOptionsItemSelected, onCreateHomeScreenShortcutCreate, and loadUrlFromTextBox.
    private String formattedUrlString;
    // homepage is used in onCreate and onOptionsItemSelected.
    private String homepage = "https://www.duckduckgo.com/";
    // enableJavaScript is used in onCreate, onCreateOptionsMenu, and onOptionsItemSelected.
    private boolean enableJavaScript;
    // enableDomStorage is used in onCreate, onCreateOptionsMenu, and onOptionsItemSelected.
    private boolean enableDomStorage;

    /*  enableSaveFormData does nothing until database storage is implemented.
    // enableSaveFormData is used in onCreate, onCreateOptionsMenu, and onOptionsItemSelected.
    private boolean enableSaveFormData;
    */

    // actionBar is used in onCreate and onOptionsItemSelected.
    private ActionBar actionBar;

    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.
    @SuppressLint("SetJavaScriptEnabled")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        final FrameLayout fullScreenVideoFrameLayout = (FrameLayout) findViewById(R.id.fullScreenVideoFrameLayout);
        final Activity mainWebViewActivity = this;

        mainWebView = (WebView) findViewById(R.id.mainWebView);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            // Remove the title from the action bar.
            actionBar.setDisplayShowTitleEnabled(false);

            // Add the custom app_bar layout, which shows the favoriteIcon, urlTextBar, and progressBar.
            actionBar.setCustomView(R.layout.app_bar);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            // Set the "go" button on the keyboard to load the URL in urlTextBox.
            EditText urlTextBox = (EditText) actionBar.getCustomView().findViewById(R.id.urlTextBox);
            urlTextBox.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // If the event is a key-down event on the "enter" button, load the URL.
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        // Load the URL into the mainWebView and consume the event.
                        try {
                            loadUrlFromTextBox();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        // If the enter key was pressed, consume the event.
                        return true;
                    } else {
                        // If any other key was pressed, do not consume the event.
                        return false;
                    }
                }
            });
        }

        mainWebView.setWebViewClient(new WebViewClient() {
            // shouldOverrideUrlLoading makes this WebView the default handler for URLs inside the app, so that links are not kicked out to other apps.
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mainWebView.loadUrl(url);
                return true;
            }

            /* These errors do not provide any useful information and clutter the screen.
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Toast.makeText(mainWebViewActivity, "Error loading " + request + "   Error: " + error, Toast.LENGTH_SHORT).show();
            }
            */

            // Update the URL in urlTextBox when the page starts to load.
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (actionBar != null) {
                    EditText urlTextBox = (EditText) actionBar.getCustomView().findViewById(R.id.urlTextBox);
                    urlTextBox.setText(url);
                }
            }

            // Update formattedUrlString and urlTextBox.  It is necessary to do this after the page finishes loading because the final URL can change during load.
            @Override
            public void onPageFinished(WebView view, String url) {
                formattedUrlString = url;

                if (actionBar != null) {
                    EditText urlTextBox = (EditText) actionBar.getCustomView().findViewById(R.id.urlTextBox);
                    urlTextBox.setText(formattedUrlString);
                }
            }
        });

        mainWebView.setWebChromeClient(new WebChromeClient() {
            // Update the progress bar when a page is loading.
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Make sure that actionBar is not null.
                if (actionBar != null) {
                    ProgressBar progressBar = (ProgressBar) actionBar.getCustomView().findViewById(R.id.progressBar);
                    progressBar.setProgress(progress);
                    if (progress < 100) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            // Set the favorite icon when it changes.
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                // Save a copy of the favorite icon for use if a shortcut is added to the home screen.
                favoriteIcon = icon;

                // Place the favorite icon in the actionBar if it is not null.
                if (actionBar != null) {
                    ImageView imageViewFavoriteIcon = (ImageView) actionBar.getCustomView().findViewById(R.id.favoriteIcon);
                    imageViewFavoriteIcon.setImageBitmap(Bitmap.createScaledBitmap(icon, 64, 64, true));
                }
            }

            // Enter full screen video
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }

                fullScreenVideoFrameLayout.addView(view);
                fullScreenVideoFrameLayout.setVisibility(View.VISIBLE);

                mainWebView.setVisibility(View.GONE);

                /* SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bars on the bottom or right of the screen.
                ** SYSTEM_UI_FLAG_FULLSCREEN hides the status bar across the top of the screen.
                ** SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the navigation and status bars ghosted overlays and automatically rehides them.
                */

                // Set the one flag supported by API >= 14.
                if (Build.VERSION.SDK_INT >= 14) {
                    view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }

                // Set the two flags that are supported by API >= 16.
                if (Build.VERSION.SDK_INT >= 16) {
                    view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
                }

                // Set all three flags that are supported by API >= 19.
                if (Build.VERSION.SDK_INT >= 19) {
                    view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }

            // Exit full screen video
            public void onHideCustomView() {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }

                mainWebView.setVisibility(View.VISIBLE);

                fullScreenVideoFrameLayout.removeAllViews();
                fullScreenVideoFrameLayout.setVisibility(View.GONE);
            }
        });

        // Allow the downloading of files.
        mainWebView.setDownloadListener(new DownloadListener() {
            // Launch the Android download manager when a link leads to a download.
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request requestUri = new DownloadManager.Request(Uri.parse(url));

                // Add the URL as the description for the download.
                requestUri.setDescription(url);

                // Show the download notification after the download is completed if the API is 11 or greater.
                if (Build.VERSION.SDK_INT >= 11) {
                    requestUri.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }

                downloadManager.enqueue(requestUri);
                Toast.makeText(mainWebViewActivity, "Download started", Toast.LENGTH_SHORT).show();
            }
        });

        // Allow pinch to zoom.
        mainWebView.getSettings().setBuiltInZoomControls(true);

        // Hide zoom controls if the API is 11 or greater.
        if (Build.VERSION.SDK_INT >= 11) {
            mainWebView.getSettings().setDisplayZoomControls(false);
        }

        // Set JavaScript initial status.
        enableJavaScript = true;
        mainWebView.getSettings().setJavaScriptEnabled(enableJavaScript);

        // Set DOM Storage initial status.
        enableDomStorage = true;
        mainWebView.getSettings().setDomStorageEnabled(enableDomStorage);

        /* Save Form Data does nothing until database storage is implemented.
        // Set Save Form Data initial status.
        enableSaveFormData = true;
        mainWebView.getSettings().setSaveFormData(enableSaveFormData);
        */

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
        MenuItem toggleJavaScriptMenuItem = menu.findItem(R.id.toggleJavaScript);
        MenuItem toggleDomStorageMenuItem = menu.findItem(R.id.toggleDomStorage);
        /* toggleSaveFormData does nothing until database storage is implemented.
        MenuItem toggleSaveFormDataMenuItem = menu.findItem(R.id.toggleSaveFormData);
        */

        // Set the initial status of the menu item checkboxes.
        toggleJavaScriptMenuItem.setChecked(enableJavaScript);
        toggleDomStorageMenuItem.setChecked(enableDomStorage);
        /* toggleSaveFormData does nothing until database storage is implemented.
        toggleSaveFormDataMenuItem.setChecked(enableSaveFormData);
        */

        return true;
    }

    @Override
    // @TargetApi(11) turns off the errors regarding copy and paste, which are removed from view in menu_webview.xml for lower version of Android.
    @TargetApi(11)
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.
    @SuppressLint("SetJavaScriptEnabled")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Sets the commands that relate to the menu entries.
        switch (menuItemId) {
            case R.id.toggleJavaScript:
                if (enableJavaScript) {
                    enableJavaScript = false;
                    menuItem.setChecked(false);
                    mainWebView.getSettings().setJavaScriptEnabled(false);
                    mainWebView.reload();
                } else {
                    enableJavaScript = true;
                    menuItem.setChecked(true);
                    mainWebView.getSettings().setJavaScriptEnabled(true);
                    mainWebView.reload();
                }
                return true;

            case R.id.toggleDomStorage:
                if (enableDomStorage) {
                    enableDomStorage = false;
                    menuItem.setChecked(false);
                    mainWebView.getSettings().setDomStorageEnabled(false);
                    mainWebView.reload();
                } else {
                    enableDomStorage = true;
                    menuItem.setChecked(true);
                    mainWebView.getSettings().setDomStorageEnabled(true);
                    mainWebView.reload();
                }
                return true;

            /* toggleSaveFormData does nothing until database storage is implemented.
            case R.id.toggleSaveFormData:
                if (enableSaveFormData) {
                    enableSaveFormData = false;
                    menuItem.setChecked(false);
                    mainWebView.getSettings().setSaveFormData(false);
                    mainWebView.reload();
                } else {
                    enableSaveFormData = true;
                    menuItem.setChecked(true);
                    mainWebView.getSettings().setSaveFormData(true);
                    mainWebView.reload();
                }
                return true;
            */

            case R.id.home:
                mainWebView.loadUrl(homepage);
                return true;

            case R.id.refresh:
                mainWebView.reload();
                return true;

            case R.id.back:
                mainWebView.goBack();
                return true;

            case R.id.forward:
                mainWebView.goForward();
                return true;

            case R.id.copyURL:
                // Make sure that actionBar is not null.
                if (actionBar != null) {
                    EditText urlTextBox = (EditText) actionBar.getCustomView().findViewById(R.id.urlTextBox);
                    clipboard.setPrimaryClip(ClipData.newPlainText("URL", urlTextBox.getText()));
                }
                return true;

            case R.id.pasteURL:
                // Make sure that actionBar is not null.
                if (actionBar != null) {
                    ClipData.Item clipboardData = clipboard.getPrimaryClip().getItemAt(0);
                    EditText urlTextBox = (EditText) actionBar.getCustomView().findViewById(R.id.urlTextBox);
                    urlTextBox.setText(clipboardData.coerceToText(this));
                    try {
                        loadUrlFromTextBox();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return true;

            case R.id.shareURL:
                // Make sure that actionBar is not null.
                if (actionBar != null) {
                    EditText urlTextBox = (EditText) actionBar.getCustomView().findViewById(R.id.urlTextBox);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, urlTextBox.getText().toString());
                    shareIntent.setType("text/plain");
                    startActivity(Intent.createChooser(shareIntent, "Share URL"));
                }
                return true;

            case R.id.addToHomescreen:
                // Show the CreateHomeScreenShortcut AlertDialog and name this instance createShortcut.
                AppCompatDialogFragment shortcutDialog = new CreateHomeScreenShortcut();
                shortcutDialog.show(getSupportFragmentManager(), "createShortcut");

                //Everything else will be handled by CreateHomeScreenShortcut and the associated listeners below.
                return true;

            case R.id.downloads:
                // Launch the system Download Manager.
                Intent downloadManangerIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);

                // Launch as a new task so that Download Manager and Privacy Browser show as separate windows in the recent tasks list.
                downloadManangerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(downloadManangerIntent);
                return true;

            case R.id.about:
                // Show the AboutDialog AlertDialog and name this instance aboutDialog.
                AppCompatDialogFragment aboutDialog = new AboutDialog();
                aboutDialog.show(getSupportFragmentManager(), "aboutDialog");
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onCreateHomeScreenShortcutCancel(DialogFragment dialog) {
        // Do nothing because the user selected "Cancel".
    }

    @Override
    public void onCreateHomeScreenShortcutCreate(DialogFragment dialog) {
        // Get shortcutNameEditText from the alert dialog.
        EditText shortcutNameEditText = (EditText) dialog.getDialog().findViewById(R.id.shortcutNameEditText);

        // Create the bookmark shortcut based on formattedUrlString.
        Intent bookmarkShortcut = new Intent();
        bookmarkShortcut.setAction(Intent.ACTION_VIEW);
        bookmarkShortcut.setData(Uri.parse(formattedUrlString));

        // Place the bookmark shortcut on the home screen.
        Intent placeBookmarkShortcut = new Intent();
        placeBookmarkShortcut.putExtra("android.intent.extra.shortcut.INTENT", bookmarkShortcut);
        placeBookmarkShortcut.putExtra("android.intent.extra.shortcut.NAME", shortcutNameEditText.getText().toString());
        placeBookmarkShortcut.putExtra("android.intent.extra.shortcut.ICON", favoriteIcon);
        placeBookmarkShortcut.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(placeBookmarkShortcut);
    }

    // Override onBackPressed so that if mainWebView can go back it does when the system back button is pressed.
    @Override
    public void onBackPressed() {
        final WebView mainWebView = (WebView) findViewById(R.id.mainWebView);

        if (mainWebView.canGoBack()) {
            mainWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public void loadUrlFromTextBox() throws UnsupportedEncodingException {
        // Make sure that actionBar is not null.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            final WebView mainWebView = (WebView) findViewById(R.id.mainWebView);
            EditText urlTextBox = (EditText) actionBar.getCustomView().findViewById(R.id.urlTextBox);

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
                // Sanitize the search input and convert it to a DuckDuckGo search.
                final String encodedUrlString = URLEncoder.encode(unformattedUrlString, "UTF-8");
                formattedUrlString = "https://duckduckgo.com/?q=" + encodedUrlString;
            }

            mainWebView.loadUrl(formattedUrlString);

            // Hides the keyboard so we can see the webpage.
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);
        }
    }
}