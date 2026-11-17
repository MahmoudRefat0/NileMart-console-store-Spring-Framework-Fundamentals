package org.route;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// prototype -> every getBean(ShoppingCart.class) gives a NEW cart
@Component
@Scope("prototype")
public class ShoppingCart {

    private final ProductService productService;
    private final int maxQuantityPerProduct;

    @Getter @Setter
    private int cartNumber;

    @Getter @Setter
    private Customer customer;

    // SKU -> quantity
    private final Map<String, Integer> lines = new LinkedHashMap<>();

    public ShoppingCart(ProductService productService,
                        @Value("${store.max-quantity-per-product}") int maxQuantityPerProduct) {
        this.productService = productService;
        this.maxQuantityPerProduct = maxQuantityPerProduct;
    }

    public void addItem(String sku, int qty) {
        if (qty <= 0) {
            throw new StoreException("Quantity must be at least 1");
        }
        Product product = productService.findBySku(sku);

        // CART-01
        if (product.getStockQuantity() == 0) {
            throw new StoreException(product.getName() + " is out of stock");
        }

        // CART-02: same product again -> add to the old quantity
        int newQty = lines.getOrDefault(sku, 0) + qty;

        // CART-03
        if (newQty > maxQuantityPerProduct) {
            throw new StoreException("You can't have more than " + maxQuantityPerProduct
                    + " of " + product.getName() + " in one cart");
        }
        if (newQty > product.getStockQuantity()) {
            throw new StoreException("Only " + product.getStockQuantity() + " "
                    + product.getName() + " available");
        }

        lines.put(sku, newQty);
    }

    // sum of the line prices
    public BigDecimal subtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> line : lines.entrySet()) {
            subtotal = subtotal.add(productService.getPrice(line.getKey(), line.getValue()));
        }
        return Money.round(subtotal);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public void clear() {
        lines.clear();
    }

    // read-only view, so nobody changes the lines without addItem()
    public Map<String, Integer> getLines() {
        return Collections.unmodifiableMap(lines);
    }
}
