package dinoosauro.filedate.sync;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class FileDateSync extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }

}
