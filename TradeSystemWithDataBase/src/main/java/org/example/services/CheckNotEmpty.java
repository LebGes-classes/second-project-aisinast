package org.example.services;

public class CheckNotEmpty {
    public static void checkStringNotEmpty(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Строка не может быть null");
        }
        if (str.trim().isEmpty()) {
            throw new IllegalArgumentException("Строка должна содержать хотя бы один символ");
        }
    }
}
