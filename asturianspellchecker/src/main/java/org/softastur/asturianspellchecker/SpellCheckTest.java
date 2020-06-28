package org.softastur.asturianspellchecker;

//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class SpellCheckTest extends AppCompatActivity implements View.OnClickListener {

    Checker checker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.print("AST tried to launch1 but failed");
        setContentView(R.layout.activity_spell_check_test);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spell_check_test, menu);
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

    public void onClick(View view) {
        checker = new Checker(getApplicationContext());

        long start = System.nanoTime();
        for(int i = 0; i < 10000; i++ ) {
            checker.getRoot(Checker.textToIntArray("tienen"));
            checker.getRoot(Checker.textToIntArray("falábemos"));
            checker.getRoot(Checker.textToIntArray("calidá"));
            checker.getRoot(Checker.textToIntArray("enorme"));
            checker.getRoot(Checker.textToIntArray("ye"));
            checker.getRoot(Checker.textToIntArray("percenciello"));
            checker.getRoot(Checker.textToIntArray("foderáse"));
            checker.getRoot(Checker.textToIntArray("orientales"));
            checker.getRoot(Checker.textToIntArray("comentaba"));
            checker.getRoot(Checker.textToIntArray("tuvi"));
            checker.getRoot(Checker.textToIntArray("resueltos"));
            checker.getRoot(Checker.textToIntArray("tienen"));
            checker.getRoot(Checker.textToIntArray("falábemos"));
            checker.getRoot(Checker.textToIntArray("calidá"));
            checker.getRoot(Checker.textToIntArray("enorme"));
            checker.getRoot(Checker.textToIntArray("ye"));
            checker.getRoot(Checker.textToIntArray("percenciello"));
            checker.getRoot(Checker.textToIntArray("foderáse"));
            checker.getRoot(Checker.textToIntArray("orientales"));
            checker.getRoot(Checker.textToIntArray("comentaba"));
            checker.getRoot(Checker.textToIntArray("tuvi"));
            checker.getRoot(Checker.textToIntArray("resueltos"));
        }
        long end = System.nanoTime();
        System.out.println("ASTv 22000 checks took " + (end-start) + " nanoseconds (" + ((end-start)/1000000) + " milliseconds)");
    }
}
