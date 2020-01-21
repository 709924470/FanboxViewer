package cn.settile.fanboxviewer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.Network.CustomPicassoLoader;

import static cn.settile.fanboxviewer.Util.Util.createImageFile;
import static cn.settile.fanboxviewer.Util.Util.galleryAddPic;
import static cn.settile.fanboxviewer.Util.Util.toBitmap;

public class ImageViewActivity extends AppCompatActivity {

    private List<String> images;
    private String detail;

    private final String TAG = this.getClass().getName();
    private int pos;
    private List<Integer> colors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        Intent i = getIntent();
        this.images = i.getStringArrayListExtra("Images");
        this.detail = i.getStringExtra("Details");
        this.pos = i.getIntExtra("Position", 0);

        ScrollGalleryView view = findViewById(R.id.image_view_main);
        view.setThumbnailSize(200)
                .setZoom(true)
                .withHiddenThumbnails(false)
                .hideThumbnailsOnClick(false)
                .addOnImageLongClickListener(position -> {
                    Snackbar.make(view, "Downloading", Snackbar.LENGTH_LONG).show();
                    File image;
                    try {
                        image = createImageFile(detail + "_" + position +
                                images.get(position)
                                        .substring(images.get(position).lastIndexOf('.') - 1));
                        Common.downloadThread(images.get(position), image,
                                () -> Snackbar.make(view, "Downloaded", Snackbar.LENGTH_LONG).show(),
                                () -> Snackbar.make(view, "Fail to download", Snackbar.LENGTH_LONG).show());
                        galleryAddPic(image.getAbsolutePath(), this);
                    }catch (Exception ex){
                        Log.e(TAG, "onCreate: ", ex);
                    }
                })
                .setFragmentManager(getSupportFragmentManager());
        for(int index = 0; index< images.size(); index++){
            String url = images.get(index);
            CustomPicassoLoader mi = new CustomPicassoLoader(url);
            final int pos = index;
            mi.onLoaded((b) -> {
                colors.add(pos,
                        Palette.from(toBitmap(b))
                                .generate()
                                .getDarkMutedColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark)));
            });
            view.addMedia(
                    MediaInfo.mediaLoader(mi)
            );
        }
        view.setCurrentItem(pos);
        new Thread(() -> {
            while (colors.size() - 1 < pos){}
            view.setBackgroundColor(colors.get(pos));
        }).start();
        view.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                if(colors.size() - 1 < pos)
                    return;
                view.setBackgroundColor(colors.get(position));
            }
        });

        FloatingActionButton fab = findViewById(R.id.image_view_download);
        fab.setOnClickListener(v -> {
            Snackbar.make(v, "Downloading", Snackbar.LENGTH_LONG).show();
            File image;
            int position = view.getCurrentItem();
            try {
                image = createImageFile(detail + "_" + position +
                        images.get(position)
                                .substring(images.get(position).lastIndexOf('.') - 1));
                Common.downloadThread(images.get(position), image,
                        () -> Snackbar.make(v, "Downloaded", Snackbar.LENGTH_LONG).show(),
                        () -> Snackbar.make(v, "Fail to download", Snackbar.LENGTH_LONG).show());
                galleryAddPic(image.getAbsolutePath(), this);
            }catch (Exception ex){
                Log.e(TAG, "onCreate: ", ex);
            }
        });
    }
}
