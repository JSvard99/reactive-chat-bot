package com.dt176g.project.output;

import com.dt176g.project.util.ChatContext;
import com.dt176g.project.util.Constants;
import com.dt176g.project.util.Pair;

/**
 * Output module for cli output.
 * 
 * @author Johan Svärd
 */
public final class CliOutput {

    private CliOutput() {
        throw new IllegalStateException(Constants.CLASS_INSTANTATION_ERROR_MSG);
    }

    /**
     * Formats the input string based on the provided chatcontext and prints it to
     * the cli.
     * 
     * @param inputPair Pair consisting of the chat context and the string to be
     *                  printed
     */
    public static void printResponse(final Pair<String, ChatContext> inputPair) {
        if (inputPair.second().isCaps()) {
            System.out.println(inputPair.second().botName() + ": " + inputPair.first().toUpperCase());
        } else {
            System.out.println(inputPair.second().botName() + ": " + inputPair.first());
        }
    }
}
