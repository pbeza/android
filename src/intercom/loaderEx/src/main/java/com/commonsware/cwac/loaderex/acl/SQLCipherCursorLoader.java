/*
 * Copyright (c) 2011-2012 CommonsWare, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.loaderex.acl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import net.sqlcipher.database.SQLiteDatabase;

public class SQLCipherCursorLoader extends AbstractCursorLoader {
  SQLiteDatabase db=null;
  String rawQuery=null;
  String[] args=null;

  /**
   * Creates a fully-specified SQLiteCursorLoader. See
   * {@link SQLiteDatabase#rawQuery(SQLiteDatabase, String, String[])
   * SQLiteDatabase.rawQuery()} for documentation on the
   * meaning of the parameters. These will be passed as-is
   * to that call.
   */
  public SQLCipherCursorLoader(Context context, SQLiteDatabase db,
                               String rawQuery, String[] args) {
    super(context);
    this.db=db;
    this.rawQuery=rawQuery;
    this.args=args;
  }

  /**
   * Runs on a worker thread and performs the actual
   * database query to retrieve the Cursor.
   */
  @Override
  protected Cursor buildCursor() {
    return(db.rawQuery(rawQuery, args));
  }

  /**
   * Writes a semi-user-readable roster of contents to
   * supplied output.
   */
  @Override
  public void dump(String prefix, FileDescriptor fd,
                   PrintWriter writer, String[] args) {
    super.dump(prefix, fd, writer, args);
    writer.print(prefix);
    writer.print("rawQuery=");
    writer.println(rawQuery);
    writer.print(prefix);
    writer.print("args=");
    writer.println(Arrays.toString(args));
  }

  public void insert(String table, String nullColumnHack,
                     ContentValues values) {
    buildInsertTask(this).execute(db, table, nullColumnHack, values);
  }

  public void update(String table, ContentValues values,
                     String whereClause, String[] whereArgs) {
    buildUpdateTask(this).execute(db, table, values, whereClause,
                                  whereArgs);
  }

  public void replace(String table, String nullColumnHack,
                      ContentValues values) {
    buildReplaceTask(this).execute(db, table, nullColumnHack, values);
  }

  public void delete(String table, String whereClause,
                     String[] whereArgs) {
    buildDeleteTask(this).execute(db, table, whereClause, whereArgs);
  }

  public void execSQL(String sql, Object[] bindArgs) {
    buildExecSQLTask(this).execute(db, sql, bindArgs);
  }

  protected ContentChangingTask buildInsertTask(SQLCipherCursorLoader loader) {
    return(new InsertTask(loader));
  }

  protected ContentChangingTask buildUpdateTask(SQLCipherCursorLoader loader) {
    return(new UpdateTask(loader));
  }

  protected ContentChangingTask buildReplaceTask(SQLCipherCursorLoader loader) {
    return(new ReplaceTask(loader));
  }

  protected ContentChangingTask buildDeleteTask(SQLCipherCursorLoader loader) {
    return(new DeleteTask(loader));
  }

  protected ContentChangingTask buildExecSQLTask(SQLCipherCursorLoader loader) {
    return(new ExecSQLTask(loader));
  }

  protected static class InsertTask extends ContentChangingTask {
    InsertTask(SQLCipherCursorLoader loader) {
      super(loader);
    }

    @Override
    protected Void doInBackground(Object... params) {
      SQLiteDatabase db=(SQLiteDatabase)params[0];
      String table=(String)params[1];
      String nullColumnHack=(String)params[2];
      ContentValues values=(ContentValues)params[3];

      db.insert(table, nullColumnHack, values);

      return(null);
    }
  }

  protected static class UpdateTask extends ContentChangingTask {
    UpdateTask(SQLCipherCursorLoader loader) {
      super(loader);
    }

    @Override
    protected Void doInBackground(Object... params) {
      SQLiteDatabase db=(SQLiteDatabase)params[0];
      String table=(String)params[1];
      ContentValues values=(ContentValues)params[2];
      String where=(String)params[3];
      String[] whereParams=(String[])params[4];

      db.update(table, values, where, whereParams);

      return(null);
    }
  }

  protected static class ReplaceTask extends ContentChangingTask {
    ReplaceTask(SQLCipherCursorLoader loader) {
      super(loader);
    }

    @Override
    protected Void doInBackground(Object... params) {
      SQLiteDatabase db=(SQLiteDatabase)params[0];
      String table=(String)params[1];
      String nullColumnHack=(String)params[2];
      ContentValues values=(ContentValues)params[3];

      db.replace(table, nullColumnHack, values);

      return(null);
    }
  }

  protected static class DeleteTask extends ContentChangingTask {
    DeleteTask(SQLCipherCursorLoader loader) {
      super(loader);
    }

    @Override
    protected Void doInBackground(Object... params) {
      SQLiteDatabase db=(SQLiteDatabase)params[0];
      String table=(String)params[1];
      String where=(String)params[2];
      String[] whereParams=(String[])params[3];

      db.delete(table, where, whereParams);

      return(null);
    }
  }

  protected static class ExecSQLTask extends ContentChangingTask {
    ExecSQLTask(SQLCipherCursorLoader loader) {
      super(loader);
    }

    @Override
    protected Void doInBackground(Object... params) {
      SQLiteDatabase db=(SQLiteDatabase)params[0];
      String sql=(String)params[1];
      Object[] bindParams=(Object[])params[2];

      db.execSQL(sql, bindParams);

      return(null);
    }
  }
}