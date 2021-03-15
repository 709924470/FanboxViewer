package cn.settile.fanboxviewer;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;

import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;

import cn.settile.fanboxviewer.Network.CustomPicassoLoader;
import cn.settile.fanboxviewer.Network.DownloadRequester;
import cn.settile.fanboxviewer.Util.Constants;

import static cn.settile.fanboxviewer.Util.Util.toBitmap;

//import static cn.settile.fanboxviewer.Network.DownloadManager.queue;

public class ImageViewActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getName();
    private List<String> images;
    private String detail;
    private int pos;
    private List<Integer> colors = new ArrayList<>();
    private List<String> thumbs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        Intent i = getIntent();
        this.images = i.getStringArrayListExtra("Images");
        this.thumbs = i.getStringArrayListExtra("Thumbnails");
        this.detail = i.getStringExtra("Details");
        Log.d(TAG, this.detail);
        this.pos = i.getIntExtra("Position", 0);

        for (int index = 0; index < thumbs.size(); index++) {
            colors.add(Color.BLACK);
        }

        ScrollGalleryView view = findViewById(R.id.image_view_main);
        view.setThumbnailSize(200)
                .setZoom(true)
                .withHiddenThumbnails(false)
                .hideThumbnailsOnClick(false)
                .addOnImageLongClickListener(position -> {
                    Snackbar.make(getWindow().getDecorView(), "Downloading", Snackbar.LENGTH_LONG).show();
                    try {
                        String extension = images.get(position)
                                .substring(images.get(position).lastIndexOf('.'));
                        String name = detail + "_" + position + extension;
                        new DownloadRequester((DownloadManager) getSystemService(DOWNLOAD_SERVICE))
                                .downloadWithCookie(images.get(position), name, Constants.Cookie);
                    } catch (Exception ex) {
                        Log.e(TAG, "onCreate: ", ex);
                    }
                })
                .setFragmentManager(getSupportFragmentManager());
        for (int index = 0; index < images.size(); index++) {
            CustomPicassoLoader mi = new CustomPicassoLoader(this, thumbs.get(index));


            final int index1 = index;

            mi.onLoaded((b) -> {
                colors.set(index1,
                        Palette.from(toBitmap(b))
                                .generate()
                                .getDarkMutedColor(Color.BLACK));
            });
            view.addMedia(
                    MediaInfo.mediaLoader(mi)
            );
        }
        view.setCurrentItem(pos);
        new Thread(() -> {
            while (colors.size() - 1 < pos) {
            }
            view.setBackgroundColor(colors.get(pos));
            view.invalidate();
        }).start();
        view.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "size=" + (colors.size() - 1) + " pos=" + position);
                if (colors.size() - 1 < position) {
                    return;
                }
                view.setBackgroundColor(colors.get(position));
                view.invalidate();
            }
        });

        FloatingActionButton fab = findViewById(R.id.image_view_download);
        fab.setOnClickListener(v -> {
            Snackbar.make(getWindow().getDecorView(), "Downloading", Snackbar.LENGTH_LONG).show();
            int position = view.getCurrentItem();
            try {
                String extension = images.get(position)
                        .substring(images.get(position).lastIndexOf('.'));
                String name = detail + "_" + position + extension;
                new DownloadRequester((DownloadManager) getSystemService(DOWNLOAD_SERVICE))
                        .downloadWithCookie(images.get(position), name, Constants.Cookie);
            } catch (Exception ex) {
                Log.e(TAG, "onCreate: ", ex);
            }
        });
    }
}
