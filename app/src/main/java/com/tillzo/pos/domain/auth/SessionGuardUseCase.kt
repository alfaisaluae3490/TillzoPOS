package com.tillzo.pos.domain.auth

import com.tillzo.pos.data.local.dao.UserDao
import com.tillzo.pos.data.local.prefs.AppSetupPrefs
import javax.inject.Inject

/**
 * M3.3 — Role-Based Access Control gate.
 *
 * FIX (2026-08-06): was a stub returning `true` for everything — the whole RBAC
 * permission engine was non-functional. Now:
 *  - No users in the DB yet (pre-provisioning / first run) → allow everything
 *    (single-owner mode, backwards compatible).
 *  - Admin-only modules (settings, user management, price edit, expense tracking)
 *    require the CURRENT signed-in email to match a stored user with role Admin.
 *  - Manager modules require Admin OR Manager.
 *  - Cashier modules (POS, inventory view) are open to all roles.
 *
 * NOTE: Users_Permissions rows are created via delta restore from the Sheet;
 * the first Admin row can be created by editing the Users_Permissions tab
 * (role = "Admin") or via future User Management screen.
 */
class SessionGuardUseCase @Inject constructor(
    private val userDao: UserDao,
    private val appSetupPrefs: AppSetupPrefs
) {

    companion object {
        const val MODULE_SETTINGS = "settings"
        const val MODULE_USER_MGMT = "user_management"
        const val MODULE_PRICE_EDIT = "price_edit"
        const val MODULE_EXPENSES = "expenses"
        const val MODULE_ADMIN_DASHBOARD = "admin_dashboard"
        const val MODULE_MANAGER = "manager"
    }

    suspend fun hasPermission(systemRowId: String, requiredModuleOrOption: String): Boolean {
        // 1. No users configured yet → single-owner mode, allow everything.
        val users = userDao.getAllUsers()
        if (users.isEmpty()) return true

        // 2. Resolve current user's role from the signed-in email.
        val currentEmail = appSetupPrefs.userEmail
        val currentUser = users.firstOrNull { it.email.equals(currentEmail, ignoreCase = true) }
        val role = currentUser?.role ?: "Cashier"

        // 3. Admin-only modules.
        val adminOnly = setOf(
            MODULE_SETTINGS, MODULE_USER_MGMT, MODULE_PRICE_EDIT,
            MODULE_EXPENSES, MODULE_ADMIN_DASHBOARD
        )
        if (requiredModuleOrOption in adminOnly) return role.equals("Admin", ignoreCase = true)

        // 4. Manager-level modules.
        if (requiredModuleOrOption == MODULE_MANAGER) {
            return role.equals("Admin", ignoreCase = true) || role.equals("Manager", ignoreCase = true)
        }

        // 5. Everything else (POS, inventory, CRM view, returns…) — all roles.
        return true
    }
}
