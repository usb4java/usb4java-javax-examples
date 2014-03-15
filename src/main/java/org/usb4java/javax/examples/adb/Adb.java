/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information. 
 */

package org.usb4java.javax.examples.adb;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbEndpoint;
import javax.usb.UsbEndpointDescriptor;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfaceDescriptor;
import javax.usb.UsbServices;
import javax.xml.bind.DatatypeConverter;

/**
 * Some static helper methods for ADB.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class Adb
{
    /** Constant for ADB class. */
    private static final byte ADB_CLASS = (byte) 0xff;

    /** Constant for ADB sub class. */
    private static final byte ADB_SUBCLASS = 0x42;

    /** Constant for ADB protocol. */
    private static final byte ADB_PROTOCOL = 1;

    /** Header for token signing. */
    private static byte[] headerOID = new byte[] {
        0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05,
        0x00, 0x04, 0x14
    };

    /**
     * Returns the list of all available ADB devices.
     * 
     * @return The list of available ADB devices.
     * @throws UsbException
     *             When USB communication failed.
     */
    public static List<AdbDevice> findDevices() throws UsbException
    {
        UsbServices services = UsbHostManager.getUsbServices();
        List<AdbDevice> usbDevices = new ArrayList<AdbDevice>();
        findDevices(services.getRootUsbHub(), usbDevices);
        return usbDevices;
    }

    /**
     * Recursively scans the specified USB hub for ADB devices and puts them
     * into the list.
     * 
     * @param hub
     *            The USB hub to scan recursively.
     * @param devices
     *            The list where to add found devices.
     */
    private static void findDevices(UsbHub hub, List<AdbDevice> devices)
    {
        for (UsbDevice usbDevice: (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            if (usbDevice.isUsbHub())
            {
                findDevices((UsbHub) usbDevice, devices);
            }
            else
            {
                checkDevice(usbDevice, devices);
            }
        }
    }

    /**
     * Checks if the specified USB device is a ADB device and adds it to the
     * list if it is.
     * 
     * @param usbDevice
     *            The USB device to check.
     * @param adbDevices
     *            The list of ADB devices to add the USB device to when it is an
     *            ADB device.
     */
    private static void checkDevice(UsbDevice usbDevice,
        List<AdbDevice> adbDevices)
    {
        UsbDeviceDescriptor deviceDesc = usbDevice.getUsbDeviceDescriptor();

        // Ignore devices from Non-ADB vendors
        if (!isAdbVendor(deviceDesc.idVendor())) return;

        // Check interfaces of device
        UsbConfiguration config = usbDevice.getActiveUsbConfiguration();
        for (UsbInterface iface: (List<UsbInterface>) config.getUsbInterfaces())
        {
            List<UsbEndpoint> endpoints = iface.getUsbEndpoints();

            // Ignore interface if it does not have two endpoints
            if (endpoints.size() != 2) continue;

            // Ignore interface if it does not match the ADB specs
            if (!isAdbInterface(iface)) continue;

            UsbEndpointDescriptor ed1 =
                endpoints.get(0).getUsbEndpointDescriptor();
            UsbEndpointDescriptor ed2 =
                endpoints.get(1).getUsbEndpointDescriptor();

            // Ignore interface if endpoints are not bulk endpoints
            if (((ed1.bmAttributes() & UsbConst.ENDPOINT_TYPE_BULK) == 0) ||
                ((ed2.bmAttributes() & UsbConst.ENDPOINT_TYPE_BULK) == 0))
                continue;
            
            // Determine which endpoint is in and which is out. If both
            // endpoints are in or out then ignore the interface
            byte a1 = ed1.bEndpointAddress();
            byte a2 = ed2.bEndpointAddress();
            byte in, out;
            if (((a1 & UsbConst.ENDPOINT_DIRECTION_IN) != 0) &&
                ((a2 & UsbConst.ENDPOINT_DIRECTION_IN) == 0))
            {
                in = a1;
                out = a2;
            }
            else if (((a2 & UsbConst.ENDPOINT_DIRECTION_IN) != 0) &&
                ((a1 & UsbConst.ENDPOINT_DIRECTION_IN) == 0))
            {
                out = a1;
                in = a2;
            }
            else continue;                
            
            // Create ADB device and add it to the list
            AdbDevice adbDevice = new AdbDevice(iface, in, out);
            adbDevices.add(adbDevice);
        }
    }

    /**
     * Checks if the specified vendor is an ADB device vendor.
     * 
     * @param vendorId
     *            The vendor ID to check.
     * @return True if ADB device vendor, false if not.
     */
    private static boolean isAdbVendor(short vendorId)
    {
        for (short adbVendorId: Vendors.VENDOR_IDS)
            if (adbVendorId == vendorId) return true;
        return false;
    }

    /**
     * Checks if the specified USB interface is an ADB interface.
     * 
     * @param iface
     *            The interface to check.
     * @return True if interface is an ADB interface, false if not.
     */
    private static boolean isAdbInterface(UsbInterface iface)
    {
        UsbInterfaceDescriptor desc = iface.getUsbInterfaceDescriptor();
        return desc.bInterfaceClass() == ADB_CLASS &&
            desc.bInterfaceSubClass() == ADB_SUBCLASS &&
            desc.bInterfaceProtocol() == ADB_PROTOCOL;
    }

    /**
     * Returns the private ADB key.
     * 
     * @return The private ADB key.
     * @throws IOException
     *             When key file could not be read.
     * @throws GeneralSecurityException
     *             When private key could not be parsed.
     */
    public static RSAPrivateKey getPrivateKey() throws IOException,
        GeneralSecurityException
    {
        File file =
            new File(System.getProperty("user.home"), ".android/adbkey");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try
        {
            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while (line != null)
            {
                if (!line.startsWith("----")) builder.append(line);
                line = reader.readLine();
            }
            byte[] bytes =
                DatatypeConverter.parseBase64Binary(builder.toString());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
            return (RSAPrivateKey) keyFactory.generatePrivate(ks);
        }
        finally
        {
            reader.close();
        }
    }

    /**
     * Returns the public ADB key.
     * 
     * @return The public ADB key.
     * @throws IOException
     *             When key file could not be read.
     */
    public static byte[] getPublicKey() throws IOException
    {
        File file =
            new File(System.getProperty("user.home"), ".android/adbkey.pub");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
        out.write(0);
        in.close();
        out.close();
        return out.toByteArray();
    }

    /**
     * Signs the specified token and returns the signature.
     * 
     * @param token
     *            The token to sign.
     * @return The signature.
     * @throws IOException
     *             When private key could not be read
     * @throws GeneralSecurityException
     *             When private key could not be parsed or token could not be
     *             signed.
     */
    public static byte[] signToken(byte[] token) throws IOException,
        GeneralSecurityException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(headerOID);
        stream.write(token);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey());
        return cipher.doFinal(stream.toByteArray());
    }
}
