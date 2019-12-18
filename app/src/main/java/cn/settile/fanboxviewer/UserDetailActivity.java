package cn.settile.fanboxviewer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import cn.settile.fanboxviewer.Adapters.Fragment.UserDetailTabAdapter;
import cn.settile.fanboxviewer.Network.FanboxParser;
import cn.settile.fanboxviewer.TabFragments.UserDetail.UserDetail;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserDetailActivity extends AppCompatActivity {

    private String url;
    private String iconUrl;
    private String userName;
    private UserDetail userDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("");

        Intent intent = getIntent();

        this.url = intent.getStringExtra("URL");
        this.userName = intent.getStringExtra("NAME");
        this.iconUrl = intent.getStringExtra("ICON");

        Picasso.get()
                .load(iconUrl)
                .placeholder(R.drawable.load_24dp)
                .into((ImageView) findViewById(R.id.detail_icon));

        TextView name = findViewById(R.id.detail_user_name);
        name.setText(userName);

        TabLayout tabLayout = findViewById(R.id.detail_tab_layout);
        ViewPager viewPager = findViewById(R.id.detail_pager);

        UserDetailTabAdapter adapter = new UserDetailTabAdapter(getSupportFragmentManager(), this);

        userDetail = UserDetail.newInstance();
        adapter.addFragment(userDetail, getResources().getString(R.string.user_info));

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
                JSONObject detail = FanboxParser.getUserDetail(this.url);
                JSONObject body = detail.getJSONObject("body");
                JSONObject creator = body.getJSONObject("creator");
                JSONObject user = creator.getJSONObject("user");

                String coverImage = creator.getString("coverImageUrl");
                String uid = user.getString("userId");
                String name = user.getString("name");
                String iconUrl = user.getString("iconUrl");

                runOnUiThread(() -> {
                    Picasso.get()
                            .load(iconUrl)
                            .placeholder(R.drawable.load_24dp)
                            .into((ImageView) findViewById(R.id.detail_icon));
                    Picasso.get()
                            .load(coverImage)
                            .placeholder(R.drawable.load_24dp)
                            .into((ImageView) findViewById(R.id.detail_header));

                    ((TextView) findViewById(R.id.detail_user_id)).setText(uid);
                    ((TextView) findViewById(R.id.detail_user_name)).setText(name);
                });

            }catch (Exception ex){
                log.error("EXCEPTION: ", ex);
            }
        }).start();
    }
}
