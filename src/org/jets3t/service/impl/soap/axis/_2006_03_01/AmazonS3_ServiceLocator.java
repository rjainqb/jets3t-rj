/**
 * AmazonS3_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package org.jets3t.service.impl.soap.axis._2006_03_01;

public class AmazonS3_ServiceLocator extends org.apache.axis.client.Service implements org.jets3t.service.impl.soap.axis._2006_03_01.AmazonS3_Service {

    public AmazonS3_ServiceLocator() {
    }


    public AmazonS3_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AmazonS3_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AmazonS3
    private java.lang.String AmazonS3_address = "https://s3.amazonaws.com/soap";

    public java.lang.String getAmazonS3Address() {
        return AmazonS3_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AmazonS3WSDDServiceName = "AmazonS3";

    public java.lang.String getAmazonS3WSDDServiceName() {
        return AmazonS3WSDDServiceName;
    }

    public void setAmazonS3WSDDServiceName(java.lang.String name) {
        AmazonS3WSDDServiceName = name;
    }

    public org.jets3t.service.impl.soap.axis._2006_03_01.AmazonS3_PortType getAmazonS3() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AmazonS3_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAmazonS3(endpoint);
    }

    public org.jets3t.service.impl.soap.axis._2006_03_01.AmazonS3_PortType getAmazonS3(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.jets3t.service.impl.soap.axis._2006_03_01.AmazonS3SoapBindingStub _stub = new org.jets3t.service.impl.soap.axis._2006_03_01.AmazonS3SoapBindingStub(portAddress, this);
            _stub.setPortName(getAmazonS3WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAmazonS3EndpointAddress(java.lang.String address) {
        AmazonS3_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.jets3t.service.impl.soap.axis._2006_03_01.AmazonS3_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.jets3t.service.impl.soap.axis._2006_03_01.AmazonS3SoapBindingStub _stub = new org.jets3t.service.impl.soap.axis._2006_03_01.AmazonS3SoapBindingStub(new java.net.URL(AmazonS3_address), this);
                _stub.setPortName(getAmazonS3WSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("AmazonS3".equals(inputPortName)) {
            return getAmazonS3();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://s3.amazonaws.com/doc/2006-03-01/", "AmazonS3");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://s3.amazonaws.com/doc/2006-03-01/", "AmazonS3"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("AmazonS3".equals(portName)) {
            setAmazonS3EndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
