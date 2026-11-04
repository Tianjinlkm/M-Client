package me.weishu.kernelsu.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.ComponentActivity

/**
 * M-Client 启动页（莱茵生命动画占位）。
 *
 * 设计目标：绝对不能卡死/闪退。
 *  - 所有 UI 构建都包在 try-catch 里，任何一步失败都不影响跳主界面。
 *  - 无论动画是否加载成功，Handler 在 2000ms 后必然跳进 MainActivity。
 *  - 使用纯黑主题（Theme.MClient.Splash），不走系统 SplashScreen，避免 Android 12+ 白屏。
 */
class SplashActivity : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val jumpTask = Runnable { goToMain() }
    @Volatile private var jumped = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 保底：无论如何 2000ms 后跳主界面（动画失败也能进）
        handler.postDelayed(jumpTask, 2000)

        try {
            setContentView(R.layout.activity_splash)

            // 简单的淡入 + 轻微缩放，纯视觉，失败也不影响跳转
            try {
                val icon = findViewById<ImageView>(R.id.splash_icon)
                icon?.apply {
                    alpha = 0f
                    scaleX = 0.85f
                    scaleY = 0.85f
                    animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(700)
                        .start()
                }
            } catch (_: Throwable) {
                // 动画失败忽略
            }
        } catch (_: Throwable) {
            // 布局/资源加载失败：直接进主界面，不能卡在这里
            goToMain()
        }
    }

    override fun onPause() {
        super.onPause()
        // 离开页面（如被系统最近任务/锁屏）也确保跳转，避免回头卡住
        goToMain()
    }

    private fun goToMain() {
        if (jumped) return
        jumped = true
        handler.removeCallbacks(jumpTask)
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Throwable) {
            // 极端情况下连 MainActivity 都起不来，至少别崩：退回系统桌面
            try {
                startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
            } catch (_: Throwable) {
            }
        }
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacks(jumpTask)
        super.onDestroy()
    }
}
