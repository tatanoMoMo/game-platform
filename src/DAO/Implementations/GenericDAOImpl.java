package DAO.Implementations;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import DAO.Interface.GenericDAO;

import java.lang.reflect.Field;
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

    public void insert(T entity) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = this.getConnection();
            String sql = this.generateInsertSQL(entity);
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                this.setParameters(pstmt, entity, false);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.close(conn, pstmt, (ResultSet)null);
        }

    }

    public void update(T entity) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = this.getConnection();
            String sql = this.generateUpdateSQL(entity);
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                this.setParameters(pstmt, entity, true);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.close(conn, pstmt, (ResultSet)null);
        }

    }

    public void delete(T entity) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = this.getConnection();
            String sql = "DELETE FROM " + this.tableName + " WHERE id = ?";
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, this.getEntityId(entity));
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.close(conn, pstmt, (ResultSet)null);
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
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<T> list = new ArrayList();

        try {
            conn = this.getConnection();
            String var10000 = this.tableName;
            String sql = "SELECT * FROM " + var10000 + (condition != null ? " WHERE " + condition : "");
            if (conn != null) {
                pstmt = conn.prepareStatement(sql);

                for(int i = 0; i < params.length; ++i) {
                    pstmt.setObject(i + 1, params[i]);
                }

                rs = pstmt.executeQuery();
            }

            if (rs != null) {
                while(rs.next()) {
                    T instance = (T)this.entityClass.getDeclaredConstructor().newInstance();

                    for(Field field : this.entityClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        field.set(instance, rs.getObject(field.getName()));
                    }

                    list.add(instance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.close(conn, pstmt, rs);
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

    private Connection getConnection() throws SQLException {
        return null;
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
            e.printStackTrace();
        }

    }
}
