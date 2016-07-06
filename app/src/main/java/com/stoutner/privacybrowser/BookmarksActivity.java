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
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

public class BookmarksActivity extends AppCompatActivity implements CreateBookmark.CreateBookmarkListener {
    private BookmarksDatabaseHandler bookmarksDatabaseHandler;
    private ListView bookmarksListView;

    // deleteBookmarkMenuItem is used in onCreate() and onPrepareOptionsMenu().
    private MenuItem deleteBookmarkMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarks_coordinatorlayout);

        // We need to use the SupportActionBar from android.support.v7.app.ActionBar until the minimum API is >= 21.
        final Toolbar bookmarksAppBar = (Toolbar) findViewById(R.id.bookmarks_toolbar);
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

        // registerForContextMenu(bookmarksListView);

        bookmarksListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                String numberSelectedString;
                long[] selectedItemsLongArray = bookmarksListView.getCheckedItemIds();

                numberSelectedString = selectedItemsLongArray.length + " " + getString(R.string.selected);

                mode.setSubtitle(numberSelectedString);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the contextual app bar.
                getMenuInflater().inflate(R.menu.bookmarks_context_menu, menu);

                mode.setTitle(R.string.bookmarks);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int menuItemId = item.getItemId();

                switch (menuItemId) {
                    case R.id.delete_bookmark:
                        // Get an array of the selected rows.
                        final long[] selectedItemsLongArray = bookmarksListView.getCheckedItemIds();

                        String snackbarMessage;

                        // Determine how many items are in the array and prepare an appropriate Snackbar message.
                        if (selectedItemsLongArray.length == 1) {
                            snackbarMessage = getString(R.string.one_bookmark_deleted);
                        } else {
                            snackbarMessage = selectedItemsLongArray.length + " " + getString(R.string.bookmarks_deleted);
                        }

                        updateBookmarksListViewExcept(selectedItemsLongArray);

                        // Show a SnackBar.
                        Snackbar.make(findViewById(R.id.bookmarks_coordinatorlayout), snackbarMessage, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Do nothing because everything will be handled by `onDismissed()` below.
                                    }
                                })
                                .setCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        switch (event) {
                                            // The user pushed the "Undo" button.
                                            case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                                // Refresh the ListView to show the rows again.
                                                updateBookmarksListView();

                                                break;

                                            // The Snackbar was dismissed without the "Undo" button being pushed.
                                            default:
                                                // Delete each selected row.
                                                for (long databaseIdLong : selectedItemsLongArray) {
                                                    // Convert `databaseIdLong` to an int.
                                                    int databaseIdInt = (int) databaseIdLong;

                                                    // Delete the database row.
                                                    bookmarksDatabaseHandler.deleteBookmark(databaseIdInt);
                                                }
                                                break;
                                        }
                                    }
                                })
                                .show();

                        // Close the contextual app bar.
                        mode.finish();
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

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

    /*
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.bookmarks_context_menu, menu);
    } */

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bookmarks_options, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Disable the delete icon.
        deleteBookmarkMenuItem = menu.findItem(R.id.delete_bookmark);
        deleteBookmarkMenuItem.setVisible(false);

        // Run all the other default commands.
        super.onPrepareOptionsMenu(menu);

        // `return true` displays the menu;
        return true;
    }*/

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

        // Setup bookmarksCursorAdapter with `this` context.  The `false` disables autoRequery.
        CursorAdapter bookmarksCursorAdapter = new CursorAdapter(this, bookmarksCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.bookmarks_item_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get the favorite icon byte array from the cursor.
                byte[] favoriteIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHandler.FAVORITEICON));

                // Convert the byte array to a Bitmap beginning at the first byte and ending at the last.
                Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

                // Display the bitmap in `bookmarkFavoriteIcon`.
                ImageView bookmarkFavoriteIcon = (ImageView) view.findViewById(R.id.bookmark_favorite_icon);
                bookmarkFavoriteIcon.setImageBitmap(favoriteIconBitmap);


                // Get the bookmark name from the cursor and display it in `bookmarkNameTextView`.
                String bookmarkNameString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHandler.BOOKMARK_NAME));
                TextView bookmarkNameTextView = (TextView) view.findViewById(R.id.bookmark_name);
                assert bookmarkNameTextView != null;  // This assert removes the warning that bookmarkNameTextView might be null.
                bookmarkNameTextView.setText(bookmarkNameString);
            }
        };

        // Update the ListView.
        bookmarksListView.setAdapter(bookmarksCursorAdapter);
    }

    private void updateBookmarksListViewExcept(long[] exceptIdLongArray) {
        // Get a Cursor with the current contents of the bookmarks database except for the specified database IDs.
        final Cursor bookmarksCursor = bookmarksDatabaseHandler.getBookmarksCursorExcept(exceptIdLongArray);

        // Setup bookmarksCursorAdapter with `this` context.  The `false` disables autoRequery.
        CursorAdapter bookmarksCursorAdapter = new CursorAdapter(this, bookmarksCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.bookmarks_item_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get the favorite icon byte array from the cursor.
                byte[] favoriteIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHandler.FAVORITEICON));

                // Convert the byte array to a Bitmap beginning at the first byte and ending at the last.
                Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

                // Display the bitmap in `bookmarkFavoriteIcon`.
                ImageView bookmarkFavoriteIcon = (ImageView) view.findViewById(R.id.bookmark_favorite_icon);
                bookmarkFavoriteIcon.setImageBitmap(favoriteIconBitmap);


                // Get the bookmark name from the cursor and display it in `bookmarkNameTextView`.
                String bookmarkNameString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHandler.BOOKMARK_NAME));
                TextView bookmarkNameTextView = (TextView) view.findViewById(R.id.bookmark_name);
                assert bookmarkNameTextView != null;  // This assert removes the warning that bookmarkNameTextView might be null.
                bookmarkNameTextView.setText(bookmarkNameString);
            }
        };

        // Update the ListView.
        bookmarksListView.setAdapter(bookmarksCursorAdapter);
    }
}