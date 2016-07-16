package im.r_c.android.dbox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;

/**
 * DBox
 * Created by richard on 7/15/16.
 */
public class DBox<T> {
    private static final String TAG = DBox.class.getSimpleName();

    private static WeakReference<Context> sContextRef;
    private static String sDatabaseName;

    private final Class<T> mClass;
    private final TableInfo mTableInfo;
    //    private Query mQuery;
    private SQLiteDatabase mDb;

    private DBox(Class<T> clz, TableInfo tableInfo) {
        mClass = clz;
        mTableInfo = tableInfo;
    }

    public static void init(Context context, String databaseName) {
        sContextRef = new WeakReference<>(context);
        sDatabaseName = databaseName;
    }

    public static <T> DBox<T> of(@NonNull Class<T> clz) {
        Context context = sContextRef.get();
        if (context == null) {
            throw new IllegalStateException("Did you forget to call DBox.init() before using it?");
        }

        DBox<T> box = new DBox<>(clz, TableInfo.of(clz));
        DatabaseHelper helper = new DatabaseHelper(context, sDatabaseName, null, 1);
        box.mDb = helper.getWritableDatabase();
        return box;
    }

    public boolean save(T t) {
        Map<String, ColumnInfo> cm = mTableInfo.mColumnMap;

        long id = 0;
        try {
            id = cm.get(TableInfo.COLUMN_ID).mField.getLong(t);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (id > 0) {
            // Record already exists
            return update(t);
        }

        Log.d(TAG, buildCreateTableSQL(mTableInfo));
        //TODO: Save record

        return false;
    }

    private boolean update(T t) {
        return false;
    }

    public DBox<T> where(Condition condition) {
        return this;
    }

    private String buildCreateTableSQL(TableInfo tableInfo) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(tableInfo.mName)
                .append(" (");

        Iterator<ColumnInfo> ciIter = tableInfo.mColumnMap.values().iterator();
        for (; ; ) {
            ColumnInfo ci = ciIter.next();
            sqlBuilder.append(ci.mName).append(" ");

            switch (ci.mType) {
                case ColumnInfo.TYPE_BOOLEAN:
                case ColumnInfo.TYPE_BYTE:
                case ColumnInfo.TYPE_SHORT:
                case ColumnInfo.TYPE_INT:
                case ColumnInfo.TYPE_LONG:
                case ColumnInfo.TYPE_DATE:
                    sqlBuilder.append("INTEGER");
                    break;
                case ColumnInfo.TYPE_FLOAT:
                case ColumnInfo.TYPE_DOUBLE:
                    sqlBuilder.append("REAL");
                    break;
                case ColumnInfo.TYPE_STRING:
                    sqlBuilder.append("TEXT");
                    break;
                case ColumnInfo.TYPE_BYTE_ARRAY:
                    sqlBuilder.append("BLOB");
                    break;
            }

            sqlBuilder.append(ci.mNotNull ? " NOT NULL" : "")
                    .append(ci.mUnique ? " UNIQUE" : "")
                    .append(ci.mPrimaryKey ? " PRIMARY KEY" : "")
                    .append(ci.mAutoIncrement ? " AUTOINCREMENT" : "");

            if (ciIter.hasNext()) {
                sqlBuilder.append(", ");
            } else {
                break;
            }
        }

        sqlBuilder.append(");");
        return sqlBuilder.toString();
    }

//    private static class Query {
//
//    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
