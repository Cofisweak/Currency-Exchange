package org.cofisweak.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.dto.CurrenciesPairDto;
import org.cofisweak.dto.ExchangeRateDto;
import org.cofisweak.exception.*;
import org.cofisweak.mapper.ExchangeRateMapper;
import org.cofisweak.model.Currency;
import org.cofisweak.model.ExchangeRate;
import org.cofisweak.service.CurrencyService;
import org.cofisweak.service.ExchangeService;
import org.cofisweak.util.ResponseBuilder;
import org.cofisweak.util.Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private static final ExchangeService exchangeService = ExchangeService.getInstance();
    private static final CurrencyService currencyService = CurrencyService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = Utils.extractCurrencyCodeFromUri(req.getRequestURI());
        try {
            checkIsValidRequest(code);
            CurrenciesPairDto currenciesPairDto = getCurrenciesPairDtoByCodePair(code);
            Optional<ExchangeRate> exchangeRate = exchangeService.getExchangeRateByCurrencies(currenciesPairDto.base(), currenciesPairDto.target());
            if (exchangeRate.isEmpty()) {
                throw new ExchangeRateNotFoundException();
            }
            ExchangeRateDto dto = ExchangeRateMapper.mapToDto(exchangeRate.get(), currenciesPairDto.base(), currenciesPairDto.target());
            ResponseBuilder.writeResultToResponse(dto, resp);
        } catch (InvalidCurrencyCodePairException | MissingFieldException e) {
            Utils.processException(e, 400, resp);
        } catch (ExchangeRateNotFoundException e) {
            Utils.processException(e, 404, resp);
        } catch (DaoException e) {
            Utils.processException(e, 500, resp);
        }
    }

    private CurrenciesPairDto getCurrenciesPairDtoByCodePair(String code) throws DaoException, ExchangeRateNotFoundException {
        String baseCurrencyCode = code.substring(0, 3);
        String targetCurrencyCode = code.substring(3);
        Optional<Currency> baseCurrency = currencyService.getCurrencyByCode(baseCurrencyCode);
        Optional<Currency> targetCurrency = currencyService.getCurrencyByCode(targetCurrencyCode);
        if (baseCurrency.isEmpty() || targetCurrency.isEmpty()) {
            throw new ExchangeRateNotFoundException();
        }
        return new CurrenciesPairDto(baseCurrency.get(), targetCurrency.get());
    }

    private static void checkIsValidRequest(String code) throws InvalidCurrencyCodePairException, MissingFieldException {
        if (Utils.isFieldEmpty(code)) {
            throw new MissingFieldException("code");
        }
        if (!Utils.isValidCurrencyCodePair(code)) {
            throw new InvalidCurrencyCodePairException();
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = Utils.extractCurrencyCodeFromUri(req.getRequestURI());
        try {
            String rateString = getRateFromRequestBody(req);
            checkIsValidRequest(code);
            BigDecimal newRate = Utils.parseRate(rateString);

            CurrenciesPairDto currenciesPairDto = getCurrenciesPairDtoByCodePair(code);
            Optional<ExchangeRate> exchangeRate = exchangeService.getExchangeRateByCurrencies(currenciesPairDto.base(), currenciesPairDto.target());
            if (exchangeRate.isEmpty()) {
                throw new ExchangeRateNotFoundException();
            }

            ExchangeRate newExchangeRate = exchangeService.updateRateOfExchangeRate(exchangeRate.get(), newRate);
            ExchangeRateDto responseDto = ExchangeRateMapper.mapToDto(newExchangeRate, currenciesPairDto.base(), currenciesPairDto.target());
            ResponseBuilder.writeResultToResponse(responseDto, resp);
        } catch (ExchangeRateNotFoundException | InvalidCurrencyCodePairException e) {
            Utils.processException(e, 404, resp);
        } catch (MissingFieldException | IllegalRateException e) {
            Utils.processException(e, 400, resp);
        } catch (DaoException e) {
            Utils.processException(e, 500, resp);
        }
    }

    private String getRateFromRequestBody(HttpServletRequest req) throws IOException, MissingFieldException {
        String parameters = req.getReader().readLine();
        if (parameters == null) {
            throw new MissingFieldException("Rate");
        }
        String[] parameterEntry = parameters.split("=");
        if (parameters.length() < 2 || !parameterEntry[0].equalsIgnoreCase("rate")) {
            throw new MissingFieldException("Rate");
        }
        return parameterEntry[1];
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if (!method.equals("PATCH")) {
            super.service(req, resp);
        } else {
            this.doPatch(req, resp);
        }
    }
}
