package im.r_c.android.dbox;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void DBoxCondition() throws Exception {
        DBoxCondition condition = new DBoxCondition()
                .equalTo("id", "123")
                .notEqualTo("name", "Richard")
                .greaterThan("id", "3")
                .lessThan("id", "10")
                .greaterThanOrEqualTo("age", "18")
                .lessThanOrEqualTo("age", "50")
                .not().between("price", "20.0", "70.5")
                .and().beginGroup()
                .like("city", "%jin%")
                .or().startsWith("city", "Bei")
                .or().beginGroup()
                .endsWith("city", "jing")
                .and().contains("city", "he")
                .endGroup()
                .or().in("city", "Berlin", "Paris")
                .endGroup()
                .isNull("hobby")
                .isNotNull("friend");
        String correct = "T.id = ? AND T.name != ? AND T.id > ? AND T.id < ? AND T.age >= ? " +
                "AND T.age <= ? AND NOT T.price BETWEEN ? AND ? AND " +
                "( T.city LIKE ? OR T.city LIKE ? OR (T.city LIKE ? AND T.city LIKE ?) " +
                "OR T.city IN (?, ?)) AND T.hobby IS NULL AND T.friend IS NOT NULL";
        String[] args = new String[]{"123", "Richard", "3", "10", "18", "50",
                "20.0", "70.5", "%jin%", "Bei%", "%jing", "%he%", "Berlin", "Paris"};
        assertEquals(correct.replace(" ", ""), condition.build("T").replace(" ", ""));
        assertArrayEquals(args, condition.getArgs().toArray());
    }

    @Test
    public void TableInfo_of() throws Exception {
        TableInfo info = TableInfo.of(Student.class);
    }
}
