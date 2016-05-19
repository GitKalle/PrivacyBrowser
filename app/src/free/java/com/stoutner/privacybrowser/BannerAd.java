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

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

class BannerAd extends AppCompatActivity{
    public static void requestAd(View view) {
        // Cast view to an AdView.
        AdView adView = (AdView) view;

        // Load an ad.
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public static void reloadAfterRotate (View view, Context applicationContext, String ad_id) {
        // Cast view to an AdView.
        AdView adView = (AdView) view;

        // Save the layout parameters.
        RelativeLayout.LayoutParams adViewLayoutParameters = (RelativeLayout.LayoutParams) adView.getLayoutParams();

        // Remove the AdView.
        RelativeLayout adViewParentLayout = (RelativeLayout) adView.getParent();
        adViewParentLayout.removeView(adView);

        // Setup the new AdView.
        adView = new AdView(applicationContext);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(ad_id);
        adView.setId(R.id.adView);
        adView.setLayoutParams(adViewLayoutParameters);

        // Display the new AdView.
        adViewParentLayout.addView(adView);

        // Request a new ad.
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public static void hideAd(View view) {
        // Cast view to an AdView.
        AdView adView = (AdView) view;

        // Hide the ad.
        adView.setVisibility(View.GONE);
    }

    public static void showAd(View view) {
        // Cast view to an AdView.
        AdView adView = (AdView) view;

        // Hide the ad.
        adView.setVisibility(View.VISIBLE);
    }

    public static void pauseAd(View view) {
        // Cast view to an AdView.
        AdView adView = (AdView) view;

        // Pause the AdView.
        adView.pause();
    }

    public static void resumeAd(View view) {
        // Cast view to an AdView.
        AdView adView = (AdView) view;

        // Resume the AdView.
        adView.resume();
    }
}