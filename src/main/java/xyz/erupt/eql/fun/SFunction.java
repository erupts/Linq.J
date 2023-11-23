package xyz.erupt.eql.fun;

import java.io.Serializable;
import java.util.function.Function;

@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
