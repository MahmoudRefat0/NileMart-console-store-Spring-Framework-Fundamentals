package org.route;

// thrown whenever a business rule is broken (out of stock, empty cart, ...)
public class StoreException extends RuntimeException {

    public StoreException(String message) {
        super(message);
    }
}
