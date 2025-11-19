package com.dt176g.project.util;

/**
 * Holds the state of the conversation if any and the necessary values of the
 * previous interactions of a conversation.
 * 
 * @author Johan Svärd
 */
public record ConversationState(String conversationState, String first, String second, String third) {

}
