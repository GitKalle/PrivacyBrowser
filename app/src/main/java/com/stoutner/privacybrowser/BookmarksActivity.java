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
import android.app.DialogFragment;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.ByteArrayOutputStream;

public class BookmarksActivity extends AppCompatActivity implements CreateBookmark.CreateBookmarkListener {
    private BookmarksDatabaseHandler bookmarksDatabaseHandler;
    private ListView bookmarksListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarks_coordinatorlayout);

        // We need to use the SupportActionBar from android.support.v7.app.ActionBar until the minimum API is >= 21.
        Toolbar bookmarksAppBar = (Toolbar) findViewById(R.id.bookmarks_toolbar);
        setSupportActionBar(bookmarksAppBar);

        // Display the home arrow on supportAppBar.
        final ActionBar appBar = getSupportActionBar();
        assert appBar != null;// This assert removes the incorrect warning in Android Studio on the following line that appBar might be null.
        appBar.setDisplayHomeAsUpEnabled(true);

        // Initialize the database handler and the ListView.
        bookmarksDatabaseHandler = new BookmarksDatabaseHandler(this, null, null, 0);
        bookmarksListView = (ListView) findViewById(R.id.bookmarks_listview);

        // Display the bookmarks in the ListView.
        updateBookmarksListView();

        // Set a listener so that tapping a list item loads the URL.  We need to store the activity in a variable so that we can return to the parent activity after loading the URL.
        final Activity bookmarksActivity = this;
        bookmarksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Convert the id from long to int to match the format of the bookmarks database.
                int databaseID = (int) id;

                // Get the bookmark URL and assign it to formattedUrlString.
                MainWebViewActivity.formattedUrlString = bookmarksDatabaseHandler.getBookmarkURL(databaseID);

                //  Load formattedUrlString and return to the main activity.
                MainWebViewActivity.mainWebView.loadUrl(MainWebViewActivity.formattedUrlString);
                NavUtils.navigateUpFromSameTask(bookmarksActivity);
            }
        });

        // Set a FloatingActionButton for creating new bookmarks.
        FloatingActionButton createBookmarkFAB = (FloatingActionButton) findViewById(R.id.create_bookmark_fab);
        assert createBookmarkFAB != null;
        createBookmarkFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the CreateBookmark AlertDialog and name the instance "@string/create_bookmark".
                DialogFragment createBookmarkDialog = new CreateBookmark();
                createBookmarkDialog.show(getFragmentManager(), "@string/create_bookmark");
            }
        });
    }

    @Override
    public void onCreateBookmarkCancel(DialogFragment createBookmarkDialogFragment) {
        // Do nothing because the user selected "Cancel".
    }

    @Override
    public void onCreateBookmarkCreate(DialogFragment createBookmarkDialogFragment) {
        // Get the EditTexts from the DialogFragment and extract the strings.
        EditText createBookmarkNameEditText = (EditText) createBookmarkDialogFragment.getDialog().findViewById(R.id.create_bookmark_name_edittext);
        String bookmarkNameString = createBookmarkNameEditText.getText().toString();
        EditText createBookmarkURLEditText = (EditText) createBookmarkDialogFragment.getDialog().findViewById(R.id.create_bookmark_url_edittext);
        String bookmarkURLString = createBookmarkURLEditText.getText().toString();

        // Convert the favoriteIcon Bitmap to a byte array.
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();
        MainWebViewActivity.favoriteIcon.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Create the bookmark.
        bookmarksDatabaseHandler.createBookmark(bookmarkNameString, bookmarkURLString, favoriteIconByteArray);

        // Refresh the ListView.
        updateBookmarksListView();
    }

    private void updateBookmarksListView() {
        // Get a Cursor with the current contents of the bookmarks database.
        final Cursor bookmarksCursor = bookmarksDatabaseHandler.getBookmarksCursor();

        // The last argument is 0 because no special behavior is required.
        SimpleCursorAdapter bookmarksAdapter = new SimpleCursorAdapter(this,
                R.layout.bookmarks_item_linearlayout,
                bookmarksCursor,
                new String[] { BookmarksDatabaseHandler.FAVORITEICON, BookmarksDatabaseHandler.BOOKMARK_NAME },
                new int[] { R.id.bookmark_favorite_icon, R.id.bookmark_name },
                0);

        // Override the handling of R.id.bookmark_favorite_icon to load an image instead of a string.
        bookmarksAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.bookmark_favorite_icon) {
                    // Get the byte array from the database.
                    byte[] favoriteIconByteArray = cursor.getBlob(columnIndex);

                    // Convert the byte array to a Bitmap beginning at the first byte and ending at the last.
                    Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

                    // Set the favoriteIconBitmap.
                    ImageView bookmarkFavoriteIcon = (ImageView) view;
                    bookmarkFavoriteIcon.setImageBitmap(favoriteIconBitmap);
                    return true;
                } else {  // Process the rest of the bookmarksAdapter with default settings.
                    return false;
                }
            }
        });

        // Update the ListView.
        bookmarksListView.setAdapter(bookmarksAdapter);
    }
}