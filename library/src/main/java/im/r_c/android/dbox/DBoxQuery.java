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

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import java.util.Arrays;

/**
 * DBox
 * Created by richard on 7/19/16.
 */

/**
 * Describe a query.
 *
 * @param <T> type of result object
 */
public class DBoxQuery<T> {
    private static final String TAG = DBoxQuery.class.getSimpleName();

    private SQLiteDatabase mDb;
    private TableInfo mTableInfo;
    private DBoxCondition mCondition;
    private StringBuilder mOrderBuilder;

    DBoxQuery(SQLiteDatabase db, TableInfo tableInfo, DBoxCondition condition) {
        mDb = db;
        mTableInfo = tableInfo;
        mCondition = condition;
        mOrderBuilder = new StringBuilder();
    }

    public DBoxQuery<T> orderBy(String... fields) {
        for (String field : fields) {
            mOrderBuilder.append(mOrderBuilder.length() == 0 ? "" : ", ")
                    .append(mTableInfo.mName).append(".").append(field);
        }
        return this;
    }

    public DBoxQuery<T> orderByDesc(String... fields) {
        for (String field : fields) {
            mOrderBuilder.append(mOrderBuilder.length() == 0 ? "" : ", ")
                    .append(mTableInfo.mName).append(".").append(field).append(" DESC");
        }
        return this;
    }

    /**
     * Get the results of the query.
     *
     * @return results object
     */
    public DBoxResults<T> results() {
        Pair<String, String[]> pair = SQLBuilder.query(mTableInfo, mCondition, mOrderBuilder);
        Log.d(TAG, pair.first);
        Log.d(TAG, Arrays.toString(pair.second));
        // This cursor will be closed in DBoxResults
        @SuppressLint("Recycle") Cursor cursor = mDb.rawQuery(pair.first, pair.second);
        return new DBoxResults<>(mTableInfo, cursor);
    }
}
