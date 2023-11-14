package org.cofisweak.dto;

import org.cofisweak.model.Currency;

import java.math.BigDecimal;

public record ExchangeResponseDto (Currency baseCurrency,
                                   Currency targetCurrency,
                                   BigDecimal rate,
                                   BigDecimal amount,
                                   BigDecimal convertedAmount) {
}
