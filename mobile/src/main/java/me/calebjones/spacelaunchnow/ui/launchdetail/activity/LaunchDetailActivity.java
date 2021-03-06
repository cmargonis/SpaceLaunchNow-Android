package me.calebjones.spacelaunchnow.ui.launchdetail.activity;

import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.arch.lifecycle.ViewModelProvider;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.kinst.jakub.view.SimpleStatefulLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.common.BaseActivity;
import me.calebjones.spacelaunchnow.common.customviews.generate.Rate;
import me.calebjones.spacelaunchnow.content.data.callbacks.Callbacks;
import me.calebjones.spacelaunchnow.content.data.details.DetailsDataRepository;
import me.calebjones.spacelaunchnow.content.database.ListPreferences;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.ui.launchdetail.DetailsViewModel;
import me.calebjones.spacelaunchnow.ui.launchdetail.TabsAdapter;
import me.calebjones.spacelaunchnow.ui.settings.SettingsActivity;
import me.calebjones.spacelaunchnow.ui.supporter.SupporterHelper;
import me.calebjones.spacelaunchnow.utils.GlideApp;
import me.calebjones.spacelaunchnow.utils.Utils;
import me.calebjones.spacelaunchnow.utils.analytics.Analytics;
import me.calebjones.spacelaunchnow.utils.customtab.CustomTabActivityHelper;
import me.calebjones.spacelaunchnow.utils.views.CustomOnOffsetChangedListener;
import me.calebjones.spacelaunchnow.utils.views.SnackbarHandler;
import timber.log.Timber;

public class LaunchDetailActivity extends BaseActivity
        implements AppBarLayout.OnOffsetChangedListener, SwipeRefreshLayout.OnRefreshListener {

    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    @BindView(R.id.fab_share)
    FloatingActionButton fabShare;
    @BindView(R.id.stateful_view)
    SimpleStatefulLayout statefulView;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.detail_profile_image)
    CircleImageView detail_profile_image;
    @BindView(R.id.detail_title)
    TextView detail_rocket;
    @BindView(R.id.detail_mission_location)
    TextView detail_mission_location;
    @BindView(R.id.detail_viewpager)
    ViewPager viewPager;
    @BindView(R.id.rootview)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.detail_swipe_refresh)
    SwipeRefreshLayout detailSwipeRefresh;
    @BindView(R.id.detail_tabs)
    TabLayout tabLayout;
    @BindView(R.id.detail_profile_backdrop)
    ImageView detail_profile_backdrop;
    @BindView(R.id.detail_appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.detail_toolbar)
    Toolbar toolbar;

    private boolean mIsAvatarShown = true;

    private int mMaxScrollSize;
    private SharedPreferences sharedPref;
    private ListPreferences sharedPreference;
    private CustomTabActivityHelper customTabActivityHelper;
    private Context context;
    private TabsAdapter tabAdapter;
    private int statusColor;
    public boolean isYouTubePlayerFullScreen = false;
    public String response;
    public Launch launch;
    private boolean fabShowable = false;
    private int launchId = 0;
    private Realm realm;
    private Rate rate;
    private DetailsDataRepository detailsDataRepository;
    private DetailsViewModel model;

    public LaunchDetailActivity() {
        super("Launch Detail Activity");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int m_theme;

        realm = getRealm();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        context = getApplicationContext();
        customTabActivityHelper = new CustomTabActivityHelper();
        sharedPreference = ListPreferences.getInstance(context);
        detailsDataRepository = new DetailsDataRepository(context, getRealm());

        if (sharedPreference.isNightModeActive(this)) {
            statusColor = ContextCompat.getColor(context, R.color.darkPrimary_dark);
        } else {
            statusColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        }
        m_theme = R.style.BaseAppTheme;

        if (getSharedPreferences("theme_changed", 0).getBoolean("recreate", false)) {
            SharedPreferences.Editor editor = getSharedPreferences("theme_changed", 0).edit();
            editor.putBoolean("recreate", false);
            editor.apply();
            recreate();
        }


        setTheme(m_theme);
        setContentView(R.layout.activity_launch_detail);
        ButterKnife.bind(this);
        model = ViewModelProviders.of(this).get(DetailsViewModel.class);
        // update UI
        model.getLaunch().observe(this, this::updateViews);


        detailSwipeRefresh.setOnRefreshListener(this);
        fabShare.hide();
        statefulView.showProgress();
        statefulView.setOfflineRetryOnClickListener(v -> fetchDataFromNetwork(launchId));

        rate = new Rate.Builder(context)
                .setTriggerCount(10)
                .setMinimumInstallTime(TimeUnit.DAYS.toMillis(3))
                .setMessage(R.string.please_rate_short)
                .setFeedbackAction(() -> showFeedback())
                .setSnackBarParent(coordinatorLayout)
                .build();

        rate.showRequest();

        if (!SupporterHelper.isSupporter()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int error) {
                    adView.setVisibility(View.GONE);
                }

            });
        } else {
            adView.setVisibility(View.GONE);
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        appBarLayout.addOnOffsetChangedListener(new CustomOnOffsetChangedListener(statusColor, getWindow()));
        appBarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appBarLayout.getTotalScrollRange();

        tabAdapter = new TabsAdapter(getSupportFragmentManager(), context);

        viewPager.setAdapter(tabAdapter);
        viewPager.setOffscreenPageLimit(3);

        tabLayout.setupWithViewPager(viewPager);

        //Grab information from Intent
        Intent mIntent = getIntent();
        String type = mIntent.getStringExtra("TYPE");

        if (type != null && type.equals("launch")) {
            launchId = mIntent.getIntExtra("launchID", 0);
            fetchDataFromDatabaseForTitle(launchId);
            fetchDataFromNetwork(launchId);
        }


        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
    }

    private void fetchDataFromNetwork(int launchId) {
        detailsDataRepository.getLaunchFromNetwork(launchId, new Callbacks.DetailsCallback() {
            @Override
            public void onLaunchLoaded(Launch launch) {
                updateViewModel(launch);
            }

            @Override
            public void onNetworkStateChanged(boolean refreshing) {
                showNetworkLoading(refreshing);
            }

            @Override
            public void onError(String message, @Nullable Throwable throwable) {
                if (throwable != null) {
                    Timber.e(throwable);
                    SnackbarHandler.showErrorSnackbar(context, coordinatorLayout, String.format("Error: %s", throwable.getLocalizedMessage()));
                } else {
                    Timber.e(message);
                    SnackbarHandler.showErrorSnackbar(context, coordinatorLayout, message);
                }
                fetchDataFromDatabase(launchId);
            }

            @Override
            public void onLaunchDeleted() {
                SnackbarHandler.showErrorSnackbar(context, coordinatorLayout, "Error: Launch not found.");
            }
        });
    }

    private void fetchDataFromDatabase(int launchId) {
        Launch launch = detailsDataRepository.getLaunch(launchId);
        if (launch != null) {
            updateViewModel(launch);
        } else {
            statefulView.showEmpty();
            SnackbarHandler.showErrorSnackbar(context, coordinatorLayout, "Unable to load launch.");
        }
    }

    private void updateViewModel(Launch launch) {
        model.getLaunch().setValue(launch);
    }

    private void fetchDataFromDatabaseForTitle(int launchId) {
        Launch launch = detailsDataRepository.getLaunch(launchId);
        if (launch != null) {
            setTitleView(launch);
        }
    }

    private void showNetworkLoading(boolean loading) {
        if (loading) {
            showLoading();
        } else {
            hideLoading();
        }
    }

    private void showLoading() {
        Timber.v("Show Loading...");
        detailSwipeRefresh.post(() -> detailSwipeRefresh.setRefreshing(true));
    }

    private void hideLoading() {
        Timber.v("Hide Loading...");
        detailSwipeRefresh.post(() -> detailSwipeRefresh.setRefreshing(false));
    }

    private void showFeedback() {
        new MaterialDialog.Builder(this)
                .title(R.string.feedback_title)
                .autoDismiss(true)
                .content(R.string.feedback_description)
                .neutralColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .negativeText(R.string.launch_data)
                .onNegative((dialog, which) -> {
                    String url = getString(R.string.launch_library_reddit);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                })
                .positiveColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .positiveText(R.string.app_feedback)
                .onPositive((dialog, which) -> dialog.getBuilder()
                        .title(R.string.need_support)
                        .content(R.string.need_support_description)
                        .neutralText(R.string.email)
                        .negativeText(R.string.cancel)
                        .positiveText(R.string.discord)
                        .onNeutral((dialog1, which1) -> {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@spacelaunchnow.me"});
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Space Launch Now - Feedback");

                            startActivity(Intent.createChooser(intent, "Email via..."));
                        })
                        .onPositive((dialog12, which12) -> {
                            String url = "https://discord.gg/WVfzEDW";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        })
                        .onNegative((dialog13, which13) -> dialog13.dismiss())
                        .show())
                .show();
    }


    private void updateViews(Launch launch) {
        if (launch != null) {
            fabShowable = true;
            this.launch = launch;
            setTitleView(launch);
            fabShare.show();
            statefulView.showContent();
        } else {
            statefulView.showEmpty();
        }
    }

    private void setTitleView(Launch launch) {
        if (!this.isDestroyed() && launch != null && launch.getRocket().getConfiguration() != null) {
            Timber.v("Loading detailLaunch %s", launch.getId());
            findProfileLogo(launch);
            findRocketImage(launch);
            detail_mission_location.setText(launch.getPad().getName());
            detail_rocket.setText(launch.getName());
        } else if (this.isDestroyed()) {
            Timber.v("DetailLaunch is destroyed, stopping loading data.");
        }
    }

    private void findRocketImage(Launch launch) {
        if (launch.getRocket().getConfiguration().getName() != null) {
            if (launch.getRocket().getConfiguration().getImageUrl() != null
                    && launch.getRocket().getConfiguration().getImageUrl().length() > 0
                    && !launch.getRocket().getConfiguration().getImageUrl().contains("placeholder")) {
                final String image = launch.getRocket().getConfiguration().getImageUrl();
                GlideApp.with(this)
                        .load(image)
                        .into(detail_profile_backdrop);
            }
        }
    }

    private void findProfileLogo(Launch launch) {

        String locationCountryCode;
        if (launch.getRocket().getConfiguration().getLaunchServiceProvider() != null) {
            if (launch.getRocket().getConfiguration().getLaunchServiceProvider().getNationUrl() != null) {
                applyProfileLogo(launch.getRocket().getConfiguration().getLaunchServiceProvider().getNationUrl());
            } else {
                locationCountryCode = launch.getRocket().getConfiguration().getLaunchServiceProvider().getCountryCode();
                //Go through various CountryCodes and assign flag.
                if (launch.getRocket().getConfiguration().getLaunchServiceProvider().getAbbrev().contains("ASA")) {
                    applyProfileLogo(getString(R.string.ariane_logo));
                } else if (launch.getRocket().getConfiguration().getLaunchServiceProvider().getAbbrev().contains("SpX")) {
                    applyProfileLogo(getString(R.string.spacex_logo));
                } else if (launch.getRocket().getConfiguration().getLaunchServiceProvider().getAbbrev().contains("BA")) {
                    applyProfileLogo(getString(R.string.Yuzhnoye_logo));
                } else if (launch.getRocket().getConfiguration().getLaunchServiceProvider().getAbbrev().contains("ULA")) {
                    applyProfileLogo(getString(R.string.ula_logo));
                } else if (locationCountryCode.length() == 3) {
                    if (locationCountryCode.contains("USA")) {
                        applyProfileLogo(getString(R.string.usa_flag));
                    } else if (locationCountryCode.contains("RUS")) {
                        applyProfileLogo(getString(R.string.rus_logo));
                    } else if (locationCountryCode.contains("CHN")) {
                        applyProfileLogo(getString(R.string.chn_logo));
                    } else if (locationCountryCode.contains("IND")) {
                        applyProfileLogo(getString(R.string.ind_logo));
                    } else if (locationCountryCode.contains("JPN")) {
                        applyProfileLogo(getString(R.string.jpn_logo));
                    }
                }
            }
        }
    }

    private void applyProfileLogo(String url) {
        Timber.d("LaunchDetailActivity - Loading Profile Image url: %s ", url);

        GlideApp.with(this)
                .load(url)
                .placeholder(R.drawable.icon_international)
                .error(R.drawable.icon_international)
                .into(detail_profile_image);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setData(String data) {
        response = data;
        Timber.v("LaunchDetailActivity - %s", response);
        Scanner scanner = new Scanner(response);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // process the line
            Timber.v("setData - %s ", line);
        }
        scanner.close();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (mMaxScrollSize == 0) {
            mMaxScrollSize = appBarLayout.getTotalScrollRange();
        }

        int percentage = (Math.abs(verticalOffset)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;
            detail_profile_image.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(200)
                    .start();
            fabShare.hide();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            detail_profile_image.animate()
                    .scaleY(1).scaleX(1)
                    .start();

            if (fabShowable) {
                fabShare.show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.v("LaunchDetailActivity onStart!");
        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    public void onStop() {
        Timber.v("LaunchDetailActivity onStop!");
        customTabActivityHelper.unbindCustomTabsService(this);
        super.onStop();
    }

    public void mayLaunchUrl(Uri parse) {
        if (customTabActivityHelper.mayLaunchUrl(parse, null, null)) {
            Timber.v("mayLaunchURL Accepted - %s", parse.toString());
        } else {
            Timber.v("mayLaunchURL Denied - %s", parse.toString());
        }
    }

    @OnClick(R.id.fab_share)
    public void onViewClicked() {
        String launchDate = "";
        String message = "";
        try {
            if (launch.getNet() != null) {
                Date date = launch.getNet();
                SimpleDateFormat sdf;
                if (!DateFormat.is24HourFormat(context)){
                    sdf = Utils.getSimpleDateFormatForUI("EEEE, MMMM dd, yyyy h:mm a zzz");
                } else {
                    sdf = Utils.getSimpleDateFormatForUI("EEEE, MMMM dd, yyyy HH:mm zzz");
                }
                launchDate = sdf.format(date);
            }
            if (launch.getPad().getLocation() != null) {

                message = launch.getName() + " launching from "
                        + launch.getPad().getLocation().getName() + "\n\n"
                        + launchDate;
            } else if (launch.getPad().getLocation() != null) {
                message = launch.getName() + " launching from "
                        + launch.getPad().getLocation().getName() + "\n\n"
                        + launchDate;
            } else {
                message = launch.getName()
                        + "\n\n"
                        + launchDate;
            }
        } catch (NullPointerException e) {
            Timber.e(e);
        }
        if (launch.getName() != null && launch.getUrl() != null) {
            ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setChooserTitle("Share: " + launch.getName())
                    .setText(String.format("%s\n\nWatch Live: %s", message, launch.getSlug()))
                    .startChooser();
            Analytics.getInstance().sendLaunchShared("Share FAB", launch.getName() + "-" + launch.getId().toString());
        } else {
            SnackbarHandler.showErrorSnackbar(this, coordinatorLayout, "Error - unable to share this launch.");
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.info_menu, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.info) {
            new MaterialDialog.Builder(this)
                    .title(R.string.improve_our_data)
                    .icon(new IconicsDrawable(this)
                            .icon(FontAwesome.Icon.faw_discord)
                            .color(Color.rgb(114, 137, 218))
                            .sizeDp(24))
                    .content(R.string.improve_our_data_content)
                    .negativeText(R.string.button_no)
                    .positiveText(R.string.ok)
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .onPositive((dialog, which) -> {
                        String discordUrl = getString(R.string.discord_url);
                        Intent discordIntent = new Intent(Intent.ACTION_VIEW);
                        discordIntent.setData(Uri.parse(discordUrl));
                        startActivity(discordIntent);
                    })
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLowMemory() {
        Timber.v("onLowMemory");
//        adView.destroy();
        super.onLowMemory();
    }

    @Override
    public void onRefresh() {
        if (launchId != 0) {
            fetchDataFromNetwork(launchId);
        }
    }
}
