package im.r_c.android.dbox;

import android.content.ContentValues;
import android.util.Pair;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * DBox
 * Created by richard on 7/17/16.
 */
class SQLBuilder {
    static String createTable(TableInfo tableInfo) {
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

    static String createTable(String tableName, String... columns) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" (");

        for (int i = 0; i < columns.length; i++) {
            sqlBuilder.append(i > 0 ? ", " : "")
                    .append(columns[i]);
        }

        sqlBuilder.append(");");
        return sqlBuilder.toString();
    }

    static String[] createAllMappingTables(TableInfo tableInfo) {
        // Example:
        // TableA.field1 -> TableB
        // TableA.field2 -> List<TableB>
        // TableA.field3 -> TableC

        // Key: table name
        // Value: mapping table create sql builder
        Map<String, StringBuilder> builderMap = new HashMap<>();

        // Key: object column info object (aka an instance field)
        // Value: table name
        Map<ObjectColumnInfo, String> tableNameMap = new HashMap<>();

        for (Map.Entry<String, ObjectColumnInfo> entry : tableInfo.mObjectColumnMap.entrySet()) {
            // Get table info of TableB or TableC
            String tn = tableNameMap.get(entry.getValue());
            if (tn == null) {
                tn = TableInfo.nameOf(entry.getValue().mElemClass);
                tableNameMap.put(entry.getValue(), tn);
            }

            // Get mapping table create sql builder
            // for TableA to TableB/TableC mapping
            StringBuilder sqlBuilder = builderMap.get(tn);
            if (sqlBuilder == null) {
                sqlBuilder = new StringBuilder();
                sqlBuilder.append("CREATE TABLE IF NOT EXISTS ")
                        // Because the two table classes can't contain objects of each other,
                        // the order of the two names does not matter.
                        // There will NEVER be both _TableA_TableB_mapping and _TableB_TableA_mapping.
                        .append(getMappingTableName(tableInfo.mName, tn))
                        .append(" (");
                builderMap.put(tn, sqlBuilder);
            }

            // Append current column definition to sql builder
            if (sqlBuilder.charAt(sqlBuilder.length() - 1) != '(') {
                // This is not the first column definition
                sqlBuilder.append(", ");
            }
            // Append column "_TableA_field1_id"
            sqlBuilder.append(getMappingTableColumnName(tableInfo.mName, entry.getKey())).append(" INTEGER");
        }

        String[] sqls = new String[builderMap.size()];
        int n = 0;
        // Append last column "_TableB_id" for all mapping table
        for (Map.Entry<String, StringBuilder> entry : builderMap.entrySet()) {
            StringBuilder builder = entry.getValue();
            builder.append(", ").append(getMappingTableColumnName(entry.getKey(), null)).append(" INTEGER NOT NULL);");
            sqls[n++] = builder.toString();
        }

        return sqls;
    }

    static String getMappingTableName(String tableA, String tableB) {
        return "_" + tableA + "_" + tableB + "_mapping";
    }

    static String getMappingTableColumnName(String table, String field) {
        return "_" + table + (field != null ? "_" + field : "") + "_id";
    }

    static ContentValues buildContentValues(TableInfo tableInfo, Object obj) {
        ContentValues values = new ContentValues();
        for (ColumnInfo ci : tableInfo.mColumnMap.values()) {
            if (TableInfo.COLUMN_ID.equals(ci.mName)) {
                continue;
            }

            try {
                if (!ci.mField.isAccessible()) {
                    ci.mField.setAccessible(true);
                }
                switch (ci.mType) {
                    case ColumnInfo.TYPE_BOOLEAN:
                        values.put(ci.mName, ci.mField.getBoolean(obj) ? 1 : 0);
                        break;
                    case ColumnInfo.TYPE_BYTE:
                        values.put(ci.mName, ci.mField.getByte(obj));
                        break;
                    case ColumnInfo.TYPE_SHORT:
                        values.put(ci.mName, ci.mField.getShort(obj));
                        break;
                    case ColumnInfo.TYPE_INT:
                        values.put(ci.mName, ci.mField.getInt(obj));
                        break;
                    case ColumnInfo.TYPE_LONG:
                        values.put(ci.mName, ci.mField.getLong(obj));
                        break;
                    case ColumnInfo.TYPE_FLOAT:
                        values.put(ci.mName, ci.mField.getFloat(obj));
                        break;
                    case ColumnInfo.TYPE_DOUBLE:
                        values.put(ci.mName, ci.mField.getDouble(obj));
                        break;
                    case ColumnInfo.TYPE_STRING:
                        values.put(ci.mName, (String) ci.mField.get(obj));
                        break;
                    case ColumnInfo.TYPE_DATE:
                        Date date = (Date) ci.mField.get(obj);
                        values.put(ci.mName, date.getTime());
                        break;
                    case ColumnInfo.TYPE_BYTE_ARRAY:
                        values.put(ci.mName, (byte[]) ci.mField.get(obj));
                        break;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (values.size() != tableInfo.mColumnMap.size() - 1) {
            // "values" contains all column values of the table except "id",
            // so normally this block will NEVER be executed.
            throw new UnknownError();
        }

        return values;
    }

    static ContentValues buildMappingContentValues(String field, String tableA, long idA, String tableB, long idB) {
        ContentValues values = new ContentValues();
        values.put(getMappingTableColumnName(tableA, field), idA);
        values.put(getMappingTableColumnName(tableB, null), idB);
        return values;
    }

    static String dropTable(String table) {
        return "DROP TABLE IF EXISTS " + table + ";";
    }

    static Pair<String, String[]> query(TableInfo tableInfo, DBoxCondition condition, StringBuilder orderBuilder) {
        // Example:
        //
        // SELECT
        //
        // FROM Student
        //   LEFT JOIN _Student_Course_mapping
        //   LEFT JOIN _Student_Clazz_mapping
        // WHERE
        //   (
        //     _Student_Course_mapping._Student_courseList_id = Student.id      -| Corresponding to "Course" table, containing 2 fields
        //     OR                                                                | Use "OR" between each field
        //     _Student_Course_mapping._Student_favoriteCourses_id = Student.id -|
        //   )
        //   AND
        //   (
        //     _Student_Clazz_mapping._Student_clazz_id = Student.id            -| Corresponding to "Clazz" table, containing 1 field
        //   )
        //   AND (
        //     {Custom where clause}
        //   );

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ").append(tableInfo.mName);

        // Key: tableB (aka table of the elem class of a field
        // Value: where clause builder
        Map<String, StringBuilder> mappingWhereBuilderMap = new HashMap<>();

        for (Map.Entry<String, ObjectColumnInfo> entry : tableInfo.mObjectColumnMap.entrySet()) {
            String field = entry.getKey();
            ObjectColumnInfo oci = entry.getValue();

            String tableB = TableInfo.nameOf(oci.mElemClass);
            String mappingTable = getMappingTableName(tableInfo.mName, tableB);

            StringBuilder mappingWhereBuilder;
            if (!mappingWhereBuilderMap.containsKey(tableB)) {
                sqlBuilder.append(" LEFT JOIN ").append(mappingTable);
                mappingWhereBuilder = new StringBuilder();
                mappingWhereBuilderMap.put(tableB, mappingWhereBuilder);
            } else {
                mappingWhereBuilder = mappingWhereBuilderMap.get(tableB);
            }

            mappingWhereBuilder.append(mappingWhereBuilder.length() == 0 ? "" : " OR ")
                    .append(mappingTable).append(".").append(getMappingTableColumnName(tableInfo.mName, field))
                    .append(" = ").append(tableInfo.mName).append(".").append(TableInfo.COLUMN_ID);
        }

        StringBuilder fullWhereBuilder = new StringBuilder();
        boolean first = true;
        for (StringBuilder mappingWhereBuilder : mappingWhereBuilderMap.values()) {
            fullWhereBuilder.append(first ? "" : " AND ")
                    .append("(")
                    .append(mappingWhereBuilder)
                    .append(")");
            first = false;
        }

        String where = condition.build(tableInfo.mName);
        if (where.length() > 0) {
            fullWhereBuilder.append(first ? "" : " AND ")
                    .append("(")
                    .append(where)
                    .append(")");
        }

        if (fullWhereBuilder.length() > 0) {
            sqlBuilder.append(" WHERE ").append(fullWhereBuilder);
        }

        sqlBuilder.append(" ORDER BY ").append(orderBuilder)
                .append(orderBuilder.length() == 0 ? "" : ", ")
                // Always order by id at the lowest priority
                .append(tableInfo.mName).append(".").append(TableInfo.COLUMN_ID);
        sqlBuilder.append(";");
        return new Pair<>(sqlBuilder.toString(), condition.getArgs());
    }
}
