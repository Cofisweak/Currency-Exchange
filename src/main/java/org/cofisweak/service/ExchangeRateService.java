package org.cofisweak.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.dao.ExchangeRateDao;
import org.cofisweak.dto.AddExchangeRateRequestDto;
import org.cofisweak.dto.ExchangeRateDto;
import org.cofisweak.exception.*;
import org.cofisweak.model.Currency;
import org.cofisweak.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateService {
    private static final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private static final CurrencyService currencyService = CurrencyService.getInstance();
    private static final ExchangeRateService INSTANCE = new ExchangeRateService();

    public List<ExchangeRateDto> getAllExchangeRates() throws DaoException, InvalidCurrencyCodeException {
        List<ExchangeRate> rates = exchangeRateDao.getAllExchangeRates();

        List<ExchangeRateDto> result = new ArrayList<>();
        for (ExchangeRate exchangeRate : rates) {
            ExchangeRateDto dto = getExchangeRateDto(exchangeRate);
            result.add(dto);
        }

        return result;
    }

    private static ExchangeRateDto getExchangeRateDto(ExchangeRate exchangeRate) throws DaoException, InvalidCurrencyCodeException {
        Optional<Currency> baseCurrency = currencyService.getCurrencyById(exchangeRate.getBaseCurrencyId());
        Optional<Currency> targetCurrency = currencyService.getCurrencyById(exchangeRate.getTargetCurrencyId());
        if (baseCurrency.isEmpty() || targetCurrency.isEmpty()) {
            throw new InvalidCurrencyCodeException();
        }
        return new ExchangeRateDto(
                exchangeRate.getId(),
                baseCurrency.get(),
                targetCurrency.get(),
                exchangeRate.getRate());
    }

    public static ExchangeRateService getInstance() {
        return INSTANCE;
    }

    public ExchangeRateDto addNewExchangeRate(AddExchangeRateRequestDto dto) throws InvalidCurrencyCodeException, MissingFieldException, DaoException, CurrencyNotFoundException, ExchangeRateAlreadyExistsException, AddExchangeRateException {
        validateExchangeRateRequestDto(dto);
        BigDecimal rate = getRateFromDto(dto);
        Optional<Currency> firstCurrency = currencyService.getCurrencyByCode(dto.baseCurrencyCode());
        Optional<Currency> secondCurrency = currencyService.getCurrencyByCode(dto.targetCurrencyCode());
        if (firstCurrency.isEmpty() || secondCurrency.isEmpty()) {
            throw new CurrencyNotFoundException();
        }
        if (firstCurrency.get().equals(secondCurrency.get())) {
            throw new AddExchangeRateException("Currency must be different");
        }
        if (getExchangeRateByCurrencies(firstCurrency.get(), secondCurrency.get()).isPresent()) {
            throw new ExchangeRateAlreadyExistsException();
        }
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setBaseCurrencyId(firstCurrency.get().getId());
        exchangeRate.setTargetCurrencyId(secondCurrency.get().getId());
        exchangeRate.setRate(rate);
        exchangeRateDao.createNewExchangeRate(exchangeRate);
        return getExchangeRateDto(exchangeRate);
    }

    private static BigDecimal getRateFromDto(AddExchangeRateRequestDto dto) throws AddExchangeRateException {
        BigDecimal rate;
        try {
            rate = new BigDecimal(dto.rate());
            rate = rate.setScale(6, RoundingMode.DOWN);
        } catch (NumberFormatException e) {
            throw new AddExchangeRateException("Illegal rate");
        }
        return rate;
    }

    private static void validateExchangeRateRequestDto(AddExchangeRateRequestDto dto) throws MissingFieldException, InvalidCurrencyCodeException {
        if (dto.baseCurrencyCode() == null || dto.baseCurrencyCode().isEmpty()) {
            throw new MissingFieldException("baseCurrencyCode");
        }
        if (dto.targetCurrencyCode() == null || dto.targetCurrencyCode().isEmpty()) {
            throw new MissingFieldException("targetCurrencyCode");
        }
        if (dto.rate() == null || dto.rate().isEmpty()) {
            throw new MissingFieldException("rate");
        }
        if (dto.baseCurrencyCode().length() != 3 || dto.targetCurrencyCode().length() != 3) {
            throw new InvalidCurrencyCodeException();
        }
    }

    public Optional<ExchangeRate> getExchangeRateByCurrencyCodes(String firstCurrencyCode, String secondCurrencyCode) throws InvalidCurrencyCodeException, DaoException, ExchangeRateNotFoundException {
        Optional<Currency> firstCurrency = currencyService.getCurrencyByCode(firstCurrencyCode);
        Optional<Currency> secondCurrency = currencyService.getCurrencyByCode(secondCurrencyCode);
        if (firstCurrency.isEmpty() || secondCurrency.isEmpty()) {
            throw new ExchangeRateNotFoundException();
        }
        return getExchangeRateByCurrencies(firstCurrency.get(), secondCurrency.get());
    }

/*    public Optional<ExchangeRate> getExchangeRateByCurrencyCodePair(String codePair) throws InvalidCurrencyCodeException, DaoException, ExchangeRateNotFoundException {
        String firstCode = codePair.substring(0, 3);
        String secondCode = codePair.substring(0, 3);
        return getExchangeRateByCurrencyCodes(firstCode, secondCode);
    }*/

    public Optional<ExchangeRate> getExchangeRateByCurrencies(Currency first, Currency second) throws DaoException {
        return exchangeRateDao.getExchangeRateByCurrencyIds(
                first.getId(),
                second.getId());
    }
}
