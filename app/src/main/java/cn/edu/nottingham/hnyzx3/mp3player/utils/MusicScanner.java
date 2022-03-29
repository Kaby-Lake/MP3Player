package cn.edu.nottingham.hnyzx3.mp3player.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import cn.edu.nottingham.hnyzx3.mp3player.components.musicItem.MusicItemViewModel;

public class MusicScanner {

    /**
     * scan all music in the external storage (sdcard) in the given path
     * and return a list of musicItemViewModel
     *
     * @param path the path to scan
     */
    public static ArrayList<MusicItemViewModel> scanMusicFromExternalPath(String path) {
        File dir = new File(Environment.getExternalStorageDirectory(), path);
        if (dir.exists() && dir.isDirectory() && dir.canRead() && dir.listFiles() != null) {
            ArrayList<MusicItemViewModel> musics = new ArrayList<>();
            try {
                for (File file : Objects.requireNonNull(dir.listFiles())) {
                    if (file.isFile() && file.getName().endsWith(".mp3")) {
                        // use MediaMetadataRetriever to get the music info
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(file.getAbsolutePath());
                        MusicItemViewModel music = new MusicItemViewModel();

                        byte[] rawCover = retriever.getEmbeddedPicture();
                        Bitmap originalCover = BitmapFactory.decodeByteArray(rawCover, 0, rawCover.length);
                        // scale to fit 300 * 300
                        music.cover = Bitmap.createScaledBitmap(originalCover, 300, 300, true);
                        music.name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        music.artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        music.duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                        music.path = file.getAbsolutePath();
                        musics.add(music);
                    }
                }
            } catch (Exception ignored) {
            }
            return musics;
        } else {
            // if the path is not a directory or cannot read, return an empty list
            return new ArrayList<>();
        }
    }

    /**
     * make an external music item view model from the given uri
     */
    public static MusicItemViewModel getMusicFromUri(Uri uri) {
        MusicItemViewModel music = new MusicItemViewModel();
        try {
            String path = uri.getPath();
            music.path = path;
            music.name = path.split("/")[path.split("/").length - 1];
            music.artist = "External Music File";
            music.uri = uri;
        } catch (Exception ignored) {

        }
        return music;
    }
}
