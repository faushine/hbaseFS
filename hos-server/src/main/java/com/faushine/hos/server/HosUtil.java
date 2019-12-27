package com.faushine.hos.server;

import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author Yuxin Fan
 * @create 2019-12-21
 */
public class HosUtil {

  static final String DIR_TABLE_PREFIX = "hos_dir_";
  static final String OBJ_TABLE_PREFIX = "hos_obj_";

  static final String DIR_META_CF = "cf";
  static final byte[] DIR_META_CF_BYTES = DIR_META_CF.getBytes();
  static final String DIR_SUBDIR_CF = "sub";
  static final byte[] DIR_SUBDIR_CF_BYTES = DIR_SUBDIR_CF.getBytes();
  static final String OBJ_CONT_CF = "c";
  static final byte[] OBJ_CONT_CF_BYTES = OBJ_CONT_CF.getBytes();
  static final String OBJ_META_CF = "cf";
  static final byte[] OBJ_META_CF_BYTES = OBJ_META_CF.getBytes();

  static final byte[] DIR_SEQID_QUALIFIER = "u".getBytes();
  static final byte[] OBJ_CONT_QUALIFIER = "c".getBytes();
  static final byte[] OBJ_LEN_QUALIFIER = "l".getBytes();
  static final byte[] OBJ_PROPS_QUALIFIER = "p".getBytes();
  static final byte[] OBJ_MEDIATYPE_QUALIFIER = "m".getBytes();

  static final String FILE_STORE_ROOT = "/hos";
  static final int FILE_STORE_THRESHOLD = 20 * 1024 * 1024;

  static final String BUCKET_DIR_SEQ_TABLE = "hos_dir_seq";
  static final String BUCKET_DIR_SEQ_CF = "s";
  static final byte[] BUCKET_DIR_SEQ_CF_BYTES = BUCKET_DIR_SEQ_CF.getBytes();
  static final byte[] BUCKET_DIR_SEQ_QUALIFIER = "s".getBytes();

  static final FilterList OBJ_META_SCAN_FILTER = new FilterList(Operator.MUST_PASS_ONE);

  static final byte[][] OBJ_REGIONS = new byte[][]{
      Bytes.toBytes("1"),
      Bytes.toBytes("4"),
      Bytes.toBytes("7")
  };

  static {
    try {
      byte[][] qualifiers = new byte[][]{HosUtil.DIR_SEQID_QUALIFIER,
          HosUtil.OBJ_LEN_QUALIFIER,
          HosUtil.OBJ_MEDIATYPE_QUALIFIER};
      for (byte[] b : qualifiers) {
        Filter filter = new QualifierFilter(CompareOp.EQUAL,
            new BinaryComparator(b));
        OBJ_META_SCAN_FILTER.addFilter(filter);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String getDirTableName(String bucket) {
    return DIR_TABLE_PREFIX + bucket;
  }

  public static String getObjTableName(String bucket) {
    return OBJ_TABLE_PREFIX + bucket;
  }

  public static String[] getDirColumnFamily() {
    return new String[]{DIR_SUBDIR_CF, DIR_META_CF};
  }

  public static String[] getObjColumnFamily() {
    return new String[]{OBJ_META_CF, OBJ_CONT_CF};
  }
}
