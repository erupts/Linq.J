package xyz.erupt.eql.util;

import xyz.erupt.eql.consts.CompareSymbol;

public class CompareUtil {

    public static boolean compare(Object value, Object compareTo, CompareSymbol compareSymbol) {
        if (null == compareTo) {
            return false;
        }
        if (value instanceof Comparable) {
            switch (compareSymbol) {
                case GT:
                    return ((Comparable<Object>) value).compareTo(compareTo) > 0;
                case LT:
                    return ((Comparable<Object>) value).compareTo(compareTo) < 0;
                case GTE:
                    return ((Comparable<Object>) value).compareTo(compareTo) >= 0;
                case LTE:
                    return ((Comparable<Object>) value).compareTo(compareTo) <= 0;
            }
        }
        return false;
    }


}
