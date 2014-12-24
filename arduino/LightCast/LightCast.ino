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
#define COMMAND_COLOR_TRANSITION  0x04

#define MAX_COMMANDS        50
#define MAX_COMMAND_BUFFER  7

#define DEBUG  1

#define LOG_INFO(msg)              Serial.print(F("I: ")); Serial.println(F(msg))
#define LOG_ERROR(msg)              Serial.print(F("E: ")); Serial.println(F(msg))
#define LOG_ERROR_CODE(msg,errCode) Serial.print(F("E: ")); Serial.print(F(msg)); Serial.println(errCode,HEX)
#if DEBUG
  #define LOG_DEBUG(msg)            Serial.print(F("D: ")); Serial.println(F(msg))
#else
  #define LOG_DEBUG(msg)
#endif

boolean readingFrameData = false;
boolean inFrameEscape = false;
int frameBufferCount = 0;
int frameBuffer[MAX_COMMAND_BUFFER];
boolean inProgramming = false;
int programSteps[MAX_COMMANDS][MAX_COMMAND_BUFFER];
int programStepCount = 0;
int programCounter = 0;
Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUMPIXELS, NEOPIXELS_PIN, NEO_GRB + NEO_KHZ800);
Adafruit_BLE_UART BTLEserial = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);
aci_evt_opcode_t bleLastStatus = ACI_EVT_DISCONNECTED;

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
}

boolean addProgramStepFromFrameBuffer(int cmd, int dataLen) {
  // sanity check
  if (cmd != frameBuffer[0]) {
    LOG_ERROR_CODE("inconsistency add step error, expected cmd: ", cmd);
    LOG_ERROR_CODE("got: ", frameBuffer[0]);
    return false;
  }

  if (programStepCount >= MAX_COMMANDS) {
    LOG_ERROR("programming steps memory is full");
    return false;
  }

  // copy the command and the data
  for (int i = 0; i < dataLen + 1; i++) {
    programSteps[programStepCount][i] = frameBuffer[i];
  }

  // clear out the remaining buffer
  for (int i = dataLen + 1; i < MAX_COMMAND_BUFFER; i++) {
    programSteps[programStepCount][i] = 0;
  }
  
  programStepCount++;
  
  return true;
}

boolean assertCommandLen(int cmd, int expected, int actual) {
  if (expected != actual) {
    LOG_ERROR_CODE("invalid data for command: ", cmd);
    LOG_ERROR_CODE("expected: ", expected);
    LOG_ERROR_CODE("got: ", actual);
    return false;
  }
  return true;
}

boolean processDelayFrame(int dataLen) {
  LOG_DEBUG("processDelayFrame");

  // delayInMillis - 2-bytes (big endian)
  if (assertCommandLen(COMMAND_DELAY, 2, dataLen) == false) {
    return false;
  }
  
  return addProgramStepFromFrameBuffer(COMMAND_DELAY, dataLen);
}

boolean processSetAnimationFrame(int dataLen) {
  LOG_DEBUG("cmdSetAnimation");
  
  // animationType - 1-byte
  // cycleTimeInMillis - 2-bytes (big endian)
  // numberOfCycles - 1-byte
  if (assertCommandLen(COMMAND_SET_ANIMATION, 4, dataLen) == false) {
    return false;
  }

  return addProgramStepFromFrameBuffer(COMMAND_SET_ANIMATION, dataLen);
}

boolean processColorTransitionFrame(int dataLen) {
  LOG_DEBUG("cmdColorTransition");

  // redValue - 1-byte
  // greenValue - 1 byte
  // blueValue - 1 byte
  // brightness - 1 byte
  // transitionTimeInMillis - 2-bytes (big endian)
  if (assertCommandLen(COMMAND_COLOR_TRANSITION, 6, dataLen) == false) {
    return false;
  }
  return addProgramStepFromFrameBuffer(COMMAND_COLOR_TRANSITION, dataLen);
}

// TODO clear out old bytes from the programming buffer

void processFrame() {
  if (frameBufferCount == 0) {
    return;
  }
  int command = frameBuffer[0];
  int commandDataLen = frameBufferCount - 1;
  
  if (command == COMMAND_START_PROGRAMMING) {
      LOG_DEBUG("COMMAND_START_PROGRAMMING");
      programStepCount = 0;
      programCounter = 0;
      inProgramming = true;
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
      saveCommand = processDelayFrame(commandDataLen);
      break;
    case COMMAND_SET_ANIMATION:
      LOG_DEBUG("COMMAND_SET_ANIMATION");
      saveCommand = processSetAnimationFrame(commandDataLen);
      break;
    case COMMAND_COLOR_TRANSITION:
      LOG_DEBUG("COMMAND_COLOR_TRANSITION");
      saveCommand = processColorTransitionFrame(commandDataLen);
      break;
    default:
      LOG_ERROR_CODE("unknown command:",frameBuffer[0]);
  }
  
  if (saveCommand) {
    
  }
}

void resetFrame() {
  LOG_DEBUG("resetFrame()");
  readingFrameData = false;
  inFrameEscape = false;
  frameBufferCount = 0;
}

void appendToFrameBuffer(int dataByte) {
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

void runCommandDelay() {
  long timeInMillis = (programSteps[programCounter][1] << 8) | programSteps[programCounter][2];
  delay(timeInMillis);
}

void runCommandSetAnimation() {
}

int currentRedValue = 0;
int currentGreenValue = 0;
int currentBlueValue = 0;
int currentBrightness = 100;

void runCommandColorTransition() {
  // redValue - 1-byte
  // greenValue - 1 byte
  // blueValue - 1 byte
  // brightness - 1 byte
  // transitionTimeInMillis - 2-bytes (big endian)

  for(int i=0;i<NUMPIXELS;i++){
    // pixels.Color takes RGB values, from 0,0,0 up to 255,255,255
    pixels.setPixelColor(i, pixels.Color(programSteps[programCounter][1],programSteps[programCounter][2],programSteps[programCounter][3]));
    pixels.setBrightness(programSteps[programCounter][4]);
    pixels.show(); // This sends the updated pixel color to the hardware.
//    delay(delayval); // Delay for a period of time (in milliseconds).
  }

//  long currentTime = 

  
//  long timeInMillis = (programSteps[programCounter][5] << 8) | programSteps[programCounter][6];

}

void programLoop() {
  // skip if current in programming or there are no steps
  if (inProgramming == true || programStepCount == 0) {
    return;
  }

  int cmd = programSteps[programCounter][0];
  switch (cmd) {
    case COMMAND_DELAY:
      LOG_DEBUG("running COMMAND_DELAY");
      runCommandDelay();
      break;
    case COMMAND_SET_ANIMATION:
      LOG_DEBUG("running COMMAND_SET_ANIMATION");
      break;
    case COMMAND_COLOR_TRANSITION:
      LOG_DEBUG("running COMMAND_COLOR_TRANSITION");
      runCommandColorTransition();
      break;
    default:
      LOG_ERROR_CODE("command handler missing: ", cmd);
  }
  
  programCounter++;

  // if overflowed, jump back to the beginning
  if (programCounter >= programStepCount) {
    programCounter = 0;
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

  int byteIn;
  
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
      processFrame();
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


