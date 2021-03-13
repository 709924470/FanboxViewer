package cn.settile.fanboxviewer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Contract;
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
import cn.settile.fanboxviewer.Network.URLRequestor;
import cn.settile.fanboxviewer.ViewModels.MainViewModel;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final static String TAG = "MainActivity";
    static boolean flag = false;
    MainViewModel viewModel = null;


    MainActivity ctx;
    AllPostFragment allPostFragment;
    private MainFragmentAdapter tabPageAdapter;
    private TabLayout tl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;

        setContentView(R.layout.activity_main);


        prepareUIAndActions();

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
        //navigationView.getMenu().getItem(0).setChecked(true);

        //TODO: IMAGE Editing for club card.
        //navigationView.getMenu().getItem(1).setEnabled(true);

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
        if (!getIntent().getBooleanExtra("NO_NETWORK", false)) {
            if (getIntent().getBooleanExtra("IS_LOGGED_IN", false)) {
                viewModel.update_is_logged_in(true);
                fetchUserInfo();
                new Thread(() -> {
                    getNotifications(messageFragment);
                    allPostFragment.updateList(FanboxParser.getAllPosts(false, this), FanboxParser.getPlans(), true);
                    subscPostFragment.updateList(FanboxParser.getSupportingPosts(false, this), true);
                }).start();

            } else {
                viewModel.update_is_logged_in(false);
            }
            viewModel.update_is_online(true);
        } else {
            viewModel.update_is_online(false);
        }


        tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mVp.setCurrentItem(tab.getPosition(), true);
                if (tab.getPosition() == 2 && !flag) {
                    messageFragment.update(true);
                    flag = !flag;
                    tab.removeBadge();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }


    void prepareUIAndActions() {

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.is_logged_in().observe(this, (it) -> {
            Log.i(TAG, it.toString());
            NavigationView navV = (NavigationView) ctx.findViewById(R.id.nav_view);
            TextView usernameV = (TextView) navV.getHeaderView(0).findViewById(R.id.main_drawer_username);
            TextView useridV = (TextView) navV.getHeaderView(0).findViewById(R.id.main_drawer_userid);
            if (!it) {
                usernameV.setOnClickListener((v) -> {
                    callLogin();
                });
                useridV.setOnClickListener((v) -> {
                    callLogin();
                });
            }
        });
        viewModel.getUser_id().observe(this, (it) -> {
            NavigationView navV = (NavigationView) ctx.findViewById(R.id.nav_view);
            TextView useridV = (TextView) navV.getHeaderView(0).findViewById(R.id.main_drawer_userid);
            useridV.setText(Objects.equals(it, "") ? getText(R.string.nav_header_subtitle) : it);
        });
        viewModel.getUser_name().observe(this, (it) -> {
            NavigationView navV = (NavigationView) ctx.findViewById(R.id.nav_view);
            TextView usernameV = (TextView) navV.getHeaderView(0).findViewById(R.id.main_drawer_username);
            usernameV.setText(Objects.equals(it, "") ? getText(R.string.nav_header_title) : it);
        });

        viewModel.getUser_icon_url().observe(this, (it) -> {
            if (!Objects.equals(it, "")) Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_settings_system_daydream_black_24dp)
                    .resize(180, 180)
                    .into((ImageView) findViewById(R.id.userIcon));
        });
    }

    ;


    private void getNotifications(MessageFragment mf) {
        mf.update(true);
    }

    private void fetchUserInfo() {
        new Thread(() -> {
            new URLRequestor("https://fanbox.cc/", (it) -> {
                try {
                    Document document = Jsoup.parse(it.body().string());
                    Element metadata = document.getElementById("metadata");
                    //TODO (fix this parser) : bug
                    String jsonStr = metadata.attr("content");

                    Common.userInfo = new JSONObject(jsonStr);
                    JSONObject user = Common.userInfo.getJSONObject("context").getJSONObject("user");
                    String iconUrl = user.getString("iconUrl");
                    String userName = user.getString("name");
                    String userId = user.getString("userId");

                    int unread = FanboxParser.getUnreadMessagesCount();

                    runOnUiThread(() -> {
                        viewModel.update_user_info(userName, userId, iconUrl);
                        if (unread != 0) {
                            Objects.requireNonNull(tl.getTabAt(2))
                                    .getOrCreateBadge().setNumber(unread);
                        }
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> Toast.makeText(getBaseContext(), "Can't get user info.\n" + ex.getMessage(), Toast.LENGTH_LONG).show());
                    Log.e("MainActivity", "fetchUserInfo: ", ex);
                }
                return null;
            }, null);

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
            //Toast.makeText(this, "HOME", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_cards) {
            //Toast.makeText(this, "Fan Cards", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, SupportingActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_search) {
            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            //TODO (SettingsActivity)
            //callSettings();
        } else if (id == R.id.nav_logout) {
            //Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
            android.webkit.CookieManager cm = CookieManager.getInstance();
            cm.removeAllCookies((it) -> {

            });

            Intent i = new Intent(this,SplashActivity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.nav_recommend) {
            Toast.makeText(this, "Recommended", Toast.LENGTH_SHORT).show();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Contract("_->null")
    public void callLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        if (!viewModel.is_logged_in().getValue()) startActivityForResult(intent, -1);
    }

    public void callSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
