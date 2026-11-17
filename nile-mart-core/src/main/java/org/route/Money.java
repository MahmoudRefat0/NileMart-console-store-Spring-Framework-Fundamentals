package org.route;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

// helper for money: 2 decimals, HALF_UP (GEN-01, GEN-04)
public final class Money {

    private Money() {
    }

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    // 2558 -> "2,558.00"
    public static String format(BigDecimal amount) {
        return String.format(Locale.US, "%,.2f", round(amount));
    }
}
