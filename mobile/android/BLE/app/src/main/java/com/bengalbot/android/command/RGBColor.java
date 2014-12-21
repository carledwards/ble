package com.bengalbot.android.command;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by jmendez on 12/20/14.
 */
public class RGBColor implements Command {
    Map<Properties, Object> properties;

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

        if (properties.containsKey(Properties.RED)) {
            r = (Integer)properties.get(Properties.RED);
        }

        if (properties.containsKey(Properties.GREEN)) {
            g = (Integer)properties.get(Properties.GREEN);
        }

        if (properties.containsKey(Properties.BLUE)) {
            b = (Integer)properties.get(Properties.BLUE);
        }

        return ByteBuffer.allocate(4)
                .put((byte)COLOR_PROGRAMMING_COMMAND_CODE)
                .put((byte)r)
                .put((byte)g)
                .put((byte)b).array();

    }

    @Override
    public void send() {

    }


    @Override
    public String getName() {
        return "RGB Color";
    }
}
