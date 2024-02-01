package xyz.erupt.linq.util;

import xyz.erupt.linq.lambda.LambdaInfo;
import xyz.erupt.linq.lambda.LambdaReflect;

public class VirtualColumn {

    private Integer number;

    private String string;

    private Boolean bool;

    public static LambdaInfo lambdaInfo() {
        return LambdaReflect.getInfo(VirtualColumn::getNumber);
    }

    public Integer getNumber() {
        return number;
    }

    public String getString() {
        return string;
    }

    public Boolean getBool() {
        return bool;
    }
}
