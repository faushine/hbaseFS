package com.faushine.hfs.server;

import com.google.common.base.Strings;

import com.faushine.hfs.common.HosObject;
import com.faushine.hfs.common.HosObjectSummary;
import com.faushine.hfs.common.ObjectListResult;
import com.faushine.hfs.common.ObjectMetaData;
import com.faushine.hfs.common.util.JsonUtil;
import com.faushine.hfs.core.ErrorCode;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.io.ByteBufferInputStream;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
public class HosStoreImpl implements IHosStore {

  private static Logger logger = Logger.getLogger(HosStoreImpl.class);
  private Connection connection = null;
  private IHdfsService fileStore;
  private String zkUrls;
  private CuratorFramework zkClient;

  public HosStoreImpl(Connection connection, IHdfsService fileStore, String zkUrls) {
    this.connection = connection;
    this.fileStore = fileStore;
    this.zkUrls = zkUrls;
    this.zkClient = CuratorFrameworkFactory.newClient(zkUrls, new ExponentialBackoffRetry(20, 5));
    this.zkClient.start();
  }

  @Override
  public void createBucketStore(String bucketName) throws IOException {
    // create index table
    HBaseServiceImpl
        .createTable(connection, HosUtil.getDirTableName(bucketName), HosUtil.getDirColumnFamily(),
            null);
    // create folder table
    HBaseServiceImpl
        .createTable(connection, HosUtil.getObjTableName(bucketName), HosUtil.getObjColumnFamily(),
            HosUtil.OBJ_REGIONS);

    // insert bucket into seq table
    Put put = new Put(bucketName.getBytes());
    put.addColumn(HosUtil.BUCKET_DIR_SEQ_CF_BYTES, HosUtil.BUCKET_DIR_SEQ_QUALIFIER,
        Bytes.toBytes(0L));
    HBaseServiceImpl.putRow(connection, HosUtil.BUCKET_DIR_SEQ_TABLE, put);

    // create hdfs index
    fileStore.mkDir(HosUtil.FILE_STORE_ROOT + "/" + bucketName);
  }

  @Override
  public void deleteBucketStore(String bucket) throws IOException {
    // delete index table and file table
    HBaseServiceImpl.deleteTable(connection, HosUtil.getDirTableName(bucket));
    HBaseServiceImpl.deleteTable(connection, HosUtil.getObjTableName(bucket));

    //delete content in seq table
    HBaseServiceImpl.deleteRow(connection, HosUtil.BUCKET_DIR_SEQ_TABLE, bucket);

    //delete index of hdfs
    fileStore.deleteDir(HosUtil.FILE_STORE_ROOT + "/" + bucket);

  }

  @Override
  public void createSeqTable() throws IOException {
    Admin admin = connection.getAdmin();
    TableName tableName = TableName.valueOf(HosUtil.BUCKET_DIR_SEQ_TABLE);
    if (admin.tableExists(tableName)) {
      return;
    }
    HBaseServiceImpl.createTable(connection, HosUtil.BUCKET_DIR_SEQ_TABLE,
        new String[]{HosUtil.BUCKET_DIR_SEQ_CF},null);
  }

  @Override
  public void put(String bucket, String key, ByteBuffer content, long length, String mediaType,
      Map<String, String> properties) throws Exception {
    // check if it is creating dir
    InterProcessMutex lock = null;
    if (key.endsWith("/")) {
      putDir(bucket, key);
      return;
    }
    // get seqId
    String dir = key.substring(0, key.lastIndexOf("/") + 1);
    String hash = null;
    while (hash == null) {
      if (!dirExist(bucket, dir)) {
        hash = putDir(bucket, dir);
      } else {
        hash = getDirSeqId(bucket, dir);
      }
    }


    // get lock
    String lockKey = key.replace("/", "_");
    lock = new InterProcessMutex(zkClient, "/hos/" + bucket + "/" + lockKey);
    try {
      lock.acquire();

      // upload file
      String fileKey = hash + "_" + key.substring(key.lastIndexOf("/") + 1);
      Put contentPut = new Put(fileKey.getBytes());

      if (!Strings.isNullOrEmpty(mediaType)) {
        contentPut.addColumn(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_MEDIATYPE_QUALIFIER, mediaType.getBytes());
      }

      if (properties != null) {
        String props = JsonUtil.toJson(properties);
        contentPut.addColumn(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_PROPS_QUALIFIER, props.getBytes());
      }
      contentPut.addColumn(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_LEN_QUALIFIER, Bytes.toBytes((long) length));

      // check filesize: if less than 20M store in hbase, otherwise store in hdfs
      if (length <= HosUtil.FILE_STORE_THRESHOLD) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(HosUtil.OBJ_CONT_QUALIFIER);
        contentPut
            .addColumn(HosUtil.OBJ_CONT_CF_BYTES, byteBuffer, System.currentTimeMillis(), content);
        byteBuffer.clear();
      } else {
        String fileDir = HosUtil.FILE_STORE_ROOT + "/" + bucket + "/" + hash;
        String name = key.substring(key.lastIndexOf("/") + 1);
        InputStream inputStream = new ByteBufferInputStream(content);
        fileStore.saveFile(fileDir, name, inputStream, length, (short) 1);
      }
      HBaseServiceImpl.putRow(connection, HosUtil.getObjTableName(bucket), contentPut);
    }finally {
      // release lock
      if (lock != null) {
        lock.release();
      }
    }
  }

  private boolean dirExist(String bucket, String key) {
    boolean exists = HBaseServiceImpl.existsRow(connection, HosUtil.getDirTableName(bucket), key);
    return exists;
  }

  private String getDirSeqId(String bucket, String key) {
    Result result = HBaseServiceImpl.getRow(connection, HosUtil.getDirTableName(bucket), key);
    if (result.isEmpty()) {
      return null;
    }
    return Bytes.toString(result.getValue(HosUtil.DIR_META_CF_BYTES, HosUtil.DIR_SEQID_QUALIFIER));
  }

  private String putDir(String bucket, String key) throws Exception {
    if (dirExist(bucket, key)) {
      return null;
    }
    // get lock from zk
    InterProcessMutex mutex = null;
    try {
      String mutexKey = key.replace("/", "_");
      mutex = new InterProcessMutex(zkClient, "/hos" + bucket + "/" + mutexKey);
      mutex.acquire();
      String dir1 = key.substring(0, key.lastIndexOf("/"));
      String name = dir1.substring(dir1.lastIndexOf("/")+1);

      // create dir
      if (name.length() > 0) {
        String parent = dir1.substring(0, dir1.lastIndexOf("/") + 1);
        if (!dirExist(bucket, parent)) {
          this.putDir(bucket, parent);
        }
        Put put = new Put(Bytes.toBytes(parent));
        put.addColumn(HosUtil.DIR_SUBDIR_CF_BYTES, Bytes.toBytes(name), Bytes.toBytes('1'));
        HBaseServiceImpl.putRow(connection, HosUtil.getDirTableName(bucket), put);
      }

      // insert dir into index table
      String seqId = getDirSeqId(bucket, key);
      String hash = seqId == null ? mkDirSeqId(bucket) : seqId;
      Put dirPut = new Put(key.getBytes());
      dirPut.addColumn(HosUtil.DIR_META_CF_BYTES, HosUtil.DIR_SEQID_QUALIFIER, Bytes.toBytes(hash));
      HBaseServiceImpl.putRow(connection, HosUtil.getDirTableName(bucket), dirPut);
      return hash;
    } finally {
      if (mutex != null) {
        // release lock
        mutex.release();
      }
    }
  }

  private String mkDirSeqId(String bucket) throws IOException {
    long v = HBaseServiceImpl.incrementColumnValue(connection, HosUtil.BUCKET_DIR_SEQ_TABLE, bucket,
        HosUtil.BUCKET_DIR_SEQ_CF_BYTES, HosUtil.BUCKET_DIR_SEQ_QUALIFIER, 1);
    return String.format("%da%d", v % 64, v);
  }

  @Override
  public HosObjectSummary getSummary(String bucket, String key) throws IOException {
    // check if it is a dir
    if (key.endsWith("/")) {
      Result result = HBaseServiceImpl.getRow(connection, HosUtil.getDirTableName(bucket), key);
      if (!result.isEmpty()) {
        // get dir attributes and convert it to HosObjectSummary
        return this.dirObjectToSummary(result, bucket, key);
      }
      return null;
    }
    // get file attributes
    String dir = key.substring(0, key.lastIndexOf("/") + 1);
    String seq = getDirSeqId(bucket, dir);

    if (seq == null) {
      return null;
    }
    String objKey = seq + "_" + key.substring(key.lastIndexOf("/") + 1);
    Result result = HBaseServiceImpl.getRow(connection, HosUtil.getObjTableName(bucket), objKey);
    if (result.isEmpty()) {
      return null;
    }
    return this.resultToObjectSummary(result, bucket, dir);
  }

  private HosObjectSummary dirObjectToSummary(Result result, String buscket, String dir) {
    HosObjectSummary summary = new HosObjectSummary();
    summary.setId(Bytes.toString(result.getRow()));
    summary.setKey(dir);
    summary.setAttrs(new HashMap<>(0));
    summary.setBucket(buscket);
    summary.setLastModifyTime(result.rawCells()[0].getTimestamp());
    summary.setLength(0L);
    summary.setMediaType("");
    if (dir.length() > 1) {
      summary.setName(dir.substring(dir.lastIndexOf("/") + 1));
    } else {
      summary.setName("");
    }
    return summary;
  }

  private HosObjectSummary resultToObjectSummary(Result result, String bucket, String dir)
      throws IOException {
    HosObjectSummary summary = new HosObjectSummary();
    long timeStamp = result.rawCells()[0].getTimestamp();
    summary.setLastModifyTime(timeStamp);
    String id = new String(result.getRow());
    summary.setId(id);
    String name = id.split("_", 2)[1];
    summary.setName(name);
    summary.setKey(dir + name);
    summary.setBucket(bucket);
    summary.setMediaType(Bytes
        .toString(result.getValue(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_MEDIATYPE_QUALIFIER)));
    String s = Bytes
        .toString(result.getValue(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_PROPS_QUALIFIER));
    if (s != null) {
      summary.setAttrs(JsonUtil.fromJson(Map.class, s));
    }
    summary.setLength(
        Bytes.toLong(result.getValue(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_LEN_QUALIFIER)));
    return summary;
  }

  @Override
  public List<HosObjectSummary> list(String bucket, String startKey, String endKey)
      throws IOException {
    String dir1 = startKey.substring(0, startKey.lastIndexOf("/") + 1).trim();
    if (dir1.length() == 0) {
      dir1 = "/";
    }
    String dir2 = endKey.substring(0, startKey.lastIndexOf("/") + 1).trim();
    if (dir2.length() == 0) {
      dir2 = "/";
    }
    String name1 = startKey.substring(startKey.lastIndexOf("/") + 1);
    String name2 = endKey.substring(startKey.lastIndexOf("/") + 1);
    String seqId = this.getDirSeqId(bucket, dir1);
    // 查找 dir1 中大于 name1 的全部文件
    List<HosObjectSummary> keys = new ArrayList<>();
    if (seqId != null && name1.length() > 0) {
      byte[] max = Bytes.createMaxByteArray(100);
      byte[] tail = Bytes.add(Bytes.toBytes(seqId), max);
      if (dir1.equals(dir2)) {
        tail = (seqId + "_" + name2).getBytes();
      }
      byte[] start = (seqId + "_" + name1).getBytes();
      ResultScanner scanner1 = HBaseServiceImpl
          .getScanner(connection, HosUtil.getObjTableName(bucket), start, tail);
      Result result = null;
      while ((result = scanner1.next()) != null) {
        HosObjectSummary summary = this.resultToObjectSummary(result, bucket, dir1);
        keys.add(summary);
      }
      if (scanner1 != null) {
        scanner1.close();
      }
    }
    //startkey~endkey之间的全部目录
    ResultScanner scanner2 = HBaseServiceImpl
        .getScanner(connection, HosUtil.getDirTableName(bucket), startKey, endKey);
    Result result = null;
    while ((result = scanner2.next()) != null) {
      String seqId2 = Bytes.toString(result.getValue(HosUtil.DIR_META_CF_BYTES,
          HosUtil.DIR_SEQID_QUALIFIER));
      if (seqId2 == null) {
        continue;
      }
      String dir = Bytes.toString(result.getRow());
      keys.add(dirObjectToSummary(result, bucket, dir));
      getDirAllFiles(bucket, dir, seqId2, keys, endKey);
    }
    if (scanner2 != null) {
      scanner2.close();
    }
    Collections.sort(keys);
    return keys;
  }

  private void getDirAllFiles(String bucket, String dir, String seqId, List<HosObjectSummary> keys,
      String endKey) throws IOException {

    byte[] max = Bytes.createMaxByteArray(100);
    byte[] tail = Bytes.add(Bytes.toBytes(seqId), max);
    if (endKey.startsWith(dir)) {
      String endKeyLeft = endKey.replace(dir, "");
      String fileNameMax = endKeyLeft;
      if (endKeyLeft.indexOf("/") > 0) {
        fileNameMax = endKeyLeft.substring(0, endKeyLeft.indexOf("/"));
      }
      tail = Bytes.toBytes(seqId + "_" + fileNameMax);
    }

    Scan scan = new Scan(Bytes.toBytes(seqId), tail);
    scan.setFilter(HosUtil.OBJ_META_SCAN_FILTER);
    ResultScanner scanner = HBaseServiceImpl
        .getScanner(connection, HosUtil.getObjTableName(bucket), scan);
    Result result = null;
    while ((result = scanner.next()) != null) {
      HosObjectSummary summary = this.resultToObjectSummary(result, bucket, dir);
      keys.add(summary);
    }
    if (scanner != null) {
      scanner.close();
    }
  }

  @Override
  public ObjectListResult listDir(String bucket, String dir, String start, int maxCount)
      throws IOException {
    // look up index table
    start = Strings.nullToEmpty(start);
    Get get = new Get(Bytes.toBytes(dir));
    get.addFamily(HosUtil.DIR_SUBDIR_CF_BYTES);
    if (!Strings.isNullOrEmpty(start)) {
      get.setFilter(new QualifierFilter(CompareOp.GREATER_OR_EQUAL,
          new BinaryComparator(Bytes.toBytes(start))));
    }
    Result dirResult = HBaseServiceImpl.getRow(connection, HosUtil.getDirTableName(bucket), get);
    List<HosObjectSummary> subDirs = null;
    if (!dirResult.isEmpty()) {
      subDirs = new ArrayList<>();
      for (Cell cell : dirResult.rawCells()) {
        HosObjectSummary summary = new HosObjectSummary();
        byte[] qualifierBytes = new byte[cell.getQualifierLength()];
        CellUtil.copyQualifierTo(cell, qualifierBytes, 0);
        String name = Bytes.toString(qualifierBytes);
        summary.setKey(dir + name + "/");
        summary.setName(name);
        summary.setLastModifyTime(cell.getTimestamp());
        summary.setMediaType("");
        summary.setBucket(bucket);
        summary.setLength(0);
        subDirs.add(summary);
        if (subDirs.size() > maxCount + 1) {
          break;
        }
      }
    }
    // look up file table
    String dirSeq = getDirSeqId(bucket,dir);
    byte[] objStart = Bytes.toBytes(dirSeq+"_"+start);
    Scan scan = new Scan();
    scan.setRowPrefixFilter(Bytes.toBytes(dirSeq + "_"));
    scan.setStartRow(objStart);
    scan.setMaxResultsPerColumnFamily(maxCount+1);
    scan.addFamily(HosUtil.OBJ_META_CF_BYTES);
    ResultScanner objScanner = HBaseServiceImpl
        .getScanner(connection, HosUtil.getObjTableName(bucket), scan);
    List<HosObjectSummary> summaries = new ArrayList<>();
    Result result = null;
    while (summaries.size() < maxCount+2 && (result = objScanner.next()) != null) {
      HosObjectSummary summary = this.resultToObjectSummary(result, bucket, dir);
      summaries.add(summary);
    }
    if (objScanner != null) {
      objScanner.close();
    }
    if (subDirs != null && subDirs.size() > 0) {
      summaries.addAll(subDirs);
    }
    //return result
    Collections.sort(summaries);
    if (summaries.size()>maxCount){
      summaries = summaries.subList(0,maxCount);
    }
    ObjectListResult listResult = new ObjectListResult();
    HosObjectSummary nextMarkerObj =
        summaries.size() > maxCount ? summaries.get(summaries.size() - 1)
            : null;
    if (nextMarkerObj != null) {
      listResult.setNextMarker(nextMarkerObj.getKey());
    }
    if (summaries.size() > maxCount) {
      summaries = summaries.subList(0, maxCount);
    }
    listResult.setMaxKeyNumber(maxCount);
    if (summaries.size() > 0) {
      listResult.setMinKey(summaries.get(0).getKey());
      listResult.setMaxKey(summaries.get(summaries.size() - 1).getKey());
    }
    listResult.setObjectCount(summaries.size());
    listResult.setObjectList(summaries);
    listResult.setBucket(bucket);

    return listResult;
  }

  @Override
  public HosObject getObject(String bucket, String key) throws IOException {
    // check if it is a dir
    if (key.endsWith("/")) {
      //load from index table
      Result result = HBaseServiceImpl.getRow(connection, HosUtil.getDirTableName(bucket), key);
      if (result.isEmpty()) {
        return null;
      }
      ObjectMetaData objectMetaData = new ObjectMetaData();
      objectMetaData.setBucket(bucket);
      objectMetaData.setKey(key);
      objectMetaData.setLength(0);
      objectMetaData.setLastMpdifyTime(result.rawCells()[0].getTimestamp());
      HosObject object = new HosObject();
      object.setMetaData(objectMetaData);
      return object;
    }
    // load from file table
    String dir = key.substring(0, key.lastIndexOf("/") + 1);
    String seq = getDirSeqId(bucket, dir);
    if (seq == null) {
      return null;
    }
    String objKey = seq + "_" + key.substring(key.lastIndexOf("/") + 1);
    Result result = HBaseServiceImpl.getRow(connection, HosUtil.getObjTableName(bucket), objKey);
    if (result.isEmpty()) {
      return null;
    }
    HosObject object = new HosObject();
    long len = Bytes.toLong(result.getValue(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_LEN_QUALIFIER));
    ObjectMetaData metaData = new ObjectMetaData();
    metaData.setBucket(bucket);
    metaData.setKey(key);
    metaData.setLastMpdifyTime(result.rawCells()[0].getTimestamp());
    metaData.setLength(len);
    metaData.setMediaType(Bytes
        .toString(result.getValue(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_MEDIATYPE_QUALIFIER)));
    byte[] b = result.getValue(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_PROPS_QUALIFIER);
    if (b != null) {
      metaData.setAttrs(JsonUtil.fromJson(Map.class, Bytes.toString(b)));
    }
    object.setMetaData(metaData);
    // load file contents
    if (result.containsNonEmptyColumn(HosUtil.OBJ_CONT_CF_BYTES, HosUtil.OBJ_CONT_QUALIFIER)) {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(
          result.getValue(HosUtil.OBJ_CONT_CF_BYTES, HosUtil.OBJ_CONT_QUALIFIER));
      object.setContent(inputStream);
    } else {
      String fileDir = HosUtil.FILE_STORE_ROOT + "/" + bucket + "/" + seq;
      InputStream inputStream = this.fileStore
          .openFile(fileDir, key.substring(key.lastIndexOf("/") + 1));
      object.setContent(inputStream);
    }
    return object;
  }

  @Override
  public void deleteObject(String bucket, String key) throws Exception {
    // check if the key is the dir
    if (key.endsWith(",")) {
      // delete dir

      // check if dir is empty
      if (!isDirEmpty(bucket, key)) {
        throw new HosServerException(ErrorCode.ERROR_PERMISSOON_DENIED, "dir is not empty");
      }
      // acquire lock
      InterProcessMutex lock = null;
      String lockKey = key.replace("/", "_");
      lock = new InterProcessMutex(zkClient, "/hos/" + bucket + "/" + lockKey);
      lock.acquire();
      // delete data from parent dir
      String dir1 = key.substring(0, key.lastIndexOf("/"));
      String name = dir1.substring(key.lastIndexOf("/") + 1);
      if (name.length() > 0) {
        String parent = key.substring(0, key.lastIndexOf(name));
        HBaseServiceImpl.deleteColumnQualifier(connection, HosUtil.getDirTableName(bucket), parent,
            HosUtil.DIR_SUBDIR_CF, name);
      }
      HBaseServiceImpl.deleteRow(connection, HosUtil.getDirTableName(bucket), key);
      // release lock
      lock.release();
      return;
    }
    // delete file
    // get file size
    // check if the file store in hdfs or hbase
    String dir = key.substring(0, key.lastIndexOf("/") + 1);
    String name = key.substring(key.lastIndexOf("/") + 1);
    String seqId = getDirSeqId(bucket, dir);
    String objKey = seqId + "_" + name;
    Get get = new Get(objKey.getBytes());
    get.addColumn(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_LEN_QUALIFIER);
    Result result = HBaseServiceImpl.getRow(connection, HosUtil.getObjTableName(bucket), get);
    if (result.isEmpty()) {
      return;
    }
    long len = Bytes.toLong(result.getValue(HosUtil.OBJ_META_CF_BYTES, HosUtil.OBJ_LEN_QUALIFIER));
    if (len > HosUtil.FILE_STORE_THRESHOLD) {
      String fileDir = HosUtil.FILE_STORE_ROOT + "/" + bucket + "/" + seqId;
      fileStore.deleteFile(fileDir, name);
    }
    HBaseServiceImpl.deleteRow(connection, HosUtil.getObjTableName(bucket), objKey);
  }

  private boolean isDirEmpty(String bucket, String dir) throws Exception {
    return listDir(bucket, dir, null, 2).getObjectList().size() == 0;
  }
}
