package org.route;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// @Primary here = "Standard" pricing (worked examples 1, 3, 4)
// to test "Promo" (example 2): move @Primary to PromotionalPricingService
@Component
@Primary
public class StandardPricingService implements PricingService{

    @Override
    public BigDecimal getPrice(Product product, int qty) {
        return product.getPrice().multiply(BigDecimal.valueOf(qty));
    }
}
