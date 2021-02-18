package cn.settile.fanboxviewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.settile.fanboxviewer.Adapters.RecyclerView.PostDetail.PostDetailRecyclerViewAdapter;
import cn.settile.fanboxviewer.Network.Bean.CardItem;
import cn.settile.fanboxviewer.Network.Bean.DetailItem;
import cn.settile.fanboxviewer.Network.Bean.DownloadItem;
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxParser;
import cn.settile.fanboxviewer.ViewModels.PostDetailViewModel;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Call;

import static cn.settile.fanboxviewer.Network.DownloadManager.queue;
import static cn.settile.fanboxviewer.Network.RESTfulClient.FanboxParser.APIJSONFactory;
import static cn.settile.fanboxviewer.Network.RESTfulClient.FanboxParser.client;

@Slf4j
public class PostDetailActivity extends AppCompatActivity {

    private final String TAG = getClass().getName();
    PostDetailViewModel viewModel = null;
    private String url;
    private String userName;
    private String iconUrl;
    private String coverUrl;
    private RecyclerView rv;
    private PostDetailRecyclerViewAdapter adapter;
    private String userId;
    private boolean isFromURL;
    private String title;
    private String fee;
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Toolbar toolbar = findViewById(R.id.post_detail_toolBar);
        prepareUIAndActions();
        toolbar.setVisibility(View.GONE);

        setTitle("");

        Intent intent = getIntent();

        this.url = intent.getStringExtra("URL");
        this.userName = intent.getStringExtra("NAME");
        this.iconUrl = intent.getStringExtra("ICON");
        this.userId = intent.getStringExtra("CID");
        this.coverUrl = intent.getStringExtra("COVER");
        this.title = intent.getStringExtra("TITLE");
        this.fee = intent.getStringExtra("FEE");
        this.time = intent.getStringExtra("TIME");

        viewModel.update_article_info(url, title, time, this.fee, coverUrl);
        viewModel.update_user_info(userName, userId, iconUrl);
        this.isFromURL = intent.getBooleanExtra("isURL", false);

        if (!isFromURL)
            delayedSetup();

        setup();

        setResult(-1);
    }

    private void delayedSetup() {

        viewModel.update_article_info(url, title, time, fee, coverUrl);
        viewModel.update_user_info(userName, userId, iconUrl);
        this.rv = findViewById(R.id.post_detail_content);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        this.adapter = new PostDetailRecyclerViewAdapter(title);
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.post_detail_download);
        fab.setOnClickListener(view1 -> {
            List<String> images = adapter.images;
            for (int i = 0; i < images.size(); i++) {
                Snackbar.make(view1, "Downloading", Snackbar.LENGTH_LONG).show();
                try {
                    String extension = images.get(i)
                            .substring(images.get(i).lastIndexOf('.'));
                    String name = title + "_" + i + extension;
                    queue(new DownloadItem(images.get(i), name));
                } catch (Exception ex) {
                    Log.e(TAG, "onCreate: ", ex);
                }
            }
        });
    }

    private void setup() {
        new Thread(() -> {
            try {
                FanboxParser fanboxParser = new FanboxParser(userId);
                Call<ResponseBody> creatorInfoCaller = client.getPostInfo(Integer.parseInt(url));
                JSONObject body = APIJSONFactory(creatorInfoCaller).getJSONObject("body");
                List<DetailItem> ldi = fanboxParser.getPostContent(body);

                if (isFromURL) {
                    CardItem detail = fanboxParser.getPostDetail(body);
                    this.userName = detail.getCreator();
                    this.iconUrl = detail.getIconUrl();
                    this.userId = detail.getUserId();
                    this.coverUrl = detail.getHeaderUrl();
                    this.title = detail.getTitle();
                    this.fee = detail.getPlan();
                    runOnUiThread(this::delayedSetup);
                }

                Future<Object> tmp = Executors.newSingleThreadExecutor().submit(() -> {
                    runOnUiThread(() -> {
                        adapter.updateItems(ldi);
                    });
                    return null;
                });
                while (!tmp.isDone()) {
                }
                tmp.get();
            } catch (Exception ex) {
                Snackbar.make(getWindow().getDecorView(), "Cannot load page: " + ex.getMessage(), Snackbar.LENGTH_LONG).show();
                log.error("EXCEPTION", ex);
            }
        }).start();
    }


    @SuppressLint("SetTextI18n")
    void prepareUIAndActions() {

        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        viewModel.getUser_name().observe(this, (it) -> {
            ((TextView) findViewById(R.id.post_detail_user_name)).setText(it);
        });
        viewModel.getArticle_title().observe(this, (it) -> {
            ((TextView) findViewById(R.id.post_detail_title)).setText(it);
        });
        viewModel.getArticle_fee().observe(this, (it) -> {
            ((TextView) findViewById(R.id.post_detail_user_id)).setText(viewModel.getArticle_time().getValue() + " - " + it);
        });
        viewModel.getArticle_time().observe(this, (it) -> {
            ((TextView) findViewById(R.id.post_detail_user_id)).setText(it + " - " + viewModel.getArticle_fee().getValue());
        });
        viewModel.getUrl_user_icon().observe(this, (it) -> {
            if (!Objects.equals(it, "")) Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.load_24dp)
                    .into((ImageView) findViewById(R.id.post_detail_icon));

        });
        viewModel.getUrl_cover().observe(this, (it) -> {
            if (!Objects.equals(it, "")) Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.load_24dp)
                    .into((ImageView) findViewById(R.id.post_detail_header));

        });
    }

}
