package im.r_c.android.dbox;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * DBox
 * Created by richard on 7/17/16.
 */
class DBUtils {
    static boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor c = null;
        boolean exists = false;
        try {
            c = db.query(tableName, null, null, null, null, null, null);
            c.close();
            exists = true;
        } catch (Exception ignored) {
        }
        return exists;
    }
}
