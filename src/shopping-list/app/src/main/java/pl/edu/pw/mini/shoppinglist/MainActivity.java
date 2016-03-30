package pl.edu.pw.mini.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final static String logTag = "Shopping List App : ";
    private final static int ADD_ITEM_REQUEST = 1, SHOW_ITEM_DETAILS_REQUEST = 2;
    private ListView shoppingItemsListView;
    private DBHelper shoppingItemsDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set action bar

        Toolbar myToolbar = (Toolbar) findViewById(R.id.menu_toolbar);
        setSupportActionBar(myToolbar);

        // Fetch all items from database and display

        this.shoppingItemsDb = new DBHelper(this);
        ArrayList<String> allShoppingItems = this.shoppingItemsDb.getAllShoppingItems();
        ArrayAdapter<String> arrAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allShoppingItems);
        this.shoppingItemsListView = (ListView) findViewById(R.id.shopping_listview);
        this.shoppingItemsListView.setAdapter(arrAdapter);
        this.shoppingItemsListView.setOnItemClickListener(new OnShoppingItemClickListener());

        Log.d(logTag, "MainActivity.onCreate() event");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.add_item:
                Log.d(logTag, "add_item");
                Bundle b = new Bundle();
                b.putInt(DBHelper.SHOPPING_ITEM_ID_COLUMN_NAME, 0);
                Intent intent = new Intent(getApplicationContext(), DisplayShoppingItemActivity.class);
                intent.putExtras(b);
                startActivityForResult(intent, ADD_ITEM_REQUEST);
                return true;
            case R.id.action_settings:
                Log.d(logTag, "action_settings");
                return true;
            default:
                Log.d(logTag, "other menu item");
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(logTag, "MainActivity.onStart() event");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(logTag, "MainActivity.onResume() event");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(logTag, "MainActivity.onPause() event");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(logTag, "MainActivity.onStop() event");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(logTag, "MainActivity.onDestroy() event");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ADD_ITEM_REQUEST) {
            printResultCodeStatus(resultCode, "adding item");
        } else if (requestCode == SHOW_ITEM_DETAILS_REQUEST) {
            printResultCodeStatus(resultCode, "showing item's details");
        } else {
            Log.d(logTag, "Unrecognized request code in onActivityResult()");
        }
    }

    private void printResultCodeStatus(int resultCode, String logMsg) {

        if (resultCode == RESULT_OK) {
            Log.d(logTag, "Successfully returned from " + logMsg);
        } else {
            Log.d(logTag, "Returned from " + logMsg + " ended with cancel or failure status");
        }
    }

    private class OnShoppingItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int cid = position + 1;
            Bundle dataBundle = new Bundle();
            dataBundle.putInt(DBHelper.SHOPPING_ITEM_ID_COLUMN_NAME, cid);
            Intent intent = new Intent(getApplicationContext(), DisplayShoppingItemActivity.class);
            intent.putExtras(dataBundle);
            startActivityForResult(intent, SHOW_ITEM_DETAILS_REQUEST);
        }
    }
}
