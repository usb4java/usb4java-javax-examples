/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information. 
 */

package org.usb4java.javax.examples.adb;

import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbPipe;

/**
 * ADB device.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public final class Device
{
    /** The USB device. Never null. */
    private final UsbDevice usbDevice;

    /** The claimed USB ADB interface. Null if not claimed. */
    private UsbInterface iface;

    /**
     * Constructs a new ADB device.
     * 
     * @param usbDevice
     *            The USB device. Must not be null.
     */
    public Device(final UsbDevice usbDevice)
    {
        if (usbDevice == null)
            throw new IllegalArgumentException("device must be set");
        this.usbDevice = usbDevice;
    }

    /**
     * Opens the ADB device. This claims the USB interface for ADB
     * communication. When you are finished communicating with the device then
     * you must call the {@link #close()} method.
     * 
     * @throws UsbException
     *             When USB communication failed.
     */
    public void open() throws UsbException
    {
        if (this.iface != null)
            throw new IllegalStateException("ADB device already open");
        final UsbConfiguration config =
            this.usbDevice.getUsbConfiguration((byte) 1);
        final UsbInterface iface = config.getUsbInterface((byte) 1);
        iface.claim();
        this.iface = iface;
    }

    /**
     * Returns the claimed ADB interface.
     * 
     * @return The claimed ADB interface. Never null.
     */
    private UsbInterface getInterface()
    {
        if (this.iface == null)
            throw new IllegalStateException("ADB device not open");
        return this.iface;
    }

    /**
     * Closes the ADB device. This releases the USB interface.
     * 
     * @throws UsbException
     *             When USB communication failed.
     */
    public void close() throws UsbException
    {
        final UsbInterface iface = getInterface();
        iface.release();
        this.iface = null;
    }

    /**
     * Sends an ADB Message.
     * 
     * @param message
     *            The message to send.
     * @throws UsbException
     *             When USB communication failed.
     */
    public void sendMessage(final Message message) throws UsbException
    {
        final UsbInterface iface = getInterface();
        final UsbEndpoint outEndpoint = iface.getUsbEndpoint((byte) 0x03);
        final UsbPipe outPipe = outEndpoint.getUsbPipe();
        final MessageHeader header = message.getHeader();
        outPipe.open();
        try
        {
            int sent = outPipe.syncSubmit(header.getBytes());
            if (sent != MessageHeader.SIZE)
                throw new InvalidMessageException(
                    "Invalid ADB message header size sent: " + sent);
            sent = outPipe.syncSubmit(message.getData());
            if (sent != header.getDataLength())
                throw new InvalidMessageException(
                    "Data size mismatch in sent ADB message. Should be "
                        + header.getDataLength() + " but is " + sent);
        }
        finally
        {
            outPipe.close();
        }
    }

    /**
     * Receives an ADB message.
     * 
     * @return The received ADB message.
     * @throws UsbException
     *             When USB communication failed.
     */
    public Message receiveMessage() throws UsbException
    {
        final UsbInterface iface = getInterface();
        final UsbEndpoint inEndpoint = iface.getUsbEndpoint((byte) 0x83);
        final UsbPipe inPipe = inEndpoint.getUsbPipe();
        inPipe.open();
        try
        {
            byte[] headerBytes = new byte[MessageHeader.SIZE];
            int received = inPipe.syncSubmit(headerBytes);
            if (received != MessageHeader.SIZE)
                throw new InvalidMessageException(
                    "Invalid ADB message header size: " + received);
            MessageHeader header = new MessageHeader(headerBytes);
            if (!header.isValid())
                throw new InvalidMessageException(
                    "ADB message header checksum failure");
            byte[] data = new byte[header.getDataLength()];
            received = inPipe.syncSubmit(data);
            if (received != header.getDataLength())
                throw new InvalidMessageException(
                    "ADB message data size mismatch. Should be "
                        + header.getDataLength() + " but is " + received);
            final Message message = Message.create(header, data);
            if (!message.isValid())
                throw new InvalidMessageException(
                    "ADB message data checksum failure");
            return message;
        }
        finally
        {
            inPipe.close();
        }
    }
}
