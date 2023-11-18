package org.cofisweak.dto;

import org.cofisweak.model.Currency;

import java.math.BigDecimal;

public record ExchangeRequestDto(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
}
