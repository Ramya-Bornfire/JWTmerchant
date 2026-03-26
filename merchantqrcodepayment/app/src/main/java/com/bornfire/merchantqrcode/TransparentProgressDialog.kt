package com.bornfire.merchantqrcode
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout

class TransparentProgressDialog(context: Context, resourceIdOfImage: Int) : Dialog(context, R.style.TransparentProgressDialog) {

    private var iv: ImageView

    init {
        setContentView(R.layout.layout_progress_dialog)

        iv = findViewById(R.id.iv)
        val wlmp = window?.attributes
        wlmp?.gravity = Gravity.CENTER_HORIZONTAL
        window?.attributes = wlmp
        setTitle(null)
        setCancelable(false)
        setOnCancelListener(null)
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
    }
    override fun show() {
        super.show()
        val anim = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = Animation.INFINITE
        anim.duration = 3000
        iv.startAnimation(anim)
    }
}
