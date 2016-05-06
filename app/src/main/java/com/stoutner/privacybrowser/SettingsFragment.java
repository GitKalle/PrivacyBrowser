/**
 * Copyright 2016 Soren Stoutner <soren@stoutner.com>.
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
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

public class SettingsFragment extends PreferenceFragment {
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener;
    private SharedPreferences savedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Initialize savedPreferences.
        savedPreferences = getPreferenceScreen().getSharedPreferences();


        // Set the JavaScript-disabled search URL as the summary text for the JavaScript-disabled search preference when the preference screen is loaded.
        // The default is "https://duckduckgo.com/html/?q=".
        final Preference javaScriptDisabledSearchPreference = findPreference("javascript_disabled_search");
        javaScriptDisabledSearchPreference.setSummary(savedPreferences.getString("javascript_disabled_search", "https://duckduckgo.com/html/?q="));

        // Set the summary text for "javascript_disabled_search_custom_url" (the default is "") and enable it if "javascript_disabled_search" is set to "Custom URL".
        final Preference javaScriptDisabledSearchCustomURLPreference = findPreference("javascript_disabled_search_custom_url");
        javaScriptDisabledSearchCustomURLPreference.setSummary(savedPreferences.getString("javascript_disabled_search_custom_url", ""));
        javaScriptDisabledSearchCustomURLPreference.setEnabled(javaScriptDisabledSearchPreference.getSummary().equals("Custom URL"));

        // Set the JavaScript-enabed searchURL as the summary text for the JavaScript-enabled search preference when the preference screen is loaded.
        // The default is "https://duckduckgo.com/?q=".
        final Preference javaScriptEnabledSearchPreference = findPreference("javascript_enabled_search");
        javaScriptEnabledSearchPreference.setSummary(savedPreferences.getString("javascript_enabled_search", "https://duckduckgo.com/?q="));

        // Set the summary text for "javascript_enabled_search_custom_url" (the default is "") and enable it if "javascript_enabled_search" is set to "Custom URL".
        final Preference javaScriptEnabledSearchCustomURLPreference = findPreference("javascript_enabled_search_custom_url");
        javaScriptEnabledSearchCustomURLPreference.setSummary(savedPreferences.getString("javascript_enabled_search_custom_url", ""));
        javaScriptEnabledSearchCustomURLPreference.setEnabled(javaScriptEnabledSearchPreference.getSummary().equals("Custom URL"));

        // Set the homepage URL as the summary text for the Homepage preference when the preference screen is loaded.  The default is "https://www.duckduckgo.com".
        final Preference homepagePreference = findPreference("homepage");
        homepagePreference.setSummary(savedPreferences.getString("homepage", "https://www.duckduckgo.com"));


        // Listen for preference changes.
        preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.
            @SuppressLint("SetJavaScriptEnabled")
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                // Several keys need to update the toggleJavaScript icon.
                MenuItem toggleJavaScript = MainWebViewActivity.mainMenu.findItem(R.id.toggleJavaScript);

                switch (key) {
                    case "javascript_enabled":
                        // Set javaScriptEnabled to the new state.  The default is false.
                        MainWebViewActivity.javaScriptEnabled = sharedPreferences.getBoolean("javascript_enabled", false);

                        // Update mainWebView and reload the website.
                        MainWebViewActivity.mainWebView.getSettings().setJavaScriptEnabled(MainWebViewActivity.javaScriptEnabled);
                        MainWebViewActivity.mainWebView.reload();

                        // Update the toggleJavaScript icon.
                        if (MainWebViewActivity.javaScriptEnabled) {
                            toggleJavaScript.setIcon(R.drawable.javascript_enabled);
                        } else {
                            if (MainWebViewActivity.firstPartyCookiesEnabled || MainWebViewActivity.domStorageEnabled) {
                                toggleJavaScript.setIcon(R.drawable.warning);
                            } else {
                                toggleJavaScript.setIcon(R.drawable.privacy_mode);
                            }
                        }
                        return;

                    case "first_party_cookies_enabled":
                        // Set firstPartyCookiesEnabled to the new state.  The default is false.
                        MainWebViewActivity.firstPartyCookiesEnabled = sharedPreferences.getBoolean("first_party_cookies_enabled", false);

                        // Update the checkbox in the options menu.
                        MenuItem firstPartyCookiesMenuItem = MainWebViewActivity.mainMenu.findItem(R.id.toggleFirstPartyCookies);
                        firstPartyCookiesMenuItem.setChecked(MainWebViewActivity.firstPartyCookiesEnabled);

                        // Update mainWebView and reload the website.
                        MainWebViewActivity.cookieManager.setAcceptCookie(MainWebViewActivity.firstPartyCookiesEnabled);
                        MainWebViewActivity.mainWebView.reload();

                        // Update the toggleJavaScript icon.
                        if (MainWebViewActivity.javaScriptEnabled) {
                            toggleJavaScript.setIcon(R.drawable.javascript_enabled);
                        } else {
                            if (MainWebViewActivity.firstPartyCookiesEnabled || MainWebViewActivity.domStorageEnabled) {
                                toggleJavaScript.setIcon(R.drawable.warning);
                            } else {
                                toggleJavaScript.setIcon(R.drawable.privacy_mode);
                            }
                        }
                        return;

                    case "third_party_cookies_enabled":
                        // Set thirdPartyCookiesEnabled to the new state.  The default is false.
                        MainWebViewActivity.thirdPartyCookiesEnabled = sharedPreferences.getBoolean("third_party_cookies_enabled", false);

                        // Update the checkbox in the options menu.
                        MenuItem thirdPartyCookiesMenuItem = MainWebViewActivity.mainMenu.findItem(R.id.toggleThirdPartyCookies);
                        thirdPartyCookiesMenuItem.setChecked(MainWebViewActivity.thirdPartyCookiesEnabled);

                        // Update mainWebView and reload the website if API >= 21.
                        if (Build.VERSION.SDK_INT >= 21) {
                            MainWebViewActivity.cookieManager.setAcceptThirdPartyCookies(MainWebViewActivity.mainWebView, MainWebViewActivity.thirdPartyCookiesEnabled);
                            MainWebViewActivity.mainWebView.reload();
                        }
                        return;

                    case "dom_storage_enabled":
                        // Set domStorageEnabled to the new state.  The default is false.
                        MainWebViewActivity.domStorageEnabled = sharedPreferences.getBoolean("dom_storage_enabled", false);

                        // Update the checkbox in the options menu.
                        MenuItem domStorageMenuItem = MainWebViewActivity.mainMenu.findItem(R.id.toggleDomStorage);
                        domStorageMenuItem.setChecked(MainWebViewActivity.domStorageEnabled);

                        // Update mainWebView and reload the website.
                        MainWebViewActivity.mainWebView.getSettings().setDomStorageEnabled(MainWebViewActivity.domStorageEnabled);
                        MainWebViewActivity.mainWebView.reload();

                        // Update the toggleJavaScript icon.
                        if (MainWebViewActivity.javaScriptEnabled) {
                            toggleJavaScript.setIcon(R.drawable.javascript_enabled);
                        } else {
                            if (MainWebViewActivity.firstPartyCookiesEnabled || MainWebViewActivity.domStorageEnabled) {
                                toggleJavaScript.setIcon(R.drawable.warning);
                            } else {
                                toggleJavaScript.setIcon(R.drawable.privacy_mode);
                            }
                        }
                        return;

                    case "javascript_disabled_search":
                        // Set the new search URL as the summary text for the JavaScript-disabled search preference.  The default is "https://duckduckgo.com/html/?q=".
                        javaScriptDisabledSearchPreference.setSummary(sharedPreferences.getString("javascript_disabled_search", "https://duckduckgo.com/html/?q="));

                        // Enable "javascript_disabled_search_custom_url" if "javascript_disabled_search" is set to "Custom URL".
                        javaScriptDisabledSearchCustomURLPreference.setEnabled(javaScriptDisabledSearchPreference.getSummary().equals("Custom URL"));

                        // Update the javaScriptDisabledSearchURL variable.  The default is "https://duckduckgo.com/html/?q=".
                        MainWebViewActivity.javaScriptDisabledSearchURL = sharedPreferences.getString("javascript_disabled_search", "https://duckduckgo.com/html/?q=");
                        return;

                    case "javascript_disabled_search_custom_url":
                        // Set the new custom search URL as the summary text for "javascript_disabled_search_custom_url".  The default is "".
                        javaScriptDisabledSearchCustomURLPreference.setSummary(sharedPreferences.getString("javascript_disabled_search_custom_url", ""));

                        // Update javaScriptDisabledSearchCustomURL.  The default is "".
                        MainWebViewActivity.javaScriptDisabledSearchCustomURL = sharedPreferences.getString("javascript_disabled_search_custom_url", "");

                    case "javascript_enabled_search":
                        // Set the new search URL as the summary text for the JavaScript-enabled search preference.  The default is "https://duckduckgo.com/?q=".
                        javaScriptEnabledSearchPreference.setSummary(sharedPreferences.getString("javascript_enabled_search", "https://duckduckgo.com/?q="));

                        // Enable "javascript_enabled_search_custom_url" if "javascript_enabled_search" is set to "Custom URL".
                        javaScriptEnabledSearchCustomURLPreference.setEnabled(javaScriptEnabledSearchPreference.getSummary().equals("Custom URL"));

                        // Update the javaScriptEnabledSearchURL variable.  The default is "https://duckduckgo.com/?q=".
                        MainWebViewActivity.javaScriptEnabledSearchURL = sharedPreferences.getString("javascript_enabled_search", "https://duckduckgo.com/?q=");
                        return;

                    case "javascript_enabled_search_custom_url":
                        // Set the new custom search URL as the summary text for "javascript_enabled_search_custom_url".  The default is "".
                        javaScriptEnabledSearchCustomURLPreference.setSummary(sharedPreferences.getString("javascript_enabled_search_custom_url", ""));

                        // Update javaScriptEnabledSearchCustomURL.  The default is "".
                        MainWebViewActivity.javaScriptEnabledSearchCustomURL = sharedPreferences.getString("javascript_enabled_search_custom_url", "");

                    case "homepage":
                        // Set the new homepage URL as the summary text for the Homepage preference.  The default is "https://www.duckduckgo.com".
                        homepagePreference.setSummary(sharedPreferences.getString("homepage", "https://www.duckduckgo.com"));

                        // Update the homepage variable.  The default is "https://www.duckduckgo.com".
                        MainWebViewActivity.homepage = sharedPreferences.getString("homepage", "https://www.duckduckgo.com");
                        return;

                    case "swipe_to_refresh_enabled":
                        // Set swipeToRefreshEnabled to the new state.  The default is true.
                        MainWebViewActivity.swipeToRefreshEnabled = sharedPreferences.getBoolean("swipe_to_refresh_enabled", true);

                        // Update swipeRefreshLayout to match the new state.
                        MainWebViewActivity.swipeToRefresh.setEnabled(MainWebViewActivity.swipeToRefreshEnabled);
                        return;

                    // If no match, do nothing.
                    default:
                }
            }
        };

        // Register the listener.
        savedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    // It is necessary to re-register the listener on every resume or it will randomly stop working because apps can be paused and resumed at any time
    // even while running in the foreground.
    @Override
    public void onResume() {
        super.onResume();
        savedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        savedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesListener);
    }
}
