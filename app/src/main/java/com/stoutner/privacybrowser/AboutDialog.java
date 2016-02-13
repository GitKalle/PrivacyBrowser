/**
 * Copyright 2015-2016 Soren Stoutner <soren@stoutner.com>.
 *
 * This file is part of Privacy Browser <https://privacybrowser.stoutner.com/>.
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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AboutDialog extends AppCompatDialogFragment {
    @Override
    // onCreateDialog requires @NonNull.
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a WebView to display about_text.html
        final WebView aboutDialogWebView = new WebView(getContext());
        aboutDialogWebView.loadUrl("file:///android_asset/about_text.html");

        // Use AlertDialog.Builder to create the AlertDialog
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.about_privacy_browser);
        alertDialogBuilder.setView(aboutDialogWebView);
        alertDialogBuilder.setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing.  The dialog will automatically be dismissed.
            }
        });

        // Assign alertDialogBuilder to an AlertDialog and show it on the screen.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        aboutDialogWebView.setWebViewClient(new WebViewClient() {
            // shouldOverrideUrlLoading lets us close AboutDialog when a link is touched.  Otherwise the dialog covers the website that loads beneath in Privacy Browser.
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                MainWebViewActivity.mainWebView.loadUrl(url);
                alertDialog.dismiss();
                return true;
            }
        });

        // onCreateDialog requires the return of an AlertDialog.
        return alertDialog;
    }
}
