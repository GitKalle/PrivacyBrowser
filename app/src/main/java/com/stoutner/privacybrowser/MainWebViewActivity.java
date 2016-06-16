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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
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
public class MainWebViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CreateHomeScreenShortcut.CreateHomeScreenSchortcutListener {
    // favoriteIcon is public static so it can be accessed from CreateHomeScreenShortcut.
    public static Bitmap favoriteIcon;
    // mainWebView is public static so it can be accessed from SettingsFragment.  It is also used in onCreate(), onOptionsItemSelected(), onNavigationItemSelected(), and loadUrlFromTextBox().
    public static WebView mainWebView;

    // mainMenu is public static so it can be accessed from SettingsFragment.  It is also used in onCreateOptionsMenu() and onOptionsItemSelected().
    public static Menu mainMenu;
    // cookieManager is public static so it can be accessed from SettingsFragment.  It is also used in onCreate(), onOptionsItemSelected(), and onNavigationItemSelected().
    public static CookieManager cookieManager;
    // javaScriptEnabled is public static so it can be accessed from SettingsFragment.  It is also used in onCreate(), onCreateOptionsMenu(), onOptionsItemSelected(), and loadUrlFromTextBox().
    public static boolean javaScriptEnabled;
    // firstPartyCookiesEnabled is public static so it can be accessed from SettingsFragment.  It is also used in onCreate(), onCreateOptionsMenu(), onPrepareOptionsMenu(), and onOptionsItemSelected().
    public static boolean firstPartyCookiesEnabled;
    // thirdPartyCookiesEnabled is used in onCreate(), onCreateOptionsMenu(), onPrepareOptionsMenu(), and onOptionsItemSelected().
    public static boolean thirdPartyCookiesEnabled;
    // domStorageEnabled is public static so it can be accessed from SettingsFragment.  It is also used in onCreate(), onCreateOptionsMenu(), and onOptionsItemSelected().
    public static boolean domStorageEnabled;
    // javaScriptDisabledSearchURL is public static so it can be accessed from SettingsFragment.  It is also used in onCreate() and loadURLFromTextBox().
    public static String javaScriptDisabledSearchURL;
    // javaScriptEnabledSearchURL is public static so it can be accessed from SettingsFragment.  It is also used in onCreate() and loadURLFromTextBox().
    public static String javaScriptEnabledSearchURL;
    // homepage is public static so it can be accessed from  SettingsFragment.  It is also used in onCreate() and onOptionsItemSelected().
    public static String homepage;
    // swipeToRefresh is public static so it can be accessed from SettingsFragment.  It is also used in onCreate().
    public static SwipeRefreshLayout swipeToRefresh;
    // swipeToRefreshEnabled is public static so it can be accessed from SettingsFragment.  It is also used in onCreate().
    public static boolean swipeToRefreshEnabled;

    // drawerToggle is used in onCreate(), onPostCreate(), onConfigurationChanged(), onNewIntent(), and onNavigationItemSelected().
    private ActionBarDrawerToggle drawerToggle;
    // drawerLayout is used in onCreate(), onNewIntent(), and onBackPressed().
    private DrawerLayout drawerLayout;
    // formattedUrlString is used in onCreate(), onOptionsItemSelected(), onCreateHomeScreenShortcutCreate(), and loadUrlFromTextBox().
    private String formattedUrlString;
    // privacyIcon is used in onCreateOptionsMenu() and updatePrivacyIcon().
    private MenuItem privacyIcon;
    // urlTextBox is used in onCreate(), onOptionsItemSelected(), and loadUrlFromTextBox().
    private EditText urlTextBox;
    // adView is used in onCreate() and onConfigurationChanged().
    private View adView;

    @Override
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.  The whole premise of Privacy Browser is built around an understanding of these dangers.
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_coordinatorlayout);

        // We need to use the SupportActionBar from android.support.v7.app.ActionBar until the minimum API is >= 21.
        Toolbar supportAppBar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(supportAppBar);
        final ActionBar appBar = getSupportActionBar();

        // This is needed to get rid of the Android Studio warning that appBar might be null.
        assert appBar != null;

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

        final FrameLayout fullScreenVideoFrameLayout = (FrameLayout) findViewById(R.id.fullScreenVideoFrameLayout);

        // Implement swipe to refresh
        swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        assert swipeToRefresh != null; //This assert removes the incorrect warning on the following line that swipeToRefresh might be null.
        swipeToRefresh.setColorSchemeResources(R.color.blue);
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainWebView.reload();
            }
        });

        mainWebView = (WebView) findViewById(R.id.mainWebView);

        // Create the navigation drawer.
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        // The DrawerTitle identifies the drawer in accessibility mode.
        drawerLayout.setDrawerTitle(GravityCompat.START, getString(R.string.navigation_drawer));

        // Listen for touches on the navigation menu.
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        assert navigationView != null; // This assert removes the incorrect warning on the following line that navigationView might be null.
        navigationView.setNavigationItemSelectedListener(this);

        // drawerToggle creates the hamburger icon at the start of the AppBar.
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

            // Set the favorite icon when it changes.
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                // Save a copy of the favorite icon for use if a shortcut is added to the home screen.
                favoriteIcon = icon;

                // Place the favorite icon in the appBar.
                ImageView imageViewFavoriteIcon = (ImageView) appBar.getCustomView().findViewById(R.id.favoriteIcon);
                imageViewFavoriteIcon.setImageBitmap(Bitmap.createScaledBitmap(icon, 64, 64, true));
            }

            // Enter full screen video
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                appBar.hide();

                // Show the fullScreenVideoFrameLayout.
                assert fullScreenVideoFrameLayout != null; //This assert removes the incorrect warning on the following line that fullScreenVideoFrameLayout might be null.
                fullScreenVideoFrameLayout.addView(view);
                fullScreenVideoFrameLayout.setVisibility(View.VISIBLE);

                // Hide the mainWebView.
                mainWebView.setVisibility(View.GONE);

                // Hide the ad if this is the free flavor.
                BannerAd.hideAd(adView);

                /* SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bars on the bottom or right of the screen.
                 * SYSTEM_UI_FLAG_FULLSCREEN hides the status bar across the top of the screen.
                 * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the navigation and status bars ghosted overlays and automatically rehides them.
                 */
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            // Exit full screen video
            public void onHideCustomView() {
                appBar.show();

                // Show the mainWebView.
                mainWebView.setVisibility(View.VISIBLE);

                // Show the ad if this is the free flavor.
                BannerAd.showAd(adView);

                // Hide the fullScreenVideoFrameLayout.
                assert fullScreenVideoFrameLayout != null; //This assert removes the incorrect warning on the following line that fullScreenVideoFrameLayout might be null.
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

        // Set JavaScript initial status.  The default value is false.
        javaScriptEnabled = savedPreferences.getBoolean("javascript_enabled", false);
        mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);

        // Initialize cookieManager.
        cookieManager = CookieManager.getInstance();

        // Set cookies initial status.  The default value is false.
        firstPartyCookiesEnabled = savedPreferences.getBoolean("first_party_cookies_enabled", false);
        cookieManager.setAcceptCookie(firstPartyCookiesEnabled);

        // Set third-party cookies initial status if API >= 21.  The default value is false.
        if (Build.VERSION.SDK_INT >= 21) {
            thirdPartyCookiesEnabled = savedPreferences.getBoolean("third_party_cookies_enabled", false);
            cookieManager.setAcceptThirdPartyCookies(mainWebView, thirdPartyCookiesEnabled);
        }

        // Set DOM storage initial status.  The default value is false.
        domStorageEnabled = savedPreferences.getBoolean("dom_storage_enabled", false);
        mainWebView.getSettings().setDomStorageEnabled(domStorageEnabled);

        // Set the user agent initial status.
        String userAgentString = savedPreferences.getString("user_agent", "Default user agent");
        switch (userAgentString) {
            case "Default user agent":
                // Do nothing.
                break;

            case "Custom user agent":
                // Set the custom user agent on mainWebView,  The default is "PrivacyBrowser/1.0".
                mainWebView.getSettings().setUserAgentString(savedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0"));
                break;

            default:
                // Set the selected user agent on mainWebView.  The default is "PrivacyBrowser/1.0".
                mainWebView.getSettings().setUserAgentString(savedPreferences.getString("user_agent", "PrivacyBrowser/1.0"));
                break;
        }

        // Set the initial string for JavaScript disabled search.
        if (savedPreferences.getString("javascript_disabled_search", "https://duckduckgo.com/html/?q=").equals("Custom URL")) {
            // Get the custom URL string.  The default is "".
            javaScriptDisabledSearchURL = savedPreferences.getString("javascript_disabled_search_custom_url", "");
        } else {
            // Use the string from javascript_disabled_search.
            javaScriptDisabledSearchURL = savedPreferences.getString("javascript_disabled_search", "https://duckduckgo.com/html/?q=");
        }

        // Set the initial string for JavaScript enabled search.
        if (savedPreferences.getString("javascript_enabled_search", "https://duckduckgo.com/?q=").equals("Custom URL")) {
            // Get the custom URL string.  The default is "".
            javaScriptEnabledSearchURL = savedPreferences.getString("javascript_enabled_search_custom_url", "");
        } else {
            // Use the string from javascript_enabled_search.
            javaScriptEnabledSearchURL = savedPreferences.getString("javascript_enabled_search", "https://duckduckgo.com/?q=");
        }


        // Set homepage initial status.  The default value is "https://www.duckduckgo.com".
        homepage = savedPreferences.getString("homepage", "https://www.duckduckgo.com");

        // Set swipe to refresh initial status.  The default is true.
        swipeToRefreshEnabled = savedPreferences.getBoolean("swipe_to_refresh_enabled", true);
        swipeToRefresh.setEnabled(swipeToRefreshEnabled);


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

        // Initialize AdView for the free flavor and request an ad.  If this is not the free flavor BannerAd.requestAd() does nothing.
        adView = findViewById(R.id.adView);
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

        // Close the navigation drawer if it is open.
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        // Load the website.
        mainWebView.loadUrl(formattedUrlString);

        // Clear the keyboard if displayed and remove the focus on the urlTextBar if it has it.
        mainWebView.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);

        // Set mainMenu so it can be used by onOptionsItemSelected.
        mainMenu = menu;

        // Initialize privacyIcon
        privacyIcon = menu.findItem(R.id.toggleJavaScript);

        // Get MenuItems for checkable menu items.
        MenuItem toggleFirstPartyCookies = menu.findItem(R.id.toggleFirstPartyCookies);
        MenuItem toggleThirdPartyCookies = menu.findItem(R.id.toggleThirdPartyCookies);
        MenuItem toggleDomStorage = menu.findItem(R.id.toggleDomStorage);

        // Set the initial status of the privacy icon.
        updatePrivacyIcon();

        // Set the initial status of the menu item checkboxes.
        toggleFirstPartyCookies.setChecked(firstPartyCookiesEnabled);
        toggleThirdPartyCookies.setChecked(thirdPartyCookiesEnabled);
        toggleDomStorage.setChecked(domStorageEnabled);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Only enable Third-Party Cookies if SDK >= 21 and First-Party Cookies are enabled.
        MenuItem toggleThirdPartyCookies = menu.findItem(R.id.toggleThirdPartyCookies);
        if ((Build.VERSION.SDK_INT >= 21) && firstPartyCookiesEnabled) {
            toggleThirdPartyCookies.setEnabled(true);
        } else {
            toggleThirdPartyCookies.setEnabled(false);
        }

        // Enable Clear Cookies if there are any.
        MenuItem clearCookies = menu.findItem(R.id.clearCookies);
        clearCookies.setEnabled(cookieManager.hasCookies());

        // Enable DOM Storage if JavaScript is enabled.
        MenuItem toggleDomStorage = menu.findItem(R.id.toggleDomStorage);
        toggleDomStorage.setEnabled(javaScriptEnabled);

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

        // Set the commands that relate to the menu entries.
        switch (menuItemId) {
            case R.id.toggleJavaScript:
                // Switch the status of javaScriptEnabled.
                javaScriptEnabled = !javaScriptEnabled;

                // Apply the new JavaScript status.
                mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);

                // Update the privacy icon.
                updatePrivacyIcon();

                // Display a Snackbar.
                if (javaScriptEnabled) {
                    Snackbar.make(findViewById(R.id.mainWebView), R.string.javascript_enabled, Snackbar.LENGTH_SHORT).show();
                } else {
                    if (firstPartyCookiesEnabled) {
                        Snackbar.make(findViewById(R.id.mainWebView), R.string.javascript_disabled, Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(R.id.mainWebView), R.string.privacy_mode, Snackbar.LENGTH_SHORT).show();
                    }
                }

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.toggleFirstPartyCookies:
                // Switch the status of firstPartyCookiesEnabled.
                firstPartyCookiesEnabled = !firstPartyCookiesEnabled;

                // Update the menu checkbox.
                menuItem.setChecked(firstPartyCookiesEnabled);

                // Apply the new cookie status.
                cookieManager.setAcceptCookie(firstPartyCookiesEnabled);

                // Update the privacy icon.
                updatePrivacyIcon();

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.toggleThirdPartyCookies:
                if (Build.VERSION.SDK_INT >= 21) {
                    // Switch the status of thirdPartyCookiesEnabled.
                    thirdPartyCookiesEnabled = !thirdPartyCookiesEnabled;

                    // Update the menu checkbox.
                    menuItem.setChecked(thirdPartyCookiesEnabled);

                    // Apply the new cookie status.
                    cookieManager.setAcceptThirdPartyCookies(mainWebView, thirdPartyCookiesEnabled);

                    // Reload the WebView.
                    mainWebView.reload();
                } // Else do nothing because SDK < 21.
                return true;

            case R.id.toggleDomStorage:
                // Switch the status of domStorageEnabled.
                domStorageEnabled = !domStorageEnabled;

                // Update the menu checkbox.
                menuItem.setChecked(domStorageEnabled);

                // Apply the new DOM Storage status.
                mainWebView.getSettings().setDomStorageEnabled(domStorageEnabled);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.clearCookies:
                if (Build.VERSION.SDK_INT < 21) {
                    cookieManager.removeAllCookie();
                } else {
                    cookieManager.removeAllCookies(null);
                }
                Snackbar.make(findViewById(R.id.mainWebView), R.string.cookies_deleted, Snackbar.LENGTH_SHORT).show();
                return true;

            case R.id.clearDomStorage:
                WebStorage webStorage = WebStorage.getInstance();
                webStorage.deleteAllData();
                Snackbar.make(findViewById(R.id.mainWebView), R.string.dom_storage_deleted, Snackbar.LENGTH_SHORT).show();
                return true;

            case R.id.share:
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

            case R.id.refresh:
                mainWebView.reload();
                return true;

            default:
                // Don't consume the event.
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    // removeAllCookies is deprecated, but it is required for API < 21.
    @SuppressWarnings("deprecation")
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case R.id.home:
                mainWebView.loadUrl(homepage);
                break;

            case R.id.back:
                if (mainWebView.canGoBack()) {
                    mainWebView.goBack();
                }
                break;

            case R.id.forward:
                if (mainWebView.canGoForward()) {
                    mainWebView.goForward();
                }
                break;

            case R.id.downloads:
                // Launch the system Download Manager.
                Intent downloadManagerIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);

                // Launch as a new task so that Download Manager and Privacy Browser show as separate windows in the recent tasks list.
                downloadManagerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(downloadManagerIntent);
                break;

            case R.id.guide:
                // Launch GuideActivity.
                Intent guideIntent = new Intent(this, GuideActivity.class);
                startActivity(guideIntent);
                break;

            case R.id.settings:
                // Launch SettingsActivity.
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.about:
                // Launch AboutActivity.
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;

            case R.id.clearAndExit:
                // Clear cookies.  The commands changed slightly in API 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    cookieManager.removeAllCookies(null);
                } else {
                    cookieManager.removeAllCookie();
                }

                // Clear DOM storage.
                WebStorage domStorage = WebStorage.getInstance();
                domStorage.deleteAllData();

                // Clear cache.  The argument of "true" includes disk files.
                mainWebView.clearCache(true);

                // Clear the back/forward history.
                mainWebView.clearHistory();

                // Destroy the internal state of the webview.
                mainWebView.destroy();

                // Close Privacy Browser.  finishAndRemoveTask also removes Privacy Browser from the recent app list.
                if (Build.VERSION.SDK_INT >= 21) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
                break;

            default:
                break;
        }

        // Close the navigation drawer.
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the state of the DrawerToggle after onRestoreInstanceState has finished.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Reload the ad if this is the free flavor.
        BannerAd.reloadAfterRotate(adView, getApplicationContext(), getString(R.string.ad_id));

        // Reinitialize the adView variable, as the View will have been removed and re-added in the free flavor by BannerAd.reloadAfterRotate().
        adView = findViewById(R.id.adView);
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

    // Override onBackPressed to handle the navigation drawer and mainWebView.
    @Override
    public void onBackPressed() {
        final WebView mainWebView = (WebView) findViewById(R.id.mainWebView);

        // Close the navigation drawer if it is available.  GravityCompat.START is the drawer on the left on Left-to-Right layout text.
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Load the previous URL if available.
            assert mainWebView != null; //This assert removes the incorrect warning in Android Studio on the following line that mainWebView might be null.
            if (mainWebView.canGoBack()) {
                mainWebView.goBack();
            } else {
                // Pass onBackPressed to the system.
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onPause() {
        // We need to pause the adView or it will continue to consume resources in the background on the free flavor.
        BannerAd.pauseAd(adView);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // We need to resume the adView for the free flavor.
        BannerAd.resumeAd(adView);
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
                formattedUrlString = javaScriptEnabledSearchURL + encodedUrlString;
            } else { // JavaScript is disabled.
                formattedUrlString = javaScriptDisabledSearchURL + encodedUrlString;
            }
        }

        mainWebView.loadUrl(formattedUrlString);

        // Hides the keyboard so we can see the webpage.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);
    }

    private void updatePrivacyIcon() {
        if (javaScriptEnabled) {
            privacyIcon.setIcon(R.drawable.javascript_enabled);
        } else {
            if (firstPartyCookiesEnabled) {
                privacyIcon.setIcon(R.drawable.warning);
            } else {
                privacyIcon.setIcon(R.drawable.privacy_mode);
            }
        }
    }
}
