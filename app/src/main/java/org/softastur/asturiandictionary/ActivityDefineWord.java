package org.softastur.asturiandictionary;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ActivityDefineWord extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {
            CharSequence text = getIntent()
                    .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);

            String word = String.valueOf(text);

            Toast.makeText(this, "Vamos definir «" + word + "»", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, org.softastur.asturianspellchecker.ActivitySpellCheckSettings.class);
            startActivity(intent);
        }else{
            Toast.makeText(this, "Nun foi posible definir.", Toast.LENGTH_SHORT).show();
        }

    }

    private void startDictionaryView(String word) {

    }


}
