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
    private static BlockingQueue<Connection> connectionPool;

    static {
        initPool();
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
