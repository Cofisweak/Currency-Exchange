package org.cofisweak.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.exception.*;
import org.cofisweak.model.Currency;
import org.cofisweak.service.CurrencyService;
import org.cofisweak.util.ResponseBuilder;
import org.cofisweak.util.Utils;

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
            Utils.processException(e, 500, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");
        try {
            checkIsValidPostRequest(name, code, sign);
            Currency currency = currencyService.addNewCurrency(name.trim(), code.trim(), sign.trim());
            ResponseBuilder.writeResultToResponse(currency, resp);
        } catch (MissingFieldException | InvalidCurrencyCodeException e) {
            Utils.processException(e, 400, resp);
        } catch (CurrencyAlreadyExistsException e) {
            Utils.processException(e, 409, resp);
        } catch (DaoException e) {
            Utils.processException(e, 500, resp);
        }
    }

    private void checkIsValidPostRequest(String name, String code, String sign) throws MissingFieldException, InvalidCurrencyCodeException {
        if (Utils.isFieldEmpty(name)) {
            throw new MissingFieldException("Name");
        }
        if (Utils.isFieldEmpty(code)) {
            throw new MissingFieldException("Code");
        }
        if (Utils.isFieldEmpty(sign)) {
            throw new MissingFieldException("Sign");
        }
        if (Utils.isInvalidCurrencyCode(code)) {
            throw new InvalidCurrencyCodeException();
        }
    }
}
