package org.route;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    // key = SKU (LinkedHashMap keeps the products in the order we added them)
    private final Map<String, Product> store = new LinkedHashMap<>();

    @PostConstruct
    void loadSeedProducts() {
        save(createProduct("PH-X5-128", "Smartphone X5 128GB", "Electronics", "16000.00", 12));
        save(createProduct("ACC-CHG-20W", "Fast Charger 20W", "Electronics", "650.00", 40));
        save(createProduct("ACC-BUDS-6", "Wireless Earbuds", "Electronics", "1100.00", 8));
        save(createProduct("FSH-GAL-001", "Cotton Galabeya", "Fashion", "900.00", 25));
        save(createProduct("HOM-PAN-28", "Non-stick Frying Pan 28cm", "Home & Kitchen", "1200.00", 3));
        save(createProduct("HOM-SPC-RCK", "Wooden Spice Rack", "Home & Kitchen", "750.00", 10));
        save(createProduct("BTY-SUN-50", "Sun Cream SPF50", "Beauty", "350.00", 0));
        save(createProduct("BK-CAI-TRI", "The Cairo Trilogy", "Books", "450.00", 15));
    }

    @PreDestroy
    void closeCatalog() {
        System.out.println("Catalog closed");
    }

    private Product createProduct(String sku, String name, String category, String price, int stock) {
        return Product.builder()
                .sku(sku)
                .name(name)
                .category(category)
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .isActive(true)
                .build();
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(store.get(sku));
    }

    @Override
    public void save(Product product) {
        store.put(product.getSku(), product);
    }
}
