package xyz.erupt.linq.engine;

import xyz.erupt.linq.schema.Row;
import xyz.erupt.linq.util.RowUtil;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Opt-in engine that materializes the source list in parallel across the common
 * ForkJoinPool. Enable it explicitly per query:
 *
 * <pre>{@code
 * Linq linq = Linq.from(bigList);
 * linq.setEngine(new ParallelEruptEngine());
 * linq...write(Foo.class);
 * }</pre>
 *
 * <p>It is never the default: a library must not silently seize every core, since the host
 * application is usually already multi-threaded (e.g. one request per thread). Parallelism only
 * kicks in above {@link #threshold} elements — for small inputs the fork/join overhead outweighs
 * the gain, so it falls back to the sequential path.
 *
 * <p>Encounter order is preserved, so results are identical to {@link EruptEngine}. Only the
 * read stage ({@code listToTable}) is parallelized here; join/group/sort keep their sequential
 * semantics.
 */
public class ParallelEruptEngine extends EruptEngine {

    /** Datasets smaller than this run sequentially. */
    private int threshold = 50_000;

    public ParallelEruptEngine() {
    }

    public ParallelEruptEngine(int threshold) {
        this.threshold = threshold;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected List<Row> toTable(List<?> from) {
        return RowUtil.listToTable(from, from.size() >= threshold);
    }

    // Direct-path transforms are stateless per element, so they parallelize safely.
    // Encounter order is preserved by the ordered stream collect.
    @Override
    protected <T> List<T> mapSource(List<?> source, Function<Object, T> mapper) {
        if (source.size() < threshold) {
            return super.mapSource(source, mapper);
        }
        return source.parallelStream().filter(Objects::nonNull).map(mapper).collect(Collectors.toList());
    }
}
