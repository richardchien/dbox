package im.r_c.android.dbox;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
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
        return saveInternal(t);
    }

    private boolean saveInternal(Object obj) {
        boolean isUpdating = false;
        long idA = getId(obj, mClass);
        if (idA > 0) {
            // Record already exists
            isUpdating = true;
        }

        // Create table if not exists
        if (!DBUtils.isTableExists(mDb, mTableInfo.mName)) {
            String sql = SQLBuilder.createTable(mTableInfo);
            mDb.execSQL(sql);
        }

        // Create mapping tables if not exist
        if (mTableInfo.mObjectColumnMap.size() > 0) {
            // Has object column
            boolean hasMappingTableNotCreated = false;
            for (ObjectColumnInfo oci : mTableInfo.mObjectColumnMap.values()) {
                // Check if all mapping tables are created
                if (!DBUtils.isTableExists(mDb, SQLBuilder.getMappingTableName(mTableInfo.mName, TableInfo.nameOf(oci.mElemClass)))) {
                    hasMappingTableNotCreated = true;
                    break;
                }
            }
            if (hasMappingTableNotCreated) {
                String[] sqls = SQLBuilder.createAllMappingTables(mTableInfo);
                for (String sql : sqls) {
                    mDb.execSQL(sql);
                }
            }
        }

        boolean ok = false;
        try {
            mDb.beginTransaction();

            // Save values into this table
            ContentValues values = SQLBuilder.buildContentValues(mTableInfo, obj);
            if (isUpdating) {
                int rowCount = mDb.update(mTableInfo.mName, values, TableInfo.COLUMN_ID + " = ?", new String[]{String.valueOf(idA)});
                if (rowCount != 1) {
                    // Effected row count is not 1,
                    // meaning something went wrong.
                    throw new Exception();
                }
            } else {
                idA = mDb.insert(mTableInfo.mName, null, values);
                if (idA <= 0) {
                    throw new Exception();
                }
                setId(obj, mClass, idA);
            }

            // If is updating, all previous mappings of this id should be deleted first
            if (isUpdating) {
                for (Map.Entry<String, ObjectColumnInfo> entry : mTableInfo.mObjectColumnMap.entrySet()) {
                    ObjectColumnInfo oci = entry.getValue();
                    String tableB = TableInfo.nameOf(oci.mElemClass);
                    String fieldName = entry.getKey();

                    mDb.delete(SQLBuilder.getMappingTableName(mTableInfo.mName, tableB),
                            SQLBuilder.getMappingTableColumnName(mTableInfo.mName, fieldName) + " = ?",
                            new String[]{String.valueOf(idA)});
                }
            }

            // Insert relationship mappings into mapping tables
            // Example:
            // _TableA_field1_id  _TableA_field2_id  _TableB_id
            //        1                   0               2
            //        0                   1               2
            //        0                   1               3
            for (Map.Entry<String, ObjectColumnInfo> entry : mTableInfo.mObjectColumnMap.entrySet()) {
                ObjectColumnInfo oci = entry.getValue();
                String fieldName = entry.getKey();

                if (!oci.mField.isAccessible()) {
                    oci.mField.setAccessible(true);
                }

                switch (oci.mType) {
                    case ObjectColumnInfo.TYPE_OBJECT: {
                        Object o = oci.mField.get(obj);
                        if (o == null) {
                            break;
                        }
                        handleObjectMapping(fieldName, idA, o, oci.mElemClass);
                        break;
                    }
                    case ObjectColumnInfo.TYPE_OBJECT_ARRAY: {
                        Object arr = oci.mField.get(obj);
                        if (arr == null || Array.getLength(arr) == 0) {
                            break;
                        }
                        for (int i = 0; i < Array.getLength(arr); i++) {
                            Object o = Array.get(arr, i);
                            if (o == null) {
                                continue;
                            }
                            handleObjectMapping(fieldName, idA, o, oci.mElemClass);
                        }
                        break;
                    }
                    case ObjectColumnInfo.TYPE_OBJECT_LIST: {
                        List list = (List) oci.mField.get(obj);
                        if (list == null || list.size() == 0) {
                            break;
                        }
                        for (Object o : list) {
                            if (o == null) {
                                continue;
                            }
                            handleObjectMapping(fieldName, idA, o, oci.mElemClass);
                        }
                        break;
                    }
                }
            }

            mDb.setTransactionSuccessful();
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDb.endTransaction();
        }

        return ok;
    }

    private void handleObjectMapping(String field, long idA, Object objB, Class<?> clzB) throws Exception {
        long idB = getId(objB, clzB);
        if (idB <= 0) {
            throw new Exception();
        }

        String tableB = TableInfo.nameOf(clzB);
        if (mDb.insert(SQLBuilder.getMappingTableName(mTableInfo.mName, tableB), null,
                SQLBuilder.buildMappingContentValues(field, mTableInfo.mName, idA, tableB, idB)) <= 0) {
            // Insert mapping failed
            throw new Exception();
        }
    }

    private long getId(Object obj, Class<?> clz) {
        try {
            Field f = clz.getDeclaredField(TableInfo.COLUMN_ID);
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            return f.getLong(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setId(Object obj, Class<?> clz, long id) {
        try {
            Field f = clz.getDeclaredField(TableInfo.COLUMN_ID);
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            f.setLong(obj, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DBox<T> where(Condition condition) {
        return this;
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
