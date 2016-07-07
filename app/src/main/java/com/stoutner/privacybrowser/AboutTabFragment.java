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

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

public class AboutTabFragment extends Fragment {
    private int tabNumber;

    // AboutTabFragment.createTab stores the tab number in the bundle arguments so it can be referenced from onCreate().
    public static AboutTabFragment createTab(int tab) {
        Bundle thisTabArguments = new Bundle();
        thisTabArguments.putInt("Tab", tab);

        AboutTabFragment thisTab = new AboutTabFragment();
        thisTab.setArguments(thisTabArguments);
        return thisTab;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the tab number in tabNumber.
        tabNumber = getArguments().getInt("Tab");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View tabLayout;

        // Load the about tab layout.  Tab numbers start at 0.
        if (tabNumber == 0) {
            // Setting false at the end of inflater.inflate does not attach the inflated layout as a child of container.
            // The fragment will take care of attaching the root automatically.
            tabLayout = inflater.inflate(R.layout.about_tab_version, container, false);

            // Version.
            TextView versionNumberText = (TextView) tabLayout.findViewById(R.id.about_version_number_text);
            String version = getString(R.string.version) + " " + BuildConfig.VERSION_NAME + " (" + getString(R.string.version_code) + " " + Integer.toString(BuildConfig.VERSION_CODE) + ")";
            versionNumberText.setText(version);

            // Brand.
            TextView versionBrandText = (TextView) tabLayout.findViewById(R.id.about_version_brand_text);
            versionBrandText.setText(Build.BRAND);

            // Manufacturer.
            TextView versionManufacturerText = (TextView) tabLayout.findViewById(R.id.about_version_manufacturer_text);
            versionManufacturerText.setText(Build.MANUFACTURER);

            // Model.
            TextView versionModelText = (TextView) tabLayout.findViewById(R.id.about_version_model_text);
            versionModelText.setText(Build.MODEL);

            // Device.
            TextView versionDeviceText = (TextView) tabLayout.findViewById(R.id.about_version_device_text);
            versionDeviceText.setText(Build.DEVICE);

            // Bootloader.
            TextView versionBootloaderText = (TextView) tabLayout.findViewById(R.id.about_version_bootloader_text);
            versionBootloaderText.setText(Build.BOOTLOADER);

            // Radio.
            TextView versionRadioText = (TextView) tabLayout.findViewById(R.id.about_version_radio_text);
            // Hide versionRadioTextView if there is no radio.
            if (Build.getRadioVersion().equals("")) {
                TextView versionRadioTitle = (TextView) tabLayout.findViewById(R.id.about_version_radio_title);
                versionRadioTitle.setVisibility(View.GONE);
                versionRadioText.setVisibility(View.GONE);
            } else { // Else, set the text.
                versionRadioText.setText(Build.getRadioVersion());
            }

            // Android.
            TextView versionAndroidText = (TextView) tabLayout.findViewById(R.id.about_version_android_text);
            String android = Build.VERSION.RELEASE + " (" + getString(R.string.api) + " " + Integer.toString(Build.VERSION.SDK_INT) + ")";
            versionAndroidText.setText(android);

            // Build.
            TextView versionBuildText = (TextView) tabLayout.findViewById(R.id.about_version_build_text);
            versionBuildText.setText(Build.DISPLAY);

            // Security Patch.
            TextView versionSecurityPatchText = (TextView) tabLayout.findViewById(R.id.about_version_securitypatch_text);
            // Build.VERSION.SECURITY_PATCH is only available for SDK_INT >= 23.
            if (Build.VERSION.SDK_INT >= 23) {
                versionSecurityPatchText.setText(Build.VERSION.SECURITY_PATCH);
            } else { // Hide versionSecurityPatchTextView.
                TextView versionSecurityPatchTitle = (TextView) tabLayout.findViewById(R.id.about_version_securitypatch_title);
                versionSecurityPatchTitle.setVisibility(View.GONE);
                versionSecurityPatchText.setVisibility(View.GONE);
            }

            // webViewLayout is only used to get the default user agent from about_tab_webview.  It is not used to render content on the screen.
            View webViewLayout = inflater.inflate(R.layout.about_tab_webview, container, false);
            WebView tabLayoutWebView = (WebView) webViewLayout.findViewById(R.id.about_tab_webview);
            String userAgentString =  tabLayoutWebView.getSettings().getUserAgentString();

            // WebKit.
            TextView versionWebKitText = (TextView) tabLayout.findViewById(R.id.about_version_webkit_text);
            // Select the substring that begins after "Safari/" and goes to the end of the string.
            String webkitVersion = userAgentString.substring(userAgentString.indexOf("Safari/") + 7);
            versionWebKitText.setText(webkitVersion);

            // Chrome.
            TextView versionChromeText = (TextView) tabLayout.findViewById(R.id.about_version_chrome_text);
            // Select the substring that begins after "Chrome/" and goes until the next " ".
            String chromeVersion = userAgentString.substring(userAgentString.indexOf("Chrome/") + 7, userAgentString.indexOf(" ", userAgentString.indexOf("Chrome/")));
            versionChromeText.setText(chromeVersion);
        } else { // load a WebView for all the other tabs.  Tab numbers start at 0.
            // Setting false at the end of inflater.inflate does not attach the inflated layout as a child of container.
            // The fragment will take care of attaching the root automatically.
            tabLayout = inflater.inflate(R.layout.about_tab_webview, container, false);
            WebView tabWebView = (WebView) tabLayout;

            switch (tabNumber) {
                case 1:
                    tabWebView.loadUrl("file:///android_asset/about_permissions.html");
                    break;

                case 2:
                    tabWebView.loadUrl("file:///android_asset/about_privacy_policy.html");
                    break;

                case 3:
                    tabWebView.loadUrl("file:///android_asset/about_changelog.html");
                    break;

                case 4:
                    tabWebView.loadUrl("file:///android_asset/about_licenses.html");
                    break;

                case 5:
                    tabWebView.loadUrl("file:///android_asset/about_contributors.html");
                    break;

                case 6:
                    tabWebView.loadUrl("file:///android_asset/about_links.html");
                    break;

                default:
                    break;
            }
        }

        return tabLayout;
    }
}