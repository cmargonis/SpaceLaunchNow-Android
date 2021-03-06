package me.calebjones.spacelaunchnow.content.services;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import me.calebjones.spacelaunchnow.BuildConfig;
import me.calebjones.spacelaunchnow.content.notifications.NotificationBuilder;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.data.models.main.LauncherConfig;
import me.calebjones.spacelaunchnow.data.models.main.Location;
import me.calebjones.spacelaunchnow.data.models.main.Pad;
import me.calebjones.spacelaunchnow.data.models.main.Rocket;
import me.calebjones.spacelaunchnow.utils.Utils;
import timber.log.Timber;

public class AppFireBaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Timber.d("From: %s", remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Timber.d("Message data payload: %s",remoteMessage.getData());
            Map<String, String> params = remoteMessage.getData();
            JSONObject data = new JSONObject(params);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss zzz", Locale.ENGLISH);
            try {
                String background = data.getString("background");
                String notificationType = data.getString("notification_type");

                if (notificationType.contains("news")){
                    return;
                }

                if (background.contains("true")) {
                    Launch launch = new Launch();
                    launch.setId(Integer.valueOf(data.getString("launch_id")));
                    launch.setNet(dateFormat.parse(data.getString("launch_net")));
                    launch.setName(data.getString("launch_name"));

                    LauncherConfig launcherConfig = new LauncherConfig();
                    launcherConfig.setImageUrl(data.getString("launch_image"));
                    Rocket rocket = new Rocket();
                    rocket.setConfiguration(launcherConfig);
                    launch.setRocket(rocket);

                    Location location = new Location();
                    location.setName(data.getString("launch_location"));
                    Pad pad = new Pad();
                    pad.setLocation(location);
                    launch.setPad(pad);
                    checkNotificationType(launch, notificationType, data.getString("webcast").contains("true"));
                }
            } catch (JSONException | ParseException e) {
                // Error parsing additional data
                Timber.e(e);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Timber.d("Message Notification Body: %s", remoteMessage.getNotification().getBody());
        }

    }

    //TODO break this out into calling various methods inside NotificationBuilder for various types instead of returning a boolean
    private void checkNotificationType(Launch launch, String notificationType, boolean webcastAvailable) {

        boolean notificationEnabled = Prefs.getBoolean("notificationEnabled", true);
        boolean netstampChanged = Prefs.getBoolean("netstampChanged", true);
        boolean webcastOnly = Prefs.getBoolean("webcastOnly", false);
        boolean twentyFourHour = Prefs.getBoolean("twentyFourHour", true);
        boolean oneHour = Prefs.getBoolean("oneHour", true);
        boolean tenMinutes = Prefs.getBoolean("tenMinutes", true);
        boolean inFlight = Prefs.getBoolean("inFlight", true);
        boolean success = Prefs.getBoolean("success", true);
        boolean oneMinute = Prefs.getBoolean("oneMinute", true);

        if (notificationEnabled) {
            Context context = getApplicationContext();

            if (notificationType.contains("netstampChanged") && netstampChanged) {
                Timber.i("Netstamp Changed enabled.");
                NotificationBuilder.notifyUserLaunchScheduleChanged(context, launch);
            }

            if (notificationType.contains("inFlight") && inFlight) {
                NotificationBuilder.notifyUserInFlight(context, launch);
            }

            if (notificationType.contains("success") && success) {
                NotificationBuilder.notifyUserSuccess(context, launch);
            }

            if (notificationType.contains("failure") && success) {
                NotificationBuilder.notifyUserFailure(context, launch);
            }

            if (notificationType.contains("partial_failure") && success) {
                NotificationBuilder.notifyUserPartialFailure(context, launch);
            }

            if (BuildConfig.DEBUG && notificationType.contains("test")) {
                NotificationBuilder.notifyUserTest(context, launch);
            }

            if (webcastOnly && !webcastAvailable) {
                Timber.i("Webcast is required for to post notification - none available therefore skipping.");
                return;
            }

            if (notificationType.contains("twentyFourHour") && twentyFourHour) {
                NotificationBuilder.notifyUserTwentyFourHours(context, launch);
            }

            if (notificationType.contains("oneHour") && oneHour) {
                NotificationBuilder.notifyUserOneHour(context, launch);
            }

            if (notificationType.contains("tenMinute") && tenMinutes) {
                NotificationBuilder.notifyUserTenMinutes(context, launch);
            }

            if (notificationType.contains("oneMinute") && oneMinute) {
                NotificationBuilder.notifyUserOneMinute(context, launch);
            }
        }
    }
}
