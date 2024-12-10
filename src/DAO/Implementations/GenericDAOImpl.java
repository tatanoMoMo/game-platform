package DAO.Implementations;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import DAO.Interface.GenericDAO;
import config.DBConfig;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericDAOImpl<T> implements GenericDAO<T> {
    private final Class<T> entityClass;
    private final String tableName;

    public GenericDAOImpl(Class<T> entityClass, String tableName) {
        this.entityClass = entityClass;
        this.tableName = tableName;
    }

    private Connection getConnection() throws SQLException {
        return DBConfig.getConnection();
    }

    public void insert(T entity) {

        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(this.generateInsertSQL(entity)) : null) {

            if (conn != null && pstmt != null) {
                this.setParameters(pstmt, entity, false);
                pstmt.executeUpdate();
            }
        } catch (SQLException | IllegalAccessException e) {
            System.err.println("insert error：" + e.getMessage());
        }

    }

    public void update(T entity) {

        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement(this.generateUpdateSQL(entity)) : null) {

            if (conn != null && pstmt != null) {
                this.setParameters(pstmt, entity, true);
                pstmt.executeUpdate();
            }
        } catch (SQLException | IllegalAccessException e) {
            System.err.println("update error：" + e.getMessage());
        }

    }

    public void delete(T entity) {

        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn != null ? conn.prepareStatement("DELETE FROM " + this.tableName + " WHERE id = ?") : null) {

            if (conn != null && pstmt != null) {
                pstmt.setInt(1, this.getEntityId(entity));
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("delete error：" + e.getMessage());
        }

    }

    public T selectById(int id) {
        List<T> list = this.selectByCondition("id = ?", id);
        return (T)(list.isEmpty() ? null : list.get(0));
    }

    public List<T> selectAll() {
        return this.selectByCondition((String)null);
    }

    public List<T> selectByCondition(String condition, Object... params) {
        List<T> list = new ArrayList<>();
        String sql = "SELECT * FROM " + this.tableName; // 假设类中有tableName字段
        if (condition != null && !condition.isEmpty()) {
            sql += " WHERE " + condition;
        }

        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; ++i) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    T instance = entityClass.getDeclaredConstructor().newInstance();

                    for (Field field : entityClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        field.set(instance, rs.getObject(field.getName()));
                    }
                    list.add(instance);
                }
            }
        } catch (SQLException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            System.err.println("selectByCondition error：" + e.getMessage());
        }

        return list;
    }

    private void setParameters(PreparedStatement pstmt, T entity, boolean isUpdate) throws IllegalAccessException, SQLException {
        int index = 1;

        for(Field field : this.entityClass.getDeclaredFields()) {
            if (!isUpdate || !field.getName().equals("id")) {
                field.setAccessible(true);
                pstmt.setObject(index++, field.get(entity));
            }
        }

        if (isUpdate) {
            pstmt.setInt(index, this.getEntityId(entity));
        }

    }

    private String generateInsertSQL(T entity) throws IllegalAccessException {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        for(Field field : this.entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.get(entity) != null) {
                columns.append(field.getName()).append(", ");
                values.append("?, ");
            }
        }

        String colString = columns.substring(0, columns.length() - 2);
        String valString = values.substring(0, values.length() - 2);
        return "INSERT INTO " + this.tableName + " (" + colString + ") VALUES (" + valString + ")";
    }

    private String generateUpdateSQL(T entity) throws IllegalAccessException {
        StringBuilder sql = new StringBuilder("UPDATE " + this.tableName + " SET ");

        for(Field field : this.entityClass.getDeclaredFields()) {
            if (!field.getName().equals("id")) {
                sql.append(field.getName()).append(" = ?, ");
            }
        }

        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");
        return sql.toString();
    }

    private int getEntityId(T entity) throws IllegalAccessException {
        for(Field field : this.entityClass.getDeclaredFields()) {
            if (field.getName().equals("id")) {
                field.setAccessible(true);
                return (Integer)field.get(entity);
            }
        }

        throw new RuntimeException("Entity does not have an id field");
    }


    private void close(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }

            if (stmt != null) {
                stmt.close();
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("conn close error：" + e.getMessage());
        }

    }
}
