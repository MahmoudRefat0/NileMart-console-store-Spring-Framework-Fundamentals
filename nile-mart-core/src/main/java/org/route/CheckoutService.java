package org.route;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final ProductService productService;
    private final ShippingQuoteService shippingQuoteService;
    private final TaxCalculator taxCalculator;
    private final OrderRegistry orderRegistry;
    private final List<NotificationSender> senders;   // Spring injects SMS + Email

    public Order placeOrder(ShoppingCart cart, Governorate governorate) {
        // 1. ORD-02: no empty carts
        if (cart.isEmpty()) {
            throw new StoreException("Cart " + cart.getCartNumber() + " is empty");
        }

        // 2. ORD-03: check the stock of EVERY line before changing anything
        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            Product product = productService.findBySku(line.getKey());
            if (product.getStockQuantity() < line.getValue()) {
                throw new StoreException("Checkout failed: only " + product.getStockQuantity()
                        + " " + product.getName() + " left in stock");
            }
        }

        // 3. totals
        BigDecimal subtotal = cart.subtotal();
        BigDecimal vat = taxCalculator.calculateVat(subtotal);
        BigDecimal shipping = shippingQuoteService.quote(governorate, subtotal);
        BigDecimal total = Money.round(subtotal.add(vat).add(shipping));

        // 4. ORD-06: all lines passed step 2, so we reduce all of them (all or none)
        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            productService.reduceStock(line.getKey(), line.getValue());
        }

        // 5. save the order (copy the lines because the cart is cleared at the end)
        Order order = new Order(orderRegistry.nextOrderNumber(), cart.getCustomer(), governorate,
                new LinkedHashMap<>(cart.getLines()), subtotal, vat, shipping, total);
        orderRegistry.save(order);

        // 6. NOT-04: send the confirmation through every sender
        String message = "Order " + order.orderNumber() + " confirmed. Total "
                + Money.format(total) + " EGP";
        for (NotificationSender sender : senders) {
            sender.send(cart.getCustomer(), message);
        }

        // 7. empty the cart
        cart.clear();
        return order;
    }
}
