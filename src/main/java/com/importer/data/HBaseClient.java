package com.importer.data;

/**
 * Created by piyush on 4/11/14.
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;

import java.util.ArrayList;

public class HBaseClient {

    public static void main(String[] args) throws Exception {
        Configuration config = HBaseConfiguration.create();
        System.out.println("hbase zookeeper Address : " + config.get("hbase.zookeeper.quorum"));

        HBaseAdmin.checkHBaseAvailable(config);
        System.out.println("HBase is running!");


        HTable table = new HTable(config, "test1");
        ArrayList<Put> data = new ArrayList<Put>();
        Put p = new Put("row1".getBytes());
        p.add("field1".getBytes(), "qualifier".getBytes(), "value1".getBytes());

        table.put(data);
        table.flushCommits();

        data.clear();
        table.close();
        System.out.println("close table");


    }
}
