package com.vadimoe.nomnom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import java.util.ArrayList;

public class CaloriesDialogFragment extends DialogFragment {

    final static String TAG = "CaloriesDialogFragment";

    SharedPreferences mSharedPref;
    SeekBar mCaloriesBar;
    Integer mCalCount;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // Restore selected items
        mCalCount = mSharedPref.getInt(getString(R.string.dialog_calories_settings), 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_calories, null);
        mCaloriesBar = (SeekBar) view.findViewById(R.id.seekBarCalories);
        mCaloriesBar.setProgress(mCalCount);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
        // Set the dialog title
        .setTitle(R.string.dialog_calories_settings)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK, so save the mSelectedItems results somewhere
                // or return them to the component that opened the dialog
                mCalCount = mCaloriesBar.getProgress();
                Log.i(TAG, "Calories count is: " + mCalCount*1000);

                SharedPreferences.Editor editor = mSharedPref.edit();
                //editor.clear();
                editor.putInt(getString(R.string.dialog_calories_settings), mCalCount);
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
