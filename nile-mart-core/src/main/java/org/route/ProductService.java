package org.route;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final PricingService pricingService;

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product findBySku(String sku) {
        return repository.findBySku(sku)
                .orElseThrow(() -> new StoreException("Product not found: " + sku));
    }

    // line price comes from the pricing service (Standard or Promotional)
    public BigDecimal getPrice(String sku, int qty) {
        Product product = findBySku(sku);
        if (!product.isActive()) {
            throw new StoreException("Product isn't available: " + sku);
        }
        return Money.round(pricingService.getPrice(product, qty));
    }

    public void reduceStock(String sku, int qty) {
        Product product = findBySku(sku);
        if (product.getStockQuantity() < qty) {
            throw new StoreException("Not enough stock for " + product.getName()
                    + ": only " + product.getStockQuantity() + " available");
        }
        product.setStockQuantity(product.getStockQuantity() - qty);
        repository.save(product);
    }
}
