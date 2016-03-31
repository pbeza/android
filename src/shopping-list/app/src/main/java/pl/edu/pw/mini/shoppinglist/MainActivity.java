package pl.edu.pw.mini.shoppinglist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = "Shopping List App : ";
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
        this.shoppingItemsListView = (ListView) findViewById(R.id.shopping_listview);
        this.shoppingItemsListView.setOnItemClickListener(new OnShoppingItemClickListener());
        int itemsNumber = refreshAllShoppingItemsListView();
        setShoppingListTitle(itemsNumber);

        Log.d(LOG_TAG, "MainActivity.onCreate() event");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.add_item_id:
                addNewItem();
                return true;
            case R.id.remove_all_settings_item_id:
                removeAllItems();
                return true;
            default:
                Log.d(LOG_TAG, "Unrecognized main_menu item");
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewItem() {
        Bundle b = new Bundle();
        b.putInt(DBHelper.SHOPPING_ITEM_ID_COLUMN_NAME, 0);
        Intent intent = new Intent(getApplicationContext(), DisplayShoppingItemActivity.class);
        intent.putExtras(b);
        startActivityForResult(intent, ADD_ITEM_REQUEST);
    }

    private void removeAllItems() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_items_question)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        shoppingItemsDb.recreateDatabase(shoppingItemsDb.getWritableDatabase());
                        refreshAllShoppingItemsListView();
                        setShoppingListTitle(0);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog d = builder.create();
        d.setTitle(R.string.delete_all_items_question_title);
        d.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int itemsNumber;

        if (requestCode == ADD_ITEM_REQUEST) {
            printResultCodeStatus(resultCode, "adding item");
        } else if (requestCode == SHOW_ITEM_DETAILS_REQUEST) {
            printResultCodeStatus(resultCode, "showing item's details");
            // Note: it's possible that item has been removed in details view
        } else {
            Log.d(LOG_TAG, "Unrecognized request code in onActivityResult()");
            return;
        }
        itemsNumber = refreshAllShoppingItemsListView();
        setShoppingListTitle(itemsNumber);
    }

    private void setShoppingListTitle(int itemsNumber) {
        TextView t = (TextView) findViewById(R.id.shopping_list_title_id);
        String title;
        if (itemsNumber == 0) {
            title = getString(R.string.shopping_list_is_empty_title);
        } else {
            title = String.format(getString(R.string.shopping_list_not_empty_title), itemsNumber);
        }
        t.setText(title);
    }

    private int refreshAllShoppingItemsListView() {
        ArrayList<ShoppingItem> allShoppingItems = this.shoppingItemsDb.getAllShoppingItems(getString(R.string.items));
        ArrayAdapter<ShoppingItem> arrAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allShoppingItems);
        this.shoppingItemsListView.setAdapter(arrAdapter);
        return allShoppingItems.size();
    }

    private void printResultCodeStatus(int resultCode, String logMsg) {

        if (resultCode == RESULT_OK) {
            Log.d(LOG_TAG, "Successfully returned from " + logMsg);
        } else {
            Log.d(LOG_TAG, "Returned from " + logMsg + " ended with cancel or failure status");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, getLocalClassName() + ".onStart() event");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, getLocalClassName() + ".onResume() event");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, getLocalClassName() + ".onPause() event");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, getLocalClassName() + ".onStop() event");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, getLocalClassName() + ".onDestroy() event");
    }

    private class OnShoppingItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ShoppingItem item = (ShoppingItem) parent.getItemAtPosition(position);
            int cid = item.getId();
            Bundle dataBundle = new Bundle();
            dataBundle.putInt(DBHelper.SHOPPING_ITEM_ID_COLUMN_NAME, cid);
            Intent intent = new Intent(getApplicationContext(), DisplayShoppingItemActivity.class);
            intent.putExtras(dataBundle);
            startActivityForResult(intent, SHOW_ITEM_DETAILS_REQUEST);
        }
    }
}
