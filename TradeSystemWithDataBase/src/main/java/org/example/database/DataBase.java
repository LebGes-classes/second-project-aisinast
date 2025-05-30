package org.example.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBase {
    private static final String DATABASE_URL = "jdbc:sqlite:trade-system.db";

    public static String getDatabaseUrl() {
        return DATABASE_URL;
    }

    // метод для выполнения SQL-запроса
    public static void makeSQLQuery(String sqlQuery) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sqlQuery);
                stmt.close();
            }
        } catch (SQLException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }

    // метод для подсчета количества строк, удовлетворяющих условию
    public static int countRowsWithCondition(String tableName, String fieldName, Object fieldValue) {
        int count = 0;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sqlQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", tableName, fieldName);

            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);

            if (fieldValue instanceof String) {
                pstmt.setString(1, (String) fieldValue);
            } else if (fieldValue instanceof Integer) {
                pstmt.setInt(1, (int) fieldValue);
            }

            ResultSet rs = pstmt.executeQuery();

            // возвращает id последней строки
            if (rs.next()) {
                count = rs.getInt(1);
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Ошибка при работе с базой данных: " + e.getMessage());
        }

        return count;
    }

    // метод для удаления строк в некоторой таблице по id
    public static void removeRaw(String tableName, int id) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
             String sqlQuery = "DELETE FROM " + tableName + " WHERE id " + " = ?";

             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             pstmt.setInt(1, id);
             pstmt.executeUpdate();

             pstmt.close();
        } catch (SQLException e)  {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }

    // метод для получения айди по строковому значению некоторого столбца
    public static int getId(String tableName, String fieldName, String fieldValue) {

        int id = (int) getCellValue(tableName, "id", fieldName, fieldValue);
        return id;

    }

    // метод для вывода таблицы
    public static void printAll(String tableName, int columnCount) {
        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl());) {
            String sqlQuery = "SELECT * FROM " + tableName;

            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sqlQuery);

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }

            rs.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    // метод для преобразования ячеек колонки в массив
    public static List<String> columnToList(String tableName, String columnName) {
        List<String> columnData = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = String.format("SELECT %s FROM %s", columnName, tableName);

            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            while (resultSet.next()) {
                String data = resultSet.getString(columnName);
                columnData.add(data);
            }

            conn.close();
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        return columnData;
    }

    // метод для извлечения значения ячейки по двум условиям
    public static Object getCellValueByTwoConditions(String tableName, String targetColumn, String column1,
                                                    Object value1, String column2, Object value2) {
        String sqlQuery = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?",
                targetColumn, tableName, column1, column2);

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setObject(1, value1);
            preparedStatement.setObject(2, value2);

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnType = meta.getColumnType(1);

                switch (columnType) {
                    case Types.INTEGER:
                        return rs.getInt(targetColumn);
                    case Types.VARCHAR:
                        return rs.getString(targetColumn);
                    case Types.DOUBLE:
                        return rs.getDouble(targetColumn);
                    default:
                        return rs.getObject(targetColumn);
                }
            }

            rs.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
        return null;
    }

    public static Object getCellValue(String tableName, String targetColumn, String column, Object value) {
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = String.format("SELECT %s from %s where %s = ?", targetColumn, tableName, column);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setObject(1, value);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        ResultSetMetaData meta = resultSet.getMetaData();
                        int columnType = meta.getColumnType(1);

                        switch (columnType) {
                            case Types.INTEGER:
                                return resultSet.getInt(targetColumn);
                            case Types.VARCHAR:
                                return resultSet.getString(targetColumn);
                            case Types.DOUBLE:
                                return resultSet.getDouble(targetColumn);
                            default:
                                return resultSet.getObject(targetColumn);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при поиске ячеек: " + e.getMessage());
        }

        return null;
    }

    public static int getLastRowId(String tableName) {
        int id = 0;

        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(String.format("SELECT MAX(id) FROM %s", tableName))) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении индекса последней строки: " + e.getMessage());
        }

        return id;
    }

}
