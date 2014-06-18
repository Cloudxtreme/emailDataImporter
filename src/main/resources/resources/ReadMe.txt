How to run this application.
=========================================

1. Using Maven assembly plugin create a executable jar which can include all the dependency jar's.

2. mvn clean install assembly:single

3. java -classpath target/emailDataImporter-1.0-SNAPSHOT.jar com.importer.data.HbaseTest
