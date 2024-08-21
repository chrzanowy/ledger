package com.chrzanowy.utils;

import java.math.BigDecimal;
import javax.money.MonetaryAmount;
import lombok.experimental.UtilityClass;
import org.javamoney.moneta.FastMoney;

@UtilityClass
public class MoneyParser {

    public MonetaryAmount parseDecimalAmount(Long amount, String currency) {
        return FastMoney.of(BigDecimal.valueOf(amount, 2), currency);
    }

}
