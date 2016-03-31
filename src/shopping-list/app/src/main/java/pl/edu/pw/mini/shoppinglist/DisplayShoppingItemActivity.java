package pl.edu.pw.mini.shoppinglist;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DisplayShoppingItemActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Details view : ";
    private EditText shoppingItemNameEditText, shoppingItemsNumberEditText;
    private DBHelper shoppingItemsDb;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_shopping_item);

        // Save item's ID

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            throw new ExceptionInInitializerError("No id in extras");
        }
        itemId = extras.getInt(DBHelper.SHOPPING_ITEM_ID_COLUMN_NAME);

        // Set action bar

        Toolbar myToolbar = (Toolbar) findViewById(R.id.details_menu_toolbar);
        setSupportActionBar(myToolbar);

        // Set back button on action bar

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // Save references to edit texts

        shoppingItemNameEditText = (EditText) findViewById(R.id.item_name_edittext);
        shoppingItemsNumberEditText = (EditText) findViewById(R.id.items_number_edittext);

        shoppingItemsNumberEditText.addTextChangedListener(new TextValidator() {
            @Override
            public void validate(String text) {
                if (!text.trim().isEmpty()) {
                    int n = Integer.parseInt(text);
                    if (n <= 0) {
                        shoppingItemsNumberEditText.setError(getString(R.string.specify_at_least_one_warning));
                    }
                }
            }
        });

        // Fetch item from database and display

        shoppingItemsDb = new DBHelper(this);
        if (itemId > 0) { // if view mode
            Cursor rs = shoppingItemsDb.getShoppingItem(itemId);
            rs.moveToFirst();
            String name = rs.getString(rs.getColumnIndex(DBHelper.SHOPPIG_ITEM_NAME_COLUMN_NAME));
            int number = rs.getInt(rs.getColumnIndex(DBHelper.SHOPPING_ITEM_NUMBER_COLUMN_NAME));
            if (!rs.isClosed()) {
                rs.close();
            }
            Button b = (Button) findViewById(R.id.save_button);
            b.setVisibility(View.INVISIBLE);
            setFormEditable(false);
            shoppingItemNameEditText.setText(name);
            shoppingItemsNumberEditText.setText(String.format("%1$d", number));
        }
    }

    private void setFormEditable(boolean editable) {
        shoppingItemNameEditText.setFocusable(editable);
        shoppingItemNameEditText.setClickable(editable);
        shoppingItemNameEditText.setFocusableInTouchMode(editable);
        shoppingItemNameEditText.setLongClickable(editable);

        shoppingItemsNumberEditText.setFocusable(editable);
        shoppingItemsNumberEditText.setClickable(editable);
        shoppingItemsNumberEditText.setFocusableInTouchMode(editable);
        shoppingItemsNumberEditText.setLongClickable(editable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_details_menu, menu);

        if (itemId == 0) { // new item mode
            MenuItem removeItem = menu.findItem(R.id.remove_item);
            removeItem.setVisible(false);

            MenuItem editItem = menu.findItem(R.id.edit_item);
            editItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.remove_item:
                removeItem();
                return true;
            case R.id.edit_item:
                editItem();
                return true;
            default:
                Log.d(LOG_TAG, "Unrecognized item_details_menu item");
                return super.onOptionsItemSelected(item);
        }
    }

    private void removeItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.remove_item_question)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        shoppingItemsDb.deleteShoppingItem(itemId);
                        Toast.makeText(getApplicationContext(), getString(R.string.deleted_successfully_msg), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog d = builder.create();
        d.setTitle(R.string.remove_question_title);
        d.show();
    }

    private void editItem() {
        setFormEditable(true);
        shoppingItemsNumberEditText.setEnabled(true);
        shoppingItemNameEditText.setEnabled(true);
        Button b = (Button) findViewById(R.id.save_button);
        b.setVisibility(View.VISIBLE);
    }

    public void saveItem(View view) {
        String itemName = shoppingItemNameEditText.getText().toString().trim();
        String itemsNumberString = shoppingItemsNumberEditText.getText().toString().trim();
        if (itemName.isEmpty()) {
            shoppingItemNameEditText.setError(getString(R.string.specify_item_name_warning));
            return;
        }
        if (itemsNumberString.isEmpty()) {
            shoppingItemsNumberEditText.setError(getString(R.string.specify_item_number_warning));
            return;
        }
        int itemsNumber = Integer.parseInt(itemsNumberString);
        String toastMsg;
        if (itemId > 0) {
            shoppingItemsDb.updateShoppingItem(itemId, itemName, itemsNumber);
            toastMsg = getString(R.string.successfully_updated);
        } else {
            shoppingItemsDb.insertShoppingItem(itemName, itemsNumber);
            toastMsg = getString(R.string.successfully_saved);
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
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

    public abstract class TextValidator implements TextWatcher {

        public abstract void validate(String text);

        @Override
        final public void afterTextChanged(Editable s) {
            String text = s.toString();
            validate(text);
        }

        @Override
        final public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        final public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
