package com.bengalbot.lightcast.command;

import java.util.Map;

/**
 */
public interface Command {

    public final static String TAG = "Command";

    /*
    00 startProgramming
    FF endProgramming
    01 Delay
    02 Animation
    03 Color
    04 Brigthness
     */

   public static final byte FRAME_START                          =       0x7E;
   public static final byte FRAME_END                            =       0x7C;
   public static final byte ESCAPE                               =      0x7D;
   public static final byte START_PROGRAMMING_COMMAND_CODE       =       0x00;
   public static final int END_PROGRAMMING_COMMAND_CODE          =       0x01;
   public static final byte DELAY_COMMAND_CODE                   =       0x02;
   public static final byte ANIMATION_PROGRAMMING_COMMAND_CODE   =       0x03;
   public static final byte COLOR_PROGRAMMING_COMMAND_CODE       =       0x04;
   public static final byte BRIGHTNESS_PROGRAMMING_COMMAND_CODE  =       0x05;

   public static final byte FRAME_START_ESCAPE                   =       0x5E;
   public static final byte FRAME_END_ESCAPE                     =       0x5C;
   public static final byte ESCAPE_ESCAPE                        =       0x5D;

   public void create(Map<com.bengalbot.lightcast.command.Properties, Object> properties);
   public byte[] parse();
   public void send();
   public String getName();
}
