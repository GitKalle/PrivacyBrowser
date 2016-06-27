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

        // Allow the user to access "dom_storage_enabled" if "javascript_enabled" is enabled.  The default is false.
        final Preference domStorageEnabled = findPreference("dom_storage_enabled");
        domStorageEnabled.setEnabled(savedPreferences.getBoolean("javascript_enabled", false));

        // Allow the user to access "third_party_cookies_enabled" if "first_party_cookies_enabled" is enabled.  The default is false.
        final Preference thirdPartyCookiesEnabled = findPreference("third_party_cookies_enabled");
        thirdPartyCookiesEnabled.setEnabled(savedPreferences.getBoolean("first_party_cookies_enabled", false));

        // Set the current user-agent as the summary text for the "user_agent" preference when the preference screen is loaded.
        final Preference userAgentPreference = findPreference("user_agent");
        switch (savedPreferences.getString("user_agent", "Default user agent")) {
            case "Default user agent":
                // Get the user agent text from the webview (which changes based on the version of Android and WebView installed).
                // Once API >= 17 we can use getDefaultUserAgent() instead of getUserAgentString().
                userAgentPreference.setSummary(MainWebViewActivity.mainWebView.getSettings().getUserAgentString());
                break;

            case "Custom user agent":
                // We can't use the string from the array because it is referenced in code and can't be translated.
                userAgentPreference.setSummary(R.string.custom_user_agent);
                break;

            default:
                // Display the user agent from the preference as the summary text.
                userAgentPreference.setSummary(savedPreferences.getString("user_agent", "Default user agent"));
                break;
        }

        // Set the summary text for "custom_user_agent" (the default is "PrivacyBrowser/1.0") and enable it if "user_agent" it set to "Custom user agent".
        final Preference customUserAgent = findPreference("custom_user_agent");
        customUserAgent.setSummary(savedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0"));
        customUserAgent.setEnabled(userAgentPreference.getSummary().equals("Custom user agent"));


        // Set the JavaScript-disabled search URL as the summary text for the JavaScript-disabled search preference when the preference screen is loaded.
        // The default is "https://duckduckgo.com/html/?q=".
        final Preference javaScriptDisabledSearchPreference = findPreference("javascript_disabled_search");
        String javaScriptDisabledSearchString = savedPreferences.getString("javascript_disabled_search", "https://duckduckgo.com/html/?q=");
        if (javaScriptDisabledSearchString.equals("Custom URL")) {
            // If set to "Custom URL", use R.string.custom_url, which will be translated, instead of the array value, which will not.
            javaScriptDisabledSearchPreference.setSummary(R.string.custom_url);
        } else {
            // Set the array value as the summary text.
            javaScriptDisabledSearchPreference.setSummary(javaScriptDisabledSearchString);
        }

        // Set the summary text for "javascript_disabled_search_custom_url" (the default is "") and enable it if "javascript_disabled_search" is set to "Custom URL".
        final Preference javaScriptDisabledSearchCustomURLPreference = findPreference("javascript_disabled_search_custom_url");
        javaScriptDisabledSearchCustomURLPreference.setSummary(savedPreferences.getString("javascript_disabled_search_custom_url", ""));
        javaScriptDisabledSearchCustomURLPreference.setEnabled(javaScriptDisabledSearchString.equals("Custom URL"));


        // Set the JavaScript-enabed searchURL as the summary text for the JavaScript-enabled search preference when the preference screen is loaded.
        // The default is "https://duckduckgo.com/?q=".
        final Preference javaScriptEnabledSearchPreference = findPreference("javascript_enabled_search");
        String javaScriptEnabledSearchString = savedPreferences.getString("javascript_enabled_search", "https://duckduckgo.com/?q=");
        if (javaScriptEnabledSearchString.equals("Custom URL")) {
            // If set to "Custom URL", use R.string.custom_url, which will be tgranslated, instead of the array value, which will not.
            javaScriptEnabledSearchPreference.setSummary(R.string.custom_url);
        } else {
            // Set the array value as the summary text.
            javaScriptEnabledSearchPreference.setSummary(javaScriptEnabledSearchString);
        }

        // Set the summary text for "javascript_enabled_search_custom_url" (the default is "") and enable it if "javascript_enabled_search" is set to "Custom URL".
        final Preference javaScriptEnabledSearchCustomURLPreference = findPreference("javascript_enabled_search_custom_url");
        javaScriptEnabledSearchCustomURLPreference.setSummary(savedPreferences.getString("javascript_enabled_search_custom_url", ""));
        javaScriptEnabledSearchCustomURLPreference.setEnabled(javaScriptEnabledSearchString.equals("Custom URL"));


        // Set the homepage URL as the summary text for the Homepage preference when the preference screen is loaded.  The default is "https://www.duckduckgo.com".
        final Preference homepagePreference = findPreference("homepage");
        homepagePreference.setSummary(savedPreferences.getString("homepage", "https://www.duckduckgo.com"));


        // Listen for preference changes.
        preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.  We know.
            @SuppressLint("SetJavaScriptEnabled")
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                switch (key) {
                    case "javascript_enabled":
                        // Set javaScriptEnabled to the new state.  The default is false.
                        MainWebViewActivity.javaScriptEnabled = sharedPreferences.getBoolean("javascript_enabled", false);

                        // Toggle the state of the "dom_storage_enabled" preference.  The default is false.
                        final Preference domStorageEnabled = findPreference("dom_storage_enabled");
                        domStorageEnabled.setEnabled(sharedPreferences.getBoolean("javascript_enabled", false));

                        // Update mainWebView and reload the website.
                        MainWebViewActivity.mainWebView.getSettings().setJavaScriptEnabled(MainWebViewActivity.javaScriptEnabled);
                        MainWebViewActivity.mainWebView.reload();

                        // Update the privacy icon.
                        updatePrivacyIcon();
                        break;

                    case "first_party_cookies_enabled":
                        // Set firstPartyCookiesEnabled to the new state.  The default is false.
                        MainWebViewActivity.firstPartyCookiesEnabled = sharedPreferences.getBoolean("first_party_cookies_enabled", false);

                        // Toggle the state of the "third_party_cookies_enabled" preference.  The default is false.
                        final Preference thirdPartyCookiesEnabled = findPreference("third_party_cookies_enabled");
                        thirdPartyCookiesEnabled.setEnabled(sharedPreferences.getBoolean("first_party_cookies_enabled", false));

                        // Update mainWebView and reload the website.
                        MainWebViewActivity.cookieManager.setAcceptCookie(MainWebViewActivity.firstPartyCookiesEnabled);
                        MainWebViewActivity.mainWebView.reload();

                        // Update the checkbox in the options menu.
                        MenuItem firstPartyCookiesMenuItem = MainWebViewActivity.mainMenu.findItem(R.id.toggleFirstPartyCookies);
                        firstPartyCookiesMenuItem.setChecked(MainWebViewActivity.firstPartyCookiesEnabled);

                        // Update the privacy icon.
                        updatePrivacyIcon();
                        break;

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
                        break;

                    case "dom_storage_enabled":
                        // Set domStorageEnabled to the new state.  The default is false.
                        MainWebViewActivity.domStorageEnabled = sharedPreferences.getBoolean("dom_storage_enabled", false);

                        // Update the checkbox in the options menu.
                        MenuItem domStorageMenuItem = MainWebViewActivity.mainMenu.findItem(R.id.toggleDomStorage);
                        domStorageMenuItem.setChecked(MainWebViewActivity.domStorageEnabled);

                        // Update mainWebView and reload the website.
                        MainWebViewActivity.mainWebView.getSettings().setDomStorageEnabled(MainWebViewActivity.domStorageEnabled);
                        MainWebViewActivity.mainWebView.reload();

                        // Update the privacy icon.
                        updatePrivacyIcon();
                        break;

                    case "save_form_data_enabled":
                        // Set saveFormDataEnabled to the new state.  The default is false.
                        MainWebViewActivity.saveFormDataEnabled = sharedPreferences.getBoolean("save_form_data_enabled", false);

                        // Update the checkbox in the options menu.
                        MenuItem saveFormDataMenuItem = MainWebViewActivity.mainMenu.findItem(R.id.toggleSaveFormData);
                        saveFormDataMenuItem.setChecked(MainWebViewActivity.saveFormDataEnabled);

                        // Update mainWebView and reload the website.
                        MainWebViewActivity.mainWebView.getSettings().setSaveFormData(MainWebViewActivity.saveFormDataEnabled);
                        MainWebViewActivity.mainWebView.reload();
                        break;

                    case "user_agent":
                        String userAgentString = sharedPreferences.getString("user_agent", "Default user agent");

                        switch (userAgentString) {
                            case "Default user agent":
                                // Set the default user agent on mainWebView, display the user agent as the summary text for userAgentPreference, and disable customUserAgent.
                                // Once API >= 17 we can use getDefaultUserAgent().  For now, setUserAgentString("") sets the WebView's default user agent.
                                MainWebViewActivity.mainWebView.getSettings().setUserAgentString("");
                                userAgentPreference.setSummary(MainWebViewActivity.mainWebView.getSettings().getUserAgentString());
                                customUserAgent.setEnabled(false);
                                break;

                            case "Custom user agent":
                                // Set the custom user agent on mainWebView, display "Custom user agent" as the summary text for userAgentPreference, and enable customUserAgent.
                                MainWebViewActivity.mainWebView.getSettings().setUserAgentString(sharedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0"));
                                userAgentPreference.setSummary(R.string.custom_user_agent);
                                customUserAgent.setEnabled(true);
                                break;

                            default:
                                // Set the user agent on mainWebView, display the user agent as the summary text for userAgentPreference, and disable customUserAgent.
                                MainWebViewActivity.mainWebView.getSettings().setUserAgentString(sharedPreferences.getString("user_agent", "Default user agent"));
                                userAgentPreference.setSummary(MainWebViewActivity.mainWebView.getSettings().getUserAgentString());
                                customUserAgent.setEnabled(false);
                                break;
                        }
                        break;

                    case "custom_user_agent":
                        // Set the new custom user agent as the summary text for "custom_user_agent".  The default is "PrivacyBrowser/1.0".
                        customUserAgent.setSummary(sharedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0"));

                        // Update mainWebView's user agent.  The default is "PrivacyBrowser/1.0".
                        MainWebViewActivity.mainWebView.getSettings().setUserAgentString(sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0"));
                        break;

                    case "javascript_disabled_search":
                        String newJavaScriptDisabledSearchString = sharedPreferences.getString("javascript_disabled_search", "https://duckduckgo.com/html/?q=");
                        if (newJavaScriptDisabledSearchString.equals("Custom URL")) {
                            // Set the summary text to R.string.custom_url, which will be translated.
                            javaScriptDisabledSearchPreference.setSummary(R.string.custom_url);

                            // Update the javaScriptDisabledSearchURL variable.  The default is "".
                            MainWebViewActivity.javaScriptDisabledSearchURL = sharedPreferences.getString("javascript_disabled_search_custom_url", "");
                        } else {  // javascript_disabled_search is not set to Custom.
                            // Set the new search URL as the summary text for the JavaScript-disabled search preference.  The default is "https://duckduckgo.com/html/?q=".
                            javaScriptDisabledSearchPreference.setSummary(newJavaScriptDisabledSearchString);

                            // Update the javaScriptDisabledSearchURL variable.  The default is "https://duckduckgo.com/html/?q=".
                            MainWebViewActivity.javaScriptDisabledSearchURL = newJavaScriptDisabledSearchString;
                        }

                        // Enable or disable javaScriptDisabledSearchCustomURLPreference.
                        javaScriptDisabledSearchCustomURLPreference.setEnabled(newJavaScriptDisabledSearchString.equals("Custom URL"));
                        break;

                    case "javascript_disabled_search_custom_url":
                        // Set the new custom search URL as the summary text for "javascript_disabled_search_custom_url".  The default is "".
                        javaScriptDisabledSearchCustomURLPreference.setSummary(sharedPreferences.getString("javascript_disabled_search_custom_url", ""));

                        // Update javaScriptDisabledSearchCustomURL.  The default is "".
                        MainWebViewActivity.javaScriptDisabledSearchURL = sharedPreferences.getString("javascript_disabled_search_custom_url", "");
                        break;

                    case "javascript_enabled_search":
                        String newJavaScriptEnabledSearchString = sharedPreferences.getString("javascript_enabled_search", "https://duckduckgo.com/?q=");
                        if (newJavaScriptEnabledSearchString.equals("Custom URL")) {
                            // Set the summary text to R.string.custom_url, which will be translated.
                            javaScriptEnabledSearchPreference.setSummary(R.string.custom_url);

                            // Update the javaScriptEnabledSearchURL variable.  The default is "".
                            MainWebViewActivity.javaScriptEnabledSearchURL = sharedPreferences.getString("javascript_enabled_search_custom_url", "");
                        } else { // javascript_enabled_search is not set to Custom.
                            // Set the new search URL as the summary text for the JavaScript-enabled search preference.  The default is "https://duckduckgo.com/?q=".
                            javaScriptEnabledSearchPreference.setSummary(newJavaScriptEnabledSearchString);

                            // Update the javaScriptEnabledSearchURL variable.  The default is "https://duckduckgo.com/?q=".
                            MainWebViewActivity.javaScriptEnabledSearchURL = newJavaScriptEnabledSearchString;
                        }

                        // Enable or disable javaScriptEnabledSearchCustomURLPreference.
                        javaScriptEnabledSearchCustomURLPreference.setEnabled(newJavaScriptEnabledSearchString.equals("Custom URL"));
                        break;

                    case "javascript_enabled_search_custom_url":
                        // Set the new custom search URL as the summary text for "javascript_enabled_search_custom_url".  The default is "".
                        javaScriptEnabledSearchCustomURLPreference.setSummary(sharedPreferences.getString("javascript_enabled_search_custom_url", ""));

                        // Update javaScriptEnabledSearchCustomURL.  The default is "".
                        MainWebViewActivity.javaScriptEnabledSearchURL = sharedPreferences.getString("javascript_enabled_search_custom_url", "");
                        break;

                    case "homepage":
                        // Set the new homepage URL as the summary text for the Homepage preference.  The default is "https://www.duckduckgo.com".
                        homepagePreference.setSummary(sharedPreferences.getString("homepage", "https://www.duckduckgo.com"));

                        // Update the homepage variable.  The default is "https://www.duckduckgo.com".
                        MainWebViewActivity.homepage = sharedPreferences.getString("homepage", "https://www.duckduckgo.com");
                        break;

                    case "swipe_to_refresh_enabled":
                        // Set swipeToRefreshEnabled to the new state.  The default is true.
                        MainWebViewActivity.swipeToRefreshEnabled = sharedPreferences.getBoolean("swipe_to_refresh_enabled", true);

                        // Update swipeRefreshLayout to match the new state.
                        MainWebViewActivity.swipeToRefresh.setEnabled(MainWebViewActivity.swipeToRefreshEnabled);
                        break;

                    default:
                        // If no match, do nothing.
                        break;
                }
            }
        };

        // Register the listener.
        savedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    // It is necessary to re-register the listener on every resume or it will randomly stop working because apps can be paused and resumed at any time
    // even while running in the foreground.
    @Override
    public void onPause() {
        super.onPause();
        savedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        savedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    private void updatePrivacyIcon() {
        // Define a reference to the toggleJavaScript icon.
        MenuItem toggleJavaScript = MainWebViewActivity.mainMenu.findItem(R.id.toggleJavaScript);

        if (MainWebViewActivity.javaScriptEnabled) {
            toggleJavaScript.setIcon(R.drawable.javascript_enabled);
        } else {
            if (MainWebViewActivity.firstPartyCookiesEnabled) {
                toggleJavaScript.setIcon(R.drawable.warning);
            } else {
                toggleJavaScript.setIcon(R.drawable.privacy_mode);
            }
        }
    }
}
