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
            exists = true;
        } catch (Exception ignored) {
        } finally {
            if (c != null && c.isClosed()) {
                c.close();
            }
        }
        return exists;
    }
}
