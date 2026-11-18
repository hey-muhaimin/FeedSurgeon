package com.example.service

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object InstalledAppDetector {
    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager ?: return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun getInstalledPackages(context: Context, packageNames: Iterable<String>): Set<String> {
        return packageNames.filterTo(LinkedHashSet()) { pkg ->
            isPackageInstalled(context, pkg)
        }
    }
}
