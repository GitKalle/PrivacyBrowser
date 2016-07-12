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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
// If we don't use `android.support.v7.app.AlertDialog` instead of `android.app.AlertDialog` then the dialog will be covered by the keyboard.
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

public class EditBookmark extends DialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface EditBookmarkListener {
        void onEditBookmarkCancel(DialogFragment editBookmarkDialogFragment);

        void onEditBookmarkSave(DialogFragment editBookmarkDialogFragment);
    }

    // `editBookmarkListener` is used in `onAttach()` and `onCreateDialog()`
    private EditBookmarkListener editBookmarkListener;

    public void onAttach(Activity parentActivity) {
        super.onAttach(parentActivity);

        // Get a handle for `EditBookmarkListener` from the `parentActivity`.
        try {
            editBookmarkListener = (EditBookmarkListener) parentActivity;
        } catch(ClassCastException exception) {
            throw new ClassCastException(parentActivity.toString() + " must implement EditBookmarkListener.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a long array with the the databaseId of the selected bookmark and convert it to an `int`.
        long[] selectedBookmarksLongArray = BookmarksActivity.bookmarksListView.getCheckedItemIds();
        int selectedBookmarkDatabaseId = (int) selectedBookmarksLongArray[0];

        // Get a `Cursor` with the specified bookmark and move it to the first position.
        Cursor bookmarkCursor = BookmarksActivity.bookmarksDatabaseHandler.getBookmarkCursor(selectedBookmarkDatabaseId);
        bookmarkCursor.moveToFirst();

        // Get the favorite icon byte array from the `Cursor`.
        byte[] favoriteIconByteArray = bookmarkCursor.getBlob(bookmarkCursor.getColumnIndex(BookmarksDatabaseHandler.FAVORITE_ICON));
        // Convert the byte array to a `Bitmap` beginning at the first byte and ending at the last.
        Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);
        // Convert the `Bitmap` to a `Drawable`.
        Drawable favoriteIconDrawable = new BitmapDrawable(getResources(), favoriteIconBitmap);

        // Use `AlertDialog.Builder` to create the `AlertDialog`.  The style formats the color of the button text.
        AlertDialog.Builder editBookmarkDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.LightAlertDialog);
        editBookmarkDialogBuilder.setTitle(R.string.edit_bookmark);
        editBookmarkDialogBuilder.setIcon(favoriteIconDrawable);
        // The parent view is `null` because it will be assigned by `AlertDialog`.
        editBookmarkDialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.edit_bookmark_dialog, null));

        // Set an `onClick()` listener for the negative button.
        editBookmarkDialogBuilder.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return the `DialogFragment` to the parent activity on cancel.
                editBookmarkListener.onEditBookmarkCancel(EditBookmark.this);
            }
        });

        // Set the `onClick()` listener fo the positive button.
        editBookmarkDialogBuilder.setPositiveButton(R.string.save, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return the `DialogFragment` to the parent activity on save.
                editBookmarkListener.onEditBookmarkSave(EditBookmark.this);
            }
        });


        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog editBookmarkDialog = editBookmarkDialogBuilder.create();

        // Show the keyboard when the `Dialog` is displayed on the screen.
        editBookmarkDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // We need to show the `AlertDialog` before we can call `setOnKeyListener()` below.
        editBookmarkDialog.show();

        // Get a `Drawable` of the favorite icon from `mainWebView` and display it in `edit_bookmark_new_favorite_icon`.
        ImageView newFavoriteIcon = (ImageView) editBookmarkDialog.findViewById(R.id.edit_bookmark_new_favorite_icon);
        assert newFavoriteIcon != null;  // Remove the warning below that `newFavoriteIcon` might be null.
        newFavoriteIcon.setImageBitmap(MainWebViewActivity.favoriteIcon);

        // Load the text for `edit_bookmark_name_edittext`.
        EditText bookmarkNameEditText = (EditText) editBookmarkDialog.findViewById(R.id.edit_bookmark_name_edittext);
        assert bookmarkNameEditText != null;  // Remove the warning below that `bookmarkNameEditText` might be null.
        bookmarkNameEditText.setText(bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHandler.BOOKMARK_NAME)));

        // Allow the `enter` key on the keyboard to save the bookmark from `edit_bookmark_name_edittext`.
        bookmarkNameEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down on the "enter" button, select the PositiveButton `Save`.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Trigger `editBookmarkListener` and return the DialogFragment to the parent activity.
                    editBookmarkListener.onEditBookmarkSave(EditBookmark.this);
                    // Manually dismiss the `AlertDialog`.
                    editBookmarkDialog.dismiss();
                    // Consume the event.
                    return true;
                } else {  // If any other key was pressed, do not consume the event.
                    return false;
                }
            }
        });

        // Load the text for `create_bookmark_url_edittext`.
        EditText bookmarkUrlEditText = (EditText) editBookmarkDialog.findViewById(R.id.edit_bookmark_url_edittext);
        assert bookmarkUrlEditText != null;// Remove the warning below that `bookmarkUrlEditText` might be null.
        bookmarkUrlEditText.setText(bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHandler.BOOKMARK_URL)));

        // Allow the "enter" key on the keyboard to save the bookmark from `edit_bookmark_url_edittext`.
        bookmarkUrlEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down on the `enter` button, select the PositiveButton `Save`.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Trigger `editBookmarkListener` and return the DialogFragment to the parent activity.
                    editBookmarkListener.onEditBookmarkSave(EditBookmark.this);
                    // Manually dismiss the `AlertDialog`.
                    editBookmarkDialog.dismiss();
                    // Consume the event.
                    return true;
                } else { // If any other key was pressed, do not consume the event.
                    return false;
                }
            }
        });

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return editBookmarkDialog;
    }
}