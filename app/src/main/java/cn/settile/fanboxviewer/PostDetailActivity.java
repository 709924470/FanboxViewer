package cn.settile.fanboxviewer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.settile.fanboxviewer.Adapters.Bean.DetailItem;
import cn.settile.fanboxviewer.Adapters.RecyclerView.PostDetail.PostDetailRecyclerViewAdapter;
import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxParser;
import lombok.extern.slf4j.Slf4j;

import static cn.settile.fanboxviewer.Util.Util.createImageFile;
import static cn.settile.fanboxviewer.Util.Util.galleryAddPic;
import static cn.settile.fanboxviewer.Util.Util.toBitmap;

@Slf4j
public class PostDetailActivity extends AppCompatActivity {

    private String url;
    private String userName;
    private String iconUrl;
    private String coverUrl;
    private RecyclerView rv;
    private PostDetailRecyclerViewAdapter adapter;

    private final String TAG = getClass().getName();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Toolbar toolbar = findViewById(R.id.post_detail_toolBar);

        toolbar.setVisibility(View.GONE);

        setTitle("");

        Intent intent = getIntent();

        this.url = intent.getStringExtra("URL");
        this.userName = intent.getStringExtra("NAME");
        this.iconUrl = intent.getStringExtra("ICON");
        this.userId = intent.getStringExtra("CID");

        ((TextView) findViewById(R.id.post_detail_user_name)).setText(userName);
        Picasso.get()
                .load(iconUrl)
                .placeholder(R.drawable.load_24dp)
                .into((ImageView) findViewById(R.id.post_detail_icon));

        this.coverUrl = intent.getStringExtra("COVER");
        String title = intent.getStringExtra("TITLE");

        ((TextView) findViewById(R.id.post_detail_title)).setText(title);

        ImageView header = findViewById(R.id.post_detail_header);
        View view = findViewById(R.id.post_detail_app_bar);
        Picasso.get()
                .load(coverUrl)
                .placeholder(R.drawable.load_24dp)
                .into(header, new Callback() {
                    @Override
                    public void onSuccess() {
                        if(view == null){
                            return;
                        }
//                        view.setBackgroundColor(
//                                Palette.from(toBitmap(header.getDrawable()))
//                                        .generate()
//                                        .getDarkMutedColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark)));
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

        String fee = intent.getStringExtra("FEE");
        fee = intent.getStringExtra("TIME") + " - " + fee;

        ((TextView) findViewById(R.id.post_detail_user_id)).setText(fee);

        Picasso.get()
                .load(iconUrl)
                .placeholder(R.drawable.load_24dp)
                .into((ImageView) findViewById(R.id.post_detail_icon));

        this.rv = findViewById(R.id.post_detail_content);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        this.adapter = new PostDetailRecyclerViewAdapter(title);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);

        setup();

        setResult(-1);

        FloatingActionButton fab = findViewById(R.id.post_detail_download);
        fab.setOnClickListener(view1 ->{
            List<String> images = adapter.images;
            for(int i = 0; i < images.size(); i++){
                Snackbar.make(view1, "Downloading", Snackbar.LENGTH_LONG).show();
                File image;
                int position = i;
                try {
                    image = createImageFile(title + "_" + position +
                            images.get(position)
                                    .substring(images.get(position).lastIndexOf('.') - 1));
                    Common.downloadThread(images.get(position), image,
                            () -> Snackbar.make(getWindow().getDecorView(), "Downloaded " + image.getName(), Snackbar.LENGTH_LONG).show(),
                            () -> Snackbar.make(getWindow().getDecorView(), "Fail to download " + image.getName(), Snackbar.LENGTH_LONG).show());
                    galleryAddPic(image.getAbsolutePath(), this);
                }catch (Exception ex){
                    Log.e(TAG, "onCreate: ", ex);
                }
            }
        });
    }

    private void setup(){
        new Thread(() -> {
            try{
                List<DetailItem> ldi = new FanboxParser(userId).getPostDetail(this.url);
                Future<Object> tmp = Executors.newSingleThreadExecutor().submit(() -> {
                    runOnUiThread(() -> {
                        adapter.updateItems(ldi);
                    });
                    return null;
                });
                while(!tmp.isDone()){}
                tmp.get();
            }catch (Exception ex){
                Snackbar.make(getWindow().getDecorView(), "Cannot load page: " + ex.getMessage(), Snackbar.LENGTH_LONG).show();
                log.error("EXCEPTION", ex);
            }
        }).start();
    }
}
