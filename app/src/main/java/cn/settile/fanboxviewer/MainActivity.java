package cn.settile.fanboxviewer;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.BaseOnTabSelectedListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Objects;

import cn.settile.fanboxviewer.Adapters.Fragment.MainFragmentAdapter;
import cn.settile.fanboxviewer.Fragments.Main.AllPostFragment;
import cn.settile.fanboxviewer.Fragments.Main.MessageFragment;
import cn.settile.fanboxviewer.Fragments.Main.SubscPostFragment;
import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxParser;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    MainActivity c;
    static boolean flag = false;

    AllPostFragment allPostFragment;
    private MainFragmentAdapter tabPageAdapter;
    private TabLayout tl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = this;
        setContentView(R.layout.activity_main_page);
        setTitle(R.string.app_name);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager mVp = findViewById(R.id.main_tab_pager); // inflating the main page
        mVp.setSaveEnabled(true);
        mVp.setOffscreenPageLimit(2);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        //TODO: IMAGE Editing for club card.
        navigationView.getMenu().getItem(1).setEnabled(false);

        tl = findViewById(R.id.main_page_tab);
        tabPageAdapter = new MainFragmentAdapter(getSupportFragmentManager(), this);
        allPostFragment = AllPostFragment.newInstance();
        tabPageAdapter.addFragment(allPostFragment, getResources().getString(R.string.tab_posts));

        SubscPostFragment subscPostFragment = SubscPostFragment.newInstance();
        tabPageAdapter.addFragment(subscPostFragment, getResources().getString(R.string.tab_subscribed));

        MessageFragment messageFragment = MessageFragment.newInstance();
        tabPageAdapter.addFragment(messageFragment, getResources().getString(R.string.tab_messages));

        mVp.setAdapter(tabPageAdapter);
        tl.setupWithViewPager(mVp);

        setResult(-1);
        if (getIntent().getBooleanExtra("isLoggedIn", false)
                && !getIntent().getBooleanExtra("NO_NETWORK", false)) {
            fetchUserInfo();
            new Thread(() -> {
                getNotifications(messageFragment);
                allPostFragment.updateList(FanboxParser.getAllPosts(false, this), FanboxParser.getPlans(), true);
                subscPostFragment.updateList(FanboxParser.getSupportingPosts(false, this), true);
            }).start();
        }

        tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mVp.setCurrentItem(tab.getPosition(), true);
                if (tab.getPosition() == 2 && !flag){
                    messageFragment.update(true);
                    flag = !flag;
                    tab.removeBadge();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void getNotifications(MessageFragment mf) {
        mf.update(true);
    }

    private void fetchUserInfo() {
        new Thread(() -> {
            Request req = new Request.Builder()
                    .url("https://www.pixiv.net/fanbox/")
                    .build();
            try (Response resp = Common.client.newCall(req).execute()) {
                Document document = Jsoup.parse(resp.body().string());
                Element metadata = document.getElementById("metadata");
                String jsonStr = metadata.attr("content");

                Common.userInfo = new JSONObject(jsonStr);
                JSONObject user = Common.userInfo.getJSONObject("context").getJSONObject("user");
                String iconUrl = user.getString("iconUrl");
                String userName = user.getString("name");
                String userId = user.getString("userId");

                int unread = FanboxParser.getUnreadMessagesCount();

                runOnUiThread(() -> {
                    TextView textView = findViewById(R.id.userName);
                    textView.setText(userName);
                    textView = findViewById(R.id.userId);
                    textView.setText(userId);

                    Picasso.get()
                            .load(iconUrl)
                            .placeholder(R.drawable.load_24dp)
                            .resize(200, 200)
                            .into((ImageView) findViewById(R.id.userIcon));

                    if(unread != 0){
                        Objects.requireNonNull(tl.getTabAt(2))
                                .getOrCreateBadge().setNumber(unread);
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> Toast.makeText(getBaseContext(), "Can't get user info.\n" + ex.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("MainActivity", "fetchUserInfo: ", ex);
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.drawer_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (id == R.id.nav_home) {
            Toast.makeText(this, "HOME", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_cards) {
            Toast.makeText(this, "Fan Cards", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_search) {
            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_recommend) {
            Toast.makeText(this, "Recommended", Toast.LENGTH_SHORT).show();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
