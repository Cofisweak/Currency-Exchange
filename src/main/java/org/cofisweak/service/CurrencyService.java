package org.cofisweak.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.dao.CurrencyDao;
import org.cofisweak.exception.*;
import org.cofisweak.model.Currency;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyService {
    private static final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private static final CurrencyService INSTANCE = new CurrencyService();

    public Optional<Currency> getCurrencyByCode(String code) throws DaoException {
        return currencyDao.getCurrencyByCode(code.toUpperCase());
    }

    public Optional<Currency> getCurrencyById(int id) throws DaoException {
        return currencyDao.getCurrencyById(id);
    }

    public List<Currency> getAllCurrencies() throws DaoException {
        return currencyDao.getAllCurrencies();
    }

    public static CurrencyService getInstance() {
        return INSTANCE;
    }

    public Currency addNewCurrency(String name, String code, String sign) throws DaoException, CurrencyAlreadyExistsException {
        Optional<Currency> existingCurrency = currencyDao.getCurrencyByCode(code);
        if (existingCurrency.isPresent()) {
            throw new CurrencyAlreadyExistsException();
        }

        Currency currency = new Currency();
        currency.setCode(code.toUpperCase());
        currency.setFullName(name);
        currency.setSign(sign);
        return currencyDao.addNewCurrency(currency);
    }
}
