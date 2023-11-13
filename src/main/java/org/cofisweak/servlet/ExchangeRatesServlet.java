package org.cofisweak.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.dto.AddCurrencyExchangeRequestDto;
import org.cofisweak.dto.ExchangeRateDto;
import org.cofisweak.exception.DaoException;
import org.cofisweak.exception.InvalidCurrencyCodeException;
import org.cofisweak.service.ExchangeRateService;
import org.cofisweak.util.ResponseBuilder;

import java.io.IOException;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private static final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AddCurrencyExchangeRequestDto requestDto = new AddCurrencyExchangeRequestDto(
                req.getParameter("baseCurrencyCode"),
                req.getParameter("targetCurrencyCode"),
                req.getParameter("code"));

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<ExchangeRateDto> rates = exchangeRateService.getAllExchangeRates();
            ResponseBuilder.writeResultToResponse(rates, resp);
        } catch (DaoException | InvalidCurrencyCodeException e) {
            resp.setStatus(500);
            ResponseBuilder.writeErrorToResponse(e.getMessage(), resp);
        }
    }
}
