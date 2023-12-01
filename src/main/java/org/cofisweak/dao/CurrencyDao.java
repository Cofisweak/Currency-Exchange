package org.cofisweak.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.exception.CurrencyAlreadyExistsException;
import org.cofisweak.exception.DaoException;
import org.cofisweak.mapper.CurrencyMapper;
import org.cofisweak.model.Currency;
import org.cofisweak.util.ConnectionManager;
import org.cofisweak.util.Utils;

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

    private static final String GET_CURRENCY_BY_ID_SQL = """
            SELECT id, code, full_name, sign
            FROM currencies
            WHERE id = ?""";

    private static final String ADD_NEW_CURRENCY_SQL = """
            INSERT INTO currencies (code, full_name, sign)
            VALUES (?, ?, ?)""";

    public List<Currency> getAllCurrencies() throws DaoException {
        List<Currency> allCurrencies = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_CURRENCIES_SQL)) {
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                Currency currency = CurrencyMapper.mapFrom(set);
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
            if (set.next()) {
                currency = CurrencyMapper.mapFrom(set);
            }
            return Optional.ofNullable(currency);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DaoException("Unable to get currency by currency code");
        }
    }

    public Currency addNewCurrency(Currency currency) throws DaoException, CurrencyAlreadyExistsException {
        try(Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            return processAddNewCurrency(currency, connection);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DaoException("Unable to add new currency");
        }
    }

    private static Currency processAddNewCurrency(Currency currency, Connection connection) throws SQLException, DaoException, CurrencyAlreadyExistsException {
        try {
            try (PreparedStatement statement = connection.prepareStatement(ADD_NEW_CURRENCY_SQL)) {
                statement.setString(1, currency.getCode());
                statement.setString(2, currency.getFullName());
                statement.setString(3, currency.getSign());
                statement.execute();
            }
            try (PreparedStatement statement = connection.prepareStatement(Utils.GET_LAST_INSERT_ROWID_SQL)) {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    currency.setId(resultSet.getInt("last_insert_rowid()"));
                } else {
                    throw new DaoException("Unable to check new currency");
                }
            }
            connection.commit();
            return currency;
        } catch (SQLException e) {
            connection.rollback();
            System.out.println(e.getMessage());
            if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                throw new CurrencyAlreadyExistsException();
            } else {
                throw new DaoException("Unable to add new currency");
            }
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }

    public Optional<Currency> getCurrencyById(int id) throws DaoException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_CURRENCY_BY_ID_SQL)) {
            statement.setInt(1, id);
            ResultSet set = statement.executeQuery();
            Currency currency = null;
            if (set.next()) {
                currency = CurrencyMapper.mapFrom(set);
            }
            return Optional.ofNullable(currency);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DaoException("Unable to get currency by currency id");
        }
    }
}
