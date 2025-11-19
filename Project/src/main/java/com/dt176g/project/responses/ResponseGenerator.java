package com.dt176g.project.responses;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.dt176g.project.util.Constants;
import com.dt176g.project.util.JsonUtil;
import com.dt176g.project.util.ConversationState;
import com.dt176g.project.util.Pair;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Module for generating responses based on the users input to the chatbot.
 * 
 * @author Johan Svärd
 */
public final class ResponseGenerator {

    private ResponseGenerator() {
        throw new IllegalStateException(Constants.CLASS_INSTANTATION_ERROR_MSG);
    }

    /**
     * Generates a response based on the input from the user.
     * 
     * @param input             the input string
     * @param conversationState the subject consisting of the conversation state
     * @param exitSubject       the subject that indicates program exit
     * @param seed              the seed to choose the response
     * @param filePath          the file path to the database to be used
     * @return the response to the input
     */
    public static Maybe<String> generateResponse(final String input,
            final BehaviorSubject<ConversationState> conversationState,
            final BehaviorSubject<Boolean> exitSubject, final int seed, final String filePath) {

        return Maybe.defer(() -> {
            if (conversationState.getValue() != null && conversationState.getValue().conversationState() != null) {
                return getStateConversationResponse(input, conversationState, filePath);
            }
            return Observable.fromIterable(getRegexMatchesValues(input))
                    .doOnNext(string -> {
                        if (string == Constants.STATE_ORDER_INITIAL) {
                            conversationState
                                    .onNext(new ConversationState(Constants.STATE_ORDER_GET_TYPE, null, null, null));
                        } else if (string == Constants.STATE_CALC_INITIAL) {
                            conversationState
                                    .onNext(new ConversationState(Constants.STATE_CALC_GET_FIRST, null, null, null));
                        } else if (string == "farewell") {
                            exitSubject.onNext(true);
                        }
                    })
                    .zipWith(Observable.<Integer, Integer>generate(() -> seed, (seed1, emitter) -> {
                        emitter.onNext(seed1);
                        return ++seed1;
                    }).subscribeOn(Schedulers.computation()),
                            (string, seed1) -> new Pair<Integer, String>(seed1, string))
                    .flatMapMaybe(pair -> JsonUtil.getResponse(Single.just(pair.second()), pair.first(), filePath))
                    .observeOn(Schedulers.computation())
                    .filter(string -> !string.isEmpty())
                    .reduce((first, second) -> first + "\n\t" + second);
        })
                .subscribeOn(Schedulers.io());
    }

    private static List<String> getRegexMatchesValues(final String input) {
        return Constants.REGEX_INPUT_PATTERNS.keySet().stream()
                .filter(pattern -> pattern.matcher(input.toLowerCase()).matches())
                .map(pattern -> Constants.REGEX_INPUT_PATTERNS.get(pattern))
                .collect(Collectors.collectingAndThen(Collectors.toList(), matchesList -> {
                    if (matchesList.isEmpty() && !input.isBlank()
                            && Constants.REGEX_ACTION_PATTERNS.keySet().stream()
                                    .filter(pattern -> pattern.matcher(input.toLowerCase()).matches()).count() == 0) {
                        return Arrays.asList("noResponse");
                    }
                    return matchesList;
                }));
    }

    private static Maybe<String> getStateConversationResponse(final String input,
            final BehaviorSubject<ConversationState> conversationState, final String filePath) {

        return Maybe.just(input)
                .flatMap(input1 -> getStateInput(conversationState.getValue().conversationState(), input1))
                .switchIfEmpty(Maybe.just(Constants.STATE_INVALID))
                .flatMap(input1 -> {
                    if (input1.equals(Constants.STATE_INVALID)) {
                        return JsonUtil.getResponse(Single.just(input1), 0, filePath);
                    }
                    updateStateSubject(conversationState, input1);
                    return JsonUtil.getResponse(Single.just(conversationState.getValue().conversationState()), 0,
                            filePath);
                })
                .flatMap(response -> {
                    if (conversationState.getValue().conversationState() == Constants.STATE_ORDER_DONE) {
                        conversationState.onNext(new ConversationState(null, conversationState.getValue().first(),
                                conversationState.getValue().second(), conversationState.getValue().third()));

                        return Maybe.just(response +
                                "\nType - " + conversationState.getValue().first() +
                                "\nColor - " + conversationState.getValue().second() +
                                "\nSize - " + conversationState.getValue().third());
                    } else if (conversationState.getValue().conversationState() == Constants.STATE_CALC_DONE) {
                        conversationState.onNext(new ConversationState(null, conversationState.getValue().first(),
                                conversationState.getValue().second(), conversationState.getValue().third()));

                        return Maybe.just(conversationState.getValue().third())
                                .map(operator -> performCalculation(operator, conversationState.getValue().first(),
                                        conversationState.getValue().second()))
                                .map(result -> {
                                    return response +
                                            "\nFirst number - " + conversationState.getValue().first() +
                                            "\nSecond number - " + conversationState.getValue().second() +
                                            "\nOperator - " + conversationState.getValue().third() +
                                            "\nResult: " + result;
                                });
                    } else if (input.toLowerCase().contains("cancel")) {
                        conversationState.onNext(new ConversationState(null, null, null, null));
                        return Maybe.just("Okay, cancelling...");
                    }
                    return Maybe.just(response);
                });

    }

    // Helper methods for getStateConversationResponse

    private static Maybe<String> getStateInput(final String conversationState, final String input) {
        return Observable.just(conversationState)
                .flatMap(state -> {
                    switch (state) {
                        case Constants.STATE_ORDER_GET_TYPE:
                            return Observable.fromArray(Constants.ORDER_TYPES);
                        case Constants.STATE_ORDER_GET_COLOR:
                            return Observable.fromArray(Constants.ORDER_COLORS);
                        case Constants.STATE_ORDER_GET_SIZE:
                            return Observable.fromArray(Constants.ORDER_SIZES);
                        case Constants.STATE_CALC_GET_OPERATOR:
                            return Observable.fromArray(Constants.CALCULATION_OPERATORS);
                        default:
                            return Observable.fromArray(input.split(" "));
                    }
                })
                .filter(string -> {
                    if (conversationState == Constants.STATE_CALC_GET_FIRST
                            || conversationState == Constants.STATE_CALC_GET_SECOND) {
                        return string.matches("([1-9]|[1-9][0-9]|[1-9][0-9][0-9])");
                    }
                    return input.toLowerCase().contains(string);
                })
                .firstElement();
    }

    private static Double performCalculation(final String operator, final String first, final String second) {
        switch (operator) {
            case "add":
                return Double.valueOf(first)
                        + Double.valueOf(second);
            case "subtract":
                return Double.valueOf(first)
                        - Double.valueOf(second);
            case "multiply":
                return Double.valueOf(first)
                        * Double.valueOf(second);
            default: // divide
                return Double.valueOf(first)
                        / Double.valueOf(second);
        }
    }

    private static void updateStateSubject(final BehaviorSubject<ConversationState> conversationState,
            final String input) {
        switch (conversationState.getValue().conversationState()) {
            case Constants.STATE_ORDER_GET_TYPE:
                conversationState
                        .onNext(new ConversationState(Constants.STATE_ORDER_GET_COLOR, input, null, null));
                break;

            case Constants.STATE_ORDER_GET_COLOR:
                conversationState.onNext(new ConversationState(Constants.STATE_ORDER_GET_SIZE,
                        conversationState.getValue().first(), input, null));
                break;

            case Constants.STATE_ORDER_GET_SIZE:
                conversationState
                        .onNext(new ConversationState(Constants.STATE_ORDER_DONE,
                                conversationState.getValue().first(),
                                conversationState.getValue().second(), input));
                break;
            case Constants.STATE_CALC_GET_FIRST:
                conversationState
                        .onNext(new ConversationState(Constants.STATE_CALC_GET_SECOND, input, null, null));
                break;

            case Constants.STATE_CALC_GET_SECOND:
                conversationState.onNext(new ConversationState(Constants.STATE_CALC_GET_OPERATOR,
                        conversationState.getValue().first(), input, null));
                break;

            case Constants.STATE_CALC_GET_OPERATOR:
                conversationState.onNext(
                        new ConversationState(Constants.STATE_CALC_DONE,
                                conversationState.getValue().first(),
                                conversationState.getValue().second(), input));
                break;
        }
    }
}
