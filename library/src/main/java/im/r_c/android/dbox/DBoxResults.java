package im.r_c.android.dbox;

import java.util.Iterator;
import java.util.List;

/**
 * DBox
 * Created by richard on 7/15/16.
 */
public class DBoxResults<T> implements Iterable<T> {
    // 不带 get 前缀的, 调用之后关闭 cursor
    // 另外提供一组 get 前缀的, 调用后不关闭 cursor

    public T first() {
        return null;
    }

    public T last() {
        return null;
    }

    public List<T> some(int start, int count) {
        return null;
    }

    public List<T> some(Filter<T> filter) {
        return null;
    }

    public List<T> all() {
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    public interface Filter<T> {
        boolean filter(T t);
    }
}
