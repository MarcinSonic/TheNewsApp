package pl.marcingorski.thenewsapp;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.settings_activity );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed ();
        Intent mainIntent = new Intent ( SettingsActivity.this, MainActivity.class );
        startActivity ( mainIntent );
    }

    public static class NewsappPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate ( savedInstanceState );
            addPreferencesFromResource ( R.xml.settings_main );

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString ();
            preference.setSummary ( stringValue );
            return true;
        }

    }

}