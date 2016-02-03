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
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
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

public class MainWebView extends AppCompatActivity implements CreateHomeScreenShortcut.CreateHomeScreenSchortcutListener {
    // favoriteIcon is public static so it can be accessed from CreateHomeScreenShortcut.
    public static Bitmap favoriteIcon;
    // mainWebView is public static so it can be accessed from AboutDialog.  It is also used in onCreate(), onOptionsItemSelected(), and loadUrlFromTextBox().
    public static WebView mainWebView;

    // mainMenu is used in onCreateOptionsMenu() and onOptionsItemSelected().
    private Menu mainMenu;
    // formattedUrlString is used in onCreate(), onOptionsItemSelected(), onCreateHomeScreenShortcutCreate(), and loadUrlFromTextBox().
    private String formattedUrlString;
    // homepage is used in onCreate() and onOptionsItemSelected().
    private String homepage = "https://www.duckduckgo.com/";
    // javaScriptEnabled is used in onCreate(), onCreateOptionsMenu(), onOptionsItemSelected(), and loadUrlFromTextBox().
    private boolean javaScriptEnabled;
    // domStorageEnabled is used in onCreate(), onCreateOptionsMenu(), and onOptionsItemSelected().
    private boolean domStorageEnabled;

    /* saveFormDataEnabled does nothing until database storage is implemented.
    // saveFormDataEnabled is used in onCreate(), onCreateOptionsMenu(), and onOptionsItemSelected().
    private boolean saveFormDataEnabled;
    */

    // cookieManager is used in onCreate() and onOptionsItemSelected().
    private CookieManager cookieManager;
    // cookiesEnabled is used in onCreate(), onCreateOptionsMenu(), and onOptionsItemSelected().
    private boolean cookiesEnabled;

    // urlTextBox is used in onCreate(), onOptionsItemSelected(), and loadUrlFromTextBox().
    private EditText urlTextBox;

    @Override
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        final FrameLayout fullScreenVideoFrameLayout = (FrameLayout) findViewById(R.id.fullScreenVideoFrameLayout);
        final Activity mainWebViewActivity = this;
        final ActionBar actionBar = getSupportActionBar();

        mainWebView = (WebView) findViewById(R.id.mainWebView);

        if (actionBar != null) {
            // Remove the title from the action bar.
            actionBar.setDisplayShowTitleEnabled(false);

            // Add the custom app_bar layout, which shows the favoriteIcon, urlTextBar, and progressBar.
            actionBar.setCustomView(R.layout.app_bar);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            // Set the "go" button on the keyboard to load the URL in urlTextBox.
            urlTextBox = (EditText) actionBar.getCustomView().findViewById(R.id.urlTextBox);
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
                if (actionBar != null) {
                    actionBar.hide();
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
                if (actionBar != null) {
                    actionBar.show();
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
        javaScriptEnabled = false;
        mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);

        // Set DOM Storage initial status.
        domStorageEnabled = false;
        mainWebView.getSettings().setDomStorageEnabled(domStorageEnabled);

        /* Save Form Data does nothing until database storage is implemented.
        // Set Save Form Data initial status.
        saveFormDataEnabled = true;
        mainWebView.getSettings().setSaveFormData(saveFormDataEnabled);
        */

        // Set Cookies initial status.
        cookiesEnabled = false;
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(cookiesEnabled);

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
    protected void onNewIntent(Intent intent) {
        // Sets the new intent as the activity intent, so that any future getIntent()s pick up this one instead of creating a new activity.
        setIntent(intent);

        if (intent.getData() != null) {
            // Get the intent data and convert it to a string.
            final Uri intentUriData = intent.getData();
            formattedUrlString = intentUriData.toString();
        }

        // Load the website.
        mainWebView.loadUrl(formattedUrlString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_webview, menu);

        // Set mainMenu so it can be used by onOptionsItemSelected.
        mainMenu = menu;

        // Get MenuItems for checkable menu items.
        MenuItem toggleJavaScript = menu.findItem(R.id.toggleJavaScript);
        MenuItem toggleDomStorage = menu.findItem(R.id.toggleDomStorage);
        /* toggleSaveFormData does nothing until database storage is implemented.
        MenuItem toggleSaveFormData = menu.findItem(R.id.toggleSaveFormData);
        */
        MenuItem toggleCookies = menu.findItem(R.id.toggleCookies);

        // Set the initial icon for toggleJavaScript
        if (javaScriptEnabled) {
            toggleJavaScript.setIcon(R.drawable.javascript_on);
        } else {
            if (domStorageEnabled || cookiesEnabled) {
                toggleJavaScript.setIcon(R.drawable.warning_on);
            } else {
                toggleJavaScript.setIcon(R.drawable.privacy_mode);
            }
        }

        // Set the initial status of the menu item checkboxes.
        toggleDomStorage.setChecked(domStorageEnabled);
        /* toggleSaveFormData does nothing until database storage is implemented.
        toggleSaveFormData.setChecked(saveFormDataEnabled);
        */
        toggleCookies.setChecked(cookiesEnabled);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Enable Clear Cookies if there are any.
        MenuItem clearCookies = menu.findItem(R.id.clearCookies);
        clearCookies.setEnabled(cookieManager.hasCookies());

        // Enable Back if canGoBack().
        MenuItem back = menu.findItem(R.id.back);
        back.setEnabled(mainWebView.canGoBack());

        // Enable forward if canGoForward().
        MenuItem forward = menu.findItem(R.id.forward);
        forward.setEnabled(mainWebView.canGoForward());

        // Run all the other default commands.
        super.onPrepareOptionsMenu(menu);

        // return true displays the menu.
        return true;
    }

    @Override
    // @TargetApi(11) turns off the errors regarding copy and paste, which are removed from view in menu_webview.xml for lower version of Android.
    @TargetApi(11)
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.
    @SuppressLint("SetJavaScriptEnabled")
    // removeAllCookies is deprecated, but it is required for API < 21.
    @SuppressWarnings("deprecation")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();

        // Some options need to access the clipboard.
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Some options need to update the drawable for toggleJavaScript.
        MenuItem toggleJavaScript = mainMenu.findItem(R.id.toggleJavaScript);

        // Sets the commands that relate to the menu entries.
        switch (menuItemId) {
            case R.id.toggleJavaScript:
                if (javaScriptEnabled) {
                    javaScriptEnabled = false;
                    mainWebView.getSettings().setJavaScriptEnabled(false);
                    mainWebView.reload();

                    // Update the toggleJavaScript icon and display a toast message.
                    if (domStorageEnabled || cookiesEnabled) {
                        menuItem.setIcon(R.drawable.warning_on);
                        if (domStorageEnabled && cookiesEnabled) {
                            Toast.makeText(getApplicationContext(), "JavaScript disabled, DOM Storage and Cookies still enabled", Toast.LENGTH_SHORT).show();
                        } else {
                            if (domStorageEnabled) {
                                Toast.makeText(getApplicationContext(), "JavaScript disabled, DOM Storage still enabled", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "JavaScript disabled, Cookies still enabled", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        menuItem.setIcon(R.drawable.privacy_mode);
                        Toast.makeText(getApplicationContext(), "Privacy Mode", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    javaScriptEnabled = true;
                    menuItem.setIcon(R.drawable.javascript_on);
                    mainWebView.getSettings().setJavaScriptEnabled(true);
                    mainWebView.reload();
                    Toast.makeText(getApplicationContext(), "JavaScript enabled", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.toggleDomStorage:
                if (domStorageEnabled) {
                    domStorageEnabled = false;
                    menuItem.setChecked(false);
                    mainWebView.getSettings().setDomStorageEnabled(false);
                    mainWebView.reload();

                    // Update the toggleJavaScript icon and display a toast message if appropriate.
                    if (!javaScriptEnabled && !cookiesEnabled) {
                        toggleJavaScript.setIcon(R.drawable.privacy_mode);
                        Toast.makeText(getApplicationContext(), "Privacy Mode", Toast.LENGTH_SHORT).show();
                    } else {
                        if (cookiesEnabled) {
                            toggleJavaScript.setIcon(R.drawable.warning_on);
                            Toast.makeText(getApplicationContext(), "Cookies still enabled", Toast.LENGTH_SHORT).show();
                        } // Else Do nothing because JavaScript is enabled.
                    }
                } else {
                    domStorageEnabled = true;
                    menuItem.setChecked(true);
                    mainWebView.getSettings().setDomStorageEnabled(true);
                    mainWebView.reload();

                    // Update the toggleJavaScript icon if appropriate.
                    if (!javaScriptEnabled) {
                        toggleJavaScript.setIcon(R.drawable.warning_on);
                    } // Else Do nothing because JavaScript is enabled.

                    Toast.makeText(getApplicationContext(), "DOM Storage enabled", Toast.LENGTH_SHORT).show();
                }
                return true;

            /* toggleSaveFormData does nothing until database storage is implemented.
            case R.id.toggleSaveFormData:
                if (saveFormDataEnabled) {
                    saveFormDataEnabled = false;
                    menuItem.setChecked(false);
                    mainWebView.getSettings().setSaveFormData(false);
                    mainWebView.reload();
                } else {
                    saveFormDataEnabled = true;
                    menuItem.setChecked(true);
                    mainWebView.getSettings().setSaveFormData(true);
                    mainWebView.reload();
                }
                return true;
            */

            case R.id.toggleCookies:
                if (cookiesEnabled) {
                    cookiesEnabled = false;
                    menuItem.setChecked(false);
                    cookieManager.setAcceptCookie(false);
                    mainWebView.reload();

                    // Update the toggleJavaScript icon and display a toast message if appropriate.
                    if (!javaScriptEnabled && !domStorageEnabled) {
                        toggleJavaScript.setIcon(R.drawable.privacy_mode);
                        Toast.makeText(getApplicationContext(), "Privacy Mode", Toast.LENGTH_SHORT).show();
                    } else {
                        if (domStorageEnabled) {
                            toggleJavaScript.setIcon(R.drawable.warning_on);
                            Toast.makeText(getApplicationContext(), "DOM Storage still enabled", Toast.LENGTH_SHORT).show();
                        } // Else Do nothing because JavaScript is enabled.
                    }
                } else {
                    cookiesEnabled = true;
                    menuItem.setChecked(true);
                    cookieManager.setAcceptCookie(true);
                    mainWebView.reload();

                    // Update the toggleJavaScript icon if appropriate.
                    if (!javaScriptEnabled) {
                        toggleJavaScript.setIcon(R.drawable.warning_on);
                    } // Else Do nothing because JavaScript is enabled.

                    Toast.makeText(getApplicationContext(), "Cookies enabled", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.clearDomStorage:
                WebStorage webStorage = WebStorage.getInstance();
                webStorage.deleteAllData();
                Toast.makeText(getApplicationContext(), "DOM storage deleted", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.clearCookies:
                if (Build.VERSION.SDK_INT < 21) {
                    cookieManager.removeAllCookie();
                } else {
                    cookieManager.removeAllCookies(null);
                }
                Toast.makeText(getApplicationContext(), "Cookies deleted", Toast.LENGTH_SHORT).show();
                return true;

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
                clipboard.setPrimaryClip(ClipData.newPlainText("URL", urlTextBox.getText()));
                return true;

            case R.id.pasteURL:
                ClipData.Item clipboardData = clipboard.getPrimaryClip().getItemAt(0);
                urlTextBox.setText(clipboardData.coerceToText(this));
                    try {
                        loadUrlFromTextBox();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                return true;

            case R.id.shareURL:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, urlTextBox.getText().toString());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share URL"));
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

            case R.id.clearAndExit:
                // Clear DOM storage.
                WebStorage domStorage = WebStorage.getInstance();
                domStorage.deleteAllData();

                // Clear cookies.
                if (Build.VERSION.SDK_INT < 21) {
                    cookieManager.removeAllCookie();
                } else {
                    cookieManager.removeAllCookies(null);
                }

                // Destroy the internal state of the webview.
                mainWebView.destroy();

                // Close Privacy Browser.
                finish();
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
        // Get the text from urlTextBox and convert it to a string.
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

            // Use the correct search URL based on javaScriptEnabled.
            if (javaScriptEnabled) {
                formattedUrlString = "https://duckduckgo.com/?q=" + encodedUrlString;
            } else {
                formattedUrlString = "https://duckduckgo.com/html/?q=" + encodedUrlString;
            }
        }

        mainWebView.loadUrl(formattedUrlString);

        // Hides the keyboard so we can see the webpage.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);
    }
}
