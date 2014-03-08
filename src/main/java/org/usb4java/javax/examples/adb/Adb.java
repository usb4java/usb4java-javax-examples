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
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;
import javax.xml.bind.DatatypeConverter;

/**
 * Some static helper methods for ADB.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class Adb
{
    /** The vendor ID of ADB devices. */
    private static final int VENDOR_ID = 0x04e8;

    /** The product ID of ADB devices. */
    private static final int PRODUCT_ID = 0x6860;

    /** Header for token signing. */
    private static byte[] headerOID = new byte[] {
        0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05,
        0x00, 0x04, 0x14
    };

    /**
     * Returns the list of all available Android devices.
     * 
     * @return The list of available Android devices.
     * @throws UsbException
     *             When USB communication failed.
     */
    public static List<Device> findDevices() throws UsbException
    {
        UsbServices services = UsbHostManager.getUsbServices();
        List<Device> usbDevices = new ArrayList<Device>();
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
    private static void findDevices(UsbHub hub, List<Device> devices)
    {
        for (UsbDevice usbDevice: (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = usbDevice.getUsbDeviceDescriptor();
            if (desc.idVendor() == VENDOR_ID && desc.idProduct() == PRODUCT_ID)
            {
                devices.add(new Device(usbDevice));
            }
            if (usbDevice.isUsbHub())
            {
                findDevices((UsbHub) usbDevice, devices);
            }
        }
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
        final File file =
            new File(System.getProperty("user.home"), ".android/adbkey");
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        try
        {
            final StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while (line != null)
            {
                if (!line.startsWith("----")) builder.append(line);
                line = reader.readLine();
            }
            final byte[] bytes =
                DatatypeConverter.parseBase64Binary(builder.toString());
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            final PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
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
        final File file =
            new File(System.getProperty("user.home"), ".android/adbkey.pub");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final InputStream in = new FileInputStream(file);
        final byte[] buffer = new byte[8192];
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
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(headerOID);
        stream.write(token);
        final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey());
        return cipher.doFinal(stream.toByteArray());
    }
}
