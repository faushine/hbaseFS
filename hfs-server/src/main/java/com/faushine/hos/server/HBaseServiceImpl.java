package com.faushine.hos.server;

import com.faushine.hos.core.ErrorCode;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;

import sun.tools.jconsole.Tab;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
public class HBaseServiceImpl {

  // create table
  public static boolean createTable(Connection connection, String tableName, String[] cfs,
      byte[][] splitKeys) {
    try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
      if (admin.tableExists(tableName)) {
        return false;
      }
      HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
      Arrays.stream(cfs).forEach(cf -> {
        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(cf);
        hColumnDescriptor.setMaxVersions(1);
        tableDescriptor.addFamily(hColumnDescriptor);
      });
      admin.createTable(tableDescriptor, splitKeys);
    } catch (Exception e) {
      e.printStackTrace();
      throw new HosServerException(ErrorCode.ERROR_HBASE, "create table error");
    }
    return true;
  }

  // delete table
  public static boolean deleteTable(Connection connection, String tableName) {
    try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
      admin.disableTable(tableName);
      admin.deleteTable(tableName);
    } catch (Exception e) {
      throw new HosServerException(ErrorCode.ERROR_HBASE, "delete table error");
    }
    return true;
  }

  // delete column family
  public static boolean deleteColumnFamily(Connection connection, String tableName, String cf) {
    try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
      admin.deleteColumn(tableName, cf);
    } catch (Exception e) {
      throw new HosServerException(ErrorCode.ERROR_HBASE, "delete cf error");
    }
    return true;
  }

  // delete column
  public static boolean deleteColumnQualifier(Connection connection, String tableName,
      String rowkey, String cf, String column) {
    Delete delete = new Delete(rowkey.getBytes());
    delete.addColumn(cf.getBytes(), column.getBytes());
    return deleteRow(connection, tableName, delete);
  }

  public static boolean deleteRow(Connection connection, String tableName, Delete delete) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      table.delete(delete);
    } catch (Exception e) {
      throw new HosServerException(ErrorCode.ERROR_HBASE, "delete qualifier error");
    }
    return true;
  }

  // delete row
  public static boolean deleteRow(Connection connection, String tableName, String rowkey) {
    Delete delete = new Delete(rowkey.getBytes());
    return deleteRow(connection, tableName, delete);
  }

  // read row
  public static final Result getRow(Connection connection, String tableName, Get get) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      return table.get(get);
    } catch (Exception e) {
      throw new HosServerException(ErrorCode.ERROR_HBASE, "get data error");
    }
  }

  public static Result getRow(Connection connection, String tableName, String rowkey) {
    Get get = new Get(rowkey.getBytes());
    return getRow(connection, tableName, get);
  }

  public static Result getRow(Connection connection, String tableName, String row,
      FilterList filterList) {
    Result rs;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Get g = new Get(Bytes.toBytes(row));
      g.setFilter(filterList);
      rs = table.get(g);
    } catch (IOException e) {
      String msg = String
          .format("get row from table=%s error. msg=%s", tableName, e.getMessage());
      throw new HosServerException(ErrorCode.ERROR_HBASE, msg);
    }
    return rs;
  }
  // exists row
  public static boolean existsRow(Connection connection, String tableName, String row) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Get g = new Get(Bytes.toBytes(row));
      return table.exists(g);
    } catch (IOException e) {
      String msg = String
          .format("check exists row from table=%s error. msg=%s", tableName, e.getMessage());
      throw new HosServerException(ErrorCode.ERROR_HBASE, msg);
    }
  }

  // load scanner
  public static ResultScanner getScanner(Connection connection, String tableName, Scan scan) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      return table.getScanner(scan);
    } catch (Exception e) {
      throw new HosServerException(ErrorCode.ERROR_HBASE, "get scanner error");
    }
  }

  public static ResultScanner getScanner(Connection connection, String tableName, String startKey,
      String endKey, FilterList filterList) {
    Scan scan = new Scan();
    scan.setStartRow(startKey.getBytes());
    scan.setStopRow(endKey.getBytes());
    scan.setFilter(filterList);
    scan.setCaching(1000);
    return getScanner(connection, tableName, scan);
  }

  public static ResultScanner getScanner(Connection connection, String tableName, byte[] startRowKey,
      byte[] stopRowKey) {
    ResultScanner results = null;
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      Scan scan = new Scan();
      scan.setStartRow(startRowKey);
      scan.setStopRow(stopRowKey);
      scan.setCaching(1000);
      results = table.getScanner(scan);
    } catch (IOException e) {
      String msg = String
          .format("scan table=%s error. msg=%s", tableName, e.getMessage());
      throw new HosServerException(ErrorCode.ERROR_HBASE, msg);
    }
    return results;
  }

  public static ResultScanner getScanner(Connection connection, String tableName, String startRowKey,
      String stopRowKey) {
    return getScanner(connection, tableName, Bytes.toBytes(startRowKey), Bytes.toBytes(stopRowKey));
  }

  // insert row
  public static boolean putRow(Connection connection, String tableName, Put put) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      table.put(put);
    } catch (Exception e) {
      throw new HosServerException(ErrorCode.ERROR_HBASE, "put row error");
    }
    return true;
  }

  // batch insertion
  public static boolean putRow(Connection connection, String tableName, List<Put> puts) {
    try (Table table = connection.getTable(TableName.valueOf(tableName))) {
      table.put(puts);
    } catch (Exception e) {
      throw new HosServerException(ErrorCode.ERROR_HBASE, "put row error");
    }
    return true;
  }

  // incrementColumnValue
  public static long incrementColumnValue(Connection connection, String tableName, String row,
      byte[] cf, byte[] qual,int num) {
    try(Table table = connection.getTable(TableName.valueOf(tableName))) {
      return table.incrementColumnValue(row.getBytes(),cf,qual,num);
    }catch (Exception e) {
      throw new HosServerException(ErrorCode.ERROR_HBASE, "put row error");
    }
  }

}
