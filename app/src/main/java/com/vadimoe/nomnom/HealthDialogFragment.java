package com.vadimoe.nomnom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import java.util.ArrayList;

public class HealthDialogFragment extends DialogFragment {

    final static String TAG = "HealthDialogFragment";

    ArrayList mSelectedItems;
    SharedPreferences mSharedPref;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Restore selected items
        int num_items = getResources().getStringArray(R.array.health_labels).length;
        boolean[] bool_sel_items = new boolean[num_items];
        for (int i = 0; i < num_items; i++) {
            bool_sel_items[i] = false;
        }
        String[] saved_sel_items = mSharedPref.getString(getString(R.string.dialog_health_settings), "").split(",");
        for (String s : saved_sel_items) {
            if (!s.equals(""))
                bool_sel_items[Integer.parseInt(s)] = true;
        }
        // Set the dialog title
        builder.setTitle(R.string.dialog_health_settings)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.health_labels, bool_sel_items,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                        // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        Log.i(TAG, "Selected items are: " + mSelectedItems.toString());
                        if (mSelectedItems.isEmpty()) return;

                        String selected_items = "";
                        for (int i = 0; i < mSelectedItems.size(); i++) {
                            if (i==0)
                                selected_items += mSelectedItems.get(i);
                            else
                                selected_items += "," + mSelectedItems.get(i);
                        }

                        SharedPreferences.Editor editor = mSharedPref.edit();
                        editor.putString(getString(R.string.dialog_health_settings), selected_items);
                        editor.commit();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }
}
