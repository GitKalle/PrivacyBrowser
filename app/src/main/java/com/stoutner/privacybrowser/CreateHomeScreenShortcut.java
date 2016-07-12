/**
 * Copyright 2015-2016 Soren Stoutner <soren@stoutner.com>.
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
// If we don't use android.support.v7.app.AlertDialog instead of android.app.AlertDialog then the dialog will be covered by the keyboard.
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class CreateHomeScreenShortcut extends DialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface CreateHomeScreenSchortcutListener {
        void onCreateHomeScreenShortcutCancel(DialogFragment dialogFragment);

        void onCreateHomeScreenShortcutCreate(DialogFragment dialogFragment);
    }

    //createHomeScreenShortcutListener is used in onAttach and and onCreateDialog.
    private CreateHomeScreenSchortcutListener createHomeScreenShortcutListener;

    // Check to make sure that the parent activity implements the listener.
    public void onAttach(Activity parentActivity) {
        super.onAttach(parentActivity);
        try {
            createHomeScreenShortcutListener = (CreateHomeScreenSchortcutListener) parentActivity;
        } catch(ClassCastException exception) {
            throw new ClassCastException(parentActivity.toString() + " must implement CreateHomeScreenShortcutListener.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a drawable version of the favorite icon.
        Drawable favoriteIconDrawable = new BitmapDrawable(getResources(), MainWebViewActivity.favoriteIcon);

        // Get the activity's layout inflater.
        LayoutInflater customDialogInflater = getActivity().getLayoutInflater();

        // Use AlertDialog.Builder to create the AlertDialog.  The style formats the color of the button text.
        AlertDialog.Builder createHomeScreenShorcutDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.LightAlertDialog);
        createHomeScreenShorcutDialogBuilder.setTitle(R.string.create_shortcut);
        createHomeScreenShorcutDialogBuilder.setIcon(favoriteIconDrawable);
        // The parent view is "null" because it will be assigned by AlertDialog.
        createHomeScreenShorcutDialogBuilder.setView(customDialogInflater.inflate(R.layout.create_home_screen_shortcut_dialog, null));

        // Set an onClick listener on the negative button.
        createHomeScreenShorcutDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createHomeScreenShortcutListener.onCreateHomeScreenShortcutCancel(CreateHomeScreenShortcut.this);
            }
        });

        // Set an onClick listener on the positive button.
        createHomeScreenShorcutDialogBuilder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createHomeScreenShortcutListener.onCreateHomeScreenShortcutCreate(CreateHomeScreenShortcut.this);
            }
        });


        // Create an AlertDialog from the AlertDialogBuilder.
        final AlertDialog createHomeScreenShortcutAlertDialog = createHomeScreenShorcutDialogBuilder.create();

        // Show the keyboard when the Dialog is displayed on the screen.
        createHomeScreenShortcutAlertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // We need to show the AlertDialog before we can call setOnKeyListener() below.
        createHomeScreenShortcutAlertDialog.show();

        // Allow the "enter" key on the keyboard to create the shortcut.
        EditText shortcutNameEditText = (EditText) createHomeScreenShortcutAlertDialog.findViewById(R.id.shortcut_name_edittext);
        assert shortcutNameEditText != null;  // Remove the warning below that shortcutNameEditText might be null.
        shortcutNameEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down on the "enter" button, select the PositiveButton "Create".
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Trigger the create listener.
                    createHomeScreenShortcutListener.onCreateHomeScreenShortcutCreate(CreateHomeScreenShortcut.this);

                    // Manually dismiss the AlertDialog.
                    createHomeScreenShortcutAlertDialog.dismiss();

                    // Consume the event.
                    return true;
                } else {  // If any other key was pressed, do not consume the event.
                    return false;
                }
            }
        });

        // onCreateDialog requires the return of an AlertDialog.
        return createHomeScreenShortcutAlertDialog;
    }
}