package com.dt176g.project.input;

import java.util.Scanner;
import com.dt176g.project.util.Constants;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Input module for cli input.
 * 
 * @author Johan Svärd
 */
public final class CliInput {

    private CliInput() {
        throw new IllegalStateException(Constants.CLASS_INSTANTATION_ERROR_MSG);
    }

    /**
     * Gets a flowable that emits string inputs from the user typed in the cli.
     * 
     * @return the Flowable of input from the user.
     */
    public static Flowable<String> getCliInputFlowable() {
        final Scanner scanner = new Scanner(System.in);
        return Flowable.<String>create(emitter -> {
            emitter.onNext("");
            while (scanner.hasNext()) {
                emitter.onNext(scanner.nextLine());
            }
            scanner.close();
            emitter.onComplete();
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .retry();
    }
}
