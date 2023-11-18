package org.cofisweak.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.dto.ExchangeRateDto;
import org.cofisweak.model.Currency;
import org.cofisweak.model.ExchangeRate;

import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateMapper {
    public static ExchangeRate mapFromResultSet(ResultSet set) throws SQLException {
        return new ExchangeRate(
                set.getInt("id"),
                set.getInt("base_currency_id"),
                set.getInt("target_currency_id"),
                set.getBigDecimal("rate"));
    }

    public static ExchangeRateDto mapToDto(ExchangeRate exchangeRate, Currency from, Currency to) {
        return new ExchangeRateDto(
                exchangeRate.getId(),
                from,
                to,
                exchangeRate.getRate());
    }
}
