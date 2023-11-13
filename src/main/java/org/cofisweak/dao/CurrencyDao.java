package org.cofisweak.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.exception.DaoException;
import org.cofisweak.model.Currency;
import org.cofisweak.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyDao {
    private static final CurrencyDao INSTANCE = new CurrencyDao();
    private static final String GET_ALL_CURRENCIES_SQL = """
            SELECT id, code, full_name, sign
            FROM currencies""";

    private static final String GET_CURRENCY_BY_CODE_SQL = """
            SELECT id, code, full_name, sign
            FROM currencies
            WHERE code = ?""";

    private static final String ADD_NEW_CURRENCY_SQL = """
            INSERT INTO currencies (code, full_name, sign)
            VALUES (?, ?, ?)""";

    public List<Currency> getAllCurrencies() throws DaoException {
        List<Currency> allCurrencies = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_CURRENCIES_SQL)) {
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                Currency currency = new Currency(
                        set.getInt("id"),
                        set.getString("code"),
                        set.getString("full_name"),
                        set.getString("sign"));
                allCurrencies.add(currency);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DaoException("Unable to get all currencies");
        }

        return allCurrencies;
    }

    public Optional<Currency> getCurrencyByCode(String code) throws DaoException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_CURRENCY_BY_CODE_SQL)) {
            statement.setString(1, code);
            ResultSet set = statement.executeQuery();
            Currency currency = null;
            while (set.next()) {
                currency = new Currency(
                        set.getInt("id"),
                        set.getString("code"),
                        set.getString("full_name"),
                        set.getString("sign"));
            }
            return Optional.ofNullable(currency);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DaoException("Unable to get currency by currency code");
        }
    }

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }
}
