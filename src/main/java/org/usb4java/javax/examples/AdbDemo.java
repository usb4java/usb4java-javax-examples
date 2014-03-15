/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information. 
 */

package org.usb4java.javax.examples;

import java.util.List;

import org.usb4java.javax.examples.adb.Adb;
import org.usb4java.javax.examples.adb.AuthMessage;
import org.usb4java.javax.examples.adb.CloseMessage;
import org.usb4java.javax.examples.adb.ConnectMessage;
import org.usb4java.javax.examples.adb.AdbDevice;
import org.usb4java.javax.examples.adb.Message;
import org.usb4java.javax.examples.adb.OkayMessage;
import org.usb4java.javax.examples.adb.OpenMessage;

/**
 * A simple ADB (Android Debug Bridge Demo). This demo sends and receives
 * some ADB commands to/from the first connected Android device. This demo
 * does not create the RSA key pair needed to communicate with the device but
 * it uses the key pair which the real ADB tool already placed in 
 * $HOME/.android/. You also must make sure the ADB daemon is not running
 * because it blocks the USB device.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class AdbDemo
{
    /**
     * Main method.
     * 
     * @param args
     *            Command-line arguments (ignored)
     * @throws Exception
     *             When something goes wrong.
     */
    public static void main(String[] args) throws Exception
    {
        // Find a ADB device to communicate with.
        List<AdbDevice> devices = Adb.findDevices();
        if (devices.isEmpty())
        {
            System.err.println("No ADB devices found");
            System.exit(1);
            return;
        }
        AdbDevice device = devices.get(0);

        // Do some ADB communication
        device.open();
        try
        {
            // Send the connect message
            Message message = new ConnectMessage(
                ConnectMessage.SYSTEM_TYPE_HOST, "12345678", "ADB Demo");
            System.out.println("Sending: " + message);
            device.sendMessage(message);

            // Repeat until we are connected
            boolean triedAuthentication = false;
            boolean sentPublicKey = false;
            boolean connected = false;
            while (!connected)
            {
                message = device.receiveMessage();
                System.out.println("Received: " + message);

                // If connect message has been received then we are finished
                if (message instanceof ConnectMessage)
                {
                    connected = true;
                }

                // Process auth message
                else if (message instanceof AuthMessage)
                {
                    AuthMessage authMessage = (AuthMessage) message;

                    // Sign token if we didn't tried it already
                    if (!triedAuthentication)
                    {
                        byte[] signature = Adb.signToken(authMessage.getData());
                        message =
                            new AuthMessage(AuthMessage.TYPE_SIGNATURE,
                                signature);
                        System.out.println("Sending: " + message);
                        device.sendMessage(message);
                        triedAuthentication = true;
                    }

                    // If token signing already failed then sent public key
                    else if (!sentPublicKey)
                    {
                        byte[] publicKey = Adb.getPublicKey();
                        message =
                            new AuthMessage(AuthMessage.TYPE_RSAPUBLICKEY,
                                publicKey);
                        System.out.println("Sending: " + message);
                        device.sendMessage(message);
                        triedAuthentication = false;
                        sentPublicKey = true;
                    }

                    // Can't authenticate for some reason
                    else
                    {
                        System.err.println("Couldn't authenticate");
                        System.exit(1);
                    }
                }

                // Unknown message received
                else
                {
                    System.err.println("Received unexpected message: "
                        + message);
                    System.exit(1);
                }
            }

            // Open "sync:"
            message = new OpenMessage(1, "sync:");
            System.out.println("Sending: " + message);
            device.sendMessage(message);
            message = device.receiveMessage();
            System.out.println("Received: " + message);
            if (!(message instanceof OkayMessage))
            {
                System.err.println("Open failed");
                System.exit(1);
            }
            int remoteId = ((OkayMessage) message).getRemoteId();

            // Close
            message = new CloseMessage(1, remoteId);
            System.out.println("Sending: " + message);
            device.sendMessage(message);
            message = device.receiveMessage();
            System.out.println("Received: " + message);
        }
        finally
        {
            device.close();
        }
    }
}
