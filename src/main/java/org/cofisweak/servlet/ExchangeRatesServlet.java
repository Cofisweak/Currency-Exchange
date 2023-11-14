package org.cofisweak.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.dto.AddExchangeRateRequestDto;
import org.cofisweak.dto.ExchangeRateDto;
import org.cofisweak.exception.*;
import org.cofisweak.service.ExchangeService;
import org.cofisweak.util.ResponseBuilder;

import java.io.IOException;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private static final ExchangeService exchangeService = ExchangeService.getInstance();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AddExchangeRateRequestDto requestDto = new AddExchangeRateRequestDto(
                req.getParameter("baseCurrencyCode"),
                req.getParameter("targetCurrencyCode"),
                req.getParameter("rate"));
        try {
            ExchangeRateDto responseDto = exchangeService.addNewExchangeRate(requestDto);
            ResponseBuilder.writeResultToResponse(responseDto, resp);
        } catch (ExchangeRateAlreadyExistsException e) {
            resp.setStatus(409);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        } catch (MissingFieldException | CurrencyNotFoundException | InvalidCurrencyCodeException |
                 IllegalRateException e) {
            resp.setStatus(400);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        } catch (DaoException e) {
            resp.setStatus(500);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateDto> rates = exchangeService.getAllExchangeRates();
            ResponseBuilder.writeResultToResponse(rates, resp);
        } catch (DaoException | InvalidCurrencyCodeException e) {
            resp.setStatus(500);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        }
    }
}
