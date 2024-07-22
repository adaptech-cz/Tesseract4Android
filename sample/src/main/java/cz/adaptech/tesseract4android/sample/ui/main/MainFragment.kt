package cz.adaptech.tesseract4android.sample.ui.main

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cz.adaptech.tesseract4android.sample.Assets.extractAssets
import cz.adaptech.tesseract4android.sample.Assets.getImageBitmap
import cz.adaptech.tesseract4android.sample.Assets.getImageFile
import cz.adaptech.tesseract4android.sample.Assets.getTessDataPath
import cz.adaptech.tesseract4android.sample.Config
import cz.adaptech.tesseract4android.sample.databinding.FragmentMainBinding
import cz.adaptech.tesseract4android.sample.ui.main.MainViewModel

class MainFragment : Fragment() {
	private var binding: FragmentMainBinding? = null

	private var viewModel: MainViewModel? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

		// Copy sample image and language data to storage
		extractAssets(requireContext())

		if (!viewModel!!.isInitialized) {
			val dataPath = getTessDataPath(requireContext())
			viewModel!!.initTesseract(dataPath, Config.TESS_LANG, Config.TESS_ENGINE)
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		binding = FragmentMainBinding.inflate(inflater, container, false)
		return binding!!.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding!!.image.setImageBitmap(getImageBitmap(requireContext()))
		binding!!.start.setOnClickListener { v: View? ->
			val imageFile = getImageFile(requireContext())
			viewModel!!.recognizeImage(imageFile)
		}
		binding!!.stop.setOnClickListener { v: View? ->
			viewModel!!.stop()
		}
		binding!!.text.movementMethod = ScrollingMovementMethod()

		viewModel!!.getProcessing().observe(viewLifecycleOwner) { processing: Boolean? ->
			binding!!.start.isEnabled = !processing!!
			binding!!.stop.isEnabled = processing
		}
		viewModel!!.getProgress().observe(viewLifecycleOwner) { progress: String? ->
			binding!!.status.text = progress
		}
		viewModel!!.getResult().observe(viewLifecycleOwner) { result: String? ->
			binding!!.text.text = result
		}
	}

	companion object {
		fun newInstance(): MainFragment {
			return MainFragment()
		}
	}
}