package im.r_c.android.dbox;

import java.util.ArrayList;
import java.util.List;

/**
 * DBox
 * Created by richard on 7/15/16.
 */
public class Condition {
    private StringBuilder mConditionBuilder;
    private List<String> mArgList;
    private int mGroupDepth = 0;

    public Condition() {
        mConditionBuilder = new StringBuilder();
        mArgList = new ArrayList<>();
    }

    public Condition equals(String prop, String value) {
        return compare(prop, "=", value);
    }

    public Condition notEquals(String prop, String value) {
        return compare(prop, "!=", value);
    }

    public Condition greaterThan(String prop, String value) {
        return compare(prop, ">", value);
    }

    public Condition lessThan(String prop, String value) {
        return compare(prop, "<", value);
    }

    public Condition greaterThanOrEquals(String prop, String value) {
        return compare(prop, ">=", value);
    }

    public Condition lessThanOrEquals(String prop, String value) {
        return compare(prop, "<=", value);
    }

    public Condition compare(String prop, String operator, String value) {
        mConditionBuilder
                .append(prop)
                .append(" ")
                .append(operator)
                .append(" ? ");
        mArgList.add(value);
        return this;
    }

    public Condition not() {
        mConditionBuilder.append("NOT ");
        return this;
    }

    public Condition group() {
        mConditionBuilder.append("(");
        mGroupDepth++;
        return this;
    }

    public Condition endGroup() {
        mConditionBuilder.append(") ");
        mGroupDepth--;
        return this;
    }

    public Condition and() {
        mConditionBuilder.append("AND ");
        return this;
    }

    public Condition or() {
        mConditionBuilder.append("OR ");
        return this;
    }

    public Condition between(String prop, String startValue, String endValue) {
        mConditionBuilder.append(prop).append(" BETWEEN ? AND ? ");
        mArgList.add(startValue);
        mArgList.add(endValue);
        return this;
    }

    public Condition like(String prop, String pattern) {
        mConditionBuilder
                .append(prop)
                .append(" LIKE '")
                .append(pattern)
                .append("' ");
        return this;
    }

    public Condition in(String prop, String... values) {
        mConditionBuilder.append(prop).append(" IN (");
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                mConditionBuilder.append(", ");
            }
            mConditionBuilder.append("?");
            mArgList.add(values[i]);
        }
        mConditionBuilder.append(") ");
        return this;
    }

    String build() {
        if (mGroupDepth != 0) {
            throw new IllegalStateException("There are " + mGroupDepth + " groups not ended.");
        }
        return mConditionBuilder.toString();
    }

    List<String> getArgList() {
        return mArgList;
    }
}
