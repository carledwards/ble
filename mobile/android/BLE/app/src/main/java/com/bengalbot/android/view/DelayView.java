package com.bengalbot.android.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.bengalbot.android.R;
import com.bengalbot.android.command.Command;
import com.bengalbot.android.command.Delay;
import com.bengalbot.android.command.Properties;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class DelayView extends CardView  implements View.OnClickListener, CommandView{

    private PresetViewActions mPresetViewctionsListener;
    private EditText mDelayEditText;
    private Map<Properties, Object> properties;

    public DelayView(Context context, PresetViewActions presetViewctionsListener) {
        super(context);
        this.mPresetViewctionsListener = presetViewctionsListener;
        initView();

    }

    public DelayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DelayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.delay_view, null);

        v.findViewById(R.id.button_close).setOnClickListener(this);
        mDelayEditText = (EditText)v.findViewById(R.id.delay_value);
        properties = new HashMap<>();

        addView(v);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_close) {
            mPresetViewctionsListener.onCloseView(this);
        }
    }


    @Override
    public Command getCommand() {

        Command command = new Delay();

        if (mDelayEditText.getText().length() > 0) {
            properties.put(Properties.MILLISECONDS, Integer.valueOf(mDelayEditText.getText().toString()));
        }


        command.create(properties);
        return command;
    }
}
