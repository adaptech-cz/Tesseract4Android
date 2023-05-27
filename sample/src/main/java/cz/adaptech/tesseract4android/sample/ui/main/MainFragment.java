package cz.adaptech.tesseract4android.sample.ui.main;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;

import cz.adaptech.tesseract4android.sample.Assets;
import cz.adaptech.tesseract4android.sample.Config;
import cz.adaptech.tesseract4android.sample.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    private MainViewModel viewModel;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Copy sample image and language data to storage
        Assets.extractAssets(requireContext());

        if (!viewModel.isInitialized()) {
            String dataPath = Assets.getTessDataPath(requireContext());
            viewModel.initTesseract(dataPath, Config.TESS_LANG, Config.TESS_ENGINE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.image.setImageBitmap(Assets.getImageBitmap(requireContext()));
        binding.start.setOnClickListener(v -> {
            File imageFile = Assets.getImageFile(requireContext());
            viewModel.recognizeImage(imageFile);
        });
        binding.stop.setOnClickListener(v -> {
            viewModel.stop();
        });
        binding.text.setMovementMethod(new ScrollingMovementMethod());

        viewModel.getProcessing().observe(getViewLifecycleOwner(), processing -> {
            binding.start.setEnabled(!processing);
            binding.stop.setEnabled(processing);
        });
        viewModel.getProgress().observe(getViewLifecycleOwner(), progress -> {
            binding.status.setText(progress);
        });
        viewModel.getResult().observe(getViewLifecycleOwner(), result -> {
            binding.text.setText(result);
        });
    }
}