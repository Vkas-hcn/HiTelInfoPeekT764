package mei.ye;

import android.content.Context;
import android.content.SharedPreferences;

public class DataPreferences {
    private static final String PREFS_NAME = "user_prefs";

    private static volatile DataPreferences instance;
    private final SharedPreferences prefs;
    
    private DataPreferences(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static DataPreferences getInstance(Context context) {
        if (instance == null) {
            synchronized (DataPreferences.class) {
                if (instance == null) {
                    instance = new DataPreferences(context);
                }
            }
        }
        return instance;
    }
    
    // Save custom string value
    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }
    
    // Get custom string value
    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }
    
    // Save custom int value
    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }
    
    // Get custom int value
    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }
    
    // Save custom boolean value
    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }
    
    // Get custom boolean value
    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }
    
    // Save custom long value
    public void putLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
    }
    
    // Get custom long value
    public long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }
    
    // Remove specific key
    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }
    
    // Clear all preferences
    public void clearAll() {
        prefs.edit().clear().apply();
    }
    
    // Check if key exists
    public boolean contains(String key) {
        return prefs.contains(key);
    }
}
