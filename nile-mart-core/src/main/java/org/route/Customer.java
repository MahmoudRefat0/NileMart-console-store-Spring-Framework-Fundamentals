package org.route;

// email can be null (then no email is sent)
public record Customer(String fullName, String phone, String email) {
}
