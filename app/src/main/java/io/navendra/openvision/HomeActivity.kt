package io.navendra.openvision

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(R.id.container, Camera2Fragment.newInstance())
            .commit()
    }
}
