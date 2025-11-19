package com.dt176g.project.chat_engine;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import com.dt176g.project.responses.ActionHandler;
import com.dt176g.project.responses.ResponseGenerator;
import com.dt176g.project.util.ChatContext;
import com.dt176g.project.util.Constants;
import com.dt176g.project.util.ConversationState;
import com.dt176g.project.util.Pair;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * Engine module used to initialize the chat bot.
 * 
 * @author Johan Svärd
 */
public final class ChatEngine {

        private ChatEngine() {
                throw new IllegalStateException(Constants.CLASS_INSTANTATION_ERROR_MSG);
        }

        /**
         * Initializes the chat engine by passing in the required functions and database
         * file path.
         * 
         * @param input      the input function that supplies an Flowable of strings
         * @param output     the output function that accepts a Pair of String and
         *                   ChatContext
         * @param dbFilePath the file path to the database to be used for responses
         */
        public static void initializeEngine(final Supplier<Flowable<String>> input,
                        final Consumer<Pair<String, ChatContext>> output,
                        final String dbFilePath) {

                final ConnectableFlowable<String> publishedInput = input.get().publish();
                final PublishSubject<String> outputSubject = PublishSubject.create();
                final BehaviorSubject<ConversationState> conversationStateSubject = BehaviorSubject.create();
                final BehaviorSubject<Boolean> exitSubject = BehaviorSubject.create();

                outputSubject
                                .observeOn(Schedulers.io())
                                .withLatestFrom(ActionHandler.getChatContextObservable(publishedInput),
                                                (response, chatContext) -> new Pair<String, ChatContext>(response,
                                                                chatContext))
                                .observeOn(Schedulers.computation())
                                .buffer(publishedInput.debounce(1500, TimeUnit.MILLISECONDS).toObservable())
                                .flatMapIterable(pair -> pair)
                                .observeOn(Schedulers.io())
                                .doOnError(error -> output.accept(new Pair<String, ChatContext>(
                                                Constants.ERROR_MESSAGES.get(
                                                                new Random().nextInt(Constants.ERROR_MESSAGES.size())),
                                                new ChatContext(false, "Error"))))
                                .subscribe(pair -> output.accept(pair));

                publishedInput
                                .observeOn(Schedulers.io())
                                .flatMapMaybe(inputString -> ResponseGenerator.generateResponse(inputString,
                                                conversationStateSubject, exitSubject,
                                                new Random().nextInt(0, 500), dbFilePath))
                                .toObservable()
                                .onErrorReturnItem(Constants.ERROR_MESSAGES
                                                .get(new Random().nextInt(Constants.ERROR_MESSAGES.size())))
                                .subscribe(outputSubject);
                publishedInput
                                .observeOn(Schedulers.io())
                                .flatMapMaybe(inputString -> ActionHandler.generateActionResponse(inputString,
                                                new Random().nextInt(0, 500), dbFilePath))
                                .toObservable()
                                .onErrorReturnItem(Constants.ERROR_MESSAGES
                                                .get(new Random().nextInt(Constants.ERROR_MESSAGES.size())))
                                .subscribe(outputSubject);

                publishedInput.connect();

                outputSubject.onNext(Constants.WELCOME_MESSAGE);

                // Necesarry to keep CLI alive.
                while (true) {
                        exitSubject.subscribe(bool -> {
                                Thread.sleep(2000);
                                System.exit(0);
                        });
                }
        }

}
