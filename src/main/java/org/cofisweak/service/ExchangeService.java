package org.cofisweak.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.dao.ExchangeRateDao;
import org.cofisweak.dto.AddExchangeRateRequestDto;
import org.cofisweak.dto.ExchangeResponseDto;
import org.cofisweak.exception.*;
import org.cofisweak.model.Currency;
import org.cofisweak.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeService {
    private static final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private static final ExchangeService INSTANCE = new ExchangeService();

    public static ExchangeService getInstance() {
        return INSTANCE;
    }

    public List<ExchangeRate> getAllExchangeRates() throws DaoException {
        return exchangeRateDao.getAllExchangeRates();
    }

    public ExchangeRate addNewExchangeRate(AddExchangeRateRequestDto dto) throws DaoException, ExchangeRateAlreadyExistsException {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setBaseCurrencyId(dto.baseCurrency().getId());
        exchangeRate.setTargetCurrencyId(dto.targetCurrency().getId());
        exchangeRate.setRate(dto.rate());
        return exchangeRateDao.addNewExchangeRate(exchangeRate);
    }

    public Optional<ExchangeRate> getExchangeRateByCurrencies(Currency first, Currency second) throws DaoException {
        return exchangeRateDao.getExchangeRateByCurrencyIds(
                first.getId(),
                second.getId());
    }

    public ExchangeRate updateRateOfExchangeRate(ExchangeRate exchangeRate, BigDecimal newRate) throws DaoException {
        return exchangeRateDao.updateRateOfExchangeRate(exchangeRate, newRate);
    }

    public ExchangeResponseDto exchange(Currency from, Currency to, BigDecimal amount) throws DaoException, ExchangeRateNotFoundException {
        if (from.getCode().equals(to.getCode())) {
            return new ExchangeResponseDto(
                    from,
                    to,
                    BigDecimal.ONE,
                    amount.setScale(2, RoundingMode.DOWN),
                    amount.setScale(2, RoundingMode.DOWN));
        }

        BigDecimal rate = getRate(from, to);
        BigDecimal convertedAmount = amount.multiply(rate);
        return new ExchangeResponseDto(
                from,
                to,
                rate.setScale(6, RoundingMode.DOWN),
                amount.setScale(2, RoundingMode.DOWN),
                convertedAmount.setScale(2, RoundingMode.DOWN));
    }

    private BigDecimal getRate(Currency baseCurrency, Currency targetCurrency) throws DaoException, ExchangeRateNotFoundException {
        Optional<BigDecimal> rate = getDirectRate(baseCurrency, targetCurrency);
        if (rate.isPresent()) {
            return rate.get();
        }
        rate = getReverseRate(baseCurrency, targetCurrency);
        if (rate.isPresent()) {
            return rate.get();
        }
        rate = getRateByCrossExchange(baseCurrency, targetCurrency);
        if (rate.isPresent()) {
            return rate.get();
        }
        throw new ExchangeRateNotFoundException();
    }

    private Optional<BigDecimal> getRateByCrossExchange(Currency baseCurrency, Currency targetCurrency) throws DaoException {
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
                    return Optional.of(suitableBaseCurrency
                            .getRate()
                            .multiply(suitableTargetCurrency.getRate()));
                }
            }
        }

        return Optional.empty();
    }

    private Optional<BigDecimal> getDirectRate(Currency baseCurrency, Currency targetCurrency) throws DaoException {
        return getExchangeRateByCurrencies(baseCurrency, targetCurrency)
                .map(ExchangeRate::getRate);

    }

    private Optional<BigDecimal> getReverseRate(Currency baseCurrency, Currency targetCurrency) throws DaoException {
        return getDirectRate(targetCurrency, baseCurrency)
                .map(rate -> BigDecimal.ONE
                        .setScale(6, RoundingMode.DOWN)
                        .divide(rate, RoundingMode.DOWN));
    }
}
