package im.r_c.android.dbox;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import java.util.Arrays;

/**
 * DBox
 * Created by richard on 7/19/16.
 */
public class DBoxQuery<T> {
    private static final String TAG = DBoxQuery.class.getSimpleName();

    private SQLiteDatabase mDb;
    private TableInfo mTableInfo;
    private DBoxCondition mCondition;
    private StringBuilder mOrderBulder;

    DBoxQuery(SQLiteDatabase db, TableInfo tableInfo, DBoxCondition condition) {
        mDb = db;
        mTableInfo = tableInfo;
        mCondition = condition;
        mOrderBulder = new StringBuilder();
    }

    public DBoxQuery<T> orderBy(String... fields) {
        for (String field : fields) {
            mOrderBulder.append(mOrderBulder.length() == 0 ? "" : ", ")
                    .append(mTableInfo.mName).append(".").append(field);
        }
        return this;
    }

    public DBoxQuery<T> orderByDesc(String... fields) {
        for (String field : fields) {
            mOrderBulder.append(mOrderBulder.length() == 0 ? "" : ", ")
                    .append(mTableInfo.mName).append(".").append(field).append(" DESC");
        }
        return this;
    }

    public DBoxResults<T> results() {
        Pair<String, String[]> pair = SQLBuilder.query(mTableInfo, mCondition, mOrderBulder);
        Log.d(TAG, pair.first);
        Log.d(TAG, Arrays.toString(pair.second));
        Cursor cursor = mDb.rawQuery(pair.first, pair.second);
        cursor.close(); //TODO: finish it!
        return new DBoxResults<>();
    }
}
