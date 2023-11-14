package org.cofisweak.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.dao.CurrencyDao;
import org.cofisweak.dto.AddCurrencyDto;
import org.cofisweak.exception.*;
import org.cofisweak.model.Currency;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyService {
    private static final CurrencyDao currencyDao = CurrencyDao.getInstance();
    private static final CurrencyService INSTANCE = new CurrencyService();

    public Optional<Currency> getCurrencyByCode(String code) throws DaoException, InvalidCurrencyCodeException {
        if (code == null || code.length() != 3) {
            throw new InvalidCurrencyCodeException();
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

    public Currency addNewCurrency(AddCurrencyDto dto) throws DaoException, CurrencyAlreadyExistsException, InvalidCurrencyCodeException, MissingFieldException {
        validateCurrencyDto(dto);

        Optional<Currency> existingCurrency = currencyDao.getCurrencyByCode(dto.code());
        if (existingCurrency.isPresent()) {
            throw new CurrencyAlreadyExistsException();
        }

        Currency currency = new Currency();
        currency.setCode(dto.code().toUpperCase());
        currency.setFullName(dto.name());
        currency.setSign(dto.sign());
        currencyDao.addNewCurrency(currency);
        return currency;
    }

    private static void validateCurrencyDto(AddCurrencyDto dto) throws MissingFieldException, InvalidCurrencyCodeException {
        if(dto.code() == null || dto.code().isEmpty()) {
            throw new MissingFieldException("code");
        }
        if(dto.sign() == null || dto.sign().isEmpty()) {
            throw new MissingFieldException("sign");
        }
        if(dto.name() == null || dto.name().isEmpty()) {
            throw new MissingFieldException("name");
        }
        if (dto.code().length() != 3) {
            throw new InvalidCurrencyCodeException();
        }
    }

}
