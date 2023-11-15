package org.cofisweak.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.dao.ExchangeRateDao;
import org.cofisweak.dto.AddExchangeRateRequestDto;
import org.cofisweak.dto.ExchangeRateDto;
import org.cofisweak.dto.ExchangeRequestDto;
import org.cofisweak.dto.ExchangeResponseDto;
import org.cofisweak.exception.*;
import org.cofisweak.model.Currency;
import org.cofisweak.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeService {
    private static final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private static final CurrencyService currencyService = CurrencyService.getInstance();
    private static final ExchangeService INSTANCE = new ExchangeService();

    public List<ExchangeRateDto> getAllExchangeRates() throws DaoException, InvalidCurrencyCodeException {
        List<ExchangeRate> rates = exchangeRateDao.getAllExchangeRates();

        List<ExchangeRateDto> result = new ArrayList<>();
        for (ExchangeRate exchangeRate : rates) {
            ExchangeRateDto dto = getExchangeRateDto(exchangeRate);
            result.add(dto);
        }

        return result;
    }

    public ExchangeRateDto getExchangeRateDto(ExchangeRate exchangeRate) throws DaoException, InvalidCurrencyCodeException {
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

    public static ExchangeService getInstance() {
        return INSTANCE;
    }

    public ExchangeRateDto addNewExchangeRate(AddExchangeRateRequestDto dto) throws InvalidCurrencyCodeException, MissingFieldException, DaoException, CurrencyNotFoundException, ExchangeRateAlreadyExistsException, IllegalRateException {
        validateAddExchangeRateRequestDto(dto);
        BigDecimal rate = parseRate(dto.rate());
        rate = rate.setScale(6, RoundingMode.DOWN);
        Optional<Currency> firstCurrency = currencyService.getCurrencyByCode(dto.baseCurrencyCode());
        Optional<Currency> secondCurrency = currencyService.getCurrencyByCode(dto.targetCurrencyCode());
        if (firstCurrency.isEmpty() || secondCurrency.isEmpty()) {
            throw new CurrencyNotFoundException();
        }
        if (firstCurrency.get().equals(secondCurrency.get())) {
            throw new IllegalRateException("Currency must be different");
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

    private static BigDecimal parseRate(String rateString) throws IllegalRateException {
        BigDecimal rate;
        try {
            rate = new BigDecimal(rateString);
        } catch (NumberFormatException e) {
            throw new IllegalRateException("Illegal rate");
        }
        return rate;
    }

    private static void validateAddExchangeRateRequestDto(AddExchangeRateRequestDto dto) throws MissingFieldException {
        validateCurrencyCode(dto.baseCurrencyCode(), "baseCurrencyCode");
        validateCurrencyCode(dto.targetCurrencyCode(), "targetCurrencyCode");
        if (dto.rate().isEmpty()) {
            throw new MissingFieldException("rate");
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

    public Optional<ExchangeRate> getExchangeRateByCurrencyCodePair(String codePair) throws InvalidCurrencyCodeException, DaoException, ExchangeRateNotFoundException, InvalidCurrencyCodePairException {
        if (codePair == null || codePair.length() != 6) {
            throw new InvalidCurrencyCodePairException();
        }
        String firstCode = codePair.substring(0, 3);
        String secondCode = codePair.substring(3);
        return getExchangeRateByCurrencyCodes(firstCode, secondCode);
    }

    public Optional<ExchangeRate> getExchangeRateByCurrencies(Currency first, Currency second) throws DaoException {
        return exchangeRateDao.getExchangeRateByCurrencyIds(
                first.getId(),
                second.getId());
    }

    public ExchangeRateDto updateRateOfExchangeRateByCurrencyCodePair(String code, String newRate) throws InvalidCurrencyCodeException, DaoException, ExchangeRateNotFoundException, MissingFieldException, IllegalRateException, InvalidCurrencyCodePairException {
        if (newRate == null || newRate.isEmpty()) {
            throw new MissingFieldException("rate");
        }
        Optional<ExchangeRate> exchangeRate = getExchangeRateByCurrencyCodePair(code);
        if (exchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException();
        }
        BigDecimal rate = parseRate(newRate);
        rate = rate.setScale(6, RoundingMode.DOWN);
        ExchangeRate newExchangeRate = exchangeRateDao.updateRateOfExchangeRate(exchangeRate.get(), rate);
        return getExchangeRateDto(newExchangeRate);
    }

    public ExchangeResponseDto exchange(ExchangeRequestDto requestDto) throws InvalidCurrencyCodeException, DaoException, ExchangeRateNotFoundException, MissingFieldException, IllegalRateException, CurrencyNotFoundException {
        validateExchangeRate(requestDto);
        Optional<Currency> baseCurrency = currencyService.getCurrencyByCode(requestDto.baseCurrencyCode());
        Optional<Currency> targetCurrency = currencyService.getCurrencyByCode(requestDto.targetCurrencyCode());
        if (baseCurrency.isEmpty() || targetCurrency.isEmpty()) {
            throw new CurrencyNotFoundException();
        }

        BigDecimal amount = parseRate(requestDto.amount());

        if (requestDto.baseCurrencyCode().equals(requestDto.targetCurrencyCode())) {
            return new ExchangeResponseDto(
                    baseCurrency.get(),
                    targetCurrency.get(),
                    BigDecimal.ONE,
                    amount,
                    amount);
        }

        BigDecimal rate = getRate(baseCurrency.get(), targetCurrency.get());
        BigDecimal convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.DOWN);
        return new ExchangeResponseDto(
                baseCurrency.get(),
                targetCurrency.get(),
                rate,
                amount,
                convertedAmount);
    }

    private BigDecimal getRate(Currency baseCurrency, Currency targetCurrency) throws InvalidCurrencyCodeException, DaoException, ExchangeRateNotFoundException {
        try {
            return getDirectRate(baseCurrency, targetCurrency);
        } catch (ExchangeRateNotFoundException ignored) {}
        try {
            return getReverseRate(baseCurrency, targetCurrency);
        } catch (ExchangeRateNotFoundException ignored) {}
        return getRateByCrossExchange(baseCurrency, targetCurrency);
    }

    private BigDecimal getRateByCrossExchange(Currency baseCurrency, Currency targetCurrency) throws DaoException, ExchangeRateNotFoundException {
        List<ExchangeRate> exchangeRates = exchangeRateDao.getAllExchangeRatesWithReversedRate();

        List<ExchangeRate> suitableBaseCurrencies = exchangeRates.stream()
                .filter(exchangeRate -> exchangeRate.getBaseCurrencyId() == baseCurrency.getId())
                .toList();
        List<ExchangeRate> suitableTargetCurrencies = exchangeRates.stream()
                .filter(exchangeRate -> exchangeRate.getTargetCurrencyId() == targetCurrency.getId())
                .toList();
        for (ExchangeRate suitableBaseCurrency : suitableBaseCurrencies) {
            for (ExchangeRate suitableTargetCurrency : suitableTargetCurrencies) {
                if (suitableBaseCurrency.getTargetCurrencyId() == suitableTargetCurrency.getBaseCurrencyId()) {
                    return suitableBaseCurrency.getRate().multiply(suitableTargetCurrency.getRate());
                }
            }
        }

        throw new ExchangeRateNotFoundException();
    }

    private BigDecimal getDirectRate(Currency baseCurrency, Currency targetCurrency) throws InvalidCurrencyCodeException, DaoException, ExchangeRateNotFoundException {
        Optional<ExchangeRate> exchangeRate = getExchangeRateByCurrencyCodes(baseCurrency.getCode(), targetCurrency.getCode());
        if (exchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException();
        }
        return exchangeRate.get().getRate();

    }

    private BigDecimal getReverseRate(Currency baseCurrency, Currency targetCurrency) throws InvalidCurrencyCodeException, DaoException, ExchangeRateNotFoundException {
        return BigDecimal.ONE
                .setScale(6, RoundingMode.DOWN)
                .divide(getDirectRate(targetCurrency, baseCurrency), RoundingMode.DOWN);
    }

    private static void validateExchangeRate(ExchangeRequestDto requestDto) throws MissingFieldException {
        validateCurrencyCode(requestDto.baseCurrencyCode(), "baseCurrencyCode");
        validateCurrencyCode(requestDto.targetCurrencyCode(), "targetCurrencyCode");
        if (requestDto.amount() == null || requestDto.amount().isEmpty()) {
            throw new MissingFieldException("amount");
        }
    }

    private static void validateCurrencyCode(String currencyCode, String fieldName) throws MissingFieldException {
        if (currencyCode == null || currencyCode.length() != 3) {
            throw new MissingFieldException(fieldName);
        }
    }
}
