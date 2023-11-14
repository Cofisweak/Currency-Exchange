package org.cofisweak.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.dto.AddCurrencyDto;
import org.cofisweak.exception.*;
import org.cofisweak.model.Currency;
import org.cofisweak.service.CurrencyService;
import org.cofisweak.util.ResponseBuilder;

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
        AddCurrencyDto dto = new AddCurrencyDto(
                req.getParameter("name"),
                req.getParameter("code"),
                req.getParameter("sign"));
        try {
            Currency currency = currencyService.addNewCurrency(dto);
            ResponseBuilder.writeResultToResponse(currency, resp);
        } catch (CurrencyAlreadyExistsException e) {
            resp.setStatus(409);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        } catch (InvalidCurrencyCodeException | MissingFieldException e) {
            resp.setStatus(400);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        } catch (DaoException e) {
            resp.setStatus(500);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        }
    }
}
