/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.epsilonworkshop.dao;

import com.epsilonworkshop.config.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author sebap
 */
public class BaseDAO {
    protected final Connection connection;

    protected BaseDAO() {
        this.connection = null;
    }

    protected BaseDAO(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        this.connection = connection;
    }

    protected Connection getConnection() throws SQLException {
        return connection != null
                ? connection
                : DBConnection.getConnection();
    }

    protected boolean hasExternalConnection() {
        return connection != null;
    }

    protected void closeConnection(Connection conn) throws SQLException {
        if (!hasExternalConnection()) {
            conn.close();
        }
    }
}
