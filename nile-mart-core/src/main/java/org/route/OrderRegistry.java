package org.route;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderRegistry {

    private final List<Order> orders = new ArrayList<>();
    private int lastNumber = 0;

    // GEN-07: NM-YYYYMMDD-NNNNN, e.g. NM-20261026-00001
    public String nextOrderNumber() {
        lastNumber++;
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("NM-%s-%05d", today, lastNumber);
    }

    public void save(Order order) {
        orders.add(order);
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders);
    }

    @PreDestroy
    void printEndOfDay() {
        BigDecimal revenue = BigDecimal.ZERO;
        for (Order order : orders) {
            revenue = revenue.add(order.total());
        }
        System.out.println("End of day: " + orders.size() + " orders, revenue "
                + Money.format(revenue) + " EGP");
    }
}
