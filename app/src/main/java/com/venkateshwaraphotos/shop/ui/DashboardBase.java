package com.venkateshwaraphotos.shop.ui;

import android.content.Intent;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.venkateshwaraphotos.shop.R;
import com.venkateshwaraphotos.shop.data.model.AuditRow;
import com.venkateshwaraphotos.shop.util.Callback;
import com.venkateshwaraphotos.shop.util.DateUtil;

import java.util.List;

/** Shared toolbar menu (change password, audit log, log out) for both dashboards. */
public abstract class DashboardBase extends BaseActivity {

    protected void setupMenu(MaterialToolbar toolbar, boolean owner) {
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.getMenu().findItem(R.id.action_audit).setVisible(owner);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_change_password) {
                showChangePassword();
            } else if (id == R.id.action_audit) {
                showAudit();
            } else if (id == R.id.action_logout) {
                logout();
            }
            return true;
        });
    }

    private void logout() {
        app().userRepo.logout();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void showChangePassword() {
        new FormDialog(this, "Change my password")
                .text("old", "Current password", "", FormDialog.PASSWORD)
                .text("new", "New password (min 6 characters)", "", FormDialog.PASSWORD)
                .text("confirm", "Confirm new password", "", FormDialog.PASSWORD)
                .show(f -> {
                    if (!f.text("new").equals(f.text("confirm"))) {
                        f.error("New passwords do not match.");
                        return;
                    }
                    app().userRepo.changeOwnPassword(f.text("old"), f.text("new"),
                            f.<Void>callback(() -> toast("Password changed.")));
                });
    }

    private void showAudit() {
        app().reportRepo.recentAudit(Ui.cb(this, (List<AuditRow> rows) -> {
            StringBuilder sb = new StringBuilder();
            for (AuditRow r : rows) {
                sb.append(DateUtil.dateTime(r.log.timestamp)).append("\n")
                        .append(r.userName).append(" - ").append(r.log.action).append(": ")
                        .append(r.log.details).append("\n\n");
            }
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Audit log (latest 100)")
                    .setMessage(sb.length() == 0 ? "No entries yet." : sb.toString())
                    .setPositiveButton("Close", null)
                    .show();
        }));
    }
}
