package org.cofisweak.dto;

import org.cofisweak.model.Currency;

public record CurrenciesPairDto (Currency base,
                                 Currency target) {
}
