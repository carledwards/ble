package com.bengalbot.lightcast.command;

import android.util.Log;

import java.util.LinkedList;

/**
 * Created by jmendez on 12/22/14.
 */
public class CommandUtils {


    public static byte[] prepareForFrames(byte[] bytes) {
        int length = bytes.length;

        LinkedList<Byte> parsedBytes = new LinkedList<Byte>();

        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == Command.FRAME_START) {

                Log.d(Command.TAG, "found Frame Start in data...escaping");
                parsedBytes.add(Command.ESCAPE);
                parsedBytes.add(Command.FRAME_START_ESCAPE);
            } else if (bytes[i] == Command.FRAME_END) {

                Log.d(Command.TAG, "found Frame End in data...escaping");
                parsedBytes.add(Command.ESCAPE);
                parsedBytes.add(Command.FRAME_END_ESCAPE);
            } else if (bytes[i] == Command.ESCAPE) {

                Log.d(Command.TAG, "found prepareForFrames in data...escaping");
                parsedBytes.add(Command.ESCAPE);
                parsedBytes.add(Command.ESCAPE_ESCAPE);
            } else {
                parsedBytes.add(bytes[i]);
            }
        }

        byte[] escapedBytes = new byte[parsedBytes.size() + 2];

        escapedBytes[0] = Command.FRAME_START;

        for (int i = 0; i < parsedBytes.size(); i++) {
            escapedBytes[i+1] = parsedBytes.get(i).byteValue();
        }
        escapedBytes[escapedBytes.length - 1] = Command.FRAME_END;

        return escapedBytes;
    }
}
