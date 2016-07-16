package im.r_c.android.dbox;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.r_c.android.dbox.annotation.Column;
import im.r_c.android.dbox.annotation.ObjectColumn;
import im.r_c.android.dbox.annotation.Table;

/**
 * DBox
 * Created by richard on 7/16/16.
 * <p>
 * Stores database table info of a specific data class.
 */
class TableInfo {
    static final String COLUMN_ID = "id";

    static final int COLUMN_TYPE_BOOLEAN = 100;
    static final int COLUMN_TYPE_BYTE = 101;
    static final int COLUMN_TYPE_SHORT = 102;
    static final int COLUMN_TYPE_INT = 103;
    static final int COLUMN_TYPE_LONG = 104;
    static final int COLUMN_TYPE_FLOAT = 105;
    static final int COLUMN_TYPE_DOUBLE = 106;
    static final int COLUMN_TYPE_STRING = 107;
    static final int COLUMN_TYPE_DATE = 108;
    static final int COLUMN_TYPE_BYTE_ARRAY = 109;
    static final int COLUMN_TYPE_OBJECT = 110;
    static final int COLUMN_TYPE_OBJECT_ARRAY = 111;
    static final int COLUMN_TYPE_OBJECT_LIST = 112;

    /**
     * Table name
     */
    String mName;

    /**
     * Key: name of instance field,
     * Value: column info.
     */
    Map<String, ColumnInfo> mColumnMap;

    /**
     * Key: name of instance field,
     * Value: object column info.
     */
    Map<String, ObjectColumnInfo> mObjectColumnMap;

    /**
     * Invisible constructor.
     */
    private TableInfo() {
    }

    /**
     * Make a TableInfo object from a data class.
     *
     * @param clz data class.
     * @return table info
     */
    static TableInfo of(Class<?> clz) {
        TableInfo ti = new TableInfo();

        Table table = clz.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("Did you forget to add \"@Table\" annotation to class \"" + clz + "\"?");
        }
        String tableName = table.value();
        ti.mName = "".equals(tableName) ? clz.getSimpleName() : tableName;

        Field[] fields = clz.getDeclaredFields();
        ti.mColumnMap = new HashMap<>();
        ti.mObjectColumnMap = new HashMap<>();
        boolean hasIdField = false;
        for (Field field : fields) {
            String fieldName = field.getName();
            if (COLUMN_ID.equals(fieldName) && field.getType() == long.class) {
                ColumnInfo ci = new ColumnInfo();
                ci.mType = COLUMN_TYPE_LONG;
                ci.mName = COLUMN_ID;
                ci.mNotNull = true;
                ci.mUnique = true;
                ci.mPrimaryKey = true;
                ci.mAutoIncrement = true;
                ci.mField = field;
                ti.mColumnMap.put(fieldName, ci);
                hasIdField = true;
                continue;
            }

            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                ti.mColumnMap.put(fieldName, parseColumnInfo(field, column));
            } else {
                ObjectColumn objectColumn = field.getAnnotation(ObjectColumn.class);
                if (objectColumn != null) {
                    ti.mObjectColumnMap.put(fieldName, parseObjectColumnInfo(field, objectColumn));
                }
            }
        }

        if (!hasIdField) {
            // There is no "long id" field
            throw new IllegalArgumentException("Did you forget to add \"long id\" field to class \"" + clz + "\"?");
        }

        return ti;
    }

    private static ColumnInfo parseColumnInfo(Field field, Column column) {
        ColumnInfo ci = new ColumnInfo();

        Class<?> type = field.getType();
        if (type == boolean.class) {
            ci.mType = COLUMN_TYPE_BOOLEAN;
        } else if (type == byte.class) {
            ci.mType = COLUMN_TYPE_BYTE;
        } else if (type == short.class) {
            ci.mType = COLUMN_TYPE_SHORT;
        } else if (type == int.class) {
            ci.mType = COLUMN_TYPE_INT;
        } else if (type == long.class) {
            ci.mType = COLUMN_TYPE_LONG;
        } else if (type == float.class) {
            ci.mType = COLUMN_TYPE_FLOAT;
        } else if (type == double.class) {
            ci.mType = COLUMN_TYPE_DOUBLE;
        } else if (type == String.class) {
            ci.mType = COLUMN_TYPE_STRING;
        } else if (type == java.util.Date.class || type == java.sql.Date.class) {
            ci.mType = COLUMN_TYPE_DATE;
        } else if (type == byte[].class) {
            ci.mType = COLUMN_TYPE_BYTE_ARRAY;
        } else {
            throw new IllegalArgumentException("Unsupported column type found: " + type + ".");
        }

        String columnName = column.name();
        ci.mName = "".equals(columnName) ? field.getName() : columnName;
        ci.mNotNull = column.notNull();
        ci.mUnique = column.unique();
        ci.mPrimaryKey = column.primaryKey();
        ci.mAutoIncrement = column.autoIncrement();
        ci.mField = field;
        return ci;
    }

    private static ObjectColumnInfo parseObjectColumnInfo(Field field, ObjectColumn objectColumn) {
        ObjectColumnInfo oci = new ObjectColumnInfo();

        Class<?> fieldType = field.getType();
        Class<?> elemType = objectColumn.value();
        if (fieldType == elemType) {
            // @ObjectColumn(Foo.class)
            // private Foo foo;
            oci.mType = COLUMN_TYPE_OBJECT;
        } else if (fieldType.isArray() && fieldType.getComponentType() == elemType) {
            // @ObjectColumn(Foo.class)
            // private Foo[] foos;
            oci.mType = COLUMN_TYPE_OBJECT_ARRAY;
        } else if (fieldType == List.class) {
            // @ObjectColumn(Foo.class)
            // private List<Foo> fooList;
            oci.mType = COLUMN_TYPE_OBJECT_LIST;
        } else {
            throw new IllegalArgumentException("Unsupported field type found: " + fieldType);
        }

        if (elemType.getAnnotation(Table.class) == null) {
            // No "Table" annotation found
            throw new IllegalArgumentException("The element type \"" + elemType + "\" of object field \"" + field + "\" is not a table.");
        }

        oci.mElemClass = elemType;
        oci.mField = field;

        return oci;
    }

    /**
     * Stores database column info of a normal field of a data class.
     * There will be an actual column in the table in database.
     */
    static class ColumnInfo {
        int mType;
        String mName;
        boolean mNotNull;
        boolean mUnique;
        boolean mPrimaryKey;
        boolean mAutoIncrement;
        Field mField;
    }

    /**
     * Represents an object field,
     * which should be a ORM data object as well.
     * <p>
     * This will not be stored in the exact table,
     * instead, it will be stored as relationship mappings
     * in another table.
     */
    static class ObjectColumnInfo {
        int mType;
        Class<?> mElemClass;
        Field mField;
    }
}
