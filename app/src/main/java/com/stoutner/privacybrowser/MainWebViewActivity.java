/**
 * Copyright 2015-2016 Soren Stoutner <soren@stoutner.com>.
 *
 * This file is part of Privacy Browser <https://www.stoutner.com/privacy-browser>.
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
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

// We need to use AppCompatActivity from android.support.v7.app.AppCompatActivity to have access to the SupportActionBar until the minimum API is >= 21.
public class MainWebViewActivity extends AppCompatActivity implements CreateHomeScreenShortcut.CreateHomeScreenSchortcutListener {
    // favoriteIcon is public static so it can be accessed from CreateHomeScreenShortcut.
    public static Bitmap favoriteIcon;
    // mainWebView is public static so it can be accessed from AboutDialog.  It is also used in onCreate(), onOptionsItemSelected(), and loadUrlFromTextBox().
    public static WebView mainWebView;

    // DrawerTottle is use in onCrate() and onPostCreate().
    private ActionBarDrawerToggle drawerToggle;
    // mainMenu is used in onCreateOptionsMenu() and onOptionsItemSelected().
    private Menu mainMenu;
    // formattedUrlString is used in onCreate(), onOptionsItemSelected(), onCreateHomeScreenShortcutCreate(), and loadUrlFromTextBox().
    private String formattedUrlString;
    // homepage is used in onCreate() and onOptionsItemSelected().
    private String homepage;
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
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.  The whole premise of Privacy Browser is built around an understanding of these dangers.
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coordinator_layout);

        // We need to use the SupportActionBar from android.support.v7.app.ActionBar until the minimum API is >= 21.
        Toolbar supportAppBar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(supportAppBar);

        final FrameLayout fullScreenVideoFrameLayout = (FrameLayout) findViewById(R.id.fullScreenVideoFrameLayout);

        // We need to use the SupportActionBar from android.support.v7.app.ActionBar until the minimum API is >= 21.
        final ActionBar appBar = getSupportActionBar();

        // Setup AdView for the free flavor.
        final View adView = findViewById(R.id.adView);

        // Implement swipe to refresh
        final SwipeRefreshLayout swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeToRefresh.setColorSchemeResources(R.color.blue);
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainWebView.reload();
            }
        });

        mainWebView = (WebView) findViewById(R.id.mainWebView);

        if (appBar != null) {
            // Add the custom url_bar layout, which shows the favoriteIcon, urlTextBar, and progressBar.
            appBar.setCustomView(R.layout.url_bar);
            appBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            // Set the "go" button on the keyboard to load the URL in urlTextBox.
            urlTextBox = (EditText) appBar.getCustomView().findViewById(R.id.urlTextBox);
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

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, supportAppBar, R.string.open_navigation, R.string.close_navigation);

        mainWebView.setWebViewClient(new WebViewClient() {
            // shouldOverrideUrlLoading makes this WebView the default handler for URLs inside the app, so that links are not kicked out to other apps.
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

                // Only update urlTextBox if the user is not typing in it.
                if (!urlTextBox.hasFocus()) {
                    urlTextBox.setText(formattedUrlString);
                }
            }
        });

        mainWebView.setWebChromeClient(new WebChromeClient() {
            // Update the progress bar when a page is loading.
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Make sure that appBar is not null.
                if (appBar != null) {
                    ProgressBar progressBar = (ProgressBar) appBar.getCustomView().findViewById(R.id.progressBar);
                    progressBar.setProgress(progress);
                    if (progress < 100) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);

                        //Stop the SwipeToRefresh indicator if it is running
                        swipeToRefresh.setRefreshing(false);
                    }
                }
            }

            // Set the favorite icon when it changes.
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                // Save a copy of the favorite icon for use if a shortcut is added to the home screen.
                favoriteIcon = icon;

                // Place the favorite icon in the appBar if it is not null.
                if (appBar != null) {
                    ImageView imageViewFavoriteIcon = (ImageView) appBar.getCustomView().findViewById(R.id.favoriteIcon);
                    imageViewFavoriteIcon.setImageBitmap(Bitmap.createScaledBitmap(icon, 64, 64, true));
                }
            }

            // Enter full screen video
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (appBar != null) {
                    appBar.hide();
                }

                // Show the fullScreenVideoFrameLayout.
                fullScreenVideoFrameLayout.addView(view);
                fullScreenVideoFrameLayout.setVisibility(View.VISIBLE);

                // Hide the mainWebView.
                mainWebView.setVisibility(View.GONE);

                // Hide the ad if this is the free flavor.
                BannerAd.hideAd(adView);

                /* SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bars on the bottom or right of the screen.
                ** SYSTEM_UI_FLAG_FULLSCREEN hides the status bar across the top of the screen.
                ** SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the navigation and status bars ghosted overlays and automatically rehides them.
                */

                // Set the one flag supported by API >= 14.
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

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
                if (appBar != null) {
                    appBar.show();
                }

                // Show the mainWebView.
                mainWebView.setVisibility(View.VISIBLE);

                // Show the ad if this is the free flavor.
                BannerAd.showAd(adView);

                // Hide the fullScreenVideoFrameLayout.
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

                // Show the download notification after the download is completed.
                requestUri.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                // Initiate the download and display a Snackbar.
                downloadManager.enqueue(requestUri);
                Snackbar.make(findViewById(R.id.mainWebView), R.string.download_started, Snackbar.LENGTH_SHORT).show();
            }
        });

        // Allow pinch to zoom.
        mainWebView.getSettings().setBuiltInZoomControls(true);

        // Hide zoom controls.
        mainWebView.getSettings().setDisplayZoomControls(false);

        // Initialize the default preference values the first time the program is run.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Get the shared preference values.
        SharedPreferences savedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set JavaScript initial status.
        javaScriptEnabled = savedPreferences.getBoolean("javascript_enabled", false);
        mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);

        // Set DOM storage initial status.
        domStorageEnabled = savedPreferences.getBoolean("dom_storage_enabled", false);
        mainWebView.getSettings().setDomStorageEnabled(domStorageEnabled);

        /* Save Form Data does nothing until database storage is implemented.
        // Set Save Form Data initial status.
        saveFormDataEnabled = true;
        mainWebView.getSettings().setSaveFormData(saveFormDataEnabled);
        */

        // Set cookies initial status.
        cookiesEnabled = savedPreferences.getBoolean("cookies_enabled", false);
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(cookiesEnabled);

        // Set homepage initial status.
        homepage = savedPreferences.getString("homepage", "https://www.duckduckgo.com");

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

        // Load the ad if this is the free flavor.
        BannerAd.requestAd(adView);
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

        // Clear the keyboard if displayed and remove the focus on the urlTextBar if it has it.
        mainWebView.requestFocus();
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
            toggleJavaScript.setIcon(R.drawable.javascript_enabled);
        } else {
            if (domStorageEnabled || cookiesEnabled) {
                toggleJavaScript.setIcon(R.drawable.warning);
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
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.
    @SuppressLint("SetJavaScriptEnabled")
    // removeAllCookies is deprecated, but it is required for API < 21.
    @SuppressWarnings("deprecation")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();

        // Some options need to update the drawable for toggleJavaScript.
        MenuItem toggleJavaScript = mainMenu.findItem(R.id.toggleJavaScript);

        // Set the commands that relate to the menu entries.
        switch (menuItemId) {
            case R.id.toggleJavaScript:
                if (javaScriptEnabled) {
                    javaScriptEnabled = false;
                    mainWebView.getSettings().setJavaScriptEnabled(false);
                    mainWebView.reload();

                    // Update the toggleJavaScript icon and display a snackbar.
                    if (domStorageEnabled || cookiesEnabled) {
                        menuItem.setIcon(R.drawable.warning);
                        if (domStorageEnabled && cookiesEnabled) {
                            Snackbar.make(findViewById(R.id.mainWebView), R.string.both_still_enabled, Snackbar.LENGTH_SHORT).show();
                        } else {
                            if (domStorageEnabled) {
                                Snackbar.make(findViewById(R.id.mainWebView), R.string.dom_storage_still_enabled, Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(findViewById(R.id.mainWebView), R.string.cookies_still_enabled, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        menuItem.setIcon(R.drawable.privacy_mode);
                        Snackbar.make(findViewById(R.id.mainWebView), R.string.privacy_mode, Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    javaScriptEnabled = true;
                    menuItem.setIcon(R.drawable.javascript_enabled);
                    mainWebView.getSettings().setJavaScriptEnabled(true);
                    mainWebView.reload();
                    Snackbar.make(findViewById(R.id.mainWebView), R.string.javascript_enabled, Snackbar.LENGTH_SHORT).show();
                }
                return true;

            case R.id.toggleDomStorage:
                if (domStorageEnabled) {
                    domStorageEnabled = false;
                    menuItem.setChecked(false);
                    mainWebView.getSettings().setDomStorageEnabled(false);
                    mainWebView.reload();

                    // Update the toggleJavaScript icon and display a snackbar if appropriate.
                    if (!javaScriptEnabled && !cookiesEnabled) {
                        toggleJavaScript.setIcon(R.drawable.privacy_mode);
                        Snackbar.make(findViewById(R.id.mainWebView), R.string.privacy_mode, Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (cookiesEnabled) {
                            toggleJavaScript.setIcon(R.drawable.warning);
                            Snackbar.make(findViewById(R.id.mainWebView), R.string.cookies_still_enabled, Snackbar.LENGTH_SHORT).show();
                        } // Else Do nothing because JavaScript is enabled.
                    }
                } else {
                    domStorageEnabled = true;
                    menuItem.setChecked(true);
                    mainWebView.getSettings().setDomStorageEnabled(true);
                    mainWebView.reload();

                    // Update the toggleJavaScript icon if appropriate.
                    if (!javaScriptEnabled) {
                        toggleJavaScript.setIcon(R.drawable.warning);
                    } // Else Do nothing because JavaScript is enabled.

                    Snackbar.make(findViewById(R.id.mainWebView), R.string.dom_storage_enabled, Snackbar.LENGTH_SHORT).show();
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

                    // Update the toggleJavaScript icon and display a snackbar if appropriate.
                    if (!javaScriptEnabled && !domStorageEnabled) {
                        toggleJavaScript.setIcon(R.drawable.privacy_mode);
                        Snackbar.make(findViewById(R.id.mainWebView), R.string.privacy_mode, Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (domStorageEnabled) {
                            toggleJavaScript.setIcon(R.drawable.warning);
                            Snackbar.make(findViewById(R.id.mainWebView), R.string.dom_storage_enabled, Snackbar.LENGTH_SHORT).show();
                        } // Else Do nothing because JavaScript is enabled.
                    }
                } else {
                    cookiesEnabled = true;
                    menuItem.setChecked(true);
                    cookieManager.setAcceptCookie(true);
                    mainWebView.reload();

                    // Update the toggleJavaScript icon if appropriate.
                    if (!javaScriptEnabled) {
                        toggleJavaScript.setIcon(R.drawable.warning);
                    } // Else Do nothing because JavaScript is enabled.

                    Snackbar.make(findViewById(R.id.mainWebView), R.string.cookies_enabled, Snackbar.LENGTH_SHORT).show();
                }
                return true;

            case R.id.clearDomStorage:
                WebStorage webStorage = WebStorage.getInstance();
                webStorage.deleteAllData();
                Snackbar.make(findViewById(R.id.mainWebView), R.string.dom_storage_deleted, Snackbar.LENGTH_SHORT).show();
                return true;

            case R.id.clearCookies:
                if (Build.VERSION.SDK_INT < 21) {
                    cookieManager.removeAllCookie();
                } else {
                    cookieManager.removeAllCookies(null);
                }
                Snackbar.make(findViewById(R.id.mainWebView), R.string.cookies_deleted, Snackbar.LENGTH_SHORT).show();
                return true;

            case R.id.addToHomescreen:
                // Show the CreateHomeScreenShortcut AlertDialog and name this instance createShortcut.
                AppCompatDialogFragment shortcutDialog = new CreateHomeScreenShortcut();
                shortcutDialog.show(getSupportFragmentManager(), "createShortcut");

                //Everything else will be handled by CreateHomeScreenShortcut and the associated listeners below.
                return true;

            case R.id.downloads:
                // Launch the system Download Manager.
                Intent downloadManagerIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);

                // Launch as a new task so that Download Manager and Privacy Browser show as separate windows in the recent tasks list.
                downloadManagerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(downloadManagerIntent);
                return true;

            case R.id.home:
                mainWebView.loadUrl(homepage);
                return true;

            case R.id.back:
                mainWebView.goBack();
                return true;

            case R.id.forward:
                mainWebView.goForward();
                return true;

            case R.id.share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, urlTextBox.getText().toString());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share URL"));
                return true;

            case R.id.settings:
                // Launch SettingsActivity.
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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

                // Clear cookies.  The commands changed slightly in API 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    cookieManager.removeAllCookies(null);
                } else {
                    cookieManager.removeAllCookie();
                }

                // Destroy the internal state of the webview.
                mainWebView.destroy();

                // Close Privacy Browser.  finishAndRemoveTask also removes Privacy Browser from the recent app list.
                if (Build.VERSION.SDK_INT >= 21) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the state of the DrawerToggle after onRestoreInstanceState has finished.
        drawerToggle.syncState();
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

    private void loadUrlFromTextBox() throws UnsupportedEncodingException {
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
