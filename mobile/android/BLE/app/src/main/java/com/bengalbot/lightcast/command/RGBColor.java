package com.bengalbot.lightcast.command;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * Created by jmendez on 12/20/14.
 */
public class RGBColor implements Command {
    Map<Properties, Object> properties;

    private static final int DEFAULT_TRANSITION_TIME_IN_MILLISECONDS = 500;
    @Override
    public void create(Map<Properties, Object> properties) {
        this.properties = properties;
    }


    @Override
    public byte[] parse() {
        //default values
        int r = 0;
        int g = 0;
        int b = 0;
        int brightness = 0;
        int transitionTime = DEFAULT_TRANSITION_TIME_IN_MILLISECONDS;

        if (properties.containsKey(Properties.RED)) {
            r = (Integer)properties.get(Properties.RED);
        }

        if (properties.containsKey(Properties.GREEN)) {
            g = (Integer)properties.get(Properties.GREEN);
        }

        if (properties.containsKey(Properties.BLUE)) {
            b = (Integer)properties.get(Properties.BLUE);
        }

        if (properties.containsKey(Properties.PERCENTAGE)) {
            brightness = (Integer)properties.get(Properties.PERCENTAGE);
        }
        if (properties.containsKey(Properties.MILLISECONDS)) {
            transitionTime = (Integer)properties.get(Properties.MILLISECONDS);
        }

        byte[] bytes = ByteBuffer.allocate(7)
                .order(ByteOrder.BIG_ENDIAN)
                .put(COLOR_PROGRAMMING_COMMAND_CODE)
                .put((byte) r)
                .put((byte)g)
                .put((byte)b)
                .put((byte) (0xFF * (brightness/100f))) // value is from 0-255 for brightness
                .putShort((short) transitionTime)
                .array();

        return CommandUtils.prepareForFrames(bytes);
    }

    @Override
    public void send() {
    }

    @Override
    public String getName() {
        return "RGB Color";
    }
}
