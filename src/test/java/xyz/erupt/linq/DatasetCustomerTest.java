package xyz.erupt.linq;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import xyz.erupt.linq.data.customer.CustomerChurnModel;
import xyz.erupt.linq.data.customer.CustomerInfo;
import xyz.erupt.linq.util.Columns;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatasetCustomerTest {

    private List<CustomerChurnModel> dataset;

    private final List<CustomerInfo> customerInfos = new ArrayList<>();

    @Before
    public void before() throws IOException {
        try (InputStream stream = DatasetCustomerTest.class.getResourceAsStream("/CustomerChurnModelDataSet.json")) {
            if (stream != null) {
                dataset = new Gson().fromJson(new InputStreamReader(stream), new TypeToken<List<CustomerChurnModel>>() {
                }.getType());
            }
        }
        customerInfos.add(new CustomerInfo(15634602L, "MM"));
        customerInfos.add(new CustomerInfo(15634602L, "KK"));
        System.out.println("dataset length:" + dataset.size());
    }

    @Test
    public void allData() {
        List<Map<String, Object>> result = Linq.from(dataset)
                .select(CustomerChurnModel::getAge, CustomerChurnModel::getGender)
                .writeMap();
        System.out.println(result.size());
    }

    @Test
    public void query() {
        List<Map<String, Object>> result = Linq.from(dataset)
                .distinct()
                .leftJoin(customerInfos, CustomerInfo::getCustomerId, CustomerChurnModel::getCustomerId)
                .select(CustomerChurnModel::getAge, CustomerChurnModel::getGender)
                .select(CustomerInfo::getNickName)
                .like(CustomerChurnModel::getGender, "Male")
                .between(CustomerChurnModel::getAge, 10, 20)
                .eq(CustomerChurnModel::getExited, true)
                .groupBy(CustomerChurnModel::getAge, CustomerChurnModel::getGender)
                .orderBy(CustomerChurnModel::getAge)
                .writeMap();
        System.out.println("result size " + result.size());
    }

}
