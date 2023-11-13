package org.cofisweak.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.model.ExchangeRate;

import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateMapper {
    public static ExchangeRate mapFrom(ResultSet set) throws SQLException {
        return new ExchangeRate(
                set.getInt("id"),
                set.getInt("base_currency_id"),
                set.getInt("target_currency_id"),
                set.getBigDecimal("rate"));
    }
}
