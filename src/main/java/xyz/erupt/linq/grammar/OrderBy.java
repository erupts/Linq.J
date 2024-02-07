package xyz.erupt.linq.grammar;

import xyz.erupt.linq.Linq;
import xyz.erupt.linq.consts.OrderByDirection;
import xyz.erupt.linq.lambda.SFunction;
import xyz.erupt.linq.schema.OrderBySchema;
import xyz.erupt.linq.util.Columns;

import java.util.ArrayList;
import java.util.List;

public interface OrderBy {

    Linq orderBy(List<OrderBySchema> orderBySchemas);

    default <R> Linq orderBy(SFunction<R, ?> column, OrderByDirection direction) {
        List<OrderBySchema> orderBySchemas = new ArrayList<>();
        orderBySchemas.add(new OrderBySchema(Columns.of(column), direction));
        return orderBy(orderBySchemas);
    }

    default <R> Linq orderBy(SFunction<R, ?> column) {
        return orderBy(column, OrderByDirection.ASC);
    }

    default <R> Linq orderByAsc(SFunction<R, ?> column) {
        return orderBy(column);
    }

    default <R> Linq orderByDesc(SFunction<R, ?> column) {
        return orderBy(column, OrderByDirection.DESC);
    }

}
