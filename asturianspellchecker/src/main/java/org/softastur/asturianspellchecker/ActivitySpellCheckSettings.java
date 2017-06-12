package org.softastur.asturianspellchecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;


public class ActivitySpellCheckSettings extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    public static final String KEY_PREF_OVERRIDE_LANGUAGE = "org.softastur.spellchecker.overrideLanguage";

    private static final int VALUE_PREF_OVERRIDE_LANGUAGE_DEFAULT      = 0;
    private static final int VALUE_PREF_OVERRIDE_LANGUAGE_ASTURIAN     = 1;
    private static final int VALUE_PREF_OVERRIDE_LANGUAGE_ASTURIAN_ALT = 2;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkInterfaceLocale(); // MUST BE CALLED FIRST AFTER SUPER.ONCREATE

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int overrideLanguage = sharedPref.getInt(KEY_PREF_OVERRIDE_LANGUAGE, VALUE_PREF_OVERRIDE_LANGUAGE_DEFAULT);


        String[] quotes = getResources().getStringArray(R.array.quotes);
        String[] authors = getResources().getStringArray(R.array.author);
        String[] author_infos = getResources().getStringArray(R.array.author_info);
        Random randomGenerator = new Random();
        int choice = randomGenerator.nextInt(4);

        setContentView(R.layout.activity_spellcheck_settings);

        ((TextView)findViewById(R.id.quote)).setText(quotes[choice]);
        ((TextView)findViewById(R.id.author)).setText(authors[choice]);
        ((TextView)findViewById(R.id.author_info)).setText(author_infos[choice]);

        RadioGroup radios = (RadioGroup) findViewById(R.id.radio_group);
        switch(overrideLanguage) {
            case VALUE_PREF_OVERRIDE_LANGUAGE_DEFAULT:
                radios.check(R.id.radio_system);
                break;
            case VALUE_PREF_OVERRIDE_LANGUAGE_ASTURIAN:
                radios.check(R.id.radio_asturian);
                break;
            case VALUE_PREF_OVERRIDE_LANGUAGE_ASTURIAN_ALT:
                radios.check(R.id.radio_asturian_alt);
                break;
            default:
        }
        radios.setOnCheckedChangeListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spellcheck_settings, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public interface OnSettingsFragmentListener {
        // TODO: Update argument type and name
        public void onNeedsRestart();
    }

    public void onCheckedChanged(RadioGroup group, int id ) {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if(id == R.id.radio_system) {
            editor.putInt(KEY_PREF_OVERRIDE_LANGUAGE, VALUE_PREF_OVERRIDE_LANGUAGE_DEFAULT);
        }else if(id == R.id.radio_asturian) {
            editor.putInt(KEY_PREF_OVERRIDE_LANGUAGE, VALUE_PREF_OVERRIDE_LANGUAGE_ASTURIAN);
        }else if(id == R.id.radio_asturian_alt) {
            editor.putInt(KEY_PREF_OVERRIDE_LANGUAGE, VALUE_PREF_OVERRIDE_LANGUAGE_ASTURIAN_ALT);
        }
        editor.commit();

        restart(this,1);

    }

    public void restart(Context context, int delay) {
        if (delay < 50) {
            delay = 50;
        }

        Intent restartIntent = new Intent();
        restartIntent.setClass(this,ActivitySpellCheckSettings.class);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent intent = PendingIntent.getActivity(
                context,
                0,
                restartIntent, PendingIntent.FLAG_ONE_SHOT
        );
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
        finish();
        System.exit(0);
    }

    private void checkInterfaceLocale() {
        int overrideLanguage =
                getPreferences(Context.MODE_PRIVATE)
                        .getInt(KEY_PREF_OVERRIDE_LANGUAGE,VALUE_PREF_OVERRIDE_LANGUAGE_DEFAULT);

        if(overrideLanguage != VALUE_PREF_OVERRIDE_LANGUAGE_DEFAULT) {
            Locale locale = VALUE_PREF_OVERRIDE_LANGUAGE_ASTURIAN == overrideLanguage
                    ? new Locale("ast")
                    : new Locale("es","XA");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(
                    config,
                    getBaseContext().getResources().getDisplayMetrics()
            );
         }
    }

}
