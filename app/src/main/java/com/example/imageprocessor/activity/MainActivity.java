package com.example.imageprocessor.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.example.imageprocessor.R;
import com.example.imageprocessor.misc.DarkModeSharedPref;
import com.example.imageprocessor.misc.LanguageSharedPref;
import com.example.imageprocessor.misc.SettingsConfig;
import com.example.imageprocessor.ui.about.AboutFragment;
import com.example.imageprocessor.ui.camera.CameraFragment;
import com.example.imageprocessor.ui.gallery.GalleryFragment;
import com.example.imageprocessor.ui.history.HistoryFragment;
import com.example.imageprocessor.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static {
        if (!OpenCVLoader.initDebug()) {
            // Do something which
        } else {
            // Close the application
        }
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    private static final String TAG = "MainActivity: ";

    private AppBarConfiguration mAppBarConfiguration;
    private SettingsConfig settingsConfig;
    private SharedPreferences sharedPreferences;
    private Locale currentLocale;

    DarkModeSharedPref darkModeSharedPref;
    LanguageSharedPref languageSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // init dark mode settings on MainActivity
        darkModeSharedPref = new DarkModeSharedPref(this);
        if (darkModeSharedPref.loadDarkModeState()) {
            setTheme(R.style.DarkTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }

        // init language settings on MainActivity
        languageSharedPref = new LanguageSharedPref(this);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        if (languageSharedPref.loadLanguageState()
                .equalsIgnoreCase("english")) {
            configuration.locale = Locale.ENGLISH;
        } else {
            configuration.locale = Locale.CHINA;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        resources.updateConfiguration(configuration, metrics);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate...");

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_camera,
                R.id.nav_gallery, R.id.nav_stitcher,
                R.id.nav_history, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId() == R.id.nav_about) {
                    Log.i(TAG, "onDestinationChanged -> AboutFragment");
                    toolbar.setVisibility(View.GONE);
                } else {
                    toolbar.setVisibility(View.VISIBLE);
                }

                // adapt the settings changes when change to any other fragments
                // init dark mode settings on MainActivity
                darkModeSharedPref = new DarkModeSharedPref(getContext());
                if (darkModeSharedPref.loadDarkModeState()) {
                    // it must be NoActionBar since using toolbar
                    setTheme(R.style.DarkTheme_NoActionBar);
                } else {
                    setTheme(R.style.AppTheme_NoActionBar);
                }

                // init language settings on MainActivity
                languageSharedPref = new LanguageSharedPref(getContext());
                Resources resources = getResources();
                Configuration configuration = resources.getConfiguration();
                if (languageSharedPref.loadLanguageState()
                        .equalsIgnoreCase("english")) {
                    configuration.locale = Locale.ENGLISH;
                } else {
                    configuration.locale = Locale.CHINA;
                }
                DisplayMetrics metrics = new DisplayMetrics();
                resources.updateConfiguration(configuration, metrics);
            }
        });
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        settingsConfig = new SettingsConfig(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentLocale = getResources().getConfiguration().locale;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_settings:
                Log.i(TAG, "action settings");
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.action_quit:
                Log.i(TAG, "action quit");
                showQuitDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged...");
        if (!currentLocale.equals(newConfig.locale)) {
            Toast.makeText(this, "Configuration Changed ..." + currentLocale, Toast.LENGTH_SHORT).show();
            currentLocale = newConfig.locale;
        }
    }

    /**
     * @deprecated initDarkMode method
     */
    @Deprecated
    private void initDarkMode() {
        boolean darkModeOn = sharedPreferences.getBoolean("dark_mode", false);
        Log.i(TAG, "Current Dark Mode: " + darkModeOn);
        if (darkModeOn) {
            settingsConfig.darkModeOn();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.i(TAG, "onSupportNavigateUp...");
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void showQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(MainActivity.this);
        quitDialog.setTitle(getString(R.string.action_quit))
                .setMessage(getString(R.string.quit_dialog_message))
                .setPositiveButton(R.string.action_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Quit the app
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume...");
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    Log.i(TAG, "Load OpenCV successfully...");
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    public Context getContext() {
        return this;
    }
}