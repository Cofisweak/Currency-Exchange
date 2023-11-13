package org.cofisweak.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.dao.CurrencyDao;
import org.cofisweak.dto.AddCurrencyDto;
import org.cofisweak.exception.AddCurrencyException;
import org.cofisweak.exception.InvalidCurrencyCodeException;
import org.cofisweak.exception.CurrencyAlreadyExistException;
import org.cofisweak.exception.DaoException;
import org.cofisweak.model.Currency;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyService {
    private static final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private static final CurrencyService INSTANCE = new CurrencyService();

    public Optional<Currency> getCurrencyByCode(String code) throws DaoException, InvalidCurrencyCodeException {
        if (code == null || code.length() != 3) {
            throw new InvalidCurrencyCodeException("Currency code must contain 3 characters");
        }
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

    public Optional<Currency> addNewCurrency(AddCurrencyDto dto) throws AddCurrencyException, DaoException, CurrencyAlreadyExistException, InvalidCurrencyCodeException {
        if(dto.code() == null || dto.code().isEmpty()) {
            throw new AddCurrencyException("Missing field \"code\"");
        }
        if(dto.sign() == null || dto.sign().isEmpty()) {
            throw new AddCurrencyException("Missing field \"sign\"");
        }
        if(dto.name() == null || dto.name().isEmpty()) {
            throw new AddCurrencyException("Missing field \"name\"");
        }
        if (dto.code().length() != 3) {
            throw new InvalidCurrencyCodeException("Currency code must contain 3 characters");
        }

        Optional<Currency> existingCurrency = currencyDao.getCurrencyByCode(dto.code());
        if (existingCurrency.isPresent()) {
            throw new CurrencyAlreadyExistException("Currency " + dto.code() + " already exists");
        }

        Currency currency = new Currency();
        currency.setCode(dto.code().toUpperCase());
        currency.setFullName(dto.name());
        currency.setSign(dto.sign());

        return currencyDao.addNewCurrency(currency);
    }

}
