package azkaban.jobExecutor.loaders.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class S3FileDownloader implements FileDownloader {

  private transient static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(S3FileDownloader.class);

  protected AmazonS3Client client;

  public S3FileDownloader() {
    client = getS3Client();
  }

  private AWSCredentials getAWSCredentials() {
    DefaultAWSCredentialsProviderChain providerChain = new DefaultAWSCredentialsProviderChain();
    return providerChain.getCredentials();
  }

  private AmazonS3Client getS3Client() {
    return new AmazonS3Client(getAWSCredentials()).withRegion(Regions.US_WEST_2);
  }

  public void setClient(AmazonS3Client client) {
    this.client = client;
  }

  public static String[] bucketAndKey(String url) {
    int firstSlash = url.indexOf("/");
    String bucket = url.substring(0, firstSlash);
    String key = url.substring(firstSlash + 1);
    String[] bucketAndKey = {bucket, key};
    return bucketAndKey;
  }

  /**
   * Download a s3 file to local disk
   *
   * @param url       Must be an S3 path
   * @param localPath local target
   */
  public String download(String url, String localPath) throws IOException {
    try {
      URI jarURI = new URI(url);
      // download the jar to local
      logger.info("Specified from s3: " + url);
      logger.info("Downloading file to " + localPath);
      String[] bucketKey = bucketAndKey(url.replace("s3://",""));
      String key = bucketKey[1];
      String bucket = bucketKey[0];
      File localFile = new File(localPath);
      GetObjectRequest request = new GetObjectRequest(bucket, key);
      client.getObject(request, localFile);
      return localPath;
    } catch (URISyntaxException e) {
      logger.error("Invalid URI: " + url);
      throw new IOException("Unable to download due to invalid URI " + url);
    }
  }

}
