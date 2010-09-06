/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2006-2010 James Murty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jets3t.service.impl.rest.httpclient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jets3t.service.Constants;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.impl.rest.AccessControlListHandler;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser.ListVersionsResultsHandler;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.S3BucketLoggingStatus;
import org.jets3t.service.model.S3BucketVersioningStatus;
import org.jets3t.service.security.AWSDevPayCredentials;
import org.jets3t.service.security.ProviderCredentials;

import com.jamesmurty.utils.XMLBuilder;

/**
 * REST/HTTP implementation of an S3Service based on the
 * <a href="http://jakarta.apache.org/commons/httpclient/">HttpClient</a> library.
 * <p>
 * This class uses properties obtained through {@link Jets3tProperties}. For more information on
 * these properties please refer to
 * <a href="http://jets3t.s3.amazonaws.com/toolkit/configuration.html">JetS3t Configuration</a>
 * </p>
 *
 * @author James Murty
 */
public class RestS3Service extends S3Service {

    private static final Log log = LogFactory.getLog(RestS3Service.class);
    private static final String AWS_SIGNATURE_IDENTIFIER = "AWS";
    private static final String AWS_REST_HEADER_PREFIX = "x-amz-";
    private static final String AWS_REST_METADATA_PREFIX = "x-amz-meta-";

    private String awsDevPayUserToken = null;
    private String awsDevPayProductToken = null;

    private boolean isRequesterPaysEnabled = false;

    /**
     * Constructs the service and initialises the properties.
     *
     * @param credentials
     * the user credentials to use when communicating with S3, may be null in which case the
     * communication is done as an anonymous user.
     *
     * @throws S3ServiceException
     */
    public RestS3Service(ProviderCredentials credentials) throws S3ServiceException {
        this(credentials, null, null);
    }

    /**
     * Constructs the service and initialises the properties.
     *
     * @param credentials
     * the S3 user credentials to use when communicating with S3, may be null in which case the
     * communication is done as an anonymous user.
     * @param invokingApplicationDescription
     * a short description of the application using the service, suitable for inclusion in a
     * user agent string for REST/HTTP requests. Ideally this would include the application's
     * version number, for example: <code>Cockpit/0.7.3</code> or <code>My App Name/1.0</code>
     * @param credentialsProvider
     * an implementation of the HttpClient CredentialsProvider interface, to provide a means for
     * prompting for credentials when necessary.
     *
     * @throws S3ServiceException
     */
    public RestS3Service(ProviderCredentials credentials, String invokingApplicationDescription,
        CredentialsProvider credentialsProvider) throws S3ServiceException
    {
        this(credentials, invokingApplicationDescription, credentialsProvider,
            Jets3tProperties.getInstance(Constants.JETS3T_PROPERTIES_FILENAME));
    }

    /**
     * Constructs the service and initialises the properties.
     *
     * @param credentials
     * the S3 user credentials to use when communicating with S3, may be null in which case the
     * communication is done as an anonymous user.
     * @param invokingApplicationDescription
     * a short description of the application using the service, suitable for inclusion in a
     * user agent string for REST/HTTP requests. Ideally this would include the application's
     * version number, for example: <code>Cockpit/0.7.3</code> or <code>My App Name/1.0</code>
     * @param credentialsProvider
     * an implementation of the HttpClient CredentialsProvider interface, to provide a means for
     * prompting for credentials when necessary.
     * @param jets3tProperties
     * JetS3t properties that will be applied within this service.
     *
     * @throws S3ServiceException
     */
    public RestS3Service(ProviderCredentials credentials, String invokingApplicationDescription,
        CredentialsProvider credentialsProvider, Jets3tProperties jets3tProperties)
        throws S3ServiceException
    {
        this(credentials, invokingApplicationDescription, credentialsProvider,
            jets3tProperties, new HostConfiguration());
    }

    /**
     * Constructs the service and initialises the properties.
     *
     * @param credentials
     * the S3 user credentials to use when communicating with S3, may be null in which case the
     * communication is done as an anonymous user.
     * @param invokingApplicationDescription
     * a short description of the application using the service, suitable for inclusion in a
     * user agent string for REST/HTTP requests. Ideally this would include the application's
     * version number, for example: <code>Cockpit/0.7.3</code> or <code>My App Name/1.0</code>
     * @param credentialsProvider
     * an implementation of the HttpClient CredentialsProvider interface, to provide a means for
     * prompting for credentials when necessary.
     * @param jets3tProperties
     * JetS3t properties that will be applied within this service.
     * @param hostConfig
     * Custom HTTP host configuration; e.g to register a custom Protocol Socket Factory
     *
     * @throws S3ServiceException
     */
    public RestS3Service(ProviderCredentials credentials, String invokingApplicationDescription,
        CredentialsProvider credentialsProvider, Jets3tProperties jets3tProperties,
        HostConfiguration hostConfig) throws S3ServiceException
    {
        super(credentials, invokingApplicationDescription, credentialsProvider, jets3tProperties, hostConfig);

        if (credentials instanceof AWSDevPayCredentials) {
            AWSDevPayCredentials awsDevPayCredentials = (AWSDevPayCredentials) credentials;
            this.awsDevPayUserToken = awsDevPayCredentials.getUserToken();
            this.awsDevPayProductToken = awsDevPayCredentials.getProductToken();
        } else {
            this.awsDevPayUserToken = jets3tProperties.getStringProperty("devpay.user-token", null);
            this.awsDevPayProductToken = jets3tProperties.getStringProperty("devpay.product-token", null);
        }

        this.setRequesterPaysEnabled(
            this.jets3tProperties.getBoolProperty("httpclient.requester-pays-buckets-enabled", false));
    }

    /**
     * Set the User Token value to use for requests to a DevPay S3 account.
     * The user token is not required for DevPay web products for which the
     * user token was created after 15th May 2008.
     *
     * @param userToken
     * the user token value provided by the AWS DevPay activation service.
     */
    public void setDevPayUserToken(String userToken) {
        this.awsDevPayUserToken = userToken;
    }

    /**
     * @return
     * the user token value to use in requests to a DevPay S3 account, or null
     * if no such token value has been set.
     */
    public String getDevPayUserToken() {
        return this.awsDevPayUserToken;
    }

    /**
     * Set the Product Token value to use for requests to a DevPay S3 account.
     *
     * @param productToken
     * the token that identifies your DevPay product.
     */
    public void setDevPayProductToken(String productToken) {
        this.awsDevPayProductToken = productToken;
    }

    /**
     * @return
     * the product token value to use in requests to a DevPay S3 account, or
     * null if no such token value has been set.
     */
    public String getDevPayProductToken() {
        return this.awsDevPayProductToken;
    }

    /**
     * Instruct the service whether to generate Requester Pays requests when
     * uploading data to S3, or retrieving data from the service. The default
     * value for the Requester Pays Enabled setting is set according to the
     * jets3t.properties setting
     * <code>httpclient.requester-pays-buckets-enabled</code>.
     *
     * @param isRequesterPays
     * if true, all subsequent S3 service requests will include the Requester
     * Pays flag.
     */
    public void setRequesterPaysEnabled(boolean isRequesterPays) {
        this.isRequesterPaysEnabled = isRequesterPays;
    }

    /**
     * Is this service configured to generate Requester Pays requests when
     * uploading data to S3, or retrieving data from the service. The default
     * value for the Requester Pays Enabled setting is set according to the
     * jets3t.properties setting
     * <code>httpclient.requester-pays-buckets-enabled</code>.
     *
     * @return
     * true if S3 service requests will include the Requester Pays flag, false
     * otherwise.
     */
    public boolean isRequesterPaysEnabled() {
        return this.isRequesterPaysEnabled;
    }

    /**
     * Creates an {@link org.apache.commons.httpclient.HttpMethod} object to handle a particular connection method.
     *
     * @param method
     *        the HTTP method/connection-type to use, must be one of: PUT, HEAD, GET, DELETE
     * @param bucketName
     *        the bucket's name
     * @param objectKey
     *        the object's key name, may be null if the operation is on a bucket only.
     * @return
     *        the HTTP method object used to perform the request
     *
     * @throws org.jets3t.service.S3ServiceException
     */
    @Override
    protected HttpMethodBase setupConnection(String method, String bucketName, String objectKey,
        Map<String, Object> requestParameters) throws S3ServiceException
    {
        HttpMethodBase httpMethod = super.setupConnection(method, bucketName, objectKey, requestParameters);

        // Set DevPay request headers.
        if (getDevPayUserToken() != null || getDevPayProductToken() != null) {
            // DevPay tokens have been provided, include these with the request.
            if (getDevPayProductToken() != null) {
                String securityToken = getDevPayUserToken() + "," + getDevPayProductToken();
                httpMethod.setRequestHeader(Constants.AMZ_SECURITY_TOKEN, securityToken);
                if (log.isDebugEnabled()) {
                    log.debug("Including DevPay user and product tokens in request: "
                        + Constants.AMZ_SECURITY_TOKEN + "=" + securityToken);
                }
            } else {
                httpMethod.setRequestHeader(Constants.AMZ_SECURITY_TOKEN, getDevPayUserToken());
                if (log.isDebugEnabled()) {
                    log.debug("Including DevPay user token in request: "
                        + Constants.AMZ_SECURITY_TOKEN + "=" + getDevPayUserToken());
                }
            }
        }

        // Set Requester Pays header to allow access to these buckets.
        if (this.isRequesterPaysEnabled()) {
            String[] requesterPaysHeaderAndValue = Constants.REQUESTER_PAYS_BUCKET_FLAG.split("=");
            httpMethod.setRequestHeader(requesterPaysHeaderAndValue[0], requesterPaysHeaderAndValue[1]);
            if (log.isDebugEnabled()) {
                log.debug("Including Requester Pays header in request: " +
                    Constants.REQUESTER_PAYS_BUCKET_FLAG);
            }
        }

        return httpMethod;
    }

    /**
     * @return
     * the endpoint to be used to connect to S3.
     */
    @Override
    public String getEndpoint() {
    	return this.jets3tProperties.getStringProperty(
                "s3service.s3-endpoint", Constants.S3_DEFAULT_HOSTNAME);
    }

    /**
     * @return
     * the virtual path inside the S3 server.
     */
    @Override
    protected String getVirtualPath() {
    	return this.jets3tProperties.getStringProperty(
                "s3service.s3-endpoint-virtual-path", "");
    }

    /**
     * @return
     * the identifier for the signature algorithm.
     */
    @Override
    protected String getSignatureIdentifier() {
    	return AWS_SIGNATURE_IDENTIFIER;
    }

    /**
     * @return
     * header prefix for general Amazon headers: x-amz-.
     */
    @Override
    public String getRestHeaderPrefix() {
    	return AWS_REST_HEADER_PREFIX;
    }

    /**
     * @return
     * header prefix for Amazon metadata headers: x-amz-meta-.
     */
    @Override
    public String getRestMetadataPrefix() {
    	return AWS_REST_METADATA_PREFIX;
    }

    /**
     * @return
     * the port number to be used for insecure connections over HTTP.
     */
    @Override
    protected int getHttpPort() {
      return this.jets3tProperties.getIntProperty("s3service.s3-endpoint-http-port", 80);
    }

    /**
     * @return
     * the port number to be used for secure connections over HTTPS.
     */
    @Override
    protected int getHttpsPort() {
      return this.jets3tProperties.getIntProperty("s3service.s3-endpoint-https-port", 443);
    }

    /**
     * @return
     * If true, all communication with S3 will be via encrypted HTTPS connections,
     * otherwise communications will be sent unencrypted via HTTP.
     */
    @Override
    protected boolean getHttpsOnly() {
      return this.jets3tProperties.getBoolProperty("s3service.https-only", true);
    }

    /**
     * @return
     * If true, JetS3t will specify bucket names in the request path of the HTTP message
     * instead of the Host header.
     */
    @Override
    protected boolean getDisableDnsBuckets() {
      return this.jets3tProperties.getBoolProperty("s3service.disable-dns-buckets", false);
    }

    /**
     * @return
     * If true, JetS3t will enable support for Storage Classes.
     */
    @Override
    protected boolean getEnableStorageClasses() {
      return this.jets3tProperties.getBoolProperty("s3service.enable-storage-classes", false);
    }

    @Override
    protected BaseVersionOrDeleteMarker[] listVersionedObjectsImpl(String bucketName,
        String prefix, String delimiter, String keyMarker, String versionMarker,
        long maxListingLength) throws S3ServiceException
    {
        return listVersionedObjectsInternal(bucketName, prefix, delimiter,
            maxListingLength, true, keyMarker, versionMarker).getItems();
    }

    @Override
    protected void updateBucketVersioningStatusImpl(String bucketName,
        boolean enabled, boolean multiFactorAuthDeleteEnabled,
        String multiFactorSerialNumber, String multiFactorAuthCode)
        throws S3ServiceException
    {
        if (log.isDebugEnabled()) {
            log.debug( (enabled ? "Enabling" : "Suspending")
                + " versioning for bucket " + bucketName
                + (multiFactorAuthDeleteEnabled ? " with Multi-Factor Auth enabled" : ""));
        }
        try {
            XMLBuilder builder = XMLBuilder
                .create("VersioningConfiguration").a("xmlns", Constants.XML_NAMESPACE)
                    .e("Status").t( (enabled ? "Enabled" : "Suspended") ).up()
                    .e("MfaDelete").t( (multiFactorAuthDeleteEnabled ? "Enabled" : "Disabled"));
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("versioning", null);
            Map<String, Object> metadata = new HashMap<String, Object>();
            if (multiFactorSerialNumber != null || multiFactorAuthCode != null) {
                metadata.put(Constants.AMZ_MULTI_FACTOR_AUTH_CODE,
                    multiFactorSerialNumber + " " + multiFactorAuthCode);
            }
            performRestPutWithXmlBuilder(bucketName, null, metadata, requestParams, builder);
        } catch (ParserConfigurationException e) {
            throw new S3ServiceException("Failed to build XML document for request", e);
        }
    }

    @Override
    protected S3BucketVersioningStatus getBucketVersioningStatusImpl(String bucketName)
        throws S3ServiceException
    {
        if (log.isDebugEnabled()) {
            log.debug( "Checking status of versioning for bucket " + bucketName);
        }
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("versioning", null);
        HttpMethodBase method = performRestGet(bucketName, null, requestParams, null);
        return getXmlResponseSaxParser()
            .parseVersioningConfigurationResponse(new HttpMethodReleaseInputStream(method));
    }

    protected VersionOrDeleteMarkersChunk listVersionedObjectsInternal(
        String bucketName, String prefix, String delimiter, long maxListingLength,
        boolean automaticallyMergeChunks, String nextKeyMarker, String nextVersionIdMarker)
        throws S3ServiceException
    {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("versions", null);
        if (prefix != null) {
            parameters.put("prefix", prefix);
        }
        if (delimiter != null) {
            parameters.put("delimiter", delimiter);
        }
        if (maxListingLength > 0) {
            parameters.put("max-keys", String.valueOf(maxListingLength));
        }

        List<BaseVersionOrDeleteMarker> items = new ArrayList<BaseVersionOrDeleteMarker>();
        List<String> commonPrefixes = new ArrayList<String>();

        boolean incompleteListing = true;
        int ioErrorRetryCount = 0;

        while (incompleteListing) {
            if (nextKeyMarker != null) {
                parameters.put("key-marker", nextKeyMarker);
            } else {
                parameters.remove("key-marker");
            }
            if (nextVersionIdMarker != null) {
                parameters.put("version-id-marker", nextVersionIdMarker);
            } else {
                parameters.remove("version-id-marker");
            }

            HttpMethodBase httpMethod = performRestGet(bucketName, null, parameters, null);
            ListVersionsResultsHandler handler = null;

            try {
                handler = getXmlResponseSaxParser()
                    .parseListVersionsResponse(
                        new HttpMethodReleaseInputStream(httpMethod));
                ioErrorRetryCount = 0;
            } catch (S3ServiceException e) {
                if (e.getCause() instanceof IOException && ioErrorRetryCount < 5) {
                    ioErrorRetryCount++;
                    if (log.isWarnEnabled()) {
                        log.warn("Retrying bucket listing failure due to IO error", e);
                    }
                    continue;
                } else {
                    throw e;
                }
            }

            BaseVersionOrDeleteMarker[] partialItems = handler.getItems();
            if (log.isDebugEnabled()) {
                log.debug("Found " + partialItems.length + " items in one batch");
            }
            items.addAll(Arrays.asList(partialItems));

            String[] partialCommonPrefixes = handler.getCommonPrefixes();
            if (log.isDebugEnabled()) {
                log.debug("Found " + partialCommonPrefixes.length + " common prefixes in one batch");
            }
            commonPrefixes.addAll(Arrays.asList(partialCommonPrefixes));

            incompleteListing = handler.isListingTruncated();
            nextKeyMarker = handler.getNextKeyMarker();
            nextVersionIdMarker = handler.getNextVersionIdMarker();
            if (incompleteListing) {
                if (log.isDebugEnabled()) {
                    log.debug("Yet to receive complete listing of bucket versions, "
                        + "continuing with key-marker=" + nextKeyMarker
                        + " and version-id-marker=" + nextVersionIdMarker);
                }
            }

            if (!automaticallyMergeChunks) {
                break;
            }
        }
        if (automaticallyMergeChunks) {
            if (log.isDebugEnabled()) {
                log.debug("Found " + items.size() + " items in total");
            }
            return new VersionOrDeleteMarkersChunk(
                prefix, delimiter,
                items.toArray(new BaseVersionOrDeleteMarker[items.size()]),
                commonPrefixes.toArray(new String[commonPrefixes.size()]),
                null, null);
        } else {
            return new VersionOrDeleteMarkersChunk(
                prefix, delimiter,
                items.toArray(new BaseVersionOrDeleteMarker[items.size()]),
                commonPrefixes.toArray(new String[commonPrefixes.size()]),
                nextKeyMarker, nextVersionIdMarker);
        }
    }

    @Override
    protected VersionOrDeleteMarkersChunk listVersionedObjectsChunkedImpl(String bucketName,
        String prefix, String delimiter, long maxListingLength, String priorLastKey,
        String priorLastVersion, boolean completeListing) throws S3ServiceException
    {
        return listVersionedObjectsInternal(bucketName, prefix, delimiter,
            maxListingLength, completeListing, priorLastKey, priorLastVersion);
    }

    @Override
    protected String getBucketLocationImpl(String bucketName)
        throws S3ServiceException
    {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving location of Bucket: " + bucketName);
        }

        Map<String, Object> requestParameters = new HashMap<String, Object>();
        requestParameters.put("location","");

        HttpMethodBase httpMethod = performRestGet(bucketName, null, requestParameters, null);
        return getXmlResponseSaxParser()
            .parseBucketLocationResponse(
                new HttpMethodReleaseInputStream(httpMethod));
    }

    @Override
    protected S3BucketLoggingStatus getBucketLoggingStatusImpl(String bucketName)
        throws S3ServiceException
    {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving Logging Status for Bucket: " + bucketName);
        }

        Map<String, Object> requestParameters = new HashMap<String, Object>();
        requestParameters.put("logging","");

        HttpMethodBase httpMethod = performRestGet(bucketName, null, requestParameters, null);
        return getXmlResponseSaxParser()
            .parseLoggingStatusResponse(
                new HttpMethodReleaseInputStream(httpMethod)).getBucketLoggingStatus();
    }

    @Override
    protected void setBucketLoggingStatusImpl(String bucketName, S3BucketLoggingStatus status)
        throws S3ServiceException
    {
        if (log.isDebugEnabled()) {
            log.debug("Setting Logging Status for bucket: " + bucketName);
        }

        Map<String, Object> requestParameters = new HashMap<String, Object>();
        requestParameters.put("logging","");

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("Content-Type", "text/plain");

        String statusAsXml = null;
        try {
            statusAsXml = status.toXml();
        } catch (Exception e) {
            throw new S3ServiceException("Unable to generate LoggingStatus XML document", e);
        }
        try {
            metadata.put("Content-Length", String.valueOf(statusAsXml.length()));
            performRestPut(bucketName, null, metadata, requestParameters,
                new StringRequestEntity(statusAsXml, "text/plain", Constants.DEFAULT_ENCODING),
                true);
        } catch (UnsupportedEncodingException e) {
            throw new S3ServiceException("Unable to encode LoggingStatus XML document", e);
        }
    }

    @Override
    protected boolean isRequesterPaysBucketImpl(String bucketName)
        throws S3ServiceException
    {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving Request Payment Configuration settings for Bucket: " + bucketName);
        }

        Map<String, Object> requestParameters = new HashMap<String, Object>();
        requestParameters.put("requestPayment","");

        HttpMethodBase httpMethod = performRestGet(bucketName, null, requestParameters, null);
        return getXmlResponseSaxParser()
            .parseRequestPaymentConfigurationResponse(
                new HttpMethodReleaseInputStream(httpMethod));
    }

    @Override
    protected void setRequesterPaysBucketImpl(String bucketName, boolean requesterPays) throws S3ServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Setting Request Payment Configuration settings for bucket: " + bucketName);
        }

        Map<String, Object> requestParameters = new HashMap<String, Object>();
        requestParameters.put("requestPayment","");

        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("Content-Type", "text/plain");

        try {
            String xml =
                "<RequestPaymentConfiguration xmlns=\"" + Constants.XML_NAMESPACE + "\">" +
                    "<Payer>" +
                        (requesterPays ? "Requester" : "BucketOwner") +
                    "</Payer>" +
                "</RequestPaymentConfiguration>";

            metadata.put("Content-Length", String.valueOf(xml.length()));
            performRestPut(bucketName, null, metadata, requestParameters,
                new StringRequestEntity(xml, "text/plain", Constants.DEFAULT_ENCODING),
                true);
        } catch (UnsupportedEncodingException e) {
            throw new S3ServiceException("Unable to encode RequestPaymentConfiguration XML document", e);
        }
    }

}
