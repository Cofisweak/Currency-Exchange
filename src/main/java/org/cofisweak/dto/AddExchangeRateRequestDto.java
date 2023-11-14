package org.cofisweak.dto;

public record AddExchangeRateRequestDto(String baseCurrencyCode,
                                        String targetCurrencyCode,
                                        String rate) {
}
