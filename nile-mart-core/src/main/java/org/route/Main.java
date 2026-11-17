package org.route;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private AnnotationConfigApplicationContext context;
    private ProductService productService;
    private CheckoutService checkoutService;

    private final Map<Integer, ShoppingCart> carts = new LinkedHashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    private int nextCartNumber = 1;

    void main() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        productService = context.getBean(ProductService.class);
        checkoutService = context.getBean(CheckoutService.class);

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            // a StoreException never ends the program, we just print the message
            try {
                switch (choice) {
                    case "1" -> showProducts();
                    case "2" -> createCart();
                    case "3" -> addProductToCart();
                    case "4" -> showCart();
                    case "5" -> checkOut();
                    case "0" -> running = false;
                    default -> System.out.println("Please choose from the menu");
                }
            } catch (StoreException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        // close the context -> @PreDestroy methods run (end of day + catalog closed)
        context.close();
    }

    private void printMenu() {
        System.out.println();
        System.out.println("========== NILEMART ==========");
        System.out.println("1. Show products");
        System.out.println("2. Create a cart");
        System.out.println("3. Add a product to a cart");
        System.out.println("4. Show a cart");
        System.out.println("5. Check out a cart");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    // 1
    private void showProducts() {
        System.out.printf("%-12s %-26s %-15s %10s %6s%n", "SKU", "Name", "Category", "Price", "Stock");
        for (Product p : productService.findAll()) {
            System.out.printf("%-12s %-26s %-15s %10s %6d%n",
                    p.getSku(), p.getName(), p.getCategory(), Money.format(p.getPrice()), p.getStockQuantity());
        }
    }

    // 2
    private void createCart() {
        String name = readText("Customer full name: ");
        String phone = readText("Phone: ");
        String email = readText("Email (Enter to skip): ");
        if (email.isEmpty()) {
            email = null;
        }

        // prototype bean -> a new cart every time
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        cart.setCartNumber(nextCartNumber++);
        cart.setCustomer(new Customer(name, phone, email));
        carts.put(cart.getCartNumber(), cart);

        System.out.println("Cart " + cart.getCartNumber() + " created for " + name);
    }

    // 3
    private void addProductToCart() {
        ShoppingCart cart = findCart(readNumber("Cart number: "));
        String sku = readText("Product SKU: ").toUpperCase();
        int qty = readNumber("Quantity: ");

        cart.addItem(sku, qty);
        Product product = productService.findBySku(sku);
        System.out.println("Added " + qty + " x " + product.getName() + " to cart " + cart.getCartNumber());
    }

    // 4
    private void showCart() {
        ShoppingCart cart = findCart(readNumber("Cart number: "));
        System.out.println("Cart " + cart.getCartNumber() + " - " + cart.getCustomer().fullName());

        if (cart.isEmpty()) {
            System.out.println("  (empty)");
            return;
        }
        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            Product product = productService.findBySku(line.getKey());
            System.out.printf("  %2d x %-26s %12s%n", line.getValue(), product.getName(),
                    Money.format(productService.getPrice(line.getKey(), line.getValue())));
        }
        System.out.println("  Subtotal: " + Money.format(cart.subtotal()) + " EGP");
    }

    // 5
    private void checkOut() {
        ShoppingCart cart = findCart(readNumber("Cart number: "));
        Governorate governorate = chooseGovernorate();

        Order order = checkoutService.placeOrder(cart, governorate);

        System.out.println("Order " + order.orderNumber() + " placed");
        System.out.println("  Subtotal : " + Money.format(order.subtotal()));
        System.out.println("  VAT 14%  : " + Money.format(order.vat()));
        System.out.println("  Shipping : " + Money.format(order.shippingFee()));
        System.out.println("  Total    : " + Money.format(order.total()) + " EGP");
    }

    private Governorate chooseGovernorate() {
        Governorate[] all = Governorate.values();
        for (int i = 0; i < all.length; i++) {
            System.out.printf("%2d. %-16s", i + 1, all[i].getDisplayName());
            if ((i + 1) % 3 == 0) {
                System.out.println();
            }
        }
        System.out.println();

        int number = readNumber("Ship to (number): ");
        if (number < 1 || number > all.length) {
            throw new StoreException("No governorate with number " + number);
        }
        return all[number - 1];
    }

    private ShoppingCart findCart(int cartNumber) {
        ShoppingCart cart = carts.get(cartNumber);
        if (cart == null) {
            throw new StoreException("Cart " + cartNumber + " not found");
        }
        return cart;
    }

    private String readText(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }

    private int readNumber(String label) {
        String text = readText(label);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new StoreException("'" + text + "' is not a number");
        }
    }
}
