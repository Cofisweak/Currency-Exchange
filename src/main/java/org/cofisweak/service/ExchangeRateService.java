package org.cofisweak.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.dao.ExchangeRateDao;
import org.cofisweak.dto.ExchangeRateDto;
import org.cofisweak.exception.DaoException;
import org.cofisweak.exception.InvalidCurrencyCodeException;
import org.cofisweak.model.Currency;
import org.cofisweak.model.ExchangeRate;

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
            Optional<Currency> baseCurrency = currencyService.getCurrencyById(exchangeRate.getBaseCurrencyId());
            Optional<Currency> targetCurrency = currencyService.getCurrencyById(exchangeRate.getTargetCurrencyId());
            ExchangeRateDto dto = new ExchangeRateDto(
                    exchangeRate.getId(),
                    baseCurrency.get(),
                    targetCurrency.get(),
                    exchangeRate.getRate());
            result.add(dto);
        }
        return result;
    }

    public static ExchangeRateService getInstance() {
        return INSTANCE;
    }
}
