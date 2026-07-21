package xyz.erupt.linq.lambda;

/**
 * Self reference for simple-typed sources (String, Number, Date...): the column IS the element.
 *
 * <pre>{@code Linq.from("C", "A", "B").gt(It::self, "A").orderByDesc(It::self).toList(String.class); }</pre>
 */
public class It {

    public Object self() {
        return null;
    }

}
