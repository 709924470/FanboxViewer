package cn.settile.fanboxviewer.ViewComponents

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.webkit.CookieManager
import cn.settile.fanboxviewer.MainActivity
import cn.settile.fanboxviewer.SplashActivity

class LogoutDialog(ctx: Context) : AlertDialog(ctx) {
    init {
        setTitle("Do you want to logout?")
        setMessage("You won't lost any of your account data.")
        setButton(BUTTON_NEGATIVE, "Logout") { dialogInterface: DialogInterface, i: Int ->

            //Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
            val cm = CookieManager.getInstance()
            cm.removeAllCookies { it: Boolean? -> }

            val i = Intent(ctx, SplashActivity::class.java)
            ctx.startActivity(i)
            (ctx as MainActivity).finish()
        }
        setButton(BUTTON_POSITIVE, "Cancel", { dialogInterface: DialogInterface, i: Int ->

        })
    }
}