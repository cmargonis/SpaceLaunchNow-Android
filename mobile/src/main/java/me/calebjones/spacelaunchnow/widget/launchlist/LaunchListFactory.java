package me.calebjones.spacelaunchnow.widget.launchlist;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import me.calebjones.spacelaunchnow.R;
import me.calebjones.spacelaunchnow.content.database.SwitchPreferences;
import me.calebjones.spacelaunchnow.content.util.QueryBuilder;
import me.calebjones.spacelaunchnow.data.models.launchlibrary.Launch;
import me.calebjones.spacelaunchnow.ui.launchdetail.activity.LaunchDetailActivity;
import me.calebjones.spacelaunchnow.utils.Utils;
import timber.log.Timber;


public class LaunchListFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private int appWidgetId;
    private SwitchPreferences switchPreferences;
    private SharedPreferences sharedPref;
    private List<Launch> launches = new RealmList<>();

    public LaunchListFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        Timber.d("AppWidgetId: %s", appWidgetId);
        switchPreferences = SwitchPreferences.getInstance(context);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void initData() {
        Timber.d("Init Data for Widget %s", appWidgetId);
        launches = new RealmList<>();
        Realm mRealm = Realm.getDefaultInstance();
        launches = getLaunches(mRealm);
        mRealm.close();
    }

    private List<Launch> getLaunches(Realm mRealm) {
        Timber.d("Getting launches.");
        Date date = new Date();

        RealmResults<Launch> launchRealms;
        if (switchPreferences.getAllSwitch()) {
            RealmQuery<Launch> query = mRealm.where(Launch.class)
                    .greaterThanOrEqualTo("net", date);
            if (switchPreferences.getNoGoSwitch()) {
                query.equalTo("status", 1);
            }
            launchRealms = query.findAllSorted("net", Sort.ASCENDING);
            Timber.v("loadLaunches - Realm query created.");
        } else {
            launchRealms = QueryBuilder.buildSwitchQuery(context, mRealm);
            Timber.v("loadLaunches - Filtered Realm query created - size: %s", launchRealms.size());
        }

        for (Launch launch : launchRealms) {
            Timber.v("Launch: %s", launch.getName());
            if (launches.size() <= 20) {
                launches.add(launch);
            }
        }
        Timber.v("Final Launches size: %s", launches.size());
        return mRealm.copyFromRealm(launches);
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return (launches.size());
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Launch launch = launches.get(position);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int colorWhite = 0xFFFFFFFF;
        int colorSecondaryWhite = 0xB3FFFFFF;
        int colorBackground = 0xFF303030;
        int widgetPrimaryTextColor = sharedPref.getInt("widget_text_color",colorWhite);
        int widgetBackgroundColor = sharedPref.getInt("widget_background_color", colorBackground);
        int widgetSecondaryTextColor = sharedPref.getInt("widget_secondary_text_color",colorSecondaryWhite);
        int widgetIconsColor = sharedPref.getInt("widget_icon_color",colorWhite);
        int widgetAlpha = Color.alpha(widgetBackgroundColor);
        Timber.v("LaunchListFactory - binding view at %s for launch %s", position, launch.getName());
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.launch_list_item);
        String[] title;
        String launchDate;
        SimpleDateFormat sdf = Utils.getSimpleDateFormatForUI("MMMM dd, yyyy");

        //Retrieve missionType
        if (launch.getMissions().size() != 0) {
            row.setImageViewResource(R.id.categoryIcon, Utils.getCategoryIcon(launch.getMissions().get(0).getTypeName()));
        } else {
            row.setImageViewResource(R.id.categoryIcon, R.drawable.ic_unknown);
        }
        row.setInt(R.id.categoryIcon, "setColorFilter", widgetIconsColor);
        row.setTextColor(R.id.launch_rocket, widgetPrimaryTextColor);
        row.setTextColor(R.id.mission, widgetSecondaryTextColor);
        row.setTextColor(R.id.launch_date, widgetSecondaryTextColor);
        row.setTextColor(R.id.location, widgetSecondaryTextColor);

        if (launch.getStatus() != null && launch.getStatus() == 2) {
            //Get launch date
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = launch.getNet();
            launchDate = sdf.format(date);

            row.setTextViewText(R.id.launch_date, String.format("%s (Unconfirmed)", launchDate));
        } else {
            //Get launch date
            if (sharedPref.getBoolean("local_time", true)) {

                sdf.toLocalizedPattern();
                Date date = launch.getNet();
                launchDate = sdf.format(date);
            } else {
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = launch.getNet();
                launchDate = sdf.format(date);
            }
            row.setTextViewText(R.id.launch_date, launchDate);
        }

        if (launch.getName() != null) {
            title = launch.getName().split("\\|");
            try {
                if (title.length > 0) {
                    row.setTextViewText(R.id.launch_rocket, title[1].trim());
                    row.setTextViewText(R.id.mission, title[0].trim());
                } else {
                    row.setTextViewText(R.id.launch_rocket, launch.getName());
                    if (launch.getMissions().size() > 0) {
                        row.setTextViewText(R.id.mission, launch.getMissions().get(0).getName());
                    }
                }
            } catch (ArrayIndexOutOfBoundsException exception) {
                row.setTextViewText(R.id.launch_rocket, launch.getName());
                if (launch.getMissions().size() > 0) {
                    row.setTextViewText(R.id.mission, launch.getMissions().get(0).getName());
                }

            }
        }

        //If pad and agency exist add it to location, otherwise get whats always available
        if (launch.getLocation() != null) {
            row.setTextViewText(R.id.location, launch.getLocation().getName());
        } else {
            row.setTextViewText(R.id.location, "Click for more information.");
        }
        row.setTextViewText(R.id.location, launch.getLocation().getName());

        Intent exploreIntent = new Intent(context, LaunchDetailActivity.class);
        exploreIntent.putExtra("TYPE", "launch");
        exploreIntent.putExtra("launchID", launch.getId());
        exploreIntent.setData(Uri.parse(exploreIntent.toUri(Intent.URI_INTENT_SCHEME)));
        exploreIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        row.setOnClickFillInIntent(R.id.rootview, exploreIntent);

        return (row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return (1);
    }

    @Override
    public long getItemId(int position) {
        return (position);
    }

    @Override
    public boolean hasStableIds() {
        return (true);
    }
}