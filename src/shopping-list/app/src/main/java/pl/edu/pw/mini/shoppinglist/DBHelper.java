package pl.edu.pw.mini.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String
            DATABASE_NAME = "ShoppingListDBName.db",
            SHOPPING_ITEM_ID_COLUMN_NAME = "id",
            SHOPPIG_LIST_TABLE_NAME = "shopping_list",
            SHOPPIG_ITEM_NAME_COLUMN_NAME = "name",
            SHOPPING_ITEM_NUMBER_COLUMN_NAME = "number";
    public static final String
            SHOPPIG_ITEM_ID_SCHEMA = SHOPPING_ITEM_ID_COLUMN_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT",
            SHOPPIG_ITEM_COLUMN_NAME_SCHEMA = SHOPPIG_ITEM_NAME_COLUMN_NAME + " TEXT",
            SHOPPING_ITEM_NUMBER_SCHEMA = SHOPPING_ITEM_NUMBER_COLUMN_NAME + " INTEGER";
    public static final String
            COLUMNS_SCHEMA = TextUtils.join(",", new String[]{SHOPPIG_ITEM_ID_SCHEMA, SHOPPIG_ITEM_COLUMN_NAME_SCHEMA, SHOPPING_ITEM_NUMBER_SCHEMA}),
            CREATE_SHOPPIG_LIST_TABLE_QUERY = "CREATE TABLE " + SHOPPIG_LIST_TABLE_NAME + "(" + COLUMNS_SCHEMA + ")",
            DROP_SHOPPIG_LIST_TABLE_QUERY = "DROP TABLE IF EXISTS " + SHOPPIG_LIST_TABLE_NAME;
    public static final int DEFAULT_ITEMS_NUMBER = 0;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SHOPPIG_LIST_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_SHOPPIG_LIST_TABLE_QUERY);
        onCreate(db);
    }

    public void insertShoppingItem(String name) {
        insertShoppingItem(name, DEFAULT_ITEMS_NUMBER);
    }

    public void insertShoppingItem(String name, int number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SHOPPIG_ITEM_NAME_COLUMN_NAME, name);
        cv.put(SHOPPING_ITEM_NUMBER_COLUMN_NAME, number);
        db.insert(SHOPPIG_LIST_TABLE_NAME, null, cv);
    }

    public void updateShoppingItem(int id, String name, int number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SHOPPIG_ITEM_NAME_COLUMN_NAME, name);
        cv.put(SHOPPING_ITEM_NUMBER_COLUMN_NAME, number);
        db.update(SHOPPIG_LIST_TABLE_NAME, cv, SHOPPING_ITEM_ID_COLUMN_NAME + " = ? ", new String[]{Integer.toString(id)});
    }

    public void deleteShoppingItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SHOPPIG_LIST_TABLE_NAME, "id = ? ", new String[]{Integer.toString(id)});
    }

    public Cursor getShoppingItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + SHOPPIG_LIST_TABLE_NAME + " WHERE " + SHOPPING_ITEM_ID_COLUMN_NAME + "=" + id, null);
    }

    public int numberShoppingItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, SHOPPIG_LIST_TABLE_NAME);
    }

    public ArrayList<ShoppingItem> getAllShoppingItems() {
        ArrayList<ShoppingItem> arr = new ArrayList<ShoppingItem>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + SHOPPIG_LIST_TABLE_NAME, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            String itemName = res.getString(res.getColumnIndex(SHOPPIG_ITEM_NAME_COLUMN_NAME));
            int number = res.getInt(res.getColumnIndex(SHOPPING_ITEM_NUMBER_COLUMN_NAME));
            int id = res.getInt(res.getColumnIndex(SHOPPING_ITEM_ID_COLUMN_NAME));
            arr.add(new ShoppingItem(id, itemName, number));
            res.moveToNext();
        }
        return arr;
    }
}
