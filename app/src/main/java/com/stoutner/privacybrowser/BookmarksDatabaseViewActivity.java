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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BookmarksDatabaseViewActivity extends AppCompatActivity {
    // `bookmarksDatabaseHandler` is used in `onCreate()` and `updateBookmarksListView()`.
    BookmarksDatabaseHandler bookmarksDatabaseHandler;

    // `bookmarksListView` is used in `onCreate()` and `updateBookmarksListView()`.
    ListView bookmarksListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarks_database_view_coordinatorlayout);

        // We need to use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        final Toolbar bookmarksDatabaseViewAppBar = (Toolbar) findViewById(R.id.bookmarks_database_view_toolbar);
        setSupportActionBar(bookmarksDatabaseViewAppBar);

        // Display the home arrow on `SupportActionBar`.
        final ActionBar appBar = getSupportActionBar();
        assert appBar != null;  // This assert removes the incorrect warning in Android Studio on the following line that appBar might be null.
        appBar.setDisplayHomeAsUpEnabled(true);

        // Initialize the database handler and the ListView.
        // `this` specifies the context.  The two `null`s do not specify the database name or a `CursorFactory`.
        // The `0` is to specify a database version, but that is set instead using a constant in `BookmarksDatabaseHandler`.
        bookmarksDatabaseHandler = new BookmarksDatabaseHandler(this, null, null, 0);
        bookmarksListView = (ListView) findViewById(R.id.bookmarks_database_view_listview);

        // Display the bookmarks in the ListView.
        updateBookmarksListView();

    }

    private void updateBookmarksListView() {
        // Get a `Cursor` with the current contents of the bookmarks database.
        final Cursor bookmarksCursor = bookmarksDatabaseHandler.getAllBookmarksCursor();

        // Setup `bookmarksCursorAdapter` with `this` context.  The `false` disables autoRequery.
        CursorAdapter bookmarksCursorAdapter = new CursorAdapter(this, bookmarksCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.bookmarks_database_view_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                boolean isFolder = (cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHandler.IS_FOLDER)) == 1);

                // Get the database ID from the `Cursor` and display it in `bookmarkDatabaseIdTextView`.
                int bookmarkDatabaseId = cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHandler._ID));
                TextView bookmarkDatabaseIdTextView = (TextView) view.findViewById(R.id.bookmarks_database_view_database_id);
                bookmarkDatabaseIdTextView.setText(String.valueOf(bookmarkDatabaseId));

                // Get the favorite icon byte array from the `Cursor`.
                byte[] favoriteIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHandler.FAVORITE_ICON));
                // Convert the byte array to a `Bitmap` beginning at the beginning at the first byte and ending at the last.
                Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);
                // Display the bitmap in `bookmarkFavoriteIcon`.
                ImageView bookmarkFavoriteIcon = (ImageView) view.findViewById(R.id.bookmarks_database_view_favorite_icon);
                bookmarkFavoriteIcon.setImageBitmap(favoriteIconBitmap);

                // Get the bookmark name from the `Cursor` and display it in `bookmarkNameTextView`.
                String bookmarkNameString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHandler.BOOKMARK_NAME));
                TextView bookmarkNameTextView = (TextView) view.findViewById(R.id.bookmarks_database_view_bookmark_name);
                bookmarkNameTextView.setText(bookmarkNameString);
                // Make the font bold for folders.
                if (isFolder) {
                    // The first argument is `null` because we don't want to chage the font.
                    bookmarkNameTextView.setTypeface(null, Typeface.BOLD);
                } else {  // Reset the font to default.
                    bookmarkNameTextView.setTypeface(Typeface.DEFAULT);
                }

                // Get the display order from the `Cursor` and display it in `bookmarkDisplayOrderTextView`.
                int bookmarkDisplayOrder = cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHandler.DISPLAY_ORDER));
                TextView bookmarkDisplayOrderTextView = (TextView) view.findViewById(R.id.bookmarks_database_view_display_order);
                bookmarkDisplayOrderTextView.setText(String.valueOf(bookmarkDisplayOrder));

                // Get the parent folder from the `Cursor` and display it in `bookmarkParentFolder`.
                String bookmarkParentFolder = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHandler.PARENT_FOLDER));
                TextView bookmarkParentFolderTextView = (TextView) view.findViewById(R.id.bookmarks_database_view_parent_folder);
                // Make the folder name gray if it is the home folder.
                if (bookmarkParentFolder.isEmpty()) {
                    bookmarkParentFolderTextView.setText(R.string.home_folder);
                    bookmarkParentFolderTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
                } else {
                    bookmarkParentFolderTextView.setText(bookmarkParentFolder);
                    bookmarkParentFolderTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                }

                // Get the bookmark URL form the `Cursor` and display it in `bookmarkUrlTextView`.
                String bookmarkUrlString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHandler.BOOKMARK_URL));
                TextView bookmarkUrlTextView = (TextView) view.findViewById(R.id.bookmarks_database_view_bookmark_url);
                bookmarkUrlTextView.setText(bookmarkUrlString);
                if (isFolder) {
                    bookmarkUrlTextView.setVisibility(View.GONE);
                } else {
                    bookmarkUrlTextView.setVisibility(View.VISIBLE);
                }
            }
        };

        // Update the ListView.
        bookmarksListView.setAdapter(bookmarksCursorAdapter);
    }
}