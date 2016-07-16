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
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;

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

    public void createBookmark(String bookmarkName, String bookmarkURL, int displayOrder, String parentFolder, byte[] favoriteIcon) {
        ContentValues bookmarkContentValues = new ContentValues();

        // ID is created automatically.
        bookmarkContentValues.put(DISPLAY_ORDER, displayOrder);
        bookmarkContentValues.put(BOOKMARK_NAME, bookmarkName);
        bookmarkContentValues.put(BOOKMARK_URL, bookmarkURL);
        bookmarkContentValues.put(PARENT_FOLDER, parentFolder);
        bookmarkContentValues.put(IS_FOLDER, false);
        bookmarkContentValues.put(FAVORITE_ICON, favoriteIcon);

        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // The second argument is `null`, which makes it so that completely null rows cannot be created.  Not a problem in our case.
        bookmarksDatabase.insert(BOOKMARKS_TABLE, null, bookmarkContentValues);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public void createFolder(String folderName, int displayOrder, String parentFolder, byte[] favoriteIcon) {
        ContentValues bookmarkContentValues = new ContentValues();

        // ID is created automatically.
        bookmarkContentValues.put(DISPLAY_ORDER, displayOrder);
        bookmarkContentValues.put(BOOKMARK_NAME, folderName);
        bookmarkContentValues.put(PARENT_FOLDER, parentFolder);
        bookmarkContentValues.put(IS_FOLDER, true);
        bookmarkContentValues.put(FAVORITE_ICON, favoriteIcon);

        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // The second argument is `null`, which makes it so that completely null rows cannot be created.  Not a problem in our case.
        bookmarksDatabase.insert(BOOKMARKS_TABLE, null, bookmarkContentValues);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public Cursor getBookmarkCursor(int databaseId) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Prepare the SQL statement to get the `Cursor` for `databaseId`
        final String GET_ONE_BOOKMARK = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + _ID + " = " + databaseId;

        // Return the results as a `Cursor`.  The second argument is `null` because there are no `selectionArgs`.
        // We can't close the `Cursor` because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_ONE_BOOKMARK, null);
    }

    public String getFolderName (int databaseId) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Prepare the SQL statement to get the `Cursor` for the folder.
        final String GET_FOLDER = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + _ID + " = " + databaseId;

        // Get `folderCursor`.  The second argument is `null` because there are no `selectionArgs`.
        Cursor folderCursor = bookmarksDatabase.rawQuery(GET_FOLDER, null);

        // Get `folderName`.
        folderCursor.moveToFirst();
        String folderName = folderCursor.getString(folderCursor.getColumnIndex(BOOKMARK_NAME));

        // Close the cursor and the database handle.
        folderCursor.close();
        bookmarksDatabase.close();

        // Return the folder name.
        return folderName;
    }

    public Cursor getFolderCursor(String folderName) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // SQL escape `folderName`.
        folderName = DatabaseUtils.sqlEscapeString(folderName);

        // Prepare the SQL statement to get the `Cursor` for the folder.
        final String GET_FOLDER = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + BOOKMARK_NAME + " = " + folderName +
                " AND " + IS_FOLDER + " = " + 1;

        // Return the results as a `Cursor`.  The second argument is `null` because there are no `selectionArgs`.
        // We can't close the `Cursor` because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_FOLDER, null);
    }

    public Cursor getFoldersCursorExcept(String exceptFolders) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Prepare the SQL statement to get the `Cursor` for the folders.
        final String GET_FOLDERS_EXCEPT = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + IS_FOLDER + " = " + 1 +
                " AND " + BOOKMARK_NAME + " NOT IN (" + exceptFolders +
                ") ORDER BY " + BOOKMARK_NAME + " ASC";

        // Return the results as a `Cursor`.  The second argument is `null` because there are no `selectionArgs`.
        // We can't close the `Cursor` because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_FOLDERS_EXCEPT, null);
    }

    public Cursor getSubfoldersCursor(String currentFolder) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // SQL escape `currentFolder.
        currentFolder = DatabaseUtils.sqlEscapeString(currentFolder);

        // Prepare the SQL statement to get the `Cursor` for the subfolders.
        final String GET_SUBFOLDERS = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + PARENT_FOLDER + " = " + currentFolder +
                " AND " + IS_FOLDER + " = " + 1;

        // Return the results as a `Cursor`.  The second argument is `null` because there are no `selectionArgs`.
        // We can't close the `Cursor` because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_SUBFOLDERS, null);
    }

    public String getParentFolder(String currentFolder) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // SQL escape `currentFolder`.
        currentFolder = DatabaseUtils.sqlEscapeString(currentFolder);

        // Prepare the SQL statement to get the parent folder.
        final String GET_PARENT_FOLDER = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + IS_FOLDER + " = " + 1 +
                " AND " + BOOKMARK_NAME + " = " + currentFolder;

        // The second argument is `null` because there are no `selectionArgs`.
        Cursor bookmarkCursor = bookmarksDatabase.rawQuery(GET_PARENT_FOLDER, null);
        bookmarkCursor.moveToFirst();

        // Store the name of the parent folder.
        String parentFolder = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(PARENT_FOLDER));

        // Close the `Cursor`.
        bookmarkCursor.close();

        return parentFolder;
    }

    public Cursor getAllBookmarksCursor() {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Get everything in the BOOKMARKS_TABLE.
        final String GET_ALL_BOOKMARKS = "Select * FROM " + BOOKMARKS_TABLE;

        // Return the results as a Cursor.  The second argument is `null` because there are no selectionArgs.
        // We can't close the Cursor because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_ALL_BOOKMARKS, null);
    }

    public Cursor getAllBookmarksCursorByDisplayOrder(String folderName) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // SQL escape `folderName`.
        folderName = DatabaseUtils.sqlEscapeString(folderName);

        // Get everything in the BOOKMARKS_TABLE.
        final String GET_ALL_BOOKMARKS = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + PARENT_FOLDER + " = " + folderName +
                " ORDER BY " + DISPLAY_ORDER + " ASC";

        // Return the results as a Cursor.  The second argument is `null` because there are no selectionArgs.
        // We can't close the Cursor because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_ALL_BOOKMARKS, null);
    }

    public Cursor getBookmarksCursorExcept(long[] exceptIdLongArray, String folderName) {
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

        // SQL escape `folderName`.
        folderName = DatabaseUtils.sqlEscapeString(folderName);

        // Prepare the SQL statement to select all items except those with the specified IDs.
        final String GET_All_BOOKMARKS_EXCEPT_SPECIFIED = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + PARENT_FOLDER + " = " + folderName +
                " AND " + _ID + " NOT IN (" + doNotGetIdsString +
                ") ORDER BY " + DISPLAY_ORDER + " ASC";

        // Return the results as a `Cursor`.  The second argument is `null` because there are no `selectionArgs`.
        // We can't close the `Cursor` because we need to use it in the parent activity.
        return bookmarksDatabase.rawQuery(GET_All_BOOKMARKS_EXCEPT_SPECIFIED, null);
    }

    public boolean isFolder(int databaseId) {
        // Get a readable database handle.
        SQLiteDatabase bookmarksDatabase = this.getReadableDatabase();

        // Prepare the SQL statement to determine if `databaseId` is a folder.
        final String CHECK_IF_FOLDER = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + _ID + " = " + databaseId;

        // Populate folderCursor.  The second argument is `null` because there are no `selectionArgs`.
        Cursor folderCursor = bookmarksDatabase.rawQuery(CHECK_IF_FOLDER, null);

        // Ascertain if this database ID is a folder.
        folderCursor.moveToFirst();
        boolean isFolder = (folderCursor.getInt(folderCursor.getColumnIndex(IS_FOLDER)) == 1);

        // Close the `Cursor` and the database handle.
        folderCursor.close();
        bookmarksDatabase.close();

        return isFolder;
    }

    public void updateBookmark(int databaseId, String bookmarkName, String bookmarkUrl) {
        // Store the updated values in `bookmarkContentValues`.
        ContentValues bookmarkContentValues = new ContentValues();

        bookmarkContentValues.put(BOOKMARK_NAME, bookmarkName);
        bookmarkContentValues.put(BOOKMARK_URL, bookmarkUrl);

        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // Update the bookmark.  The last argument is `null` because there are no `whereArgs`.
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

        // Update the bookmark.  The last argument is `null` because there are no `whereArgs`.
        bookmarksDatabase.update(BOOKMARKS_TABLE, bookmarkContentValues, _ID + " = " + databaseId, null);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public void updateFolder(int databaseId, String oldFolderName, String newFolderName) {
        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // Update the folder first.  Store the updated values in `folderContentValues`.
        ContentValues folderContentValues = new ContentValues();

        folderContentValues.put(BOOKMARK_NAME, newFolderName);

        // Run the update on the folder.  The last argument is `null` because there are no `whereArgs`.
        bookmarksDatabase.update(BOOKMARKS_TABLE, folderContentValues, _ID + " = " + databaseId, null);


        // Update the bookmarks inside the folder with the new parent folder name.
        ContentValues bookmarkContentValues = new ContentValues();

        bookmarkContentValues.put(PARENT_FOLDER, newFolderName);

        // SQL escape `oldFolderName`.
        oldFolderName = DatabaseUtils.sqlEscapeString(oldFolderName);

        // Run the update on the bookmarks.  The last argument is `null` because there are no `whereArgs`.
        bookmarksDatabase.update(BOOKMARKS_TABLE, bookmarkContentValues, PARENT_FOLDER + " = " + oldFolderName, null);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public void updateFolder(int databaseId, String oldFolderName, String newFolderName, byte[] folderIcon) {
        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // Update the folder first.  Store the updated values in `folderContentValues`.
        ContentValues folderContentValues = new ContentValues();

        folderContentValues.put(BOOKMARK_NAME, newFolderName);
        folderContentValues.put(FAVORITE_ICON, folderIcon);

        // Run the update on the folder.  The last argument is `null` because there are no `whereArgs`.
        bookmarksDatabase.update(BOOKMARKS_TABLE, folderContentValues, _ID + " = " + databaseId, null);


        // Update the bookmarks inside the folder with the new parent folder name.
        ContentValues bookmarkContentValues = new ContentValues();

        bookmarkContentValues.put(PARENT_FOLDER, newFolderName);

        // SQL escape `oldFolderName`.
        oldFolderName = DatabaseUtils.sqlEscapeString(oldFolderName);

        // Run the update on the bookmarks.  The last argument is `null` because there are no `whereArgs`.
        bookmarksDatabase.update(BOOKMARKS_TABLE, bookmarkContentValues, PARENT_FOLDER + " = " + oldFolderName, null);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public void updateBookmarkDisplayOrder(int databaseId, int displayOrder) {
        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // Store the new display order in `bookmarkContentValues`.
        ContentValues bookmarkContentValues = new ContentValues();
        bookmarkContentValues.put(DISPLAY_ORDER, displayOrder);

        // Update the database.  The last argument is `null` because there are no `whereArgs`.
        bookmarksDatabase.update(BOOKMARKS_TABLE, bookmarkContentValues, _ID + " = " + databaseId, null);

        // Close the database handle.
        bookmarksDatabase.close();
    }

    public void moveToFolder(int databaseId, String newFolder) {
        // Get a writable database handle.
        SQLiteDatabase bookmarksDatabase = this.getWritableDatabase();

        // Get the highest `DISPLAY_ORDER` in the new folder
        String newFolderSqlEscaped = DatabaseUtils.sqlEscapeString(newFolder);
        final String NEW_FOLDER = "Select * FROM " + BOOKMARKS_TABLE +
                " WHERE " + PARENT_FOLDER + " = " + newFolderSqlEscaped +
                " ORDER BY " + DISPLAY_ORDER + " ASC";
        // The second argument is `null` because there are no `selectionArgs`.
        Cursor newFolderCursor = bookmarksDatabase.rawQuery(NEW_FOLDER, null);
        int displayOrder;
        if (newFolderCursor.getCount() > 0) {
            newFolderCursor.moveToLast();
            displayOrder = newFolderCursor.getInt(newFolderCursor.getColumnIndex(DISPLAY_ORDER)) + 1;
        } else {
            displayOrder = 0;
        }
        newFolderCursor.close();

        // Store the new values in `bookmarkContentValues`.
        ContentValues bookmarkContentValues = new ContentValues();
        bookmarkContentValues.put(DISPLAY_ORDER, displayOrder);
        bookmarkContentValues.put(PARENT_FOLDER, newFolder);

        // Update the database.  The last argument is 'null' because there are no 'whereArgs'.
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