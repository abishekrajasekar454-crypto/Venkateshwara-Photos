package com.venkateshwaraphotos.shop.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.venkateshwaraphotos.shop.data.entity.Expense;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    long insert(Expense e);

    @Update
    void update(Expense e);

    @Delete
    void delete(Expense e);

    @Query("SELECT * FROM expenses WHERE date >= :fromTs AND date < :toTs ORDER BY date DESC, id DESC")
    LiveData<List<Expense>> observeBetween(long fromTs, long toTs);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE date >= :fromTs AND date < :toTs")
    LiveData<Double> observeTotal(long fromTs, long toTs);
}
