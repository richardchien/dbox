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

import java.util.ArrayList;
import java.util.List;

/**
 * DBox
 * Created by richard on 7/15/16.
 */

/**
 * Build a condition that will act as the where clause
 * in a query.
 */
public class DBoxCondition {
    private List<Builder> mBuilderList;
    private List<String> mArgList;
    private int mGroupDepth = 0;

    public DBoxCondition() {
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
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return "NOT";
            }
        });
        return this;
    }

    public DBoxCondition beginGroup() {
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
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return "AND";
            }
        });
        return this;
    }

    public DBoxCondition or() {
        mBuilderList.add(new Builder() {
            @Override
            public String build(String table) {
                return "OR";
            }
        });
        return this;
    }

    public DBoxCondition between(final String column, String startValue, String endValue) {
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
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("?");
            mArgList.add(values[i]);
        }
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

    /**
     * Build the full condition where clause.
     *
     * @param table the table that the conditions act on
     * @return where clause
     */
    String build(String table) {
        if (mGroupDepth != 0) {
            throw new IllegalStateException("There are " + mGroupDepth + " groups haven't been ended.");
        }

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

    /**
     * Get arguments that should be bound to the where clause.
     * Must be called after {@link #build(String)}.
     *
     * @return arguments
     */
    String[] getArgs() {
        String[] args = new String[mArgList.size()];
        return mArgList.toArray(args);
    }

    /**
     * Make it easy for different conditions
     * to be built uniformly.
     */
    private interface Builder {
        String build(String table);
    }
}
