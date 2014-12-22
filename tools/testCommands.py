#! /usr/bin/env python

import serial
import fnmatch
import os
import sys
import threading
import struct
import time
from array import array

def print_error(msg):
    sys.stderr.write("%s\n" % msg)

def print_serial_in(data):
    sys.stdout.write("<-- [%s] - %s\n" % (",".join("{:02X}".format(x) for x in array('B', data)), data))

def print_serial_out(data):
    sys.stdout.write("--> [%s]\n" % ",".join("{:02X}".format(x) for x in data))

def int_to_big_endian(val):
    packed = struct.pack('>H', val)
    return [ord(packed[0]),ord(packed[1])]

class BleClient(object):
    def __init__(self):
        self.thread = None
        self.ser = serial.Serial()
        self.ser.timeout = .5
        self.open_serial()

    def get_serial_port_name(self):
        for file in os.listdir('/dev'):
            if fnmatch.fnmatch(file, 'tty.usbmodem*'):
                return '%s%s' % ('/dev/', file)
        return None

    def is_serial_open(self):
        return self.ser.isOpen()

    def open_serial(self, baudrate=9600):
        if not self.is_serial_open():
            try:
                self.ser.baudrate = baudrate
                self.ser.port = self.get_serial_port_name()
                self.ser.open()
            except OSError as ose:
                print_error("Opening serial port error%s: %s" % (self.ser.port, ose))

            if self.is_serial_open():
                self.thread = threading.Thread(target=self.read_from_serial)
                self.thread.daemon = True
                self.thread.start()
        return self.is_serial_open()
                    
    def close_serial(self):
        if self.is_serial_open():
            self.ser.close()
            self.ser = None
            if self.thread:
                self.thread.join()

    def escape_byte(self, byte):
        if byte == 0x7E:
            return [0x7D, 0x5E]
        if byte == 0x7C:
            return [0x7D, 0x5C]
        if byte == 0x7D:
            return [0x7D, 0x5D]
        return [byte]

    def ser_write(self, data):
        print_serial_out(data)
        self.ser.write(data)

    def append_escaped_data(self, dataList, arg):
        param_type = type(arg)
        if (param_type == int):
            escaped_data = self.escape_byte(arg & 0xFF)
            for data_item in escaped_data:
                dataList.append(data_item)
            return
        elif param_type == list:
            for argItem in arg:
                self.append_escaped_data(dataList, argItem)
            return
        else:
            print_error('unsupported type: %s' % param_type)
    
        
    def send_frame(self, command, *dataArgs):
        if not self.is_serial_open():
            return

        try:
            frame = []
            frame.append(0x7E)
            for b in self.escape_byte(command):
                frame.append(b)

            for data in dataArgs:
                self.append_escaped_data(frame, data)

            frame.append(0x7C)
            
            self.ser_write(frame)
        except serial.serialutil.SerialException as se:
            print_error("error writing to serial port %s: %s" % (self.ser.port, se))
            self.close_serial()

    def read_from_serial(self):
        try:
            while self.ser:
                buf = self.ser.read(16)
                if len(buf) > 0:
                    print_serial_in(buf)
        except serial.serialutil.SerialException as se:
            print_error("error reading from serial port %s: %s" % (self.ser.port, se))

    def cmd_start_programming(self):
        self.send_frame(0x00)

    def cmd_end_programming(self):
        self.send_frame(0x01)

    def cmd_delay(self, delayInMillis):
        self.send_frame(0x02, int_to_big_endian(delayInMillis))

    def cmd_set_animation(self, animationType, cycleTimeInMillis, numberOfCycles):
        self.send_frame(0x03, animationType, int_to_big_endian(cycleTimeInMillis), numberOfCycles)
        
    def cmd_color_transition(self, redValue, greenValue, blueValue, brightness, transitionTimeInMillis):
        self.send_frame(0x04, redValue, greenValue, blueValue, brightness, int_to_big_endian(transitionTimeInMillis))
        
    
def main():
    client = BleClient()
    client.cmd_start_programming()
    client.cmd_delay(1000)
    client.cmd_set_animation(1, 100, 3)
    client.cmd_color_transition(0, 0, 0, 100, 1000)
    client.cmd_color_transition(255, 255, 255, 100, 1000)
    client.cmd_end_programming()
    time.sleep(100000)
    client.close_serial()

if __name__ == "__main__":
    main()
    