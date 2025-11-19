package com.dt176g.project;

import javax.swing.SwingUtilities;

import com.dt176g.project.chat_engine.ChatEngine;
// import com.dt176g.project.input.CliInput;
// import com.dt176g.project.output.CliOutput;
import com.dt176g.project.view.ChatBotView;

/**
 * The main starting point for Project Assignment.
 * 
 * @author Erik Ström
 */
public final class Project {
    private Project() { // Utility classes should not have a public or default constructor
        throw new IllegalStateException("Utility class");
    }

    /**
     * Starting point for the chat bot. Contains both CLI implementation(commented
     * out here) and a GUI implementation.
     * 
     * @param args command arguments.
     * @throws InterruptedException
     */
    public static void main(final String... args) {
        /* CLI startup, needs commented out imports to compile. */
        // ChatEngine.initializeEngine(() -> CliInput.getCliInputFlowable(),
        // (response) -> CliOutput.printResponse(response),
        // "database.json");

        /* GUI startup */
        SwingUtilities.invokeLater(() -> new ChatBotView());
        ChatEngine.initializeEngine(() -> ChatBotView.getGuiInputFlowable(),
                (response) -> ChatBotView.printResponse(response),
                "database.json");
    }
}
