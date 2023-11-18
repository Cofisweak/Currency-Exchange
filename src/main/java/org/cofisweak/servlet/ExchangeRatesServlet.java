package org.cofisweak.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cofisweak.dto.AddExchangeRateRequestDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private static final ExchangeService exchangeService = ExchangeService.getInstance();
    private static final CurrencyService currencyService = CurrencyService.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateString = req.getParameter("rate");

        try {
            checkIsValidPostRequest(baseCurrencyCode, targetCurrencyCode, rateString);
            AddExchangeRateRequestDto requestDto = getAddExchangeRateRequestDto(baseCurrencyCode.trim(), targetCurrencyCode.trim(), rateString.trim());
            if (requestDto.baseCurrency().equals(requestDto.targetCurrency())) {
                throw new IncorrectDataException("Currencies must be different");
            }
            ExchangeRate exchangeRate = exchangeService.addNewExchangeRate(requestDto);
            ExchangeRateDto responseDto = ExchangeRateMapper.mapToDto(exchangeRate, requestDto.baseCurrency(), requestDto.targetCurrency());
            ResponseBuilder.writeResultToResponse(responseDto, resp);
        } catch (InvalidCurrencyCodeException | MissingFieldException | IllegalRateException |
                 IncorrectDataException e) {
            Utils.processException(e, 400, resp);
        } catch (ExchangeRateNotFoundException e) {
            Utils.processException(e, 404, resp);
        } catch (ExchangeRateAlreadyExistsException e) {
            Utils.processException(e, 409, resp);
        } catch (DaoException e) {
            Utils.processException(e, 500, resp);
        }
    }

    private static AddExchangeRateRequestDto getAddExchangeRateRequestDto(String baseCurrencyCode, String targetCurrencyCode, String rateString) throws ExchangeRateNotFoundException, IllegalRateException, DaoException {
        Optional<Currency> baseCurrencyOptional = currencyService.getCurrencyByCode(baseCurrencyCode);
        Optional<Currency> targetCurrencyOptional = currencyService.getCurrencyByCode(targetCurrencyCode);
        BigDecimal amount = Utils.parseRate(rateString);
        if (baseCurrencyOptional.isEmpty() || targetCurrencyOptional.isEmpty()) {
            throw new ExchangeRateNotFoundException();
        }
        Currency baseCurrency = baseCurrencyOptional.get();
        Currency targetCurrency = targetCurrencyOptional.get();
        return new AddExchangeRateRequestDto(baseCurrency, targetCurrency, amount);
    }

    private static void checkIsValidPostRequest(String baseCurrencyCode, String targetCurrencyCode, String rateString) throws InvalidCurrencyCodeException, MissingFieldException {
        if (Utils.isFieldEmpty(baseCurrencyCode)) {
            throw new MissingFieldException("Base Currency Code");
        }
        if (Utils.isFieldEmpty(targetCurrencyCode)) {
            throw new MissingFieldException("Target Currency Code");
        }
        if (Utils.isFieldEmpty(rateString)) {
            throw new MissingFieldException("Rate");
        }
        if (Utils.isInvalidCurrencyCode(baseCurrencyCode)) {
            throw new InvalidCurrencyCodeException();
        }
        if (Utils.isInvalidCurrencyCode(targetCurrencyCode)) {
            throw new InvalidCurrencyCodeException();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRate> rates = exchangeService.getAllExchangeRates();
            List<ExchangeRateDto> result = new ArrayList<>();
            for (ExchangeRate rate : rates) {
                Optional<Currency> from = currencyService.getCurrencyById(rate.getBaseCurrencyId());
                Optional<Currency> to = currencyService.getCurrencyById(rate.getTargetCurrencyId());
                if (from.isEmpty() || to.isEmpty()) {
                    continue;
                }
                ExchangeRateDto dto = ExchangeRateMapper.mapToDto(rate, from.get(), to.get());
                result.add(dto);
            }
            ResponseBuilder.writeResultToResponse(result, resp);
        } catch (DaoException e) {
            Utils.processException(e, 500, resp);
        }
    }
}
