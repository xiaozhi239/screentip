package com.huangsz.android.screentip.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.common.utils.ViewUtils;
import com.huangsz.android.screentip.connect.model.TextConfigModel;

import java.util.Comparator;

/**
 * Config text on watch face. Such as user customized text, date or weather.
 */
public class TextConfigDialog extends BaseConfigDialog {

    private EditText mContent;

    private SeekBar mCoordinateX;

    private SeekBar mCoordinateY;

    private Spinner mColorSpinner;

    private Spinner mFontSpinner;

    private TextConfigModel mTextModel = new TextConfigModel();

    public static TextConfigDialog newInstance(String title, Handler handler, int handlerFlag) {
        TextConfigDialog dialog = new TextConfigDialog();
        setupDialog(dialog, title, handler, handlerFlag);
        return dialog;
    }

    @Override
    public void customizeView(AlertDialog.Builder builder) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_text_config, null);
        builder.setView(view);
        onInitialize(view);
        builder.setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmConfig();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just close.
            }
        });
    }

    private void maybeRestoreStatus() {
        if (mTextModel.isEmpty()) {
            return;
        }
        mContent.setText(mTextModel.getContent());
        mCoordinateX.setProgress((int)mTextModel.getCoordinateX());
        mCoordinateY.setProgress((int)mTextModel.getCoordinateY());
        ViewUtils.setSpinnerSelection(mColorSpinner, mTextModel.getColor());
        ViewUtils.setSpinnerSelection(mFontSpinner, mTextModel.getTextSize(),
                new Comparator<Object>() {
                    @Override
                    public int compare(Object o, Object t1) {
                        return getFontDpFromSpinnerSelection((String) o) - (int) t1;
                    }
                });
    }

    private void onInitialize(View view) {
        mContent = (EditText) view.findViewById(R.id.dialog_text_content);
        mCoordinateX = (SeekBar) view.findViewById(R.id.dialog_text_coordinate_x);
        mCoordinateY = (SeekBar) view.findViewById(R.id.dialog_text_coordinate_y);
        mColorSpinner = (Spinner) view.findViewById(R.id.dialog_text_color_spinner);
        mFontSpinner = (Spinner) view.findViewById(R.id.dialog_text_font_spinner);
        maybeRestoreStatus();
    }

    private void confirmConfig() {
        mTextModel.setColor(mColorSpinner.getSelectedItem().toString());
        mTextModel.setTextSize(getFontDpFromSpinnerSelection(
                mFontSpinner.getSelectedItem().toString()));
        mTextModel.setContent(mContent.getText().toString());
        mTextModel.setCoordinateX(mCoordinateX.getProgress());
        mTextModel.setCoordinateY(mCoordinateY.getProgress());
        sendMessage(mTextModel);
    }

    private int getFontDpFromSpinnerSelection(String spinnerSelection) {
        String fontInDp = spinnerSelection.trim();  // Example: '12dp'
        return Integer.parseInt(fontInDp.substring(0, fontInDp.length() - 2));
    }
}
