package org.cofisweak.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.model.ExceptionResponse;

import java.io.IOException;
import java.io.PrintWriter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseBuilder {
    private static final GsonBuilder builder = new GsonBuilder();
    private static final Gson gson = builder.create();
    public static void writeErrorToResponse(String e, HttpServletResponse resp) throws IOException {
        ExceptionResponse exceptionResponse = new ExceptionResponse(e);
        writeResultToResponse(exceptionResponse, resp);
    }

    public static void writeResultToResponse(Object object, HttpServletResponse resp) throws IOException {
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(object));
        }
    }
}
