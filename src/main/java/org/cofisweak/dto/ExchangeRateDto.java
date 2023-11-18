package org.cofisweak.dto;

import org.cofisweak.model.Currency;

import java.math.BigDecimal;

public record ExchangeRateDto(long id,
                              Currency baseCurrency,
                              Currency targetCurrency,
                              BigDecimal rate) {
}
