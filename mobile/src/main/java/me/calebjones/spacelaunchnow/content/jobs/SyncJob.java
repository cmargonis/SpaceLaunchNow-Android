package me.calebjones.spacelaunchnow.content.jobs;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import me.calebjones.spacelaunchnow.content.DataManager;
import me.calebjones.spacelaunchnow.data.models.Constants;
import me.calebjones.spacelaunchnow.utils.Utils;
import timber.log.Timber;

public class SyncJob extends Job {

    public static final String TAG = Constants.ACTION_CHECK_NEXT_LAUNCH_TIMER + "_SYNC";

    public static void schedulePeriodicJob(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("background_sync", true)) {
            Timber.i("Background sync enabled, configuring JobRequest.");

            JobRequest.Builder builder = new JobRequest.Builder(SyncJob.TAG)
                    .setUpdateCurrent(true)
                    .setPersisted(true);

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("data_saver", false)) {
                Timber.v("DataSaver mode enabled...periodic set to once per day.");
                builder.setPeriodic(TimeUnit.DAYS.toMillis(1), 7200000);
            } else {
                Timber.v("DataSaver mode not enabled...every six hours.");
                builder.setPeriodic(TimeUnit.HOURS.toMillis(6), 7200000);
            }

            Timber.i("Scheduling JobRequests for %s", TAG);
            builder.build().schedule();
            JobUtils.logJobRequest();
        }
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Timber.d("Running job ID: %s Tag: %s", params.getId(), params.getTag());
        DataManager dataManager = new DataManager(getContext());
        dataManager.getNextUpcomingLaunchesMini();

        int count = 0;
        while (dataManager.isRunning()) {
            try {
                count += 100;
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Timber.e("ERROR - %s %s", TAG, e.getLocalizedMessage());
                Crashlytics.logException(e);
            }
        }
        Timber.i("%s complete...returning success after %s milliseconds.", TAG, count);
        return Result.SUCCESS;
    }
}
