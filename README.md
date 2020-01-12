# hbaseFS

hbaseFS is a distributed file system that provides a small amount of highly replicated, consistent storage by using [HBase](hbaseFS.sqlhttps://hbase.apache.org/) which is an open-source, distributed, versioned, non-relational database on top of Hadoop and HDFS. 

Overview
========

hbaseFS is implemented in Java with springboot. The main features of hbaseFS are:

- Providing user identity management and resource authentication. 
- Supporting basic file operations.
- Supporting bucket management.

Building
========

Get the source code:

```
git clone git@github.com:faushine/hbaseFS.git
```

Create schemes in your MySQL database:

```
/hbaseFS/hbaseFS.sql
```

Run hbase:

```
sh /hbase-1.2.4/bin/start-hbase.sh
```

Setup properties in:

```
/hbaseFS/hfs-web/src/main/resources/application.properties
```

If you never use hbase before, please follow this instruction: https://www.datageekinme.com/setup/setting-up-my-mac-hbase/

Build project:

```
mvn clean package -Dmaven.test.skip=true
```

## Usage

Start server :

```
java -jar hfs-web/target/hfs-web-1.0-SNAPSHOT.jar 
```

API usages are shown in:

```
hbaseFS.postman_collection.json
```

## Future Directions

- Add messaging queue for current limiting
- Encrypting files
