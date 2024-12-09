package DAO.Interface;

import java.util.List;

public interface GenericDAO<T> {
    void insert(T var1);

    void update(T var1);

    void delete(T var1);

    T selectById(int var1);

    List<T> selectAll();

    List<T> selectByCondition(String var1, Object... var2);
}
