package org.cofisweak.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.model.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyMapper {
    public static Currency mapFrom(ResultSet set) throws SQLException {
        return new Currency(
                set.getInt("id"),
                set.getString("code"),
                set.getString("full_name"),
                set.getString("sign"));
    }
}
