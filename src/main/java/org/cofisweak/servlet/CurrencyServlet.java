package org.cofisweak.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.exception.DaoException;
import org.cofisweak.exception.InvalidCurrencyCodeException;
import org.cofisweak.model.Currency;
import org.cofisweak.service.CurrencyService;
import org.cofisweak.util.ResponseBuilder;
import org.cofisweak.util.ValidatorManager;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private static final CurrencyService currencyService = CurrencyService.getInstance();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURI = req.getRequestURI();
        String code = requestURI
                .substring(requestURI.lastIndexOf("/") + 1)
                .trim();
        try {
            validateGetRequest(code);
        } catch (InvalidCurrencyCodeException e) {
            resp.setStatus(400);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
            return;
        }

        try {
            Optional<Currency> currency = currencyService.getCurrencyByCode(code);
            if (currency.isPresent()) {
                ResponseBuilder.writeResultToResponse(currency.get(), resp);
            } else {
                resp.setStatus(404);
                ResponseBuilder.writeErrorToResponse("Currency code not founded", resp);
            }
        } catch (DaoException e) {
            resp.setStatus(500);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        }
    }

    private static void validateGetRequest(String code) throws InvalidCurrencyCodeException {
        if (ValidatorManager.isValidCurrencyCode(code)) {
            throw new InvalidCurrencyCodeException();
        }
    }
}
