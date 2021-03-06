package me.calebjones.spacelaunchnow.content.data.upcoming;

import android.content.Context;
import android.support.annotation.UiThread;

import java.util.List;

import io.realm.Realm;
import me.calebjones.spacelaunchnow.content.data.callbacks.Callbacks;
import me.calebjones.spacelaunchnow.data.models.main.Launch;
import me.calebjones.spacelaunchnow.data.models.main.LaunchList;

/**
 * Responsible for retrieving data from the Realm cache.
 */

public class UpcomingDataRepository {

    private UpcomingDataLoader dataLoader;
    private Realm realm;

    private final Context context;

    public UpcomingDataRepository(Context context, Realm realm) {
        this.context = context;
        this.dataLoader = new UpcomingDataLoader(context);
        this.realm = realm;
    }


    @UiThread
    public void getUpcomingLaunches(int count, String search, String lspName, String serialNumber, Integer launcherId, Callbacks.ListCallbackMini launchCallback) {
        getUpcomingLaunchesFromNetwork(count, search, lspName, serialNumber, launcherId, launchCallback);
    }

    private void getUpcomingLaunchesFromNetwork(int count, String search, String lspName, String serialNumber, Integer launcherId, Callbacks.ListCallbackMini callback) {

        callback.onNetworkStateChanged(true);
        dataLoader.getUpcomingLaunchesList(30, count, search, lspName, serialNumber, launcherId, new Callbacks.ListNetworkCallbackMini() {
            @Override
            public void onSuccess(List<LaunchList> launches, int next, int total) {
                callback.onNetworkStateChanged(false);
                callback.onLaunchesLoaded(launches, next, total);
            }

            @Override
            public void onNetworkFailure(int code) {
                callback.onNetworkStateChanged(false);
                callback.onError("Unable to load launch data.", null);
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onNetworkStateChanged(false);
                callback.onError("An error has occurred! Uh oh.", throwable);
            }
        });
    }
}


