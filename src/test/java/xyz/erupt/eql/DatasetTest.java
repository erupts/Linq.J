package xyz.erupt.eql;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import xyz.erupt.eql.data.CustomerChurnModel;
import xyz.erupt.eql.data.CustomerInfo;
import xyz.erupt.eql.util.Columns;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatasetTest {

    private List<CustomerChurnModel> dataset;

    private final List<CustomerInfo> customerInfos = new ArrayList<>();

    @Before
    public void before() throws IOException {
        try (InputStream stream = DatasetTest.class.getResourceAsStream("/CustomerChurnModelDataSet.json")) {
            if (stream != null) {
                dataset = new Gson().fromJson(new InputStreamReader(stream), new TypeToken<List<CustomerChurnModel>>() {
                }.getType());
            }
        }
        customerInfos.add(new CustomerInfo(15634602L, "MM"));
        customerInfos.add(new CustomerInfo(15634602L, "KK"));
    }

    @Test
    public void test() {
        List<Map<String, Object>> result = Linq.from(dataset)
                .distinct()
                .innerJoin(customerInfos, CustomerInfo::getCustomerId, CustomerChurnModel::getCustomerId)
                .select(
                        Columns.of(CustomerChurnModel::getAge),
                        Columns.of(CustomerChurnModel::getGender),
                        Columns.of(CustomerInfo::getNickName)
//                        Columns.sum(CustomerChurnModel::getAge, "sum")
                )
//                .like(CustomerChurnModel::getGender, "Male")
//                .between(CustomerChurnModel::getAge, 10, 20)
//                .eq(CustomerChurnModel::getExited, true)
//                .groupBy(Columns.of(CustomerChurnModel::getAge), Columns.of(CustomerChurnModel::getGender))
                .orderBy(CustomerChurnModel::getAge)
                .write();
        System.out.println(result);
    }

}
