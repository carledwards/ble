#include <Adafruit_NeoPixel.h>
#include <SPI.h>
#include "Adafruit_BLE_UART.h"

// Connect CLK/MISO/MOSI to hardware SPI
// e.g. On UNO & compatible: CLK = 13, MISO = 12, MOSI = 11
#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2     // This should be an interrupt pin, on Uno thats #2 or #3
#define ADAFRUITBLE_RST 9

#define NEOPIXELS_PIN  6
#define NUMPIXELS      12


#define FRAME_START          0x7E
#define FRAME_END            0x7C
#define FRAME_ESCAPE         0x7D
#define FRAME_ESCAPE_START   0x5E
#define FRAME_ESCAPE_END     0x5C
#define FRAME_ESCAPE_ESCAPE  0x5D

#define COMMAND_START_PROGRAMMING 0x00
#define COMMAND_END_PROGRAMMING   0x01
#define COMMAND_DELAY             0x02
#define COMMAND_SET_ANIMATION     0x03
#define COMMAND_SET_RGB           0x04

#define MAX_COMMANDS        50
#define MAX_COMMAND_BUFFER  7

#define DEBUG  0

#define LOG_INFO(msg)               Serial.print(F("I: ")); Serial.println(F(msg))
#define LOG_ERROR(msg)              Serial.print(F("E: ")); Serial.println(F(msg))
#define LOG_ERROR_CODE(msg,errCode) Serial.print(F("E: ")); Serial.print(F(msg)); Serial.println(errCode,HEX)
#if DEBUG
  #define LOG_DEBUG(msg)            Serial.print(F("D: ")); Serial.println(F(msg))
#else
  #define LOG_DEBUG(msg)
#endif

byte STARTUP_PROGRAM[][MAX_COMMAND_BUFFER+1] = {
  {0x01, COMMAND_START_PROGRAMMING},
  {0x07, COMMAND_SET_RGB, 0xFF, 0xFF, 0xFF, 0x00, 0x0B, 0xD0},
  {0x03, COMMAND_DELAY, 0x07, 0xD0},
  {0x07, COMMAND_SET_RGB, 0xFF, 0xFF, 0xFF, 0x30, 0x07, 0xD0},
  {0x01, COMMAND_END_PROGRAMMING},
  {0x00}
};

boolean readingFrameData = false;
boolean inFrameEscape = false;
byte frameBufferCount = 0;
byte frameBuffer[MAX_COMMAND_BUFFER];
boolean inProgramming = false;
byte programSteps[MAX_COMMANDS][MAX_COMMAND_BUFFER];
byte programStepCount = 0;
byte programCounter = 0;
boolean inStepContinuation = false;
Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUMPIXELS, NEOPIXELS_PIN, NEO_GRB + NEO_KHZ800);
Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);
aci_evt_opcode_t bleLastStatus = ACI_EVT_DISCONNECTED;
unsigned long startTime;
byte currentRedValue = 0;
byte currentGreenValue = 0;
byte currentBlueValue = 0;
byte currentBrightness = 0x00;
byte startingRedValue = 0;
byte startingGreenValue = 0;
byte startingBlueValue = 0;
byte startingBrightness = 0;


boolean addProgramStep(byte cmd, byte commandData[], byte commandDataLength) {
  if (programStepCount >= MAX_COMMANDS) {
    LOG_ERROR("programming steps memory is full");
    return false;
  }

  // copy the command and the data
  programSteps[programStepCount][0] = cmd;
  for (byte i = 1; i < commandDataLength; i++) {
    programSteps[programStepCount][i] = commandData[i-1];
  }

  // clear out the remaining buffer
  for (byte i = commandDataLength; i < MAX_COMMAND_BUFFER; i++) {
    programSteps[programStepCount][i] = 0;
  }
  
  programStepCount++;
  
  return true;
}

void eraseProgram() {
  programStepCount = 0;
  programCounter = 0;
  inProgramming = true;
  inStepContinuation = false;
}

boolean assertCommandLen(byte cmd, byte expected, byte actual) {
  if (expected != actual) {
    LOG_ERROR_CODE("invalid data for command: ", cmd);
    LOG_ERROR_CODE("expected: ", expected);
    LOG_ERROR_CODE("got: ", actual);
    return false;
  }
  return true;
}

boolean processDelayFrame(byte commandData[], byte commandDataLength) {
  LOG_DEBUG("processDelayFrame");

  // delayInMillis - 2-bytes (big endian)
  if (assertCommandLen(COMMAND_DELAY, 2, commandDataLength) == false) {
    return false;
  }
  
  return addProgramStep(COMMAND_DELAY, commandData, commandDataLength);
}

boolean processSetAnimationFrame(byte commandData[], byte commandDataLength) {
  LOG_DEBUG("cmdSetAnimation");
  
  // animationType - 1-byte
  // cycleTimeInMillis - 2-bytes (big endian)
  // numberOfCycles - 1-byte
  if (assertCommandLen(COMMAND_SET_ANIMATION, 4, commandDataLength) == false) {
    return false;
  }

  return addProgramStep(COMMAND_SET_ANIMATION, commandData, commandDataLength);
}

boolean processSetRGBFrame(byte commandData[], byte commandDataLength) {
  LOG_DEBUG("cmdSetRGB");

  // redValue - 1-byte
  // greenValue - 1 byte
  // blueValue - 1 byte
  // brightness - 1 byte
  // transitionTimeInMillis - 2-bytes (big endian)
  if (assertCommandLen(COMMAND_SET_RGB, 6, commandDataLength) == false) {
    return false;
  }
  return addProgramStep(COMMAND_SET_RGB, commandData, commandDataLength);
}

// TODO clear out old bytes from the programming buffer

void processFrame(byte frameData[], byte frameDataLength) {
  if (frameDataLength == 0) {
    return;
  }
  byte command = frameData[0];
  
  if (command == COMMAND_START_PROGRAMMING) {
      LOG_DEBUG("COMMAND_START_PROGRAMMING");
      eraseProgram();
      return;
  }
  
  if (inProgramming == false) {
    LOG_ERROR_CODE("received command outside of programming: ", command);
    return;
  }
  
  boolean saveCommand = false;
  switch (command) {
    case COMMAND_END_PROGRAMMING:
      LOG_DEBUG("COMMAND_END_PROGRAMMING");
      inProgramming = false;
      break;
    case COMMAND_DELAY:
      LOG_DEBUG("COMMAND_DELAY");
      saveCommand = processDelayFrame(&frameData[1], frameDataLength-1);
      break;
    case COMMAND_SET_ANIMATION:
      LOG_DEBUG("COMMAND_SET_ANIMATION");
      saveCommand = processSetAnimationFrame(&frameData[1], frameDataLength-1);
      break;
    case COMMAND_SET_RGB:
      LOG_DEBUG("COMMAND_SET_RGB");
      saveCommand = processSetRGBFrame(&frameData[1], frameDataLength-1);
      break;
    default:
      LOG_ERROR_CODE("unknown command:",command);
  }
}

void resetFrame() {
  LOG_DEBUG("resetFrame()");
  readingFrameData = false;
  inFrameEscape = false;
  frameBufferCount = 0;
}

void appendToFrameBuffer(byte dataByte) {
  LOG_DEBUG("appendToFrameBuffer");

  if (frameBufferCount >= MAX_COMMAND_BUFFER) {
    LOG_ERROR_CODE("frame too large for command: ", frameBuffer[0]);
    resetFrame();
    return;
  }
  else {
    frameBuffer[frameBufferCount] = dataByte;
    frameBufferCount++;
  }
}

boolean runCommandDelay(boolean isContinuation) {
  unsigned long timeInMillis = (programSteps[programCounter][1] << 8) | programSteps[programCounter][2];
  if (isContinuation == false) {
    startTime = millis();
  }

  return (millis() - startTime > timeInMillis);
}

boolean runCommandSetAnimation(boolean isContinuation) {
  return true;
}

void setPixelColorAndBrightness(byte red, byte green, byte blue, byte brightness) {
  for(int i=0;i<NUMPIXELS;i++){
    pixels.setPixelColor(i, pixels.Color(red, green, blue));
    pixels.setBrightness(brightness);
  }
  pixels.show();
  currentRedValue = red;
  currentGreenValue = green;
  currentBlueValue = blue;
  currentBrightness = brightness;
}

boolean runCommandSetRGB(boolean isContinuation) {
  // redValue - 1-byte
  // greenValue - 1 byte
  // blueValue - 1 byte
  // brightness - 1 byte
  // transitionTimeInMillis - 2-bytes (big endian)

  if (isContinuation == false) {
    startTime = millis();
  }

  byte targetRedValue = programSteps[programCounter][1];
  byte targetGreenValue = programSteps[programCounter][2];
  byte targetBlueValue = programSteps[programCounter][3];
  byte targetBrightness = programSteps[programCounter][4];
  unsigned long durationTimeInMillis = (programSteps[programCounter][5] << 8) | programSteps[programCounter][6];

  unsigned long currentTime = millis();

  if (currentTime-startTime > durationTimeInMillis) {
    setPixelColorAndBrightness(targetRedValue, targetGreenValue, targetBlueValue, targetBrightness);
    return true;
  }
 
  if (isContinuation == false) {
    // first time in continuation, save off the original values
    startingRedValue = currentRedValue;
    startingGreenValue = currentGreenValue;
    startingBlueValue = currentBlueValue;
    startingBrightness = currentBrightness;
  }

  // calculate next step value for each item based upon elapsed time so far
  float percentage = (currentTime-startTime)/float(durationTimeInMillis);
  byte redValue = 0;
  byte greenValue = 0;
  byte blueValue = 0;
  byte brightness = 0;
  if (startingRedValue > targetRedValue) {
    redValue = startingRedValue - ((startingRedValue-targetRedValue)*percentage);
  }
  else {
    redValue = startingRedValue + ((targetRedValue-startingRedValue)*percentage);
  }
  if (startingGreenValue > targetGreenValue) {
    greenValue = startingGreenValue - ((startingGreenValue-targetGreenValue)*percentage);
  }
  else {
    greenValue = startingGreenValue + ((targetGreenValue-startingGreenValue)*percentage);
  }
  if (startingBlueValue > targetBlueValue) {
    blueValue = startingBlueValue - ((startingBlueValue-targetBlueValue)*percentage);
  }
  else {
    blueValue = startingBlueValue + ((targetBlueValue-startingBlueValue)*percentage);
  }
  if (startingBrightness > targetBrightness) {
    brightness = startingBrightness - ((startingBrightness-targetBrightness)*percentage);
  }
  else {
    brightness = startingBrightness + ((targetBrightness-startingBrightness)*percentage);
  }
  
  setPixelColorAndBrightness(redValue, greenValue, blueValue, brightness);

  return false;
}

void programLoop() {
  // skip if current in programming or there are no steps
  if (inProgramming == true || programStepCount == 0) {
    return;
  }

  boolean stepCompleted = true;
  byte cmd = programSteps[programCounter][0];
  switch (cmd) {
    case COMMAND_DELAY:
      LOG_DEBUG("running COMMAND_DELAY");
      stepCompleted = runCommandDelay(inStepContinuation);
      break;
    case COMMAND_SET_ANIMATION:
      LOG_DEBUG("running COMMAND_SET_ANIMATION");
      break;
    case COMMAND_SET_RGB:
      LOG_DEBUG("running COMMAND_SET_RGB");
      stepCompleted = runCommandSetRGB(inStepContinuation);
      break;
    default:
      LOG_ERROR_CODE("command handler missing: ", cmd);
  }

  if (stepCompleted) {
    inStepContinuation = false;
    programCounter++;
  
    // if overflowed, jump back to the beginning
    if (programCounter >= programStepCount) {
      programCounter = 0;
    }
  }
  else {
    inStepContinuation = true;
  }  
}

void setup()
{
  // start serial port at 9600 bps:
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect
  }
  BTLEserial.begin();
  pixels.begin();
  Serial.print(F("Light-Cast Ready"));

  for(int i=0; STARTUP_PROGRAM[i][0]!=0x00; i++){
    processFrame(&STARTUP_PROGRAM[i][1], STARTUP_PROGRAM[i][0]);
  }
}

void loop()
{
  // process the program steps
  programLoop();

  BTLEserial.pollACI();

  // Ask what is our current status
  aci_evt_opcode_t status = BTLEserial.getState();
  // If the status changed....
  if (status != bleLastStatus) {
    // print it out!
    if (status == ACI_EVT_DEVICE_STARTED) {
        LOG_INFO("BLE: Advertising started");
    }
    if (status == ACI_EVT_CONNECTED) {
        LOG_INFO("BLE: Connected");
    }
    if (status == ACI_EVT_DISCONNECTED) {
        LOG_INFO("BLE: Disconnected or advertising timed out");
    }
    // OK set the last status change to this one
    bleLastStatus = status;
  }

  byte byteIn;
  
  if (BTLEserial.available()) {
    byteIn = BTLEserial.read();
  }
  else if (Serial.available()) {
    byteIn = Serial.read();
  }
  else {
    return;
  }
  
  if (byteIn == FRAME_START) {
    LOG_DEBUG("got FRAME_START");
    // force frame start
    resetFrame();
    readingFrameData = true;
    return;
  } else if (inFrameEscape == true) {
    LOG_DEBUG("got inFrameEscape");
    switch (byteIn) {
      case FRAME_ESCAPE_START:
        appendToFrameBuffer(FRAME_START);
        break;
      case FRAME_ESCAPE_END:
        appendToFrameBuffer(FRAME_END);
        break;
      case FRAME_ESCAPE_ESCAPE:
        appendToFrameBuffer(FRAME_ESCAPE);
        break;
      default:
        // bad escape frame type
        resetFrame();
        return;
      inFrameEscape = false;
      return;
    }
  } else if (readingFrameData == true) {
    if (byteIn == FRAME_END) {
      LOG_DEBUG("got FRAME_END");
      processFrame(frameBuffer, frameBufferCount);
      resetFrame();
      return;
    }
    else if (byteIn == FRAME_ESCAPE) {
      inFrameEscape = true;
      return;
    }
    else {
      appendToFrameBuffer(byteIn);
    }
  }
  else {
    // ignore, we are not in a frame and this is not a frame_start
  }
}


