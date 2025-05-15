package org.example.services;

public class Check {
    public static void stringNotEmpty(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Строка не может быть null");
        }
        if (str.trim().isEmpty()) {
            throw new IllegalArgumentException("Строка должна содержать хотя бы один символ");
        }
    }

    public static void phoneNumberIsCorrect(String number) {
        if (number.length() == 12 ) {
            throw new IllegalArgumentException("Длина номера телефона должна быть 12 символов");
        }

        if (number.charAt(0) == '+' && number.charAt(1) == '7') {
            throw new IllegalArgumentException("Номер телефона должен начинаться с \"+7\"");
        }
    }
}
