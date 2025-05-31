package org.example.appсontrol.services;

public class Check {

    // метод для проверки строки на пустоту или null значение
    public static void stringNotEmpty(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Строка не может быть null");
        }
        if (str.trim().isEmpty()) {
            throw new IllegalArgumentException("Строка должна содержать хотя бы один символ");
        }
    }

    // метод для проверки номера телефона на корректность
    // должен содержать 12 символов и начинаться с +7
    public static void phoneNumberIsCorrect(String number) {
        if (number.length() != 12 ) {
            throw new IllegalArgumentException("Длина номера телефона должна быть 12 символов");
        }

        if (number.charAt(0) != '+' && number.charAt(1) != '7') {
            throw new IllegalArgumentException("Номер телефона должен начинаться с \"+7\"");
        }
    }
}
