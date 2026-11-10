package com.venkateshwaraphotos.shop.ui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.Const;
import com.venkateshwaraphotos.shop.data.entity.Category;
import com.venkateshwaraphotos.shop.data.entity.Product;
import com.venkateshwaraphotos.shop.data.model.DesignStockRow;
import com.venkateshwaraphotos.shop.data.model.VariantInfo;
import com.venkateshwaraphotos.shop.util.Fmt;
import com.venkateshwaraphotos.shop.viewmodel.CatalogViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Cascading Category -> Product -> Variant (-> Design) drop-downs, loaded on demand from the database.
 * Nothing is hard-coded: whatever the owner created is what appears. Bind it to a view that includes
 * layout/view_item_selector.xml. Call dispose() when the host screen/dialog goes away.
 */
public class ItemSelector {

    public interface Listener {
        void onChanged();
    }

    private interface PosListener {
        void onPos(int position);
    }

    private final Context ctx;
    private final CatalogViewModel vm;
    private final Listener listener;
    private final Spinner spCategory;
    private final Spinner spProduct;
    private final Spinner spVariant;
    private final Spinner spDesign;
    private final View designGroup;

    private List<Category> categories = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private List<VariantInfo> variants = new ArrayList<>();
    private List<DesignStockRow> designs = new ArrayList<>();
    private Product selProduct;
    private VariantInfo selVariant;
    private DesignStockRow selDesign;
    private long pendingVariantId;
    private long pendingDesignId;
    private boolean disposed;

    public ItemSelector(Context ctx, View root, CatalogViewModel vm, Listener listener) {
        this.ctx = ctx;
        this.vm = vm;
        this.listener = listener;
        spCategory = root.findViewById(R.id.spCategory);
        spProduct = root.findViewById(R.id.spProduct);
        spVariant = root.findViewById(R.id.spVariant);
        spDesign = root.findViewById(R.id.spDesign);
        designGroup = root.findViewById(R.id.designGroup);

        spCategory.setOnItemSelectedListener(new PosWatcher(this::onCategory));
        spProduct.setOnItemSelectedListener(new PosWatcher(this::onProduct));
        spVariant.setOnItemSelectedListener(new PosWatcher(this::onVariant));
        spDesign.setOnItemSelectedListener(new PosWatcher(this::onDesign));

        fill(spProduct, "Select product", new ArrayList<String>(), 0);
        fill(spVariant, "Select variant", new ArrayList<String>(), 0);
        vm.loadActiveCategories(Ui.cb(ctx, (List<Category> list) -> {
            if (disposed) return;
            categories = list;
            List<String> names = new ArrayList<>();
            for (Category c : list) names.add(c.name);
            fill(spCategory, "Select category", names, 0);
        }));
    }

    public void dispose() {
        disposed = true;
    }

    public VariantInfo getVariant() {
        return selVariant;
    }

    public DesignStockRow getDesign() {
        return selDesign;
    }

    public boolean needsDesign() {
        return selVariant != null && Const.TRACK_DESIGN.equals(selVariant.trackingMode);
    }

    /** True when a variant (and a design, if the product needs one) has been chosen. */
    public boolean isComplete() {
        return selVariant != null && (!needsDesign() || selDesign != null);
    }

    public int availableStock() {
        if (selVariant == null) return 0;
        if (needsDesign()) return selDesign == null ? 0 : selDesign.quantity;
        return selVariant.quantity;
    }

    /** Reloads stock numbers for the current selection (e.g. after a sale), keeping the selection. */
    public void refreshStock() {
        if (selProduct == null || selVariant == null) return;
        pendingVariantId = selVariant.variantId;
        pendingDesignId = selDesign == null ? 0 : selDesign.designId;
        loadVariants(selProduct.id);
    }

    // ----- internal -----
    private void onCategory(int pos) {
        selProduct = null;
        selVariant = null;
        selDesign = null;
        clearDesigns();
        variants = new ArrayList<>();
        fill(spVariant, "Select variant", new ArrayList<String>(), 0);
        products = new ArrayList<>();
        fill(spProduct, "Select product", new ArrayList<String>(), 0);
        if (pos > 0 && pos - 1 < categories.size()) {
            vm.loadActiveProducts(categories.get(pos - 1).id, Ui.cb(ctx, (List<Product> list) -> {
                if (disposed) return;
                products = list;
                List<String> names = new ArrayList<>();
                for (Product p : list) names.add(p.name);
                fill(spProduct, "Select product", names, 0);
            }));
        }
        listener.onChanged();
    }

    private void onProduct(int pos) {
        selProduct = pos > 0 && pos - 1 < products.size() ? products.get(pos - 1) : null;
        selVariant = null;
        selDesign = null;
        clearDesigns();
        variants = new ArrayList<>();
        fill(spVariant, "Select variant", new ArrayList<String>(), 0);
        if (selProduct != null) {
            loadVariants(selProduct.id);
        }
        listener.onChanged();
    }

    private void loadVariants(long productId) {
        vm.loadSellableVariants(productId, Ui.cb(ctx, (List<VariantInfo> list) -> {
            if (disposed) return;
            variants = list;
            List<String> names = new ArrayList<>();
            int keep = 0;
            for (int i = 0; i < list.size(); i++) {
                VariantInfo v = list.get(i);
                names.add(v.variantName + "  |  " + Fmt.money(v.sellingPrice) + "  |  stock " + v.quantity);
                if (pendingVariantId != 0 && v.variantId == pendingVariantId) keep = i + 1;
            }
            pendingVariantId = 0;
            fill(spVariant, "Select variant", names, keep);
        }));
    }

    private void onVariant(int pos) {
        selVariant = pos > 0 && pos - 1 < variants.size() ? variants.get(pos - 1) : null;
        selDesign = null;
        clearDesigns();
        if (needsDesign()) {
            designGroup.setVisibility(View.VISIBLE);
            final VariantInfo v = selVariant;
            vm.loadDesigns(v.productId, v.variantId, Ui.cb(ctx, (List<DesignStockRow> list) -> {
                if (disposed || selVariant == null || selVariant.variantId != v.variantId) return;
                designs = list;
                List<String> names = new ArrayList<>();
                int keep = 0;
                for (int i = 0; i < list.size(); i++) {
                    DesignStockRow d = list.get(i);
                    names.add(d.designName + "  |  stock " + d.quantity);
                    if (pendingDesignId != 0 && d.designId == pendingDesignId) keep = i + 1;
                }
                pendingDesignId = 0;
                fill(spDesign, "Select design", names, keep);
            }));
        }
        listener.onChanged();
    }

    private void onDesign(int pos) {
        selDesign = pos > 0 && pos - 1 < designs.size() ? designs.get(pos - 1) : null;
        listener.onChanged();
    }

    private void clearDesigns() {
        designs = new ArrayList<>();
        designGroup.setVisibility(View.GONE);
        fill(spDesign, "Select design", new ArrayList<String>(), 0);
    }

    private void fill(Spinner sp, String prompt, List<String> names, int selected) {
        List<String> all = new ArrayList<>();
        all.add(prompt);
        all.addAll(names);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, all);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        if (selected > 0 && selected < all.size()) sp.setSelection(selected);
    }

    private static class PosWatcher implements AdapterView.OnItemSelectedListener {
        private final PosListener l;

        PosWatcher(PosListener l) {
            this.l = l;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            l.onPos(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            l.onPos(0);
        }
    }
}
