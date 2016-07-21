package im.r_c.android.dbox;

import android.database.Cursor;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DBox
 * Created by richard on 7/15/16.
 */
public class DBoxResults<T> implements Iterable<T> {
    private TableInfo mTableInfo;
    private Cursor mCursor;

    DBoxResults(TableInfo tableInfo, Cursor cursor) {
        mTableInfo = tableInfo;
        mCursor = cursor;
    }

    public T first() {
        T t = getFirst();
        close();
        return t;
    }

    public T last() {
        T t = getLast();
        close();
        return t;
    }

    public T one(int index) {
        T t = getOne(index);
        close();
        return t;
    }

    public List<T> some(int start, int count) {
        List<T> list = getSome(start, count);
        close();
        return list;
    }

    public List<T> some(Filter<T> filter) {
        List<T> list = getSome(filter);
        close();
        return list;
    }

    public List<T> all() {
        List<T> list = getAll();
        close();
        return list;
    }

    public T getFirst() {
        T t = null;
        if (moveToFirst()) {
            Iterator<T> iter = iterator();
            t = iter.next();
        }
        return t;
    }

    public T getLast() {
        T t = null;
        if (moveToLast()) {
            Iterator<T> iter = iterator();
            t = iter.next();
        }
        return t;
    }

    public T getOne(int index) {
        T t = null;
        if (moveTo(index)) {
            Iterator<T> iter = iterator();
            t = iter.next();
        }
        return t;
    }

    public List<T> getSome(int start, int count) {
        List<T> list = new ArrayList<>();
        if (moveTo(start)) {
            Iterator<T> iter = iterator();
            for (int i = 0; i < count && iter.hasNext(); i++) {
                list.add(iter.next());
            }
        }
        return list;
    }

    public List<T> getSome(Filter<T> filter) {
        List<T> list = new ArrayList<>();
        if (moveToFirst()) {
            for (T t : this) {
                if (filter.filter(t)) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    public List<T> getAll() {
        List<T> list = new ArrayList<>();
        if (moveToFirst()) {
            for (T t : this) {
                list.add(t);
            }
        }
        return list;
    }

    public void close() {
        if (!mCursor.isClosed()) {
            mCursor.close();
        }
    }

    private boolean moveToFirst() {
        return mCursor.moveToFirst();
    }

    private boolean moveToLast() {
        if (mCursor.moveToLast()) {
            long lastId, id = getId();
            do {
                if (!mCursor.moveToPrevious()) {
                    break;
                }
                lastId = id;
                id = getId();
            } while (lastId == id);
            mCursor.moveToNext();
            return true;
        }
        return false;
    }

    private boolean moveTo(int index) {
        if (mCursor.moveToFirst()) {
            long lastId, id = getId();
            boolean found = true;
            for (int i = 0; i < index; i++) {
                do {
                    if (!mCursor.moveToNext()) {
                        found = false;
                        break;
                    }
                    lastId = id;
                    id = getId();
                } while (lastId == id);

                if (!found) {
                    break;
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }

    private long getId() {
        return mCursor.getLong(mCursor.getColumnIndex(TableInfo.COLUMN_ID));
    }

    @Override
    public Iterator<T> iterator() {
        return new ResultsIterator<>(mTableInfo, mCursor);
    }

    private static class ResultsIterator<T> implements Iterator<T> {
        private TableInfo mTableInfo;
        private Cursor mCursor;

        private ResultsIterator(TableInfo tableInfo, Cursor cursor) {
            mTableInfo = tableInfo;
            mCursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return mCursor.getPosition() < mCursor.getCount();
        }

        @Override
        public T next() {
            T result = null;
            boolean ok = false;

            try {
                Constructor<?> constructor = mTableInfo.mClass.getDeclaredConstructor();
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                result = (T) constructor.newInstance();

                // Set normal fields.
                // These fields are all the same as long as the id is the same,
                // no matter which position the cursor is,
                // so just read the first position.
                for (ColumnInfo ci : mTableInfo.mColumnMap.values()) {
                    if (!ci.mField.isAccessible()) {
                        ci.mField.setAccessible(true);
                    }

                    int columnIdx = mCursor.getColumnIndex(ci.mName);
                    switch (ci.mType) {
                        case ColumnInfo.TYPE_BOOLEAN:
                            ci.mField.setBoolean(result, mCursor.getInt(columnIdx) != 0);
                            break;
                        case ColumnInfo.TYPE_BYTE:
                            ci.mField.setByte(result, (byte) mCursor.getInt(columnIdx));
                            break;
                        case ColumnInfo.TYPE_SHORT:
                            ci.mField.setShort(result, mCursor.getShort(columnIdx));
                            break;
                        case ColumnInfo.TYPE_INT:
                            ci.mField.setInt(result, mCursor.getInt(columnIdx));
                            break;
                        case ColumnInfo.TYPE_LONG:
                            ci.mField.setLong(result, mCursor.getLong(columnIdx));
                            break;
                        case ColumnInfo.TYPE_DATE:
                            ci.mField.set(result, new Date(mCursor.getLong(columnIdx)));
                            break;
                        case ColumnInfo.TYPE_FLOAT:
                            ci.mField.setFloat(result, mCursor.getFloat(columnIdx));
                            break;
                        case ColumnInfo.TYPE_DOUBLE:
                            ci.mField.setDouble(result, mCursor.getDouble(columnIdx));
                            break;
                        case ColumnInfo.TYPE_STRING:
                            ci.mField.set(result, mCursor.getString(columnIdx));
                            break;
                        case ColumnInfo.TYPE_BYTE_ARRAY:
                            ci.mField.set(result, mCursor.getBlob(columnIdx));
                            break;
                    }
                }

                // Index: tableA_field id column index
                // Value: processed max index of the corresponding list
                SparseIntArray maxIndexArr = new SparseIntArray();

                // Index: tableA_field id column index
                // Value: single object of the field is set or not
                SparseBooleanArray singleObjFieldArr = new SparseBooleanArray();

                // Index: tableA_field id column index
                // Value: list object of the field
                SparseArray<List<Object>> listFieldArr = new SparseArray<>();

                // Index: tableB id column index
                // Value: DBox object
                SparseArray<DBox<?>> boxArr = new SparseArray<>();

                // This map is for avoiding creating an object with same id for more than one time.
                // Key: table name + "#" + id, e.g. "Student#1"
                // Value: cached object
                Map<String, Object> objCache = new HashMap<>();

                long lastId, idA = mCursor.getLong(mCursor.getColumnIndex(TableInfo.COLUMN_ID));
                do {
                    for (Map.Entry<String, ObjectColumnInfo> entry : mTableInfo.mObjectColumnMap.entrySet()) {
                        String field = entry.getKey();

                        int idAColIdx = mCursor.getColumnIndex(SQLBuilder.getMappingTableIdColumn(mTableInfo.mName, field));
                        if (mCursor.getLong(idAColIdx) <= 0) {
                            // This cursor position is not for current field
                            continue;
                        }

                        ObjectColumnInfo oci = entry.getValue();
                        String tableB = TableInfo.nameOf(oci.mElemClass);
                        int idBColIdx = mCursor.getColumnIndex(SQLBuilder.getMappingTableIdColumn(tableB, null));
                        long idB = mCursor.getLong(idBColIdx);

                        if (oci.mType == ObjectColumnInfo.TYPE_OBJECT) {
                            // Field of single object, only need to be set once
                            if (singleObjFieldArr.get(idAColIdx)) {
                                // The field has been set
                                continue;
                            }

                            if (!oci.mField.isAccessible()) {
                                oci.mField.setAccessible(true);
                            }

                            String objKey = tableB + "#" + idB;
                            Object obj = objCache.get(objKey);
                            if (obj != null) {
                                // Hit object cache
                                oci.mField.set(result, obj);
                            } else {
                                // Get DBox object for recursively query
                                DBox<?> box = getOrCreateBox(boxArr, oci.mElemClass, idBColIdx);
                                // Recursively query the object
                                obj = box.find(new DBoxCondition().equalTo(TableInfo.COLUMN_ID, "" + idB)).results().first();
                                oci.mField.set(result, obj);
                                objCache.put(objKey, obj);
                            }
                            singleObjFieldArr.put(idAColIdx, true);
                        } else {
                            int indexColIdx = mCursor.getColumnIndex(SQLBuilder.getMappingTableIndexColumn(mTableInfo.mName, field));
                            int index = mCursor.getInt(indexColIdx);
                            if (index <= maxIndexArr.get(idAColIdx, -1)) {
                                // The object has been added to the list
                                continue;
                            }

                            // The field is type of Array or List, then put the list in a SparseArray.
                            // We will set them to the corresponding fields later.
                            List<Object> list = listFieldArr.get(idAColIdx);
                            if (list == null) {
                                list = new ArrayList<>();
                                listFieldArr.put(idAColIdx, list);
                            }

                            String objKey = tableB + "#" + idB;
                            Object obj = objCache.get(objKey);
                            if (obj != null) {
                                // Hit object cache
                                list.add(obj);
                            } else {
                                // Get DBox object for recursively query
                                DBox<?> box = getOrCreateBox(boxArr, oci.mElemClass, idBColIdx);
                                // Recursively query the object
                                obj = box.find(new DBoxCondition().equalTo(TableInfo.COLUMN_ID, "" + idB)).results().first();
                                list.add(obj);
                                objCache.put(objKey, obj);
                            }

                            maxIndexArr.put(idAColIdx, index);
                        }
                    }

                    if (!mCursor.moveToNext()) {
                        break;
                    }
                    lastId = idA;
                    idA = mCursor.getLong(mCursor.getColumnIndex(TableInfo.COLUMN_ID));
                } while (lastId == idA);

                // In the loop before, we have set all field with a single object,
                // and made lists for all array and list fields.
                // Now let's set these lists to those array and list fields.
                for (Map.Entry<String, ObjectColumnInfo> entry : mTableInfo.mObjectColumnMap.entrySet()) {
                    ObjectColumnInfo oci = entry.getValue();

                    if (oci.mType == ObjectColumnInfo.TYPE_OBJECT) {
                        // We have set single object field, so skip it
                        continue;
                    }

                    if (!oci.mField.isAccessible()) {
                        oci.mField.setAccessible(true);
                    }

                    String field = entry.getKey();
                    int idAColIdx = mCursor.getColumnIndex(SQLBuilder.getMappingTableIdColumn(mTableInfo.mName, field));
                    List<Object> list = listFieldArr.get(idAColIdx);
                    if (list == null) {
                        // No record of this field,
                        // so make it an empty list.
                        list = new ArrayList<>();
                    }

                    if (oci.mType == ObjectColumnInfo.TYPE_OBJECT_LIST) {
                        oci.mField.set(result, list);
                    } else if (oci.mType == ObjectColumnInfo.TYPE_OBJECT_ARRAY) {
                        if (list.size() != 0) {
                            Object array = Array.newInstance(oci.mElemClass, list.size());
                            for (int i = 0; i < list.size(); i++) {
                                Array.set(array, i, list.get(i));
                            }
                            oci.mField.set(result, array);
                        }
                    }
                }

                ok = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (ok) {
                return result;
            } else {
                return null;
            }
        }

        private DBox<?> getOrCreateBox(SparseArray<DBox<?>> array, Class<?> clz, int index) {
            DBox<?> box = array.get(index);
            if (box == null) {
                box = DBox.of(clz);
                array.put(index, box);
            }
            return box;
        }
    }

    public interface Filter<T> {
        boolean filter(T t);
    }
}
