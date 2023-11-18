package org.cofisweak.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.exception.CurrencyNotFoundException;
import org.cofisweak.exception.DaoException;
import org.cofisweak.exception.InvalidCurrencyCodeException;
import org.cofisweak.model.Currency;
import org.cofisweak.service.CurrencyService;
import org.cofisweak.util.ResponseBuilder;
import org.cofisweak.util.Utils;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private static final CurrencyService currencyService = CurrencyService.getInstance();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = Utils.extractCurrencyCodeFromUri(req.getRequestURI());
        try {
            validateGetRequest(code);
            Optional<Currency> currency = currencyService.getCurrencyByCode(code);
            if (currency.isEmpty()) {
                throw new CurrencyNotFoundException();
            }
            ResponseBuilder.writeResultToResponse(currency.get(), resp);
        } catch (InvalidCurrencyCodeException e) {
            Utils.processException(e, 400, resp);
        } catch (CurrencyNotFoundException e) {
            Utils.processException(e, 404, resp);
        } catch (DaoException e) {
            Utils.processException(e, 500, resp);
        }
    }

    private static void validateGetRequest(String code) throws InvalidCurrencyCodeException {
        if (Utils.isInvalidCurrencyCode(code)) {
            throw new InvalidCurrencyCodeException();
        }
    }
}
