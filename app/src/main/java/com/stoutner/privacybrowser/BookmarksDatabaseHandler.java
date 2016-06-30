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

    public static final String ID = "_id";
    public static final String DISPLAYORDER = "displayorder";
    public static final String BOOKMARK_NAME = "bookmarkname";
    public static final String BOOKMARK_URL = "bookmarkurl";
    public static final String PARENTFOLDER = "parentfolder";
    public static final String ISFOLDER = "isfolder";
    public static final String FAVORITEICON = "favoriteicon";

    public BookmarksDatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, BOOKMARKS_DATABASE, factory, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase bookmarksDatabase) {
        // Create the database if it doesn't exist.
        String CREATE_BOOKMARKS_TABLE = "CREATE TABLE " + BOOKMARKS_TABLE + " (" +
                ID + " integer primary key, " +
                DISPLAYORDER + " integer, " +
                BOOKMARK_NAME + " text, " +
                BOOKMARK_URL + " text, " +
                PARENTFOLDER + " text, " +
                ISFOLDER + " boolean, " +
                FAVORITEICON + " blob);";

        bookmarksDatabase.execSQL(CREATE_BOOKMARKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase bookmarksDatabase, int oldVersion, int newVersion) {
        // Code for upgrading the database will be added here when the schema version > 1.
    }

    public void createBookmark(String bookmarkName, String bookmarkURL, byte[] favoriteIcon) {
        ContentValues bookmarkContentValues = new ContentValues();

        // ID is created automatically.
        bookmarkContentValues.put(BOOKMARK_NAME, bookmarkName);
        bookmarkContentValues.put(BOOKMARK_URL, bookmarkURL);
        bookmarkContentValues.put(PARENTFOLDER, "");
        bookmarkContentValues.put(ISFOLDER, false);
        bookmarkContentValues.put(FAVORITEICON, favoriteIcon);

        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // The second argument is "null", which makes it so that completely null rows cannot be created.  Not a problem in our case.
        bookmarksDatabase.insert(BOOKMARKS_TABLE, null, bookmarkContentValues);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public Cursor getBookmarksCursor() {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Get everything in the BOOKMARKS_TABLE.
        String GET_ALL_BOOKMARKS = "Select * FROM " + BOOKMARKS_TABLE;

        // Return the results as a Cursor.  The second argument is "null" because there are no selectionArgs.
        // We can't close the Cursor because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_ALL_BOOKMARKS, null);
    }

    public String getBookmarkURL(int databaseID) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Get the row for the selected databaseID.
        String GET_BOOKMARK_URL = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + ID + " = " + databaseID;

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
}
