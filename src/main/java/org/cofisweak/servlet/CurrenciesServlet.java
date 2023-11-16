package org.cofisweak.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.exception.*;
import org.cofisweak.model.Currency;
import org.cofisweak.service.CurrencyService;
import org.cofisweak.util.ResponseBuilder;
import org.cofisweak.util.ValidatorManager;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private static final CurrencyService currencyService = CurrencyService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Currency> currencies = currencyService.getAllCurrencies();
            ResponseBuilder.writeResultToResponse(currencies, resp);
        } catch (DaoException e) {
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
            resp.setStatus(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");
        try {
            checkIsValidPostRequest(name, code, sign);
        } catch (MissingFieldException | InvalidCurrencyCodeException e) {
            resp.setStatus(400);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
            return;
        }

        try {
            Currency currency = currencyService.addNewCurrency(name, code, sign);
            ResponseBuilder.writeResultToResponse(currency, resp);
        } catch (CurrencyAlreadyExistsException e) {
            resp.setStatus(409);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        } catch (DaoException e) {
            resp.setStatus(500);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        }
    }

    private void checkIsValidPostRequest(String name, String code, String sign) throws MissingFieldException, InvalidCurrencyCodeException {
        ValidatorManager.checkIsFieldFilled(name, "Name");
        ValidatorManager.checkIsFieldFilled(code, "Code");
        ValidatorManager.checkIsFieldFilled(sign, "Sign");
        if (!ValidatorManager.isValidCurrencyCode(code)) {
            throw new InvalidCurrencyCodeException();
        }
    }
}
