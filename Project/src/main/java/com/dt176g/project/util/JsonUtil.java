package com.dt176g.project.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.json.JSONObject;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Utility module for getting a response from the JSON database.
 * 
 * @author Johan Svärd
 */
public final class JsonUtil {

    private JsonUtil() {
        throw new IllegalStateException(Constants.CLASS_INSTANTATION_ERROR_MSG);
    }

    /**
     * Gets a response from the database if one was found.
     * 
     * @param request    the request for the database
     * @param seed       the seed to choose which response to return
     * @param dbFilePath the filepath for the database to be used.
     * @return a Maybe<String> which is empty if no response was found or error
     *         occured.
     */
    public static Maybe<String> getResponse(final Single<String> request, final int seed, final String dbFilePath) {
        return request
                .map(string -> getResponses(dbFilePath).getJSONArray(string))
                .map(jsonArr -> jsonArr.getString(seed % jsonArr.toList().size()))
                .onErrorComplete();
    }

    private static JSONObject getResponses(final String filePath) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(JsonUtil.class.getClassLoader().getResourceAsStream(filePath)))) {
            return new JSONObject(reader.lines().collect(Collectors.joining())).getJSONObject("responses");
        } catch (IOException e) {
            throw new IOException();
        }
    }

}

