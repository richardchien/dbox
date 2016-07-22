/*
 * Copyright 2016 Richard Chien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Provide a bunch of methods for saving, removing, finding objects, etc.
 */
public class DBox<T> {
    private static final String TAG = DBox.class.getSimpleName();

    private static WeakReference<Context> sContextRef;
    private static String sDatabaseName;

    private final Class<T> mClass;
    private final TableInfo mTableInfo;
    private SQLiteDatabase mDb;

    /**
     * Invisible constructor.
     *
     * @param clz       class that represents a table
     * @param tableInfo table info of the class
     */
    private DBox(Class<T> clz, TableInfo tableInfo) {
        mClass = clz;
        mTableInfo = tableInfo;
    }

    /**
     * Initialize DBox.
     *
     * @param context      context
     * @param databaseName filename of database
     */
    public static void init(Context context, String databaseName) {
        sContextRef = new WeakReference<>(context);
        sDatabaseName = databaseName;
    }

    /**
     * Obtain a DBox object of a class.
     *
     * @param clz class that represents a table
     * @param <T> type of objects the box can handle
     * @return a box object
     */
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

    /**
     * Find all objects of a class.
     *
     * @return query object
     */
    public DBoxQuery<T> findAll() {
        return find(new DBoxCondition());
    }

    /**
     * Find objects of a class that satisfy a specific condition.
     *
     * @param condition condition (aka where clause)
     * @return query object
     */
    public DBoxQuery<T> find(DBoxCondition condition) {
        return new DBoxQuery<>(mDb, mTableInfo, condition);
    }

    /**
     * Save or update (if already exists) an object.
     * <p>
     * This will change the object's id
     * if the object does not exist before
     * or it has been removed by {@link #clear()} or {@link #drop()}.
     *
     * @param obj object to save
     * @return succeeded or not
     */
    public boolean save(T obj) {
        boolean isUpdating = false;
        long idA = getId(obj, mClass);
        if (idA > 0) {
            // Record already exists
            isUpdating = true;
        }

        boolean ok = false;
        try {
            mDb.beginTransaction();

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

            // Save values into this table
            ContentValues values = SQLBuilder.buildContentValues(mTableInfo, obj);
            if (isUpdating) {
                int rowCount = mDb.update(mTableInfo.mName, values, TableInfo.COLUMN_ID + " = ?", new String[]{String.valueOf(idA)});
                if (rowCount == 0) {
                    // The record does not exist in fact,
                    // so insert it.
                    isUpdating = false;
                } else if (rowCount != 1) {
                    // Effected row count is not 1,
                    // meaning something went wrong.
                    throw new Exception();
                }

                // If is updating, all previous mappings of this id should be deleted first
                deleteAllMappingsOfId(idA);
            }
            if (!isUpdating) {
                // Newly insert
                idA = mDb.insert(mTableInfo.mName, null, values);
                if (idA <= 0) {
                    throw new Exception();
                }
                setId(obj, mClass, idA);
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

                switch (oci.mType) {
                    case ObjectColumnInfo.TYPE_OBJECT: {
                        Object o = oci.mField.get(obj);
                        if (o == null) {
                            break;
                        }
                        handleObjectMapping(fieldName, -1, idA, o, oci.mElemClass);
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
                            handleObjectMapping(fieldName, i, idA, o, oci.mElemClass);
                        }
                        break;
                    }
                    case ObjectColumnInfo.TYPE_OBJECT_LIST: {
                        List list = (List) oci.mField.get(obj);
                        if (list == null || list.size() == 0) {
                            break;
                        }
                        for (int i = 0; i < list.size(); i++) {
                            Object o = list.get(i);
                            if (o == null) {
                                continue;
                            }
                            handleObjectMapping(fieldName, i, idA, o, oci.mElemClass);
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

    /**
     * Remove an object (must have an id).
     * <p>
     * After removing, the object's id will be set to 0.
     *
     * @param obj object to remove
     * @return succeeded or not
     */
    public boolean remove(T obj) {
        long idA = getId(obj, mClass);
        if (idA <= 0) {
            // Record hasn't been saved yet
            return false;
        }

        boolean ok = false;
        try {
            mDb.beginTransaction();

            // Remove record in this table
            int rowCount = mDb.delete(mTableInfo.mName, TableInfo.COLUMN_ID + " = ?", new String[]{String.valueOf(idA)});
            if (rowCount != 1) {
                // Effected row count is not 1,
                // meaning something went wrong.
                throw new Exception();
            }

            // Remove mappings in mapping tables
            deleteAllMappingsOfId(idA);

            setId(obj, mClass, 0);

            mDb.setTransactionSuccessful();
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDb.endTransaction();
        }

        return ok;
    }

    /**
     * (CAUTIOUS!!)
     * Remove all objects.
     * <p>
     * After a box being cleared, re-saving previous objects with non-zero ids
     * of the corresponding class will give them new ids.
     *
     * @return succeeded or not
     */
    public boolean clear() {
        boolean ok = false;
        try {
            mDb.beginTransaction();

            // Remove all records in this table
            mDb.delete(mTableInfo.mName, null, null);

            // Remove all mappings in mapping tables
            for (Map.Entry<String, ObjectColumnInfo> entry : mTableInfo.mObjectColumnMap.entrySet()) {
                String tableB = TableInfo.nameOf(entry.getValue().mElemClass);
                mDb.delete(SQLBuilder.getMappingTableName(mTableInfo.mName, tableB), null, null);
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

    /**
     * (CAUTIOUS!!)
     * Drop the table of this box
     * and all mapping tables entirely.
     * <p>
     * After a box being dropped, re-saving previous objects with non-zero ids
     * of the corresponding class will give them new ids.
     *
     * @return succeeded or not
     */
    public boolean drop() {
        boolean ok = false;
        try {
            mDb.beginTransaction();

            mDb.execSQL(SQLBuilder.dropTable(mTableInfo.mName));

            for (ObjectColumnInfo oci : mTableInfo.mObjectColumnMap.values()) {
                String tableB = TableInfo.nameOf(oci.mElemClass);
                mDb.execSQL(SQLBuilder.dropTable(SQLBuilder.getMappingTableName(mTableInfo.mName, tableB)));
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

    private void deleteAllMappingsOfId(long id) {
        for (Map.Entry<String, ObjectColumnInfo> entry : mTableInfo.mObjectColumnMap.entrySet()) {
            ObjectColumnInfo oci = entry.getValue();
            String tableB = TableInfo.nameOf(oci.mElemClass);
            String fieldName = entry.getKey();

            mDb.delete(SQLBuilder.getMappingTableName(mTableInfo.mName, tableB),
                    SQLBuilder.getMappingTableIdColumn(mTableInfo.mName, fieldName) + " = ?",
                    new String[]{String.valueOf(id)});
        }
    }

    private void handleObjectMapping(String field, int index, long idA, Object objB, Class<?> clzB) throws Exception {
        long idB = getId(objB, clzB);
        if (idB <= 0) {
            throw new Exception();
        }

        String tableB = TableInfo.nameOf(clzB);
        if (mDb.insert(SQLBuilder.getMappingTableName(mTableInfo.mName, tableB), null,
                SQLBuilder.buildMappingContentValues(field, index, mTableInfo.mName, idA, tableB, idB)) <= 0) {
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
