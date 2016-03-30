package pl.edu.pw.mini.shoppinglist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.util.Log.wtf;

public class DisplayShoppingItemActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Details view : ";
    private EditText shoppingItemNameEditText, shoppingItemsNumberEditText;
    private DBHelper shoppingItemsDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_shopping_item);

        // Set action bar

        Toolbar myToolbar = (Toolbar) findViewById(R.id.details_menu_toolbar);
        setSupportActionBar(myToolbar);

        // Set back button on action bar

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // Save references to edit texts

        shoppingItemNameEditText = (EditText) findViewById(R.id.item_name_edittext);
        shoppingItemsNumberEditText = (EditText) findViewById(R.id.items_number_edittext);

        // Fetch item from database and display

        shoppingItemsDb = new DBHelper(this);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            wtf(LOG_TAG, "Unexpected behaviour: No extras in intent!");
            return;
        }
        int id = extras.getInt(DBHelper.SHOPPING_ITEM_ID_COLUMN_NAME);
        if (id > 0) { // if view mode
            Cursor rs = shoppingItemsDb.getShoppingItem(id);
            rs.moveToFirst(); // TODO: 30.03.16
            String name = rs.getString(rs.getColumnIndex(DBHelper.SHOPPIG_ITEM_NAME_COLUMN_NAME));
            int number = rs.getInt(rs.getColumnIndex(DBHelper.SHOPPING_ITEM_NUMBER_COLUMN_NAME));
            if (!rs.isClosed()) {
                rs.close();
            }
            Button b = (Button) findViewById(R.id.save_button);
            b.setVisibility(View.INVISIBLE);

            shoppingItemNameEditText.setText(name);
            shoppingItemNameEditText.setFocusable(false);
            shoppingItemNameEditText.setClickable(false);

            shoppingItemsNumberEditText.setText(Integer.toString(number));
            shoppingItemsNumberEditText.setFocusable(false);
            shoppingItemsNumberEditText.setClickable(false);
        }
    }

    public void saveItem(View view) {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        int id = extras.getInt(DBHelper.SHOPPING_ITEM_ID_COLUMN_NAME);
        String itemName = shoppingItemNameEditText.getText().toString();
        int itemsNumber = Integer.parseInt(shoppingItemsNumberEditText.getText().toString());
        String toastMsg = "Successfully ";
        if (id > 0) {
            shoppingItemsDb.updateShoppingItem(id, itemName, itemsNumber);
            toastMsg += "updated!";
        } else {
            shoppingItemsDb.insertShoppingItem(itemName, itemsNumber);
            toastMsg += "saved!";
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
}
