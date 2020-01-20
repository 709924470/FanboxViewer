package cn.settile.fanboxviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.settile.fanboxviewer.Adapters.Bean.DetailItem;
import cn.settile.fanboxviewer.Adapters.RecyclerView.PostDetail.PostDetailRecyclerViewAdapter;
import cn.settile.fanboxviewer.Network.FanboxParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostDetailActivity extends AppCompatActivity {

    private String url;
    private String userName;
    private String iconUrl;
    private String coverUrl;
    private RecyclerView rv;
    private PostDetailRecyclerViewAdapter adapter;

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

        ((TextView) findViewById(R.id.post_detail_user_name)).setText(userName);
        Picasso.get()
                .load(iconUrl)
                .placeholder(R.drawable.load_24dp)
                .into((ImageView) findViewById(R.id.post_detail_icon));

        this.coverUrl = intent.getStringExtra("COVER");
        String title = intent.getStringExtra("TITLE");

        ((TextView) findViewById(R.id.post_detail_title)).setText(title);

        Picasso.get()
                .load(coverUrl)
                .placeholder(R.drawable.load_24dp)
                .into((ImageView) findViewById(R.id.post_detail_header));

        String fee = intent.getStringExtra("FEE");
        fee = intent.getStringExtra("time") + " - " + fee;

        ((TextView) findViewById(R.id.post_detail_user_id)).setText(fee);

        Picasso.get()
                .load(iconUrl)
                .placeholder(R.drawable.load_24dp)
                .into((ImageView) findViewById(R.id.post_detail_icon));

        this.rv = findViewById(R.id.post_detail_content);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        this.adapter = new PostDetailRecyclerViewAdapter();
        rv.setLayoutManager(llm);
        rv.setAdapter(adapter);

        setup();

        setResult(-1);

        FloatingActionButton fab = findViewById(R.id.post_detail_download);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setup(){
        new Thread(() -> {
            try{
                List<DetailItem> ldi = FanboxParser.getPostDetail(this.url, this);
                Future<Object> tmp = Executors.newSingleThreadExecutor().submit(() -> {
                    runOnUiThread(() -> {
                        adapter.updateItems(ldi);
                    });
                    return null;
                });
                while(!tmp.isDone()){}
                tmp.get();
            }catch (Exception ex){
                log.error("EXCEPTION", ex);
            }
        }).start();
    }
}
