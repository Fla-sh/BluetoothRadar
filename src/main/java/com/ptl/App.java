package com.ptl;

import com.ptl.Scanner;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
/*
    Main class
 */

public class App 
{
    public static void main( String[] args ) {
        new Thread(new Scanner(), "ScannerThread").start();
        //while(true);
    }
}
