package org.cofisweak.dao;

import lombok.NoArgsConstructor;
import org.cofisweak.exception.DaoException;
import org.cofisweak.mapper.ExchangeRateMapper;
import org.cofisweak.model.ExchangeRate;
import org.cofisweak.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ExchangeRateDao {
    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();
    private static final String GET_ALL_EXCHANGE_RATES_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM exchange_rates""";

    public List<ExchangeRate> getAllExchangeRates() throws DaoException {
        List<ExchangeRate> result = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_EXCHANGE_RATES_SQL)) {
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                ExchangeRate rate = ExchangeRateMapper.mapFrom(set);
                result.add(rate);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DaoException("Unable to get all exchange rates");
        }

        return result;
    }

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }
}
