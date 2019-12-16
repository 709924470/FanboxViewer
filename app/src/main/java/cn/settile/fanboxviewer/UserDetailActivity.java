package cn.settile.fanboxviewer;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;

import cn.settile.fanboxviewer.Network.FanboxParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserDetailActivity extends AppCompatActivity {

    private String url;
    private String iconUrl;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        tabLayout.setupWithViewPager(viewPager);

        setup();
    }

    private void setup(){
        new Thread(() -> {
            try {
                JSONObject detail = FanboxParser.getUserDetail(this.url);
                JSONObject body = detail.getJSONObject("body");
                JSONObject user = body.getJSONObject("creator").getJSONObject("user");

                String uid = user.getString("userId");
                String name = user.getString("name");
                String iconUrl = user.getString("iconUrl");

                Picasso.get()
                        .load(iconUrl)
                        .placeholder(R.drawable.load_24dp)
                        .into((ImageView) findViewById(R.id.detail_icon));

                runOnUiThread(() -> {
                    ((TextView) findViewById(R.id.detail_user_name)).setText(name);
                    ((TextView) findViewById(R.id.detail_user_id)).setText(uid);
                });

            }catch (Exception ex){
                log.error("EXCEPTION: ", ex);
            }
        });
    }
}
