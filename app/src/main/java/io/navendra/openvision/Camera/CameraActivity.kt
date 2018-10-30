package io.navendra.openvision.Camera

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.navendra.openvision.R

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                Camera2Fragment.newInstance()
            )
            .commit()
    }
}
