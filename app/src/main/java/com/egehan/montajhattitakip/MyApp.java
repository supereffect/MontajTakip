package com.egehan.montajhattitakip;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MyApp extends Application {

    private static FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    public void onCreate() {
        super.onCreate();

        // Firebase başlat
        FirebaseApp.initializeApp(this);

        // Remote Config instance al
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Config ayarları
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // her 1 saatte bir fetch
                .build();

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        fetchRemoteConfig();
    }

    private void fetchRemoteConfig() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("RemoteConfig", "Fetch başarılı: " + mFirebaseRemoteConfig.getString("wallpaperurl"));
                    } else {
                        Log.e("RemoteConfig", "Fetch başarısız");
                    }
                });
    }

    public static String getWallpaperUrl() {
        if (mFirebaseRemoteConfig != null) {
            return mFirebaseRemoteConfig.getString("wallpaperurl");
        }
        return "";
    }
}
