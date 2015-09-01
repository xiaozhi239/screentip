package com.huangsz.android.screentip.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.huangsz.com.screentip.connect.model.TextConfigModel;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.huangsz.android.screentip.R;

/**
 * Config text on watch face. Such as user customized text, date or weather.
 */
public class TextConfigDialog extends BaseConfigDialog {

    private EditText mContent;

    private EditText mCoordinateX;

    private EditText mCoordinateY;

    private Spinner mColorSpinner;

    private Spinner mFontSpinner;

    public static TextConfigDialog newInstance(String title, Handler handler, int handlerFlag) {
        TextConfigDialog dialog = new TextConfigDialog();
        setupDialog(dialog, title, handler, handlerFlag);
        return dialog;
    }

    @Override
    public void customizeView(AlertDialog.Builder builder) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_text_config, null);
        builder.setView(view);
        setElements(view);
        builder.setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmConfig();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, just close. TODO(huangsz) preserve current state.
            }
        });
    }

    private void setElements(View view) {
        mContent = (EditText) view.findViewById(R.id.dialog_text_content);
        mCoordinateX = (EditText) view.findViewById(R.id.dialog_text_coordinate_x);
        mCoordinateY = (EditText) view.findViewById(R.id.dialog_text_coordinate_y);
        mColorSpinner = (Spinner) view.findViewById(R.id.dialog_text_color_spinner);
        mFontSpinner = (Spinner) view.findViewById(R.id.dialog_text_font_spinner);
    }

    private void confirmConfig() {
        TextConfigModel model = new TextConfigModel();
        model.getDataMap().putString(TextConfigModel.KEY_TEXT_COLOR,
                mColorSpinner.getSelectedItem().toString());
        model.getDataMap().putString(TextConfigModel.KEY_TEXT_FONT,
                mFontSpinner.getSelectedItem().toString());
        model.getDataMap().putString(TextConfigModel.KEY_TEXT_CONTENT,
                mContent.getText().toString());
        model.getDataMap().putInt(TextConfigModel.KEY_COORDINATE_X,
                Integer.valueOf(mCoordinateX.getText().toString()));
        model.getDataMap().putInt(TextConfigModel.KEY_COORDINATE_Y,
                Integer.valueOf(mCoordinateY.getText().toString()));
        sendMessage(model);
    }

}
