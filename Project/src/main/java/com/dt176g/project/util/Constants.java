package com.dt176g.project.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Module containing constants for this project, includes bots default name,
 * error messages, regex pattern for responses and welcome message.
 * 
 * @author Johan Svärd
 */
public final class Constants {

        private Constants() {
                throw new IllegalStateException(Constants.CLASS_INSTANTATION_ERROR_MSG);
        }

        public static final String DEFAULT_BOT_NAME = "BOT";

        public static final String CLASS_INSTANTATION_ERROR_MSG = "Class should not be instantiated.";

        public static final String STATE_INVALID = "invalid";

        public static final String STATE_ORDER_INITIAL = "orderInitial";
        public static final String STATE_ORDER_GET_TYPE = "orderGetType";
        public static final String STATE_ORDER_GET_COLOR = "orderGetColor";
        public static final String STATE_ORDER_GET_SIZE = "orderGetSize";
        public static final String STATE_ORDER_DONE = "orderDone";

        public static final String STATE_CALC_INITIAL = "calcInitial";
        public static final String STATE_CALC_GET_FIRST = "calcGetFirst";
        public static final String STATE_CALC_GET_SECOND = "calcGetSecond";
        public static final String STATE_CALC_GET_OPERATOR = "calcGetOperator";
        public static final String STATE_CALC_DONE = "calcDone";

        public static final Map<Pattern, String> REGEX_INPUT_PATTERNS = Map.of(
                        Pattern.compile(".*(tell)\s*(me)*\s(a)\s*(joke).*"), "joke",
                        Pattern.compile("help"), "help",
                        Pattern.compile(".*(tell)\s*(me)*\s(a)\s*(fun)\s*(fact).*"), "funFact",
                        Pattern.compile("((hi)|(hello)|(greetings)).*"), "greeting",
                        Pattern.compile("((goodbye)|(farewell)|(bye)|(bye bye)).*"), "farewell",
                        Pattern.compile(".*(tell)\s*(me)*\s*(a)\s*(qoute).*"), "qoute",
                        Pattern.compile(".*(make)\s*(a)\s*(order).*"), STATE_ORDER_INITIAL,
                        Pattern.compile(".*(make)\s*(a)\s*(calculation).*"), STATE_CALC_INITIAL);

        public static final Map<Pattern, String> REGEX_ACTION_PATTERNS = Map.of(
                        Pattern.compile(".*(shout)\s*(at)\s*(me).*"), "capsTrue",
                        Pattern.compile(".*(stop)\s*(shouting).*"), "capsFalse",
                        Pattern.compile(".*(change)\s*(your)*\s*(name)\s*(to).*"), "nameChange");

        public static final String[] ORDER_TYPES = { "shirt", "pants", "hat" };

        public static final String[] ORDER_COLORS = { "green", "red", "blue" };

        public static final String[] ORDER_SIZES = { "small", "medium", "large" };

        public static final String[] CALCULATION_OPERATORS = { "add", "subtract", "multiply", "divide" };

        public static final List<String> ERROR_MESSAGES = Arrays.asList(
                        "Hmm... Something went wrong there. Try again.",
                        "It seems something went wrong here...",
                        "Something is not right here... Please try again.");

        public static final String WELCOME_MESSAGE = "Hello and welcome! Try typing something (Hint: Im fond of jokes, fun facts and qoutes). \nYou enter the input in the white box below, and then send it away by pressing the Send button or pressing enter. \nIf you need some pointers you can type \"help\" in the chat and I will type what I can do! \nIf you want to exit just type 'Goodbye' or some other farewell phrase and the chat will close.";
}
