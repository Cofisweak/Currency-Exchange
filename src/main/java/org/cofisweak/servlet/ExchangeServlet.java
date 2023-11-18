package org.cofisweak.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.dto.ExchangeRequestDto;
import org.cofisweak.dto.ExchangeResponseDto;
import org.cofisweak.exception.*;
import org.cofisweak.model.Currency;
import org.cofisweak.service.CurrencyService;
import org.cofisweak.service.ExchangeService;
import org.cofisweak.util.ResponseBuilder;
import org.cofisweak.util.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private static final ExchangeService exchangeService = ExchangeService.getInstance();
    private static final CurrencyService currencyService = CurrencyService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String fromCurrencyCode = req.getParameter("from");
        String toCurrencyCode = req.getParameter("to");
        String amountString = req.getParameter("amount");

        try {
            checkIsValidGetRequest(fromCurrencyCode, toCurrencyCode, amountString);
            ExchangeRequestDto requestDto = getExchangeRequestDto(fromCurrencyCode, toCurrencyCode, amountString);
            ExchangeResponseDto responseDto = exchangeService.exchange(requestDto.fromCurrency(), requestDto.toCurrency(), requestDto.amount());
            ResponseBuilder.writeResultToResponse(responseDto, resp);
        } catch (InvalidCurrencyCodeException | MissingFieldException | IllegalRateException e) {
            Utils.processException(e, 400, resp);
        } catch (ExchangeRateNotFoundException e) {
            Utils.processException(e, 404, resp);
        } catch (DaoException e) {
            Utils.processException(e, 500, resp);
        }
    }

    private static ExchangeRequestDto getExchangeRequestDto(String fromCurrencyCode, String toCurrencyCode, String amountString) throws ExchangeRateNotFoundException, IllegalRateException, DaoException {
        Optional<Currency> fromCurrencyOptional = currencyService.getCurrencyByCode(fromCurrencyCode);
        Optional<Currency> toCurrencyOptional = currencyService.getCurrencyByCode(toCurrencyCode);
        BigDecimal amount = Utils.parseRate(amountString);
        if (fromCurrencyOptional.isEmpty() || toCurrencyOptional.isEmpty()) {
            throw new ExchangeRateNotFoundException();
        }
        Currency fromCurrency = fromCurrencyOptional.get();
        Currency toCurrency = toCurrencyOptional.get();
        return new ExchangeRequestDto(amount, fromCurrency, toCurrency);
    }

    private static void checkIsValidGetRequest(String fromCurrencyCode, String toCurrencyCode, String amountString) throws InvalidCurrencyCodeException, MissingFieldException {
        if (Utils.isFieldEmpty(fromCurrencyCode)) {
            throw new MissingFieldException("From Currency Code");
        }
        if (Utils.isFieldEmpty(toCurrencyCode)) {
            throw new MissingFieldException("To Currency Code");
        }
        if (Utils.isFieldEmpty(amountString)) {
            throw new MissingFieldException("Amount");
        }
        if (Utils.isInvalidCurrencyCode(fromCurrencyCode)) {
            throw new InvalidCurrencyCodeException();
        }
        if (Utils.isInvalidCurrencyCode(toCurrencyCode)) {
            throw new InvalidCurrencyCodeException();
        }
    }
}
