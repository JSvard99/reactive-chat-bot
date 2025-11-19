package com.dt176g.project.responses;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.dt176g.project.util.ChatContext;
import com.dt176g.project.util.Constants;
import com.dt176g.project.util.JsonUtil;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Module for handling the actions that can be taken by the chatbot, which are
 * shout/no shout and name change.
 * 
 * @author Johan Svärd
 */
public final class ActionHandler {

    private ActionHandler() {
        throw new IllegalStateException(Constants.CLASS_INSTANTATION_ERROR_MSG);
    }

    /**
     * Generates a response to an input if an action was requested by the bot.
     * 
     * @param input      the input string
     * @param seed       the seed to choose the response
     * @param dbFilePath the path to the database to be used
     * 
     * @return Maybe<String> of the response, is empty if no action was requested.
     */
    public static Maybe<String> generateActionResponse(final String input, final int seed, final String dbFilePath) {
        return Observable.fromIterable(getActionRegexMatches(input))
                .flatMapMaybe(string -> JsonUtil.getResponse(Single.just(string), seed, dbFilePath))
                .reduce((first, second) -> first + "\n\t" + second)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Gets an observable of the chat contexts that emits a new updated on on each
     * action requested by the user.
     * 
     * @param input the input that this observable "listens" on for requested
     *              actions
     * @return the observable of chat contexts
     */
    public static Observable<ChatContext> getChatContextObservable(final Flowable<String> input) {
        return input
                .observeOn(Schedulers.io())
                .flatMapIterable(string -> getActionRegexMatches(string))
                .observeOn(Schedulers.computation())
                .filter(string -> !string.equals("nameChange"))
                .scan(new ChatContext(false, Constants.DEFAULT_BOT_NAME), (chatContext, string) -> {
                    if (string.equals("capsTrue"))
                        return new ChatContext(true, chatContext.botName());
                    else if (string.equals("capsFalse"))
                        return new ChatContext(false, chatContext.botName());
                    else
                        return new ChatContext(chatContext.isCaps(), string);
                })
                .toObservable();
    }

    private static List<String> getActionRegexMatches(final String input) {
        return Constants.REGEX_ACTION_PATTERNS.keySet().stream()
                .filter(pattern -> pattern.matcher(input.toLowerCase()).matches())
                .map(pattern -> {
                    if (Constants.REGEX_ACTION_PATTERNS.get(pattern).equals("nameChange")) {
                        return (String) Stream.of(
                                input.toLowerCase().replaceAll(pattern.toString().substring(0, pattern.toString().length() - 1), "")
                                        .split(""))
                                .takeWhile(letter -> !letter.matches("[.!-,|/\s]"))
                                .collect(Collectors.joining());
                    }
                    return Constants.REGEX_ACTION_PATTERNS.get(pattern);
                })
                .flatMap(match -> {
                    if (!match.equals("capsTrue") && !match.equals("capsFalse")) {
                        return Stream.of(match, "nameChange");
                    }
                    return Stream.of(match);
                })
                .collect(Collectors.toList());
    }
}
