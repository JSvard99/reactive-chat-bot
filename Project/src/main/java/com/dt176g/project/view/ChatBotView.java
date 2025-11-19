package com.dt176g.project.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import com.dt176g.project.util.ChatContext;
import com.dt176g.project.util.Pair;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * The view for the GUI implemenatation of the chatbot.
 * 
 * @author Johan Svärd
 */
public final class ChatBotView extends JFrame {

    private static final JPanel mainPanel = new JPanel(new BorderLayout());
    private static final JPanel inputPanel = new JPanel();
    private static final JButton sendButton = new JButton("Send");
    private static final JTextArea inputField = new JTextArea(3, 20);
    private static final JTextArea chatDisplay = new JTextArea();
    private static final JScrollPane chatDisplayScroll = new JScrollPane(chatDisplay);

    /**
     * Constructor for the ChatBotView which creates a window in where the user can
     * chat with a bot.
     */
    public ChatBotView() {
        setTitle("Chat bot");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatDisplay.setBackground(Color.GRAY);
        chatDisplay.setEditable(false);
        mainPanel.add(chatDisplayScroll, BorderLayout.CENTER);

        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.add(new JScrollPane(inputField));
        inputPanel.add(sendButton);

        inputField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    sendButton.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

        });

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    /**
     * Gets a flowable that emits string inputs from the user sent through the GUI.
     * 
     * @return the Flowable of input from the user.
     */
    public static Flowable<String> getGuiInputFlowable() {
        return Flowable.<Boolean>create(emitter -> {
            emitter.onNext(false);
            sendButton.addActionListener(event -> emitter.onNext(true));
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .retry()
                .map(bool -> {
                    return inputField.getText();
                })
                .doOnNext(text -> {
                    if (!text.isEmpty()) {
                        printResponse(new Pair<String, ChatContext>(text, new ChatContext(false, "You")));
                        inputField.setText(null);
                    }
                });
    }

    /**
     * Formats the input string based on the provided chatcontext and prints it to
     * the GUI.
     * 
     * @param inputPair Pair consisting of the chat context and the string to be
     *                  printed
     */
    public static void printResponse(final Pair<String, ChatContext> inputPair) {
        if (inputPair.second().isCaps()) {
            chatDisplay.append("\n" + inputPair.second().botName() + ": " + inputPair.first().toUpperCase());
        } else {
            chatDisplay.append("\n" + inputPair.second().botName() + ": " + inputPair.first());
        }
        SwingUtilities.invokeLater(() -> chatDisplayScroll.getVerticalScrollBar().setValue(Integer.MAX_VALUE));
    }
}
