package com.faushine.hos.web.rest;

import com.google.common.base.Splitter;

import com.faushine.hos.common.BucketModel;
import com.faushine.hos.common.HosHeaders;
import com.faushine.hos.common.HosObject;
import com.faushine.hos.common.HosObjectSummary;
import com.faushine.hos.common.ObjectListResult;
import com.faushine.hos.core.CoreUtil;
import com.faushine.hos.core.usermgr.model.SystemRole;
import com.faushine.hos.core.usermgr.model.UserInfo;
import com.faushine.hos.server.IBucketService;
import com.faushine.hos.server.IHosStore;
import com.faushine.hos.web.security.ContextUtil;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Yuxin Fan
 * @create 2019-12-24
 */
@RestController
@RequestMapping("hos/v1/")
public class HosController extends BaseController {

  private static Logger logger = Logger.getLogger(HosController.class);

  @Autowired
  @Qualifier("bucketServiceImpl")
  IBucketService bucketService;

  @Autowired
  @Qualifier("hosStore")
  IHosStore hosStore;

  public HosController() {
    File file = new File(TMP_DIR);
    file.mkdirs();
  }

  private static long MAX_FILE_IN_MEMORY = 2 * 1024 * 1024;

  private final int readBufferSize = 32 * 1024;

  private static String TMP_DIR = System.getProperty("user.dir") + File.separator + "tmp";

  // create bucket
  @RequestMapping(value = "bucket", method = RequestMethod.POST)
  public Object createBucket(@RequestParam("bucket") String bucketName,
      @RequestParam(name = "detail", required = false, defaultValue = "") String detail) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!currentUser.getSystemRole().equals(SystemRole.VISITOR)) {
      bucketService.addBucket(currentUser, bucketName, detail);
      try {
        hosStore.createBucketStore(bucketName);
      } catch (IOException ioe) {
        bucketService.deleteBucket(bucketName);
        return "create bucket error";
      }
      return "success";
    }
    return "PERMISSION DENIED";
  }

  // delete bucket
  @RequestMapping(value = "bucket", method = RequestMethod.DELETE)
  public Object deleteBucket(@RequestParam("bucket") String bucket) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl.checkBucketOwner(currentUser.getUserName(), bucket)) {
      try {
        hosStore.deleteBucketStore(bucket);
      } catch (IOException ioe) {
        return "delete bucket error";
      }
      bucketService.deleteBucket(bucket);
      return "success";
    }
    return "PERMISSION DENIED";
  }

  @RequestMapping(value = "bucket", method = RequestMethod.PUT)
  public Object updateBucket(
      @RequestParam(name = "bucket") String bucket,
      @RequestParam(name = "detail") String detail) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    BucketModel bucketModel = bucketService.getBucketByName(bucket);
    if (operationAccessControl
        .checkBucketOwner(currentUser.getUserName(), bucketModel.getBucketName())) {
      bucketService.updateBucket(bucket, detail);
      return "success";
    }
    return "PERMISSION DENIED";
  }

  @RequestMapping(value = "bucket", method = RequestMethod.GET)
  public Object getBucket(@RequestParam(name = "bucket") String bucket) {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    BucketModel bucketModel = bucketService.getBucketByName(bucket);
    if (operationAccessControl
        .checkPermission(currentUser.getUserId(), bucketModel.getBucketName())) {
      return bucketModel;
    }
    return "PERMISSION DENIED";

  }


  // get list of bucket
  @RequestMapping(value = "bucket/list", method = RequestMethod.GET)
  public Object getBucket() {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    List<BucketModel> bucketModels = bucketService.getUserBuckets(currentUser.getUserId());
    return bucketModels;
  }

  // upload file / create folder
  @RequestMapping(value = "object", method = {RequestMethod.PUT, RequestMethod.POST})
  @ResponseBody
  public Object putObject(@RequestParam("bucket") String bucket, @RequestParam("key") String key,
      @RequestParam(value = "mediaType", required = false) String mediaType,
      @RequestParam(value = "content", required = false) MultipartFile file,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      response.setStatus(HttpStatus.SC_FORBIDDEN);
      response.getWriter().write("Permission denied");
      return "Permission denied";
    }
    if (!key.startsWith(File.separator)) {
      response.setStatus(HttpStatus.SC_BAD_REQUEST);
      response.getWriter().write("object key must start with /");
    }

    Enumeration<String> headNames = request.getHeaderNames();
    Map<String, String> attrs = new HashMap<>();
    String contentEncoding = request.getHeader("content-encoding");
    if (contentEncoding != null) {
      attrs.put("content-encoding", contentEncoding);
    }
    while (headNames.hasMoreElements()) {
      String header = headNames.nextElement();
      if (header.startsWith(HosHeaders.COMMON_ATTR_PREFIX)) {
        attrs.put(header.replace(HosHeaders.COMMON_ATTR_PREFIX, ""), request.getHeader(header));
      }
    }
    ByteBuffer buffer = null;
    File distFile = null;
    try {
      //put dir object
      if (key.endsWith(File.separator)) {
        if (file != null) {
          response.setStatus(HttpStatus.SC_BAD_REQUEST);
          file.getInputStream().close();
          return null;
        }
        hosStore.put(bucket, key, null, 0, mediaType, attrs);
        response.setStatus(HttpStatus.SC_OK);
        return "success";
      }
      if (file == null || file.getSize() == 0) {
        response.setStatus(HttpStatus.SC_BAD_REQUEST);
        response.getWriter().write("object content could not be empty");
        return "object content could not be empty";
      }

      // upload file: cache->disk->hbase
      if (file != null) {
        if (file.getSize() > MAX_FILE_IN_MEMORY) {
          distFile = new File(TMP_DIR + File.separator + UUID.randomUUID().toString());
          file.transferTo(distFile);
          file.getInputStream().close();
          buffer = new FileInputStream(distFile).getChannel()
              .map(FileChannel.MapMode.READ_ONLY, 0, file.getSize());
        } else {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          org.apache.commons.io.IOUtils.copy(file.getInputStream(), outputStream);
          buffer = ByteBuffer.wrap(outputStream.toByteArray());
          file.getInputStream().close();
        }
      }
      hosStore.put(bucket, key, buffer, file.getSize(), mediaType, attrs);
      return "success";
    } catch (Exception e) {
      logger.error(e.toString());
      response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write("server error");
      return "server error";
    } finally {
      if (buffer != null) {
        buffer.clear();
      }
      if (file != null) {
        try {
          file.getInputStream().close();
        } catch (Exception e) {
          //nothing to do
        }
      }
      if (distFile != null) {
        distFile.delete();
      }
    }
  }

  @RequestMapping(value = "object/list/dir", method = RequestMethod.GET)
  public ObjectListResult listObjectByDir(@RequestParam("bucket") String bucket,
      @RequestParam("dir") String dir,
      @RequestParam(value = "startKey", required = false, defaultValue = "") String start,
      HttpServletResponse response) throws Exception {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      if (!dir.startsWith(File.separator) || !dir.endsWith(File.separator)) {
        response.setStatus(HttpStatus.SC_BAD_REQUEST);
        response.getWriter().write("dir must start with / and end with /");
        return null;
      }
      if ("".equals(start) || start.equals("/")) {
        start = null;
      }
      if (start != null) {
        List<String> segs = StreamSupport
            .stream(Splitter.on("/").trimResults().omitEmptyStrings().split(start).spliterator(),
                false).collect(Collectors.toList());
        start = segs.get(segs.size() - 1);
      }
      ObjectListResult result = this.hosStore.listDir(bucket, dir, start, 100);
      return result;
    }
    response.setStatus(HttpStatus.SC_FORBIDDEN);
    response.getWriter().write("Permission denied");
    return null;
  }

  @RequestMapping(value = "object", method = RequestMethod.DELETE)
  public Object deleteObject(@RequestParam("bucket") String bucket, @RequestParam("key") String key)
      throws Exception {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      this.hosStore.deleteObject(bucket, key);
      return "success";
    }
    return "PERMISSION DENIED";
  }

  // list object
  @RequestMapping(value = "object/list", method = RequestMethod.GET)
  public ObjectListResult listObject(@RequestParam("bucket") String bucket,
      @RequestParam("startKey") String startKey,
      @RequestParam("endKey") String endKey,
      HttpServletResponse response)
      throws IOException {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      response.setStatus(HttpStatus.SC_FORBIDDEN);
      response.getWriter().write("Permission denied");
      return null;
    }
    if (startKey.compareTo(endKey) > 0) {
      response.setStatus(HttpStatus.SC_BAD_REQUEST);
      return null;
    }
    ObjectListResult result = new ObjectListResult();
    List<HosObjectSummary> summaryList = hosStore.list(bucket, startKey, endKey);
    result.setBucket(bucket);
    if (summaryList.size() > 0) {
      result.setMaxKey(summaryList.get(summaryList.size() - 1).getKey());
      result.setMinKey(summaryList.get(0).getKey());
    }
    result.setObjectCount(summaryList.size());
    result.setObjectList(summaryList);
    return result;
  }

  // get file info
  @RequestMapping(value = "object/info", method = RequestMethod.GET)
  public HosObjectSummary getSummary(String bucket, String key, HttpServletResponse response)
      throws IOException {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (!operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      response.setStatus(HttpStatus.SC_FORBIDDEN);
      response.getWriter().write("Permission denied");
      return null;
    }
    HosObjectSummary summary = hosStore.getSummary(bucket, key);
    if (summary == null) {
      response.setStatus(HttpStatus.SC_NOT_FOUND);
    }
    return summary;
  }

  // download file
  @RequestMapping(value = "object/content", method = RequestMethod.GET)
  public void getObject(@RequestParam("bucket") String bucket, @RequestParam("key") String key,
      HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserInfo currentUser = ContextUtil.getCurrentUser();
    if (operationAccessControl.checkPermission(currentUser.getUserId(), bucket)) {
      HosObject object = hosStore.getObject(bucket, key);
      if (object == null) {
        response.setStatus(404);
        return;
      }
      // todo set headers
      OutputStream outputStream = response.getOutputStream();
      InputStream inputStream = object.getContent();
      try {
        byte[] buffer = new byte[readBufferSize];
        int len = -1;
        while ((len = inputStream.read(buffer)) > 0) {
          outputStream.write(buffer, 0, len);
        }
        response.flushBuffer();
      } finally {
        inputStream.close();
        outputStream.close();
      }
    }
  }
}
