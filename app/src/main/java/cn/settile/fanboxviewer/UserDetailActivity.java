package cn.settile.fanboxviewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.settile.fanboxviewer.Network.Bean.CardItem;
import cn.settile.fanboxviewer.Network.Bean.DetailItem;
import cn.settile.fanboxviewer.Adapters.Fragment.UserDetailFragmentAdapter;
import cn.settile.fanboxviewer.Fragments.UserDetail.PostFragment;
import cn.settile.fanboxviewer.Fragments.UserDetail.UserDetailFragment;
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserDetailActivity extends AppCompatActivity {

    private String url;
    private String iconUrl;
    private String userName;

    private UserDetailFragment userDetail;
    private PostFragment posts;
    List<DetailItem> details = null;
    private String userId;
    private boolean isFromURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolBar);
        toolbar.setVisibility(View.GONE);

        setTitle("");

        Intent intent = getIntent();

        this.url = intent.getStringExtra("URL");
        this.userName = intent.getStringExtra("NAME");
        this.iconUrl = intent.getStringExtra("ICON");
        this.userId = intent.getStringExtra("CID");

        this.isFromURL = intent.getBooleanExtra("isURL", false);

        if (!Objects.equals(iconUrl, null))
            Picasso.get()
                    .load(iconUrl)
                    .placeholder(R.drawable.load_24dp)
                    .into((ImageView) findViewById(R.id.detail_icon));

        TextView name = findViewById(R.id.detail_user_name);
        if (!Objects.equals(userName, null))
            name.setText(userName);

        TabLayout tabLayout = findViewById(R.id.detail_tab_layout);
        ViewPager viewPager = findViewById(R.id.detail_pager);

        UserDetailFragmentAdapter adapter = new UserDetailFragmentAdapter(getSupportFragmentManager(), this);

        userDetail = UserDetailFragment.newInstance();
        adapter.addFragment(userDetail, getResources().getString(R.string.user_info));

        posts = PostFragment.newInstance();
        adapter.addFragment(posts, getResources().getString(R.string.posts));

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        setResult(-1);

        setup();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setup(){
        new Thread(() -> {
            try {
                FanboxParser fanboxParser = new FanboxParser(userId);
                JSONObject detail = fanboxParser.getUserDetail();
                JSONObject body = detail.getJSONObject("body");
                JSONObject user = body.getJSONObject("user");

                if(isFromURL){
                    // Official account's icon
                    iconUrl = user.optString("iconUrl", "https://pixiv.pximg.net/c/160x160_90_a2_g5/fanbox/public/images/user/20390859/icon/ZYMvxFOQp6LyH9nv0luZdH5s.jpeg");
                    userName = user.optString("name", "www");
                    if (!userName.equals("www"))
                        ((TextView) findViewById(R.id.detail_user_name)).setText(userName);
                }

                String description = body.optString("description");
                JSONArray links = body.optJSONArray("profileLinks");
                JSONArray items = body.optJSONArray("profileItems");

                details = new ArrayList<>();
                for(int i = 0; i < links.length(); i++){
                    details.add(new DetailItem(DetailItem.Type.TEXT, links.getString(i)));
                }
                for(int i = 0; i < items.length(); i++){
                    DetailItem item = new DetailItem(items.getJSONObject(i).getString("type").equals("image") ? DetailItem.Type.IMAGE : DetailItem.Type.TEXT,
                            items.getJSONObject(i).optString("imageUrl"));
                    item.extra.add(0, items.getJSONObject(i).optString("thumbnailUrl"));
                    details.add(item);
                }
                details.add(new DetailItem(DetailItem.Type.TEXT, description));

                String coverImage = body.getString("coverImageUrl");

                String uid = user.getString("userId");
                posts.setUserID(uid);
                Executors.newSingleThreadExecutor().submit(() -> {
                            List<CardItem> lci = fanboxParser.getUserPosts();
                            posts.updateList(lci, true);
                            posts.nextUrl = null;
                            return null;
                        });

                String name = user.getString("name");
                String iconUrl = user.getString("iconUrl");

                runOnUiThread(() -> {
                    Picasso.get()
                            .load(iconUrl)
                            .placeholder(R.drawable.load_24dp)
                            .into((ImageView) findViewById(R.id.detail_icon));

                    ImageView header = findViewById(R.id.detail_header);
                    Picasso.get()
                            .load(coverImage)
                            .placeholder(R.drawable.load_24dp)
                            .into(header);

                    ((TextView) findViewById(R.id.detail_user_id)).setText(uid);
                    ((TextView) findViewById(R.id.detail_user_name)).setText(name);
                });
                Future<Object> tmp = Executors.newSingleThreadExecutor().submit(() -> {
                    runOnUiThread(() -> {
                        while(userDetail.uda == null){}
                        findViewById(R.id.detail_loading).setVisibility(View.GONE);
                        userDetail.uda.updateItems(details);
                    });
                    return null;
                });
                while(!tmp.isDone()){}
                tmp.get();
            }catch (Exception ex){
                log.error("EXCEPTION: ", ex);
            }
        }).start();
    }
}
