/*
 * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
 * This is a java.net project, see https://jets3t.dev.java.net/
 * 
 * Copyright 2009 James Murty
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
package org.jets3t.service.mx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

public class MxDelegate implements MxInterface {
    private static final Log log = LogFactory.getLog(MxDelegate.class);
    
    private MxInterface handler = null; 
    
    private static MxDelegate instance = null;
    
    
    public static MxDelegate getInstance() {
        if (instance == null) {
            instance = new MxDelegate();
        }
        return instance;
    }

    protected MxDelegate() {
    }
    
    /**
     * Initialize, or reinitialize, the JMX instrumentation support in JetS3t. This method 
     * <strong>must</strong> be invoked at least once within a JVM for the JMX instrumentation 
     * to work. When JetS3t's instrumentation is enabled Service and Exception events are always
     * logged, whereas Bucket and Object event logging must be specifically enabled. 
     * <p>
     * This method checks the given properties for the following System properties:
     * <table>
     * <tr><th>Property</th><th>Effect</th></tr>
     * <tr><td><tt>com.sun.management.jmxremote</tt></td>
     *     <td>If present, enable JMX instrumentation for JetS3t for Java 1.5. On Java 1.5 this 
     *     System setting is required to enable JMX in general, and if it is present then
     *     we automatically enable instrumentation for JetS3t as well.</td></tr>
     * <tr><td><tt>jets3t.mx</tt></td>
     *     <td>If present, enable JMX instrumentation for JetS3t for Java 1.6+. Because Java 
     *     1.6+ no longer requires the "com.sun.management.jmxremote" System setting for JMX to 
     *     be enabled in general, this property can be used as a substitute that allows 
     *     users to decide whether JetS3t's JMX instrumentation should be turned on or off.
     *     </td></tr>
     * </table>
     */
    public void init() {
        if (System.getProperty("com.sun.management.jmxremote") == null
			&& System.getProperty("jets3t.mx") == null) 
        {
        	this.handler = null;
            return;
        }
        
        try {
            // Load the contribs.mx.MxImpl implementation class, if available.
            Class impl = Class.forName("contribs.mx.MxImpl");
            this.handler = (MxInterface) impl.newInstance();
        } catch (ClassNotFoundException e) {
            log.error(
                "JMX instrumentation package 'contribs.mx' could not be found, "
                + " instrumentation will not available", e);
        } catch (RuntimeException e) {
        	throw e;
        } catch (Exception e) {
            log.error(
                "JMX instrumentation implementation in package 'contribs.mx' "
                + " could not be loaded", e);                
        }            
    }
    
    /**
     * @return
     * true if the JetS3t's JMX delegate has been initialized and activated.
     */
    public boolean isJmxDelegationActive() {
    	return this.handler != null;
    }
    
    public void registerS3ServiceMBean() {
        if (handler != null) {
            handler.registerS3ServiceMBean();
        }
    }
    
    public void registerS3ServiceExceptionMBean() {
        if (handler != null) {
            handler.registerS3ServiceExceptionMBean();
        }
    }
    
    public void registerS3ServiceExceptionEvent() {
        if (handler != null) {
            handler.registerS3ServiceExceptionEvent();
        }
    }

    public void registerS3ServiceExceptionEvent(String s3ErrorCode) {
        if (handler != null) {
            handler.registerS3ServiceExceptionEvent(s3ErrorCode);
        }
    }
    
    public void registerS3BucketMBeans(S3Bucket[] buckets) {
        if (handler != null) {
            handler.registerS3BucketMBeans(buckets);
        }
    }
    
    public void registerS3BucketListEvent(String bucketName) {
        if (handler != null) {
            handler.registerS3BucketListEvent(bucketName);
        }
    }

    public void registerS3ObjectMBean(String bucketName, S3Object[] objects) {
        if (handler != null) {
            handler.registerS3ObjectMBean(bucketName, objects);
        }
    }

    public void registerS3ObjectPutEvent(String bucketName, String key) {
        if (handler != null) {
            handler.registerS3ObjectPutEvent(bucketName, key);
        }
    }

    public void registerS3ObjectGetEvent(String bucketName, String key) {
        if (handler != null) {
            handler.registerS3ObjectGetEvent(bucketName, key);
        }
    }

    public void registerS3ObjectHeadEvent(String bucketName, String key) {
        if (handler != null) {
            handler.registerS3ObjectHeadEvent(bucketName, key);
        }
    }

    public void registerS3ObjectDeleteEvent(String bucketName, String key) {
        if (handler != null) {
            handler.registerS3ObjectDeleteEvent(bucketName, key);
        }
    }

    public void registerS3ObjectCopyEvent(String bucketName, String key) {
        if (handler != null) {
            handler.registerS3ObjectCopyEvent(bucketName, key);
        }
    }
    
}
