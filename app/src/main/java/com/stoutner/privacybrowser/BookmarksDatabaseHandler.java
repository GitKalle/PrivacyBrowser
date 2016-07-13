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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookmarksDatabaseHandler extends SQLiteOpenHelper {
    private static final int SCHEMA_VERSION = 1;
    private static final String BOOKMARKS_DATABASE = "bookmarks.db";
    private static final String BOOKMARKS_TABLE = "bookmarks";

    public static final String _ID = "_id";
    public static final String DISPLAY_ORDER = "displayorder";
    public static final String BOOKMARK_NAME = "bookmarkname";
    public static final String BOOKMARK_URL = "bookmarkurl";
    public static final String PARENT_FOLDER = "parentfolder";
    public static final String IS_FOLDER = "isfolder";
    public static final String FAVORITE_ICON = "favoriteicon";

    public BookmarksDatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, BOOKMARKS_DATABASE, factory, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase bookmarksDatabase) {
        // Create the database if it doesn't exist.
        String CREATE_BOOKMARKS_TABLE = "CREATE TABLE " + BOOKMARKS_TABLE + " (" +
                _ID + " integer primary key, " +
                DISPLAY_ORDER + " integer, " +
                BOOKMARK_NAME + " text, " +
                BOOKMARK_URL + " text, " +
                PARENT_FOLDER + " text, " +
                IS_FOLDER + " boolean, " +
                FAVORITE_ICON + " blob);";

        bookmarksDatabase.execSQL(CREATE_BOOKMARKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase bookmarksDatabase, int oldVersion, int newVersion) {
        // Code for upgrading the database will be added here when the schema version > 1.
    }

    public void createBookmark(String bookmarkName, String bookmarkURL, int displayOrder, byte[] favoriteIcon) {
        ContentValues bookmarkContentValues = new ContentValues();

        // ID is created automatically.
        bookmarkContentValues.put(DISPLAY_ORDER, displayOrder);
        bookmarkContentValues.put(BOOKMARK_NAME, bookmarkName);
        bookmarkContentValues.put(BOOKMARK_URL, bookmarkURL);
        bookmarkContentValues.put(PARENT_FOLDER, "");
        bookmarkContentValues.put(IS_FOLDER, false);
        bookmarkContentValues.put(FAVORITE_ICON, favoriteIcon);

        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // The second argument is "null", which makes it so that completely null rows cannot be created.  Not a problem in our case.
        bookmarksDatabase.insert(BOOKMARKS_TABLE, null, bookmarkContentValues);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public Cursor getBookmarkCursor(int databaseId) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Prepare the SQL statement to get the cursor for `databaseId`
        final String GET_ONE_BOOKMARK = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + _ID + " = " + databaseId;

        // Return the results as a `Cursor`.  The second argument is `null` because there are no selectionArgs.
        // We can't close the `Cursor` because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_ONE_BOOKMARK, null);
    }

    public Cursor getBookmarksCursorExcept(long[] exceptIdLongArray) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Prepare a string that contains the comma-separated list of IDs not to get.
        String doNotGetIdsString = "";
        // Extract the array to `doNotGetIdsString`.
        for (long databaseIdLong : exceptIdLongArray) {
            // If this is the first number, only add the number.
            if (doNotGetIdsString.isEmpty()) {
                doNotGetIdsString = String.valueOf(databaseIdLong);
            } else {  // If there already is a number in the string, place a `,` before the number.
                doNotGetIdsString = doNotGetIdsString + "," + databaseIdLong;
            }
        }

        // Prepare the SQL statement to select all items except those with the specified IDs.
        final String GET_All_BOOKMARKS_EXCEPT_SPECIFIED = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + _ID + " NOT IN (" + doNotGetIdsString + ") ORDER BY " + DISPLAY_ORDER + " ASC";

        // Return the results as a `Cursor`.  The second argument is `null` because there are no selectionArgs.
        // We can't close the `Cursor` because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_All_BOOKMARKS_EXCEPT_SPECIFIED, null);
    }

    public Cursor getAllBookmarksCursor() {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Get everything in the BOOKMARKS_TABLE.
        final String GET_ALL_BOOKMARKS = "Select * FROM " + BOOKMARKS_TABLE + " ORDER BY " + DISPLAY_ORDER + " ASC";

        // Return the results as a Cursor.  The second argument is `null` because there are no selectionArgs.
        // We can't close the Cursor because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_ALL_BOOKMARKS, null);
    }

    public String getBookmarkURL(int databaseId) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Prepare the SQL statement to get the row for the selected databaseId.
        final String GET_BOOKMARK_URL = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + _ID + " = " + databaseId;

        // Save the results as Cursor and move it to the first (only) row.  The second argument is "null" because there are no selectionArgs.
        Cursor bookmarksCursor = bookmarksDatabase.rawQuery(GET_BOOKMARK_URL, null);
        bookmarksCursor.moveToFirst();

        // Get the int that identifies the "BOOKMARK_URL" column and save the string as bookmarkURL.
        int urlColumnInt = bookmarksCursor.getColumnIndex(BOOKMARK_URL);
        String bookmarkURLString = bookmarksCursor.getString(urlColumnInt);

        // Close the Cursor and the database handle.
        bookmarksCursor.close();
        bookmarksDatabase.close();

        // Return the bookmarkURLString.
        return bookmarkURLString;
    }

    public void updateBookmark(int databaseId, String bookmarkName, String bookmarkUrl) {
        // Store the updated values in `bookmarkContentValues`.
        ContentValues bookmarkContentValues = new ContentValues();

        bookmarkContentValues.put(BOOKMARK_NAME, bookmarkName);
        bookmarkContentValues.put(BOOKMARK_URL, bookmarkUrl);

        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // Update the database.  The last argument is `null` because there are no `whereArgs`.
        bookmarksDatabase.update(BOOKMARKS_TABLE, bookmarkContentValues, _ID + " = " + databaseId, null);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public void updateBookmark(int databaseId, String bookmarkName, String bookmarkUrl, byte[] favoriteIcon) {
        // Store the updated values in `bookmarkContentValues`.
        ContentValues bookmarkContentValues = new ContentValues();

        bookmarkContentValues.put(BOOKMARK_NAME, bookmarkName);
        bookmarkContentValues.put(BOOKMARK_URL, bookmarkUrl);
        bookmarkContentValues.put(FAVORITE_ICON, favoriteIcon);

        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // Update the database.  The last argument is `null` because there are no `whereArgs`.
        bookmarksDatabase.update(BOOKMARKS_TABLE, bookmarkContentValues, _ID + " = " + databaseId, null);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public void updateBookmarkDisplayOrder(int databaseId, int displayOrder) {
        // Store the updated values in `bookmarkContentValues`.
        ContentValues bookmarkContentValues = new ContentValues();

        bookmarkContentValues.put(DISPLAY_ORDER, displayOrder);

        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // Update the database.  The last argument is `null` because there are no `whereArgs`.
        bookmarksDatabase.update(BOOKMARKS_TABLE, bookmarkContentValues, _ID + " = " + databaseId, null);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public void deleteBookmark(int databaseId) {
        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // Deletes the row with the given databaseId.  The last argument is null because we don't need additional parameters.
        bookmarksDatabase.delete(BOOKMARKS_TABLE, _ID + " = " + databaseId, null);

        // Close the database handle.
        bookmarksDatabase.close();
    }
}