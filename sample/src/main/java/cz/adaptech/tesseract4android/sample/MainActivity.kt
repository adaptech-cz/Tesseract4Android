package cz.adaptech.tesseract4android.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.adaptech.tesseract4android.sample.ui.main.MainFragment

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		if (savedInstanceState == null) {
			supportFragmentManager.beginTransaction()
				.replace(R.id.container, MainFragment.newInstance())
				.commitNow()
		}
	}
}