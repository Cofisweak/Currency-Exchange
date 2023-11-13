package org.cofisweak.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExchangeRate {
    int id;
    int baseCurrencyId;
    int targetCurrencyId;
    BigDecimal rate;
}
