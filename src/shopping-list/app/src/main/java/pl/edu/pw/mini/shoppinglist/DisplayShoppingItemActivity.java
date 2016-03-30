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
            shoppingItemNameEditText.setText(name);
            shoppingItemsNumberEditText.setText(Integer.toString(number));
            setFormEditable(false);
        }
    }

    private void setFormEditable(boolean editable) {
        shoppingItemNameEditText.setFocusable(editable);
        shoppingItemNameEditText.setClickable(editable);
        shoppingItemNameEditText.setFocusableInTouchMode(editable);

        shoppingItemsNumberEditText.setFocusable(editable);
        shoppingItemsNumberEditText.setClickable(editable);
        shoppingItemsNumberEditText.setFocusableInTouchMode(editable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_details_menu, menu);

        if (itemId == 0) { // new item mode
            MenuItem item = menu.findItem(R.id.remove_item);
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.remove_item:
                Log.d(LOG_TAG, "remove_item");
                removeItem();
                return true;
            case R.id.edit_item:
                Log.d(LOG_TAG, "edit_item");
                setFormEditable(true);
                shoppingItemsNumberEditText.setEnabled(true);
                shoppingItemNameEditText.setEnabled(true);
                Button b = (Button) findViewById(R.id.save_button);
                b.setVisibility(View.VISIBLE);
                return true;
            default:
                Log.d(LOG_TAG, "other item_details_menu item");
                return super.onOptionsItemSelected(item);
        }
    }

    private void removeItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.deleteItemQuestion)
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
                        // User cancelled the dialog
                    }
                });
        AlertDialog d = builder.create();
        d.setTitle(R.string.remove_question_title);
        d.show();
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
        String toastMsg = "Successfully ";
        if (itemId > 0) {
            shoppingItemsDb.updateShoppingItem(itemId, itemName, itemsNumber);
            toastMsg += "updated!";
        } else {
            shoppingItemsDb.insertShoppingItem(itemName, itemsNumber);
            toastMsg += "saved!";
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    public abstract class TextValidator implements TextWatcher {

        public abstract void validate(String text);

        @Override
        final public void afterTextChanged(Editable s) {
            String text = s.toString();
            validate(text);
        }

        @Override
        final public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

        @Override
        final public void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }
    }
}
