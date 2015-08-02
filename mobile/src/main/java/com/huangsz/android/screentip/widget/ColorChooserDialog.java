package com.huangsz.android.screentip.widget;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.huangsz.android.screentip.R;

public class ColorChooserDialog extends DialogFragment {

    private Listener colorSelectedListener;

    private String title;

    public ColorChooserDialog(String title) {
        this.title = title;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        colorSelectedListener = (Listener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setItems(R.array.colors_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] colours = getResources().getStringArray(R.array.colors_array);
                        colorSelectedListener.onColourSelected(colours[which], getTag());
                    }
                });
        return builder.create();
    }

    public interface Listener {
        void onColourSelected(String colour, String tag);
    }
}
