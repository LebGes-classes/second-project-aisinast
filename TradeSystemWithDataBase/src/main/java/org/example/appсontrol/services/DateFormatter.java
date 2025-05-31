package org.example.appсontrol.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatter {

    // метод для получения времени
    public static String getDate() {
        // получаем текущее время
        LocalDateTime localDateTime = LocalDateTime.now();

        // приводим к нужному виду и преобразовываем в строку
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm");
        String formattedDateTime = localDateTime.format(formatter);

        return formattedDateTime;
    }
}
