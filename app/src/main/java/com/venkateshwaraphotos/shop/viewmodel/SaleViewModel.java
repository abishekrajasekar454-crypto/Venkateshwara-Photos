package com.venkateshwaraphotos.shop.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.venkateshwaraphotos.shop.ShopApplication;
import com.venkateshwaraphotos.shop.data.model.CartLine;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.Fmt;

import java.util.ArrayList;
import java.util.List;

/** Holds the cart (survives rotation) and completes the sale through the repository. */
public class SaleViewModel extends AndroidViewModel {
    private final ShopApplication app;
    private final MutableLiveData<List<CartLine>> cart = new MutableLiveData<>(new ArrayList<CartLine>());

    public SaleViewModel(@NonNull Application application) {
        super(application);
        app = (ShopApplication) application;
    }

    public LiveData<List<CartLine>> getCart() {
        return cart;
    }

    public static double totalOf(List<CartLine> lines) {
        double t = 0;
        if (lines != null) {
            for (CartLine l : lines) t += l.lineTotal();
        }
        return t;
    }

    /** Returns an error message, or null when the line was added. Final checks repeat inside the DB transaction. */
    public String addLine(CartLine line, int availableStock) {
        if (line.quantity <= 0) return "Enter a quantity of at least 1.";
        if (line.unitPrice <= 0) return "Enter a valid price.";
        if (line.unitPrice + 0.0001 < line.minPrice) {
            return "Price cannot be below the minimum of " + Fmt.money(line.minPrice) + ".";
        }
        List<CartLine> current = new ArrayList<>(cart.getValue());
        int already = 0;
        for (CartLine c : current) {
            if (c.sameItem(line)) already += c.quantity;
        }
        if (already + line.quantity > availableStock) {
            return "Only " + availableStock + " in stock" + (already > 0 ? " (" + already + " already in cart)" : "") + ".";
        }
        for (CartLine c : current) {
            if (c.sameItem(line) && Math.abs(c.unitPrice - line.unitPrice) < 0.0001) {
                c.quantity += line.quantity;
                cart.setValue(current);
                return null;
            }
        }
        current.add(line);
        cart.setValue(current);
        return null;
    }

    public void removeLine(CartLine line) {
        List<CartLine> current = new ArrayList<>(cart.getValue());
        current.remove(line);
        cart.setValue(current);
    }

    public void completeSale(String payment, final Callback<Long> cb) {
        app.salesRepo.completeSale(cart.getValue(), payment, new Callback<Long>() {
            @Override
            public void onSuccess(Long id) {
                cart.setValue(new ArrayList<CartLine>());
                cb.onSuccess(id);
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }
}
