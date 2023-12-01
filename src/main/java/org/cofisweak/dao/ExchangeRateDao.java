package org.cofisweak.dao;

import lombok.NoArgsConstructor;
import org.cofisweak.exception.DaoException;
import org.cofisweak.exception.ExchangeRateAlreadyExistsException;
import org.cofisweak.mapper.ExchangeRateMapper;
import org.cofisweak.model.ExchangeRate;
import org.cofisweak.util.ConnectionManager;
import org.cofisweak.util.Utils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
public class ExchangeRateDao {
    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();
    private static final String GET_ALL_EXCHANGE_RATES_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM exchange_rates""";

    private static final String GET_EXCHANGE_RATE_BY_CURRENCY_IDS_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM exchange_rates
            WHERE base_currency_id = ? AND target_currency_id = ?""";

    private static final String ADD_NEW_EXCHANGE_RATE_SQL = """
            INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)""";

    private static final String UPDATE_RATE_OF_EXCHANGE_RATE_SQL = """
            UPDATE exchange_rates
            SET rate = ?
            WHERE id = ?""";

    private static final String GET_ALL_EXCHANGE_RATES_WITH_REVERSED_RATE_SQL = """
            SELECT
                      base_currency_id,
                      target_currency_id,
                      rate
                  FROM exchange_rates
                  UNION SELECT
                            target_currency_id,
                            base_currency_id,
                            1.0 / rate as rate
                  FROM exchange_rates""";

    public List<ExchangeRate> getAllExchangeRates() throws DaoException {
        List<ExchangeRate> result = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_EXCHANGE_RATES_SQL)) {
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                ExchangeRate rate = ExchangeRateMapper.mapFromResultSet(set);
                result.add(rate);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DaoException("Unable to get all exchange rates");
        }

        return result;
    }

    public Optional<ExchangeRate> getExchangeRateByCurrencyIds(int first, int second) throws DaoException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_EXCHANGE_RATE_BY_CURRENCY_IDS_SQL)) {
            statement.setInt(1, first);
            statement.setInt(2, second);
            ResultSet set = statement.executeQuery();
            ExchangeRate exchangeRate = null;
            if (set.next()) {
                exchangeRate = ExchangeRateMapper.mapFromResultSet(set);
            }
            return Optional.ofNullable(exchangeRate);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DaoException("Unable to get exchange rate");
        }
    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }

    public ExchangeRate addNewExchangeRate(ExchangeRate rate) throws DaoException, ExchangeRateAlreadyExistsException {
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);
            return processAddNewExchangeRate(rate, connection);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DaoException("Unable to add new exchange rate");
        }
    }

    private static ExchangeRate processAddNewExchangeRate(ExchangeRate rate, Connection connection) throws DaoException, SQLException, ExchangeRateAlreadyExistsException {
        try {
            try (PreparedStatement statement = connection.prepareStatement(ADD_NEW_EXCHANGE_RATE_SQL)) {
                statement.setInt(1, rate.getBaseCurrencyId());
                statement.setInt(2, rate.getTargetCurrencyId());
                statement.setBigDecimal(3, rate.getRate());
                statement.execute();
            }
            try (PreparedStatement statement = connection.prepareStatement(Utils.GET_LAST_INSERT_ROWID_SQL)) {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    rate.setId(resultSet.getInt("last_insert_rowid()"));
                } else {
                    throw new DaoException("Unable to check new exchange rate");
                }
            }
            connection.commit();
            return rate;
        } catch (SQLException e) {
            connection.rollback();
            System.out.println(e.getMessage());
            if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                throw new ExchangeRateAlreadyExistsException();
            } else {
                throw new DaoException("Unable to add new exchange rate");
            }
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public ExchangeRate updateRateOfExchangeRate(ExchangeRate exchangeRate, BigDecimal rate) throws DaoException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_RATE_OF_EXCHANGE_RATE_SQL)) {
            statement.setBigDecimal(1, rate);
            statement.setInt(2, exchangeRate.getId());
            statement.execute();
            exchangeRate.setRate(rate);
            return exchangeRate;
        } catch (SQLException e) {
            throw new DaoException("Unable to update rate of exchange rate");
        }
    }

    public List<ExchangeRate> getAllExchangeRatesWithReversedRate() throws DaoException {
        List<ExchangeRate> result = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_EXCHANGE_RATES_WITH_REVERSED_RATE_SQL)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                ExchangeRate exchangeRate = new ExchangeRate();
                exchangeRate.setBaseCurrencyId(resultSet.getInt("base_currency_id"));
                exchangeRate.setTargetCurrencyId(resultSet.getInt("target_currency_id"));
                exchangeRate.setRate(resultSet.getBigDecimal("rate"));
                result.add(exchangeRate);
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to get all exchange rates with reversed rate");
        }

        return result;
    }
}
