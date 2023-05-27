package cz.adaptech.tesseract4android.sample;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Assets {

    /**
     * Returns locally accessible directory where our assets are extracted.
     */
    @NonNull
    public static File getLocalDir(@NonNull Context context) {
        return context.getFilesDir();
    }

    /**
     * Returns locally accessible directory path which contains the "tessdata" subdirectory
     * with *.traineddata files.
     */
    @NonNull
    public static String getTessDataPath(@NonNull Context context) {
        return getLocalDir(context).getAbsolutePath();
    }

    @NonNull
    public static File getImageFile(@NonNull Context context) {
        return new File(getLocalDir(context), Config.IMAGE_NAME);
    }

    @Nullable
    public static Bitmap getImageBitmap(@NonNull Context context) {
        return BitmapFactory.decodeFile(getImageFile(context).getAbsolutePath());
    }

    public static void extractAssets(@NonNull Context context) {
        AssetManager am = context.getAssets();

        File localDir = getLocalDir(context);
        if (!localDir.exists() && !localDir.mkdir()) {
            throw new RuntimeException("Can't create directory " + localDir);
        }

        File tessDir = new File(getTessDataPath(context), "tessdata");
        if (!tessDir.exists() && !tessDir.mkdir()) {
            throw new RuntimeException("Can't create directory " + tessDir);
        }

        // Extract all assets to our local directory.
        // All *.traineddata into "tessdata" subdirectory, other files into root.
        try {
            for (String assetName : am.list("")) {
                final File targetFile;
                if (assetName.endsWith(".traineddata")) {
                    targetFile = new File(tessDir, assetName);
                } else {
                    targetFile = new File(localDir, assetName);
                }
                if (!targetFile.exists()) {
                    copyFile(am, assetName, targetFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(@NonNull AssetManager am, @NonNull String assetName,
                                 @NonNull File outFile) {
        try (
                InputStream in = am.open(assetName);
                OutputStream out = new FileOutputStream(outFile)
        ) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
