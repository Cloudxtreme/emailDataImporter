package com.importer.data;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import java.io.IOException;

/**
 * Created by piyush on 4/18/14.
 */
public class TestHortonSandbox {
    public static void main(String[] args) throws IOException {
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "plumemshdfs-1.qa.messageone.com");
        config.set("hbase.zookeeper.property.clientPort","2181");
        System.out.println("hbase zookeeper Address : " + config.get("hbase.zookeeper.quorum"));
        try {
            HBaseAdmin.checkHBaseAvailable(config);
        } catch (MasterNotRunningException e) {
            System.out.println("HBase is not running.");
            System.exit(1);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Service Exception");
            System.exit(1);
        }
        System.out.println("***Things look healthy ***");
    }
}