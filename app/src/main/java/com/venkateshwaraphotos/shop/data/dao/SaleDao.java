package com.venkateshwaraphotos.shop.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.venkateshwaraphotos.shop.data.entity.Sale;
import com.venkateshwaraphotos.shop.data.entity.SaleItem;
import com.venkateshwaraphotos.shop.data.model.PaymentTotal;
import com.venkateshwaraphotos.shop.data.model.SaleRow;
import com.venkateshwaraphotos.shop.data.model.SalesSummary;
import com.venkateshwaraphotos.shop.data.model.TopItem;

import java.util.List;

@Dao
public interface SaleDao {
    @Insert
    long insertSale(Sale s);

    @Insert
    void insertItem(SaleItem i);

    @Query("UPDATE sales SET totalAmount = :total, totalCost = :cost, itemCount = :units WHERE id = :id")
    void updateTotals(long id, double total, double cost, int units);

    /** employeeId = 0 means all employees. */
    @Query("SELECT s.*, u.fullName AS employeeName FROM sales s JOIN users u ON u.id = s.employeeId "
            + "WHERE s.timestamp >= :fromTs AND s.timestamp < :toTs AND (:employeeId = 0 OR s.employeeId = :employeeId) "
            + "ORDER BY s.timestamp DESC, s.id DESC")
    LiveData<List<SaleRow>> observeSales(long fromTs, long toTs, long employeeId);

    @Query("SELECT s.*, u.fullName AS employeeName FROM sales s JOIN users u ON u.id = s.employeeId "
            + "WHERE (:employeeId = 0 OR s.employeeId = :employeeId) ORDER BY s.timestamp DESC, s.id DESC LIMIT :maxRows")
    LiveData<List<SaleRow>> observeRecent(long employeeId, int maxRows);

    @Query("SELECT s.*, u.fullName AS employeeName FROM sales s JOIN users u ON u.id = s.employeeId WHERE s.id = :id")
    LiveData<SaleRow> observeSale(long id);

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId ORDER BY id")
    LiveData<List<SaleItem>> observeItems(long saleId);

    @Query("SELECT COALESCE(SUM(totalAmount), 0) AS revenue, COALESCE(SUM(totalCost), 0) AS cogs, COUNT(*) AS saleCount "
            + "FROM sales WHERE timestamp >= :fromTs AND timestamp < :toTs AND (:employeeId = 0 OR employeeId = :employeeId)")
    LiveData<SalesSummary> observeSummary(long fromTs, long toTs, long employeeId);

    @Query("SELECT paymentMode AS mode, COALESCE(SUM(totalAmount), 0) AS amount, COUNT(*) AS cnt FROM sales "
            + "WHERE timestamp >= :fromTs AND timestamp < :toTs GROUP BY paymentMode ORDER BY amount DESC")
    LiveData<List<PaymentTotal>> observePayments(long fromTs, long toTs);

    @Query("SELECT si.productName || ' - ' || si.variantName AS name, SUM(si.quantity) AS qty, "
            + "SUM(si.quantity * si.unitPrice) AS revenue FROM sale_items si JOIN sales s ON s.id = si.saleId "
            + "WHERE s.timestamp >= :fromTs AND s.timestamp < :toTs GROUP BY si.variantId ORDER BY revenue DESC LIMIT 5")
    LiveData<List<TopItem>> observeTopItems(long fromTs, long toTs);
}
