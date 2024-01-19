package xyz.erupt.eql;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import xyz.erupt.eql.data.CustomerChurnModel;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DatasetTest {

    private final List<CustomerChurnModel> dataset = new ArrayList<>();

    @Before
    public void before() throws IOException {
        try (Reader reader = Files.newBufferedReader(Paths.get("/CustomerChurnModelDataSet.csv"));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            for (CSVRecord record : parser) {
                CustomerChurnModel obj = new CustomerChurnModel(); // 自定义的类名称
                String value1 = record.get("columnName1"); // 指定CSV文件中的列名
                int value2 = Integer.parseInt(record.get("columnName2"));
                double value3 = Double.parseDouble(record.get("columnName3"));
                dataset.add(obj);
            }
        }
    }

    @Test
    public void test() {
        System.out.println(dataset);
    }

}
