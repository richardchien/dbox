package im.r_c.android.dbox;

import java.util.ArrayList;
import java.util.List;

/**
 * DBox
 * Created by richard on 7/15/16.
 */
@SuppressWarnings("WeakerAccess")
public class DBoxCondition {
    //    private StringBuilder mConditionBuilder;
    private List<Builder> mBuilderList;
    private List<String> mArgList;
    private int mGroupDepth = 0;

    public DBoxCondition() {
//        mConditionBuilder = new StringBuilder();
        mBuilderList = new ArrayList<>();
        mArgList = new ArrayList<>();
    }

    public DBoxCondition equalTo(String column, String value) {
        return compare(column, "=", value);
    }

    public DBoxCondition notEqualTo(String column, String value) {
        return compare(column, "!=", value);
    }

    public DBoxCondition greaterThan(String column, String value) {
        return compare(column, ">", value);
    }

    public DBoxCondition lessThan(String column, String value) {
        return compare(column, "<", value);
    }

    public DBoxCondition greaterThanOrEqualTo(String column, String value) {
        return compare(column, ">=", value);
    }

    public DBoxCondition lessThanOrEqualTo(String column, String value) {
        return compare(column, "<=", value);
    }

    public DBoxCondition compare(final String column, final String operator, String value) {
//        mConditionBuilder
//                .append(column)
//                .append(" ")
//                .append(operator)
//                .append(" ? ");
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return table + "." + column + " " + operator + " ?";
            }
        });
        mArgList.add(value);
        return this;
    }

    public DBoxCondition not() {
//        mConditionBuilder.append("NOT ");
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return "NOT";
            }
        });
        return this;
    }

    public DBoxCondition beginGroup() {
//        mConditionBuilder.append("(");
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return "(";
            }
        });
        mGroupDepth++;
        return this;
    }

    public DBoxCondition endGroup() {
//        mConditionBuilder.append(") ");
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return ")";
            }
        });
        mGroupDepth--;
        return this;
    }

    public DBoxCondition and() {
//        mConditionBuilder.append("AND ");
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return "AND";
            }
        });
        return this;
    }

    public DBoxCondition or() {
//        mConditionBuilder.append("OR ");
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return "OR";
            }
        });
        return this;
    }

    public DBoxCondition between(final String column, String startValue, String endValue) {
//        mConditionBuilder.append(column).append(" BETWEEN ? AND ? ");
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return table + "." + column + " BETWEEN ? AND ?";
            }
        });
        mArgList.add(startValue);
        mArgList.add(endValue);
        return this;
    }

    public DBoxCondition contains(String column, String part) {
        return like(column, "%" + part + "%");
    }

    public DBoxCondition startsWith(String column, String prefix) {
        return like(column, prefix + "%");
    }

    public DBoxCondition endsWith(String column, String suffix) {
        return like(column, "%" + suffix);
    }

    public DBoxCondition like(final String column, String pattern) {
//        mConditionBuilder
//                .append(column)
//                .append(" LIKE '")
//                .append(pattern)
//                .append("' ");
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return table + "." + column + " LIKE ?";
            }
        });
        mArgList.add(pattern);
        return this;
    }

    public DBoxCondition in(final String column, String... values) {
//        mConditionBuilder.append(column).append(" IN (");
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
//                mConditionBuilder.append(", ");
                stringBuilder.append(", ");
            }
//            mConditionBuilder.append("?");
            stringBuilder.append("?");
            mArgList.add(values[i]);
        }
//        mConditionBuilder.append(") ");
        stringBuilder.append(")");
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return table + "." + column + " IN " + stringBuilder.toString();
            }
        });
        return this;
    }

    public DBoxCondition isNull(final String column) {
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return table + "." + column + " IS NULL";
            }
        });
        return this;
    }

    public DBoxCondition isNotNull(final String column) {
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return table + "." + column + " IS NOT NULL";
            }
        });
        return this;
    }

    String build(String table) {
        if (mGroupDepth != 0) {
            throw new IllegalStateException("There are " + mGroupDepth + " groups haven't been ended.");
        }
//        return mConditionBuilder.toString();
        StringBuilder whereClauseBuilder = new StringBuilder();
        String last = null;
        for (int i = 0; i < mBuilderList.size(); i++) {
            Builder builder = mBuilderList.get(i);
            String part = builder.build(table);
            if (i != 0 && !(")".equals(part) || "OR".equals(part) || "AND".equals(part))
                    && !("(".equals(last) || "OR".equals(last) || "AND".equals(last) || "NOT".equals(last))) {
                // Not ")" or "OR", so add a default "AND"
                whereClauseBuilder.append("AND ");
            }
            whereClauseBuilder.append(part).append(" ");
            last = part;
        }
        return whereClauseBuilder.toString();
    }

    String[] getArgs() {
        String[] args = new String[mArgList.size()];
        return mArgList.toArray(args);
    }

    private interface Builder {
        String build(String table);
    }
}
