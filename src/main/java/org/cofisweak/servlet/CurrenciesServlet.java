package org.cofisweak.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.exception.DaoException;
import org.cofisweak.model.Currency;
import org.cofisweak.model.ExceptionResponse;
import org.cofisweak.service.CurrencyService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private static final CurrencyService currencyService = CurrencyService.getInstance();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            List<Currency> currencies = currencyService.getAllCurrencies();
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(currencies));
            }
        } catch (DaoException e) {
            ExceptionResponse exceptionResponse = new ExceptionResponse(e.getMessage());
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(exceptionResponse));
            }
            resp.setStatus(500);
        }
    }
}
