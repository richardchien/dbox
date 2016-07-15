package im.r_c.android.dbox;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * DBox
 * Created by richard on 7/15/16.
 */
public class DBox<T> {
    private static WeakReference<Context> sContextRef;
    private static String sDatabaseName;
    private final Class<T> mClass;
    private Query mQuery;
    private SQLiteDatabase mDb;

    private DBox(Class<T> clz) {
        mClass = clz;
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

        DBox<T> box = new DBox<>(clz);
        DatabaseHelper helper = new DatabaseHelper(context, sDatabaseName, null, 1);
        box.mDb = helper.getWritableDatabase();
        return box;
    }

    public DBox<T> where(Condition condition) {
        return this;
    }

    private static class Query {

    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
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
