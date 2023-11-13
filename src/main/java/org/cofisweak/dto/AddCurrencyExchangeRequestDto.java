package org.cofisweak.dto;

public record AddCurrencyExchangeRequestDto(String baseCurrencyCode,
                                            String targetCurrencyCode,
                                            String rate) {
}
