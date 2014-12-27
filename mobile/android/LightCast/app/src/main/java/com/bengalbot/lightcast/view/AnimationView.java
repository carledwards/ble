package com.bengalbot.lightcast.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bengalbot.lightcast.R;
import com.bengalbot.lightcast.command.Animation;
import com.bengalbot.lightcast.command.Command;
import com.bengalbot.lightcast.command.Properties;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class AnimationView extends CardView  implements View.OnClickListener, CommandView{

    private PresetViewActions mPresetViewctionsListener;
    private RadioGroup mTransitionRadioGroup;
    private TextView mAnimationTextView;
    private EditText mDurationAnimationEditText;
    private int selectedRadioItem;
    private Map<Properties, Object> properties;


    public AnimationView(Context context, PresetViewActions presetViewctionsListener) {
        super(context);
        this.mPresetViewctionsListener = presetViewctionsListener;
        initView();

    }

    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.transition_view, null);
        v.findViewById(R.id.button_close).setOnClickListener(this);

        properties = new HashMap<Properties, Object>();

        mAnimationTextView = (TextView)v.findViewById(R.id.transition_label);
        mDurationAnimationEditText = (EditText)v.findViewById(R.id.animation_duration_value);

        mTransitionRadioGroup = (RadioGroup)v.findViewById(R.id.transition_group);

        mTransitionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int resIdItem) {

                if (resIdItem == R.id.linear) {
                    selectedRadioItem = 0;
                }
                if (resIdItem == R.id.flashing) {
                    selectedRadioItem = 1;
                }

            }
        });
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

        Command command = new Animation();

        properties.put(Properties.ANIMATION_TYPE, selectedRadioItem);
        if (mDurationAnimationEditText.getText().length() > 0) {
            properties.put(Properties.MILLISECONDS, Integer.valueOf(mDurationAnimationEditText.getText().toString()));
        }

        command.create(properties);
        return command;
    }
}
