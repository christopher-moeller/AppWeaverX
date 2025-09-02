package com.appweaverx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdLineHelper {

    private static final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private CmdLineHelper() {}

    public static String getUserInputAsString(String message, String defaultValue) {
        final String internalMessage = message + " (default: " + defaultValue + ")";
        final String userInput = getUserInputAsString(internalMessage);
        return userInput.isEmpty() ? defaultValue : userInput;
    }

    public static void printMessage(String message) {
        System.out.println(message);
    }

    public static String getUserInputAsString(String message) {
        printMessage(message);
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean getUserInputAsBoolean(String message, boolean defaultValue) {
        final String internalMessage = message + " (y/n)";
        String userInput = null;
        while (userInput == null  || (!userInput.equalsIgnoreCase("y") && !userInput.equalsIgnoreCase("n"))) {
            userInput = getUserInputAsString(internalMessage, defaultValue ? "y" : "n");
        }

        return userInput.equalsIgnoreCase("y");
    }

}
