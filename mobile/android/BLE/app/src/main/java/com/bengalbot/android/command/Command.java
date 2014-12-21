package com.bengalbot.android.command;

import java.util.Map;

/**
 */
public interface Command {

    /*
    00 startProgramming
    FF endProgramming
    01 Delay
    02 Animation
    03 Color
    04 Brigthness
     */

   public static final int START_PROGRAMMING_COMMAND_CODE       =       0x00;
   public static final int END_PROGRAMMING_COMMAND_CODE         =       0xFF;
   public static final int DELAY_COMMAND_CODE                   =       0x01;
   public static final int ANIMATION_PROGRAMMING_COMMAND_CODE   =       0x02;
   public static final int COLOR_PROGRAMMING_COMMAND_CODE       =       0x03;
   public static final int BRIGHTNESS_PROGRAMMING_COMMAND_CODE  =       0x04;

   public void create(Map<com.bengalbot.android.command.Properties, Object> properties);
   public byte[] parse();
   public void send();
   public String getName();
}
