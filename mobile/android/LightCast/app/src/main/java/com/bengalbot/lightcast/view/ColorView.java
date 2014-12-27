package com.bengalbot.lightcast.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bengalbot.lightcast.R;
import com.bengalbot.lightcast.command.Command;
import com.bengalbot.lightcast.command.Properties;
import com.bengalbot.lightcast.command.RGBColor;

import java.util.HashMap;
import java.util.Map;

import afzkl.development.colorpickerview.dialog.ColorPickerDialog;

/**
 */
public class ColorView extends CardView  implements View.OnClickListener, CommandView{

    private PresetViewActions mPresetViewctionsListener;
    private TextView mColorTextView;
    private TextView mBrightnessTextView;
    private SeekBar mSeekBar;
    private EditText mDurationTransition;
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
        mSeekBar = (SeekBar)v.findViewById(R.id.brightness_seek);
        mBrightnessTextView = (TextView)v.findViewById(R.id.brightness_seek_label);
        mDurationTransition = (EditText)v.findViewById(R.id.duration_transition_value);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mBrightnessTextView.setText(getContext().getString(R.string.brightness_value, Integer.toString(i)) + "%");
                properties.put(Properties.PERCENTAGE, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBrightnessTextView.setText(getContext().getString(R.string.brightness_value, Integer.toString(mSeekBar.getProgress())) + "%");
        addView(v);
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

        //percentage is set in the seekbar Progress

        if (mDurationTransition.getText().length() > 0) {
            properties.put(Properties.MILLISECONDS, Integer.valueOf(mDurationTransition.getText().toString()));
        }

        command.create(properties);
        return command;
    }
}
