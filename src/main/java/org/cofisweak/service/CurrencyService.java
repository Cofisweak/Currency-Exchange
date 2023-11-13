package org.cofisweak.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.dao.CurrencyDao;
import org.cofisweak.exception.DaoException;
import org.cofisweak.model.Currency;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyService {
    private static final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private static final CurrencyService INSTANCE = new CurrencyService();

    public Optional<Currency> getCurrencyByCode(String code) throws DaoException {
        return currencyDao.getCurrencyByCode(code);
    }

    public List<Currency> getAllCurrencies() throws DaoException {
        return currencyDao.getAllCurrencies();
    }

    public static CurrencyService getInstance() {
        return INSTANCE;
    }
}
