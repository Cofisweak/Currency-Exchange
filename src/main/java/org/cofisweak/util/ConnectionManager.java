package org.cofisweak.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.cofisweak.exception.DatabaseNotAvailableException;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectionManager {
    private static final String DB_URL = "db.url";
    private static final int DEFAULT_POOL_SIZE = 5;
    private static final String CREATE_DATABASE_IF_NOT_EXISTS_SQL = """
            CREATE TABLE IF NOT EXISTS currencies
            (
                id   INTEGER PRIMARY KEY AUTOINCREMENT,
                code VARCHAR(3) NOT NULL,
                full_name VARCHAR(70) NOT NULL,
                sign VARCHAR(5) NOT NULL
            );
            CREATE UNIQUE INDEX IF NOT EXISTS currency_code_index
                ON currencies (code);
            CREATE TABLE IF NOT EXISTS exchange_rates (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                base_currency_id INTEGER,
                target_currency_id INTEGER,
                rate DECIMAL(6),
                FOREIGN KEY (base_currency_id) REFERENCES currencies(id) ON DELETE CASCADE,
                FOREIGN KEY (target_currency_id) REFERENCES currencies(id) ON DELETE CASCADE
            );
            CREATE UNIQUE INDEX IF NOT EXISTS exchange_pair_index
                ON exchange_rates (base_currency_id, target_currency_id);""";
    private static BlockingQueue<Connection> connectionPool;

    static {
        initPool();
        initDefaultDatabase();
    }

    @SneakyThrows
    private static void initDefaultDatabase() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_DATABASE_IF_NOT_EXISTS_SQL)) {
            statement.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new DatabaseNotAvailableException("Unable to init database");
        }
    }

    private static Connection openConnection() throws SQLException {
        Connection connection = open();
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) ->
                        method.getName().equals("close") ?
                                connectionPool.add((Connection) proxy) :
                                method.invoke(connection, args));
    }

    private static Connection open() throws SQLException {
        return DriverManager.getConnection(PropertiesManager.get(DB_URL));
    }

    @SneakyThrows
    public static Connection getConnection() {
        return connectionPool.take();
    }

    @SneakyThrows
    private static void initPool() {
        try {
            Class.forName("org.sqlite.JDBC");
            String poolSizeString = PropertiesManager.get("db.pool.size");
            int poolSize = poolSizeString == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSizeString);
            connectionPool = new ArrayBlockingQueue<>(poolSize);

            for (int i = 0; i < poolSize; i++) {
                Connection connection = openConnection();
                connectionPool.add(connection);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            throw new DatabaseNotAvailableException("Unable to init connection pool");
        }
    }
}
