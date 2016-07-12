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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
// If we don't use `android.support.v7.app.AlertDialog` instead of `android.app.AlertDialog` then the dialog will be covered by the keyboard.
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class CreateBookmark extends DialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface CreateBookmarkListener {
        void onCreateBookmarkCancel(DialogFragment createBookmarkDialogFragment);

        void onCreateBookmarkCreate(DialogFragment createBookmarkDialogFragment);
    }

    // `createBookmarkListener` is used in `onAttach()` and `onCreateDialog()`
    private CreateBookmarkListener createBookmarkListener;


    public void onAttach(Activity parentActivity) {
        super.onAttach(parentActivity);

        // Get a handle for `CreateBookmarkListener` from the `parentActivity`.
        try {
            createBookmarkListener = (CreateBookmarkListener) parentActivity;
        } catch(ClassCastException exception) {
            throw new ClassCastException(parentActivity.toString() + " must implement CreateBookmarkListener.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a drawable version of the favorite icon.
        Drawable favoriteIconDrawable = new BitmapDrawable(getResources(), MainWebViewActivity.favoriteIcon);

        // Use `AlertDialog.Builder` to create the `AlertDialog`.  The style formats the color of the button text.
        AlertDialog.Builder createBookmarkDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.LightAlertDialog);
        createBookmarkDialogBuilder.setTitle(R.string.create_bookmark);
        createBookmarkDialogBuilder.setIcon(favoriteIconDrawable);
        // The parent view is `null` because it will be assigned by `AlertDialog`.
        createBookmarkDialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.create_bookmark_dialog, null));

        // Set an `onClick()` listener for the negative button.
        createBookmarkDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return the `DialogFragment` to the parent activity on cancel.
                createBookmarkListener.onCreateBookmarkCancel(CreateBookmark.this);
            }
        });

        // Set an `onClick()` listener for the positive button.
        createBookmarkDialogBuilder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return the `DialogFragment` to the parent activity on create.
                createBookmarkListener.onCreateBookmarkCreate(CreateBookmark.this);
            }
        });


        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog createBookmarkDialog = createBookmarkDialogBuilder.create();

        // Show the keyboard when the `Dialog` is displayed on the screen.
        createBookmarkDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // We need to show the `AlertDialog` before we can call `setOnKeyListener()` below.
        createBookmarkDialog.show();

        // Allow the `enter` key on the keyboard to create the bookmark from `create_bookmark_name_edittext`.
        EditText createBookmarkNameEditText = (EditText) createBookmarkDialog.findViewById(R.id.create_bookmark_name_edittext);
        assert createBookmarkNameEditText != null;  // Remove the warning below that createBookmarkNameEditText might be null.
        createBookmarkNameEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down on the `enter` button, select the PositiveButton `Create`.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Trigger `createBookmarkListener` and return the DialogFragment to the parent activity.
                    createBookmarkListener.onCreateBookmarkCreate(CreateBookmark.this);
                    // Manually dismiss the `AlertDialog`.
                    createBookmarkDialog.dismiss();
                    // Consume the event.
                    return true;
                } else {  // If any other key was pressed, do not consume the event.
                    return false;
                }
            }
        });

        // Set the formattedUrlString as the initial text of `create_bookmark_url_edittext`.
        EditText createBookmarkUrlEditText = (EditText) createBookmarkDialog.findViewById(R.id.create_bookmark_url_edittext);
        assert createBookmarkUrlEditText != null;// Remove the warning below that `createBookmarkUrlEditText` might be null.
        createBookmarkUrlEditText.setText(MainWebViewActivity.formattedUrlString);

        // Allow the `enter` key on the keyboard to create the bookmark from `create_bookmark_url_edittext`.
        createBookmarkUrlEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down on the "enter" button, select the PositiveButton "Create".
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Trigger `createBookmarkListener` and return the DialogFragment to the parent activity.
                    createBookmarkListener.onCreateBookmarkCreate(CreateBookmark.this);
                    // Manually dismiss the `AlertDialog`.
                    createBookmarkDialog.dismiss();
                    // Consume the event.
                    return true;
                } else { // If any other key was pressed, do not consume the event.
                    return false;
                }
            }
        });

        // `onCreateDialog()` requires the return of an `AlertDialog`.
        return createBookmarkDialog;
    }
}