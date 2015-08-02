package com.huangsz.android.screentip.widget;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.huangsz.android.screentip.R;

public class ColorChooserDialog extends DialogFragment {

    private static final String KEY_DIALOG_TITLE = "KEY_DIALOG_TITLE";

    private Listener mColorSelectedListener;

    public static ColorChooserDialog newInstance(String title) {
        ColorChooserDialog dialog = new ColorChooserDialog();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DIALOG_TITLE, title);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mColorSelectedListener = (Listener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String title = getArguments().getString(KEY_DIALOG_TITLE);
        builder.setTitle(title)
                .setItems(R.array.colors_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] colours = getResources().getStringArray(R.array.colors_array);
                        mColorSelectedListener.onColourSelected(colours[which], getTag());
                    }
                });
        return builder.create();
    }

    public interface Listener {
        void onColourSelected(String color, String tag);
    }
}
