package com.importer.data;

/**
 * Created by piyush on 4/26/14.
 */

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseTest {

    public static void main(String[] args) throws IOException {

        Configuration config = HBaseConfiguration.create();

        HTable table = new HTable(config, "testHBaseTable");

        Put p = new Put(Bytes.toBytes("testRow1"));

       /* p.add(Bytes.toBytes("testFamily"), Bytes.toBytes("someQualifier1"),
                Bytes.toBytes("Some Value"));
        table.put(p);
*/
        Get g = new Get(Bytes.toBytes("testRow1"));
        Result r = table.get(g);
        byte[] value = r.getValue(Bytes.toBytes("testFamily1"), Bytes.toBytes("someQualifier1"));

        String valueStr = Bytes.toString(value);
        System.out.println("GET: " + valueStr);

        Scan s = new Scan();
        s.addColumn(Bytes.toBytes("testFamily"), Bytes.toBytes("someQualifier"));

        ResultScanner scanner = table.getScanner(s);

        try {
            for (Result rr = scanner.next(); rr != null; rr = scanner.next()) {
                System.out.println("Found row: " + rr);
            }

        } finally {
            scanner.close();
        }
    }
}