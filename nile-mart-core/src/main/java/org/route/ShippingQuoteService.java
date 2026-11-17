package org.route;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShippingQuoteService {

    private final ShippingCalculator zoneCalculator;
    private final ShippingCalculator freeCalculator;
    private final BigDecimal freeShippingThreshold;

    // no @Qualifier on the first one -> Spring gives us the @Primary bean (ZoneShippingCalculator)
    public ShippingQuoteService(ShippingCalculator zoneCalculator,
                                @Qualifier("freeShipping") ShippingCalculator freeCalculator,
                                @Value("${store.free-shipping-threshold}") BigDecimal freeShippingThreshold) {
        this.zoneCalculator = zoneCalculator;
        this.freeCalculator = freeCalculator;
        this.freeShippingThreshold = freeShippingThreshold;
    }

    // SHIP-02: free when subtotal >= 5,000 and the zone allows it
    public BigDecimal quote(Governorate governorate, BigDecimal subtotal) {
        boolean bigOrder = subtotal.compareTo(freeShippingThreshold) >= 0;
        boolean zoneAllowsFree = governorate.getZone().isFreeShippingEligible();

        if (bigOrder && zoneAllowsFree) {
            return freeCalculator.calculate(governorate);
        }
        return zoneCalculator.calculate(governorate);
    }
}
