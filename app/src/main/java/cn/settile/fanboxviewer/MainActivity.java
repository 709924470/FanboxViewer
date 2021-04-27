package cn.settile.fanboxviewer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

import cn.settile.fanboxviewer.ViewComponents.LogoutDialog;
import cn.settile.fanboxviewer.ViewModels.MainViewModel;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final String TAG = MainActivity.class.getName();
    public static boolean flag = false;
    public MainViewModel viewModel = null;
    MainActivity ctx = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;

        setContentView(R.layout.activity_main);


        prepareUIAndActions();

        setTitle(R.string.app_name);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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

        setResult(-1);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_main);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navigationView, navController);

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph())
                        .setDrawerLayout(drawer)
                        .build();

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                navHostFragment.getNavController().navigateUp();
                Log.d(TAG, destination.getLabel().toString());
//                switch (destination.getId()) {
//                    case R.id.supportingFragment:
//                        navHostFragment.getNavController().navigateUp();
//                        break;
//                    case R.id.mainTabFragment:
//                        navHostFragment.getNavController().navigateUp();
//                }
            }
        });

    }


    void prepareUIAndActions() {

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        {

        }


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
        } else if (id == R.id.nav_supporting) {
            //Toast.makeText(this, "Fan Cards", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_search) {
            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            //TODO (SettingsActivity)
            //callSettings();
        } else if (id == R.id.nav_logout) {
            //Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
            new LogoutDialog(this).show();
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
