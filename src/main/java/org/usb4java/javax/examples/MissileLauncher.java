/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt for licensing information.
 */

package org.usb4java.javax.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;

/**
 * Controls a USB missile launcher (Only compatible with Vendor/Product
 * 1130:0202).
 * 
 * @author Klaus Reimer <k@ailis.de>
 */
public class MissileLauncher
{
    /** The vendor ID of the missile launcher. */
    private static final short VENDOR_ID = 0x1130;

    /** The product ID of the missile launcher. */
    private static final short PRODUCT_ID = 0x0202;

    /** First init packet to send to the missile launcher. */
    private static final byte[] INIT_A = new byte[] { 85, 83, 66, 67, 0, 0, 4,
        0 };

    /** Second init packet to send to the missile launcher. */
    private static final byte[] INIT_B = new byte[] { 85, 83, 66, 67, 0, 64, 2,
        0 };

    /** Command to rotate the launcher up. */
    private static final int CMD_UP = 0x01;

    /** Command to rotate the launcher down. */
    private static final int CMD_DOWN = 0x02;

    /** Command to rotate the launcher to the left. */
    private static final int CMD_LEFT = 0x04;

    /** Command to rotate the launcher to the right. */
    private static final int CMD_RIGHT = 0x08;

    /** Command to fire a missile. */
    private static final int CMD_FIRE = 0x10;

    /**
     * Recursively searches for the missile launcher device on the specified USB
     * hub and returns it. If there are multiple missile launchers attached then
     * this simple demo only returns the first one.
     * 
     * @param hub
     *            The USB hub to search on.
     * @return The missile launcher USB device or null if not found.
     */
    public static UsbDevice findMissileLauncher(UsbHub hub)
    {
        UsbDevice launcher = null;

        for (UsbDevice device: (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            if (device.isUsbHub())
            {
                launcher = findMissileLauncher((UsbHub) device);
                if (launcher != null) return launcher;
            }
            else
            {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
                if (desc.idVendor() == VENDOR_ID &&
                    desc.idProduct() == PRODUCT_ID) return device;
            }
        }
        return null;
    }

    /**
     * Sends a message to the missile launcher.
     * 
     * @param device
     *            The USB device handle.
     * @param message
     *            The message to send.
     * @throws UsbException
     *             When sending the message failed.
     */
    public static void sendMessage(UsbDevice device, byte[] message)
        throws UsbException
    {
        UsbControlIrp irp = device.createUsbControlIrp(
            (byte) (UsbConst.REQUESTTYPE_TYPE_CLASS |
            UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE), (byte) 0x09,
            (short) 2, (short) 1);
        irp.setData(message);
        device.syncSubmit(irp);
    }

    /**
     * Sends a command to the missile launcher.
     * 
     * @param device
     *            The USB device handle.
     * @param command
     *            The command to send.
     * @throws UsbException
     *             When USB communication failed.
     */
    public static void sendCommand(UsbDevice device, int command)
        throws UsbException
    {
        byte[] message = new byte[64];
        message[1] = (byte) ((command & CMD_LEFT) > 0 ? 1 : 0);
        message[2] = (byte) ((command & CMD_RIGHT) > 0 ? 1 : 0);
        message[3] = (byte) ((command & CMD_UP) > 0 ? 1 : 0);
        message[4] = (byte) ((command & CMD_DOWN) > 0 ? 1 : 0);
        message[5] = (byte) ((command & CMD_FIRE) > 0 ? 1 : 0);
        message[6] = 8;
        message[7] = 8;
        sendMessage(device, INIT_A);
        sendMessage(device, INIT_B);
        sendMessage(device, message);
    }

    /**
     * Read a key from stdin and returns it.
     * 
     * @return The read key.
     */
    public static char readKey()
    {
        try
        {
            String line =
                new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (line.length() > 0) return line.charAt(0);
            return 0;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to read key", e);
        }
    }

    /**
     * Main method.
     * 
     * @param args
     *            Command-line arguments (Ignored)
     * @throws UsbException
     *             When an USB error was reported which wasn't handled by this
     *             program itself.
     */
    public static void main(String[] args) throws UsbException
    {
        // Search for the missile launcher USB device and stop when not found
        UsbDevice device = findMissileLauncher(
            UsbHostManager.getUsbServices().getRootUsbHub());
        if (device == null)
        {
            System.err.println("Missile launcher not found.");
            System.exit(1);
            return;
        }

        // Claim the interface
        UsbConfiguration configuration = device.getUsbConfiguration((byte) 1);
        UsbInterface iface = configuration.getUsbInterface((byte) 1);
        iface.claim(new UsbInterfacePolicy()
        {            
            @Override
            public boolean forceClaim(UsbInterface usbInterface)
            {
                return true;
            }
        });

        // Read commands and execute them
        System.out.println("WADX = Move, S = Stop, F = Fire, Q = Exit");
        boolean exit = false;
        while (!exit)
        {
            System.out.print("> ");
            char key = readKey();
            switch (key)
            {
                case 'w':
                    sendCommand(device, CMD_UP);
                    break;

                case 'x':
                    sendCommand(device, CMD_DOWN);
                    break;

                case 'a':
                    sendCommand(device, CMD_LEFT);
                    break;

                case 'd':
                    sendCommand(device, CMD_RIGHT);
                    break;

                case 'f':
                    sendCommand(device, CMD_FIRE);
                    break;

                case 's':
                    sendCommand(device, 0);
                    break;

                case 'q':
                    exit = true;
                    break;

                default:
            }
        }
        System.out.println("Exiting");
    }
}
