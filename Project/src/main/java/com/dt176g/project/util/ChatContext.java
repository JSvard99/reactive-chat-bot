package com.dt176g.project.util;

/**
 * Holds the context parameters used by the chatbot, which are isCaps if the bot
 * should type in all caps and botName which is the current name of the bot.
 * 
 * @author Johan Svärd
 */
public record ChatContext(boolean isCaps, String botName) {

}
