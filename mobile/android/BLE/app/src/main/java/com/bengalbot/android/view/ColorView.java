package com.bengalbot.android.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bengalbot.android.R;
import com.bengalbot.android.command.Command;
import com.bengalbot.android.command.Properties;
import com.bengalbot.android.command.RGBColor;

import java.util.HashMap;
import java.util.Map;

import afzkl.development.colorpickerview.dialog.ColorPickerDialog;

/**
 */
public class ColorView extends CardView  implements View.OnClickListener, CommandView{

    private PresetViewActions mPresetViewctionsListener;
    private TextView mColorTextView;
    private ImageView mImageColor;
    private Map<Properties, Object> properties;
    private int mSelectedColor;

    public ColorView(Context context, PresetViewActions presetViewctionsListener) {
        super(context);
        this.mPresetViewctionsListener = presetViewctionsListener;
        initView();

    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.color_view, null);

        properties = new HashMap<>();

        v.findViewById(R.id.button_close).setOnClickListener(this);
        mColorTextView = (TextView)v.findViewById(R.id.color_label);
        mColorTextView.setText(getResources().getString(R.string.color_value, 0, 0, 0));
        mImageColor = (ImageView)v.findViewById(R.id.color_selected);
        mImageColor.setOnClickListener(this);

        addView(v);
    }


    private String colorToHexString(int color) {
        return String.format("#%06X", 0xFFFFFFFF & color);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.button_close) {
            mPresetViewctionsListener.onCloseView(this);
        } else if (id == R.id.color_selected) {
            openColorDialog();
        }

    }

    private ColorPickerDialog colorDialog = new ColorPickerDialog(getContext(), 0XFF000000);
    private void openColorDialog() {

        colorDialog.setAlphaSliderVisible(true);
        colorDialog.setTitle("Pick a Color!");

        colorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(android.R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                mSelectedColor = colorDialog.getColor();
                mImageColor.setBackgroundColor(mSelectedColor);

                mColorTextView.setText(getResources().getString(R.string.color_value, Color.red(mSelectedColor), Color.green(mSelectedColor), Color.blue(mSelectedColor)));

            }
        });

        colorDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Nothing to do here.
            }
        });

        colorDialog.show();

    }


    @Override
    public Command getCommand() {

        Command command = new RGBColor();

        properties.put(Properties.RED, Color.red(mSelectedColor));
        properties.put(Properties.GREEN, Color.green(mSelectedColor));
        properties.put(Properties.BLUE, Color.blue(mSelectedColor));

        command.create(properties);
        return command;
    }
}
