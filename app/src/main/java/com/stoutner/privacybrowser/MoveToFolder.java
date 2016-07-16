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
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
// If we don't use `android.support.v7.app.AlertDialog` instead of `android.app.AlertDialog` then the dialog will be covered by the keyboard.
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

public class MoveToFolder extends DialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface MoveToFolderListener {
        void onCancelMoveToFolder(DialogFragment dialogFragment);

        void onMoveToFolder(DialogFragment dialogFragment);
    }

    // `moveToFolderListener` is used in `onAttach()` and `onCreateDialog`.
    private MoveToFolderListener moveToFolderListener;

    public void onAttach(Activity parentActivity) {
        super.onAttach(parentActivity);

        // Get a handle for `MoveToFolderListener` from `parentActivity`.
        try {
            moveToFolderListener = (MoveToFolderListener) parentActivity;
        } catch(ClassCastException exception) {
            throw new ClassCastException(parentActivity.toString() + " must implement EditBookmarkFolderListener.");
        }
    }

    // `exceptFolders` is used in `onCreateDialog()` and `addSubfoldersToExceptFolders()`.
    private String exceptFolders;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use `AlertDialog.Builder` to create the `AlertDialog`.  The style formats the color of the button text.
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.LightAlertDialog);
        dialogBuilder.setTitle(R.string.move_to_folder);
        // The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.move_to_folder_dialog, null));

        // Set an `onClick()` listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return the `DialogFragment` to the parent activity on cancel.
                moveToFolderListener.onCancelMoveToFolder(MoveToFolder.this);
            }
        });

        // Set the `onClick()` listener fo the positive button.
        dialogBuilder.setPositiveButton(R.string.move, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return the `DialogFragment` to the parent activity on save.
                moveToFolderListener.onMoveToFolder(MoveToFolder.this);
            }
        });

        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // We need to show the `AlertDialog` before we can modify items in the layout.
        alertDialog.show();

        // Initialize the `Cursor` and `CursorAdapter` variables.
        Cursor foldersCursor;
        CursorAdapter foldersCursorAdapter;

        // Check to see if we are in the `Home Folder`.
        if (BookmarksActivity.currentFolder.isEmpty()) {  // Don't display `Home Folder` at the top of the `ListView`.
            // Initialize `exceptFolders`.
            exceptFolders = "";

            // If a folder is selected, add it and all children to the list of folders not to display.
            long[] selectedBookmarksLongArray = BookmarksActivity.bookmarksListView.getCheckedItemIds();
            for (long databaseIdLong : selectedBookmarksLongArray) {
                // Get `databaseIdInt` for each selected bookmark.
                int databaseIdInt = (int) databaseIdLong;

                // If `databaseIdInt` is a folder.
                if (BookmarksActivity.bookmarksDatabaseHandler.isFolder(databaseIdInt)) {
                    // Get the name of the selected folder.
                    String folderName = BookmarksActivity.bookmarksDatabaseHandler.getFolderName(databaseIdInt);

                    if (exceptFolders.isEmpty()){
                        // Add the selected folder to the list of folders not to display.
                        exceptFolders = DatabaseUtils.sqlEscapeString(folderName);
                    } else {
                        // Add the selected folder to the end of the list of folders not to display.
                        exceptFolders = exceptFolders + "," + DatabaseUtils.sqlEscapeString(folderName);
                    }

                    // Add the selected folder's subfolders to the list of folders not to display.
                    addSubfoldersToExceptFolders(folderName);
                }
            }

            // Get a `Cursor` containing the folders to display.
            foldersCursor = BookmarksActivity.bookmarksDatabaseHandler.getFoldersCursorExcept(exceptFolders);

            // Setup `foldersCursorAdaptor` with `this` context.  `false` disables autoRequery.
            foldersCursorAdapter = new CursorAdapter(alertDialog.getContext(), foldersCursor, false) {
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    // Inflate the individual item layout.  `false` does not attach it to the root.
                    return getActivity().getLayoutInflater().inflate(R.layout.move_to_folder_item_linearlayout, parent, false);
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    // Get the folder icon from `cursor`.
                    byte[] folderIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHandler.FAVORITE_ICON));
                    // Convert the byte array to a `Bitmap` beginning at the first byte and ending at the last.
                    Bitmap folderIconBitmap = BitmapFactory.decodeByteArray(folderIconByteArray, 0, folderIconByteArray.length);
                    // Display `folderIconBitmap` in `move_to_folder_icon`.
                    ImageView folderIconImageView = (ImageView) view.findViewById(R.id.move_to_folder_icon);
                    assert folderIconImageView != null;  // Remove the warning below that `currentIconImageView` might be null;
                    folderIconImageView.setImageBitmap(folderIconBitmap);

                    // Get the folder name from `cursor` and display it in `move_to_folder_name_textview`.
                    String folderName = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHandler.BOOKMARK_NAME));
                    TextView folderNameTextView = (TextView) view.findViewById(R.id.move_to_folder_name_textview);
                    folderNameTextView.setText(folderName);
                }
            };
        } else {  // Display `Home Folder` at the top of the `ListView`.
            // Get the home folder icon drawable and convert it to a `Bitmap`.  `this` specifies the current context.
            Drawable homeFolderIconDrawable = ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.folder_grey_bitmap);
            BitmapDrawable homeFolderIconBitmapDrawable = (BitmapDrawable) homeFolderIconDrawable;
            Bitmap homeFolderIconBitmap = homeFolderIconBitmapDrawable.getBitmap();
            // Convert the folder `Bitmap` to a byte array.  `0` is for lossless compression (the only option for a PNG).
            ByteArrayOutputStream homeFolderIconByteArrayOutputStream = new ByteArrayOutputStream();
            homeFolderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, homeFolderIconByteArrayOutputStream);
            byte[] homeFolderIconByteArray = homeFolderIconByteArrayOutputStream.toByteArray();

            // Setup a `MatrixCursor` for the `Home Folder`.
            String[] homeFolderMatrixCursorColumnNames = {BookmarksDatabaseHandler._ID, BookmarksDatabaseHandler.BOOKMARK_NAME, BookmarksDatabaseHandler.FAVORITE_ICON};
            MatrixCursor homeFolderMatrixCursor = new MatrixCursor(homeFolderMatrixCursorColumnNames);
            homeFolderMatrixCursor.addRow(new Object[]{0, getString(R.string.home_folder), homeFolderIconByteArray});

            // Add the parent folder to the list of folders not to display.
            exceptFolders = DatabaseUtils.sqlEscapeString(BookmarksActivity.currentFolder);

            // If a folder is selected, add it and all children to the list of folders not to display.
            long[] selectedBookmarksLongArray = BookmarksActivity.bookmarksListView.getCheckedItemIds();
            for (long databaseIdLong : selectedBookmarksLongArray) {
                // Get `databaseIdInt` for each selected bookmark.
                int databaseIdInt = (int) databaseIdLong;

                // If `databaseIdInt` is a folder.
                if (BookmarksActivity.bookmarksDatabaseHandler.isFolder(databaseIdInt)) {
                    // Get the name of the selected folder.
                    String folderName = BookmarksActivity.bookmarksDatabaseHandler.getFolderName(databaseIdInt);

                    // Add the selected folder to the end of the list of folders not to display.
                    exceptFolders = exceptFolders + "," + DatabaseUtils.sqlEscapeString(folderName);

                    // Add the selected folder's subfolders to the list of folders not to display.
                    addSubfoldersToExceptFolders(folderName);
                }
            }

            // Get a `foldersCursor`.
            foldersCursor = BookmarksActivity.bookmarksDatabaseHandler.getFoldersCursorExcept(exceptFolders);

            // Combine `homeFolderMatrixCursor` and `foldersCursor`.
            MergeCursor foldersMergeCursor = new MergeCursor(new Cursor[]{homeFolderMatrixCursor, foldersCursor});

            // Setup `foldersCursorAdaptor` with `this` context.  `false` disables autoRequery.
            foldersCursorAdapter = new CursorAdapter(alertDialog.getContext(), foldersMergeCursor, false) {
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    // Inflate the individual item layout.  `false` does not attach it to the root.
                    return getActivity().getLayoutInflater().inflate(R.layout.move_to_folder_item_linearlayout, parent, false);
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    // Get the folder icon from `cursor`.
                    byte[] folderIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHandler.FAVORITE_ICON));
                    // Convert the byte array to a `Bitmap` beginning at the first byte and ending at the last.
                    Bitmap folderIconBitmap = BitmapFactory.decodeByteArray(folderIconByteArray, 0, folderIconByteArray.length);
                    // Display `folderIconBitmap` in `move_to_folder_icon`.
                    ImageView folderIconImageView = (ImageView) view.findViewById(R.id.move_to_folder_icon);
                    assert folderIconImageView != null;  // Remove the warning below that `currentIconImageView` might be null;
                    folderIconImageView.setImageBitmap(folderIconBitmap);

                    // Get the folder name from `cursor` and display it in `move_to_folder_name_textview`.
                    String folderName = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHandler.BOOKMARK_NAME));
                    TextView folderNameTextView = (TextView) view.findViewById(R.id.move_to_folder_name_textview);
                    folderNameTextView.setText(folderName);
                }
            };
        }

        // Display the ListView
        ListView foldersListView = (ListView) alertDialog.findViewById(R.id.move_to_folder_listview);
        assert foldersListView != null;  // Remove the warning below that `foldersListView` might be null.
        foldersListView.setAdapter(foldersCursorAdapter);

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }

    public void addSubfoldersToExceptFolders(String folderName) {
        // Get a `Cursor` will all the immediate subfolders.
        Cursor subfoldersCursor = BookmarksActivity.bookmarksDatabaseHandler.getSubfoldersCursor(folderName);

        for (int i = 0; i < subfoldersCursor.getCount(); i++) {
            // Move `subfolderCursor` to the current item.
            subfoldersCursor.moveToPosition(i);

            // Get the name of the subfolder.
            String subfolderName = subfoldersCursor.getString(subfoldersCursor.getColumnIndex(BookmarksDatabaseHandler.BOOKMARK_NAME));

            // Run the same tasks for any subfolders of the subfolder.
            addSubfoldersToExceptFolders(subfolderName);

            // Add the subfolder to `exceptFolders`.
            subfolderName = DatabaseUtils.sqlEscapeString(subfolderName);
            exceptFolders = exceptFolders + "," + subfolderName;
        }

    }
}
