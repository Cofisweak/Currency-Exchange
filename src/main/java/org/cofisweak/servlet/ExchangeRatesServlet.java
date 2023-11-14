package org.cofisweak.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.dto.AddExchangeRateRequestDto;
import org.cofisweak.dto.ExchangeRateDto;
import org.cofisweak.exception.*;
import org.cofisweak.service.ExchangeRateService;
import org.cofisweak.util.ResponseBuilder;

import java.io.IOException;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private static final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AddExchangeRateRequestDto requestDto = new AddExchangeRateRequestDto(
                req.getParameter("baseCurrencyCode"),
                req.getParameter("targetCurrencyCode"),
                req.getParameter("rate"));
        try {
            ExchangeRateDto responseDto = exchangeRateService.addNewExchangeRate(requestDto);
            ResponseBuilder.writeResultToResponse(responseDto, resp);
        } catch (ExchangeRateAlreadyExistsException e) {
            resp.setStatus(409);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        } catch (MissingFieldException | CurrencyNotFoundException | InvalidCurrencyCodeException | AddExchangeRateException e) {
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
            List<ExchangeRateDto> rates = exchangeRateService.getAllExchangeRates();
            ResponseBuilder.writeResultToResponse(rates, resp);
        } catch (DaoException | InvalidCurrencyCodeException e) {
            resp.setStatus(500);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        }
    }
}
