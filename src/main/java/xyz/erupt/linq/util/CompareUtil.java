package xyz.erupt.linq.util;

import xyz.erupt.linq.consts.CompareSymbol;

public class CompareUtil {

    public static boolean compare(Object value, Object compareTo, CompareSymbol compareSymbol) {
        if (null == compareTo) return false;
        if (value instanceof Comparable) {
            Comparable<Object> comparable = ((Comparable<Object>) value);
            switch (compareSymbol) {
                case GT:
                    return comparable.compareTo(compareTo) > 0;
                case LT:
                    return comparable.compareTo(compareTo) < 0;
                case GTE:
                    return comparable.compareTo(compareTo) >= 0;
                case LTE:
                    return comparable.compareTo(compareTo) <= 0;
            }
        }
        return false;
    }


}
