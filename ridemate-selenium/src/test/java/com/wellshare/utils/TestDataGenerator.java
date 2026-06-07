package com.wellshare.utils;

import com.github.javafaker.Faker;

/**
 * Generates random test data using JavaFaker
 * Ensures tests don't conflict with each other
 */
public class TestDataGenerator {

    private static final Faker faker = new Faker();

    /** Random full name */
    public static String randomName() {
        return faker.name().fullName();
    }

    /** Random unique email (won't clash with existing users) */
    public static String randomEmail() {
        return "test_" + faker.number().digits(6) + "@wellshare.com";
    }

    /** Random 10-digit Indian phone number */
    public static String randomPhone() {
        return "9" + faker.number().digits(9);
    }

    /** Random city name */
    public static String randomCity() {
        String[] cities = {
            "Chennai", "Bangalore", "Mumbai", "Delhi",
            "Hyderabad", "Pune", "Kolkata", "Coimbatore"
        };
        return cities[faker.random().nextInt(cities.length)];
    }

    /** Fixed strong password (meets validation rules) */
    public static String validPassword() {
        return "Test@1234";
    }

    /** Random vehicle plate number */
    public static String randomPlateNumber() {
        return "TN" + faker.number().digits(2) + "AB" + faker.number().digits(4);
    }
}
