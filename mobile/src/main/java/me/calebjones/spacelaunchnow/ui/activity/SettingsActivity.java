package me.calebjones.spacelaunchnow.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.content.database.ListPreferences;
import me.calebjones.spacelaunchnow.ui.fragment.NestedPreferenceFragment;
import me.calebjones.spacelaunchnow.ui.fragment.SettingsFragment;
import me.calebjones.spacelaunchnow.ui.fragment.SettingsFragment.Callback;

public class SettingsActivity extends AppCompatActivity implements Callback {
    private static final String NESTED = "NESTED";
    private TextView toolbarTitle;
    private static ListPreferences sharedPreference;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int m_theme;
        this.context = getApplicationContext();

        sharedPreference = ListPreferences.getInstance(this.context);

        if (sharedPreference.getNightMode()) {
            m_theme = R.style.DarkTheme_NoActionBar;
        } else {
            m_theme = R.style.LightTheme_NoActionBar;
        }
        setTheme(m_theme);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbarTitle = (TextView) findViewById(R.id.title_text);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        toolbarTitle.setText(R.string.action_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.settings_content_frame, new SettingsFragment()).commit();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                super.onBackPressed();
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                getSupportFragmentManager().popBackStack();
                toolbarTitle.setText(R.string.settings);
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            getSupportFragmentManager().popBackStack();
            this.toolbarTitle.setText(R.string.settings);
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    public void onNestedPreferenceSelected(int key) {
        getSupportFragmentManager().beginTransaction().replace(R.id.settings_content_frame, NestedPreferenceFragment.newInstance(key), NESTED).addToBackStack(NESTED).commit();
    }

}