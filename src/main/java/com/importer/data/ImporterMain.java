/**
 * Created by piyush on 4/4/14.
 */
package com.importer.data;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


public class ImporterMain {

    private static final String MAIL_FOLDER = "./data/maildir/";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");


    /**
     * @author piyush_g
     */
    static class HBaseHelper {

        private static Configuration conf = HBaseConfiguration.create();

        public static HBaseHelper create() throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
            System.out.println("HBaseHelper Creating config ........." );
            HBaseHelper result = new HBaseHelper();
            System.out.println("Reading Config File ----------------- " + conf.toString());
            result.hbase = new HBaseAdmin(conf);
            System.out.println("HBaseHelper got the config ........."+ result.hbase.toString());

            return result;
        }

        private HBaseAdmin hbase;


        static {

            System.out.println("Zookeeper quorum configuration===========" +  conf.get("hbase.zookeeper.quorum"));
            System.out.println("Zookeeper master configuration===========" +  conf.get("hbase.master"));

        }

        private HBaseHelper() {

        }

        public HTable createTable(String tableName, String... descriptors)
                throws IOException {
            if (tableExists(tableName)) {
                dropTable(tableName);
            }
            return doCreateTable(tableName, descriptors);
        }

        private HTable doCreateTable(String tableName, String... descriptors)
                throws IOException {
            HTableDescriptor descriptor = new HTableDescriptor(tableName);
            for (String each : descriptors) {
                HColumnDescriptor cd = new HColumnDescriptor(each.getBytes());
                descriptor.addFamily(cd);
            }
            hbase.createTable(descriptor);
            debug(String.format("Database %s created", tableName));
            return new HTable(conf,tableName);
        }

        public void dropTable(String tableName) throws IOException {
            hbase.disableTable(tableName);
            hbase.deleteTable(tableName);
        }

        public HTable getOrCreateTable(String tableName, String... descriptors)
                throws IOException {
            if (!tableExists(tableName)) {
                doCreateTable(tableName, descriptors);
            }
            return new HTable(conf,tableName);
        }

        public void insert(HTable table, String rowKey, List<String> values)
                throws IOException {
            if (values.size() == 3) {
                Put put = new Put(Bytes.toBytes(rowKey));
                put.add(Bytes.toBytes(values.get(0)),
                        Bytes.toBytes(values.get(1)),
                        Bytes.toBytes(values.get(2)));
                table.put(put);
            }
        }

        public boolean tableExists(String tableName) throws IOException {
            return hbase.tableExists(tableName);
        }
    }


    static class Mail {

        public final static String ID = "id";

        public final static String PERSON = "person";

        public final static String FOLDER = "folder";

        public final static String BODY = "body";

        public static Mail create(String person, String folder, String body) {
            return create(UUID.randomUUID().toString(), person, folder, body);
        }

        public static Mail create(String id, String person, String folder, String body) {
            Mail result = new Mail();
            result.id = id;
            result.folder = folder;
            result.person = person;
            result.body = body;
            return result;
        }

        private String id;

        private String person;

        private String folder;

        private String body;

        public Mail() {

        }

        public String getPerson() {
            return person;
        }

        public String getFolder() {
            return folder;
        }

        public String getBody() {
            return body;
        }

        public String getFirstLine() {
            return body.substring(0, body.indexOf('\n'));
        }

        public String getId() {
            return id;
        }

        public String toString() {
            return String.format("(%s: %s; %s: %s; %s: %s; %s: %s)", ID, id,
                    PERSON, person, FOLDER, folder, BODY,
                    StringUtils.substring(body, 0, 16));
        }

    }


    static class MailFactory {

        public static MailFactory create(String filename) throws FileNotFoundException {
            MailFactory result = new MailFactory();
            result.openFile(filename);
            return result;
        }

        private DataInputStream in;

        private BufferedReader filereader;

        private void closeFile() throws IOException {
            in.close();
        }

        public Mail getMail() throws IOException {
            String buffer = new String();
            String line = readLine();
            if (line.startsWith("From ")) {
                line = readLine();
            }
            while (line != null && !line.startsWith("From ")) {
                buffer += line + "\n";
                line = readLine();
            }

            if (line == null) {
                closeFile();
                return null;
            }
            return null;
        }

        private BufferedReader openFile(String filename) throws FileNotFoundException {
            if (filereader == null) {
                FileInputStream fstream = new FileInputStream(filename);
                in = new DataInputStream(fstream);
                filereader = new BufferedReader(new InputStreamReader(in));
            }
            return filereader;
        }

        private String readLine() throws IOException {
            return filereader != null ? filereader.readLine() : new String();
        }

        public static Mail createMail(String filename) {
            try {
                String[] parts = StringUtils.split(filename, "/");
                int size = parts.length;
                if (size >= 3) {
                    String folder = parts[size - 2];
                    String person = parts[size - 3];
                    String body = FileUtils.readFileToString(new File(filename));

                    return Mail.create(person, folder, body);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private static void debug(Object obj) {
        System.out.println(String.format("### DEBUG: %s", obj.toString()));
    }

    public static void main(String[] args) {
        Mail mail;
        long failed = 0, imported = 0;

        File dir = new File(args.length == 0 ? MAIL_FOLDER : args[0]);

        if (dir.exists()) {
            try {
                System.out.println("Directory Exists Calling Hbase -------------");
                HBaseHelper hbase = HBaseHelper.create();
                HTable table = hbase.createTable("enron", Mail.PERSON, Mail.FOLDER, Mail.BODY);
                Collection<File> files = FileUtils.listFiles(dir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
                for (File each : files) {
                    String filename = each.getCanonicalPath();
                    mail = MailFactory.createMail(filename);

                    String body = mail.getBody();
                    if (body != null && !body.isEmpty()) {
                        // System.out.println("### Insert mail: " + mail);
                        hbase.insert(table, mail.getId(), Arrays.asList(Mail.PERSON, "", mail.getPerson()));
                        hbase.insert(table, mail.getId(), Arrays.asList(Mail.FOLDER, "", mail.getFolder()));
                        hbase.insert(table, mail.getId(), Arrays.asList(Mail.BODY, "", body));
                        imported++;
                    } else {
                        failed++;
                    }
                }
                System.out.println(String.format("Total: %d; Imported: %d; Failed: %d", files.size(), imported, failed));
            } catch (MasterNotRunningException e) {

                e.printStackTrace();
            } catch (ZooKeeperConnectionException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }

        }

    }
}

