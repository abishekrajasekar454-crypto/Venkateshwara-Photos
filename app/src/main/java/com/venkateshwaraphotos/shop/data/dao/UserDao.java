package com.venkateshwaraphotos.shop.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.venkateshwaraphotos.shop.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User u);

    @Update
    void update(User u);

    @Query("SELECT * FROM users WHERE username = :username COLLATE NOCASE LIMIT 1")
    User findByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :id")
    User findById(long id);

    @Query("SELECT COUNT(*) FROM users")
    int count();

    @Query("SELECT * FROM users WHERE role = 'EMPLOYEE' ORDER BY fullName")
    LiveData<List<User>> observeEmployees();

    @Query("SELECT * FROM users ORDER BY fullName")
    LiveData<List<User>> observeAll();
}
