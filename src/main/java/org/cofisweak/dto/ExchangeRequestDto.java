package org.cofisweak.dto;

import java.math.BigDecimal;

public record ExchangeRequestDto(String baseCurrencyCode,
                                 String targetCurrencyCode,
                                 String amount) {
}
