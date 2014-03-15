/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package org.usb4java.javax.examples.adb;

import java.nio.charset.Charset;

/**
 * ADB connect message.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class ConnectMessage extends Message
{
    /** Constant for default protocol version. */
    public static final int DEFAULT_PROTOCOL_VERSION = 0x01000000;

    /** Constant for default maximum message body size. */
    public static final int DEFAULT_MAX_DATA = 4096;

    /** Constant for system type "bootloader". */
    public static final String SYSTEM_TYPE_BOOTLOADER = "bootloader";

    /** Constant for system type "device". */
    public static final String SYSTEM_TYPE_DEVICE = "device";

    /** Constant for system type "host". */
    public static final String SYSTEM_TYPE_HOST = "host";

    /**
     * Constructs a new connect message.
     * 
     * @param header
     *            The ADB message header.
     * @param data
     *            The ADB message data.
     */
    public ConnectMessage(MessageHeader header, byte[] data)
    {
        super(header, data);
    }

    /**
     * Constructs a new connect message.
     * 
     * @param version
     *            The protocol version.
     * @param maxData
     *            The maximum message body size.
     * @param systemType
     *            The system type. Must be either
     *            {@link #SYSTEM_TYPE_BOOTLOADER}, {@link #SYSTEM_TYPE_DEVICE}
     *            or {@link #SYSTEM_TYPE_HOST}.
     * @param serialNo
     *            The serial number. A unique ID or empty.
     * @param banner
     *            The banner. A human-readable version or identifier string.
     */
    public ConnectMessage(int version, int maxData, String systemType, 
        String serialNo, String banner)
    {
        this(version, maxData, buildIdentity(systemType, serialNo, banner));
    }

    /**
     * Constructs a new connect message.
     * 
     * @param version
     *            The protocol version.
     * @param maxData
     *            The maximum message body size.
     * @param identity
     *            The identity as a UTF-8 encoded character array.
     */
    public ConnectMessage(int version, int maxData, byte[] identity)
    {
        super(MessageHeader.CMD_CNXN, version, maxData, identity);
    }

    /**
     * Constructs a new connect message.
     * 
     * @param systemType
     *            The system type. Must be either
     *            {@link #SYSTEM_TYPE_BOOTLOADER}, {@link #SYSTEM_TYPE_DEVICE}
     *            or {@link #SYSTEM_TYPE_HOST}.
     * @param serialNo
     *            The serial number. A unique ID or empty.
     * @param banner
     *            The banner. A human-readable version or identifier string.
     */
    public ConnectMessage(String systemType, String serialNo, String banner)
    {
        this(DEFAULT_PROTOCOL_VERSION, DEFAULT_MAX_DATA, systemType, serialNo,
            banner);
    }

    /**
     * Builds and returns the identity payload.
     * 
     * @param systemType
     *            The system type.
     * @param serialNo
     *            The serial number. A unique ID or empty.
     * @param banner
     *            The banner. A human-readable version or identifier string.
     * @return The identity payload.
     */
    private static byte[] buildIdentity(String systemType, String serialNo, 
        String banner)
    {
        if (systemType == null)
            throw new IllegalArgumentException("systemType must be set");
        if (serialNo == null)
            throw new IllegalArgumentException("serialNo must be set");
        if (banner == null)
            throw new IllegalArgumentException("banner must be set");
        return (systemType + ":" + serialNo + ":" + banner + '\0')
            .getBytes(Charset.forName("UTF-8"));
    }

    /**
     * Returns the protocol version.
     * 
     * @return The protocol version.
     */
    public int getVersion()
    {
        return this.header.getArg0();
    }

    /**
     * Returns the maximum message body size the remote is willing to accept.
     * 
     * @return The maximum message body size.
     */
    public int getMaxData()
    {
        return this.header.getArg1();
    }

    /**
     * Returns the system identity string.
     * 
     * @return The system identity string.
     */
    public String getIdentity()
    {
        int len = this.data.length;
        while (len > 0 && this.data[len - 1] == 0) len--;
        return new String(this.data, 0, len, Charset.forName("UTF-8"));
    }

    /**
     * Returns the system type.
     * 
     * @return The system type.
     */
    public String getSystemType()
    {
        return getIdentity().split(":")[0];
    }

    /**
     * Returns the serial number.
     * 
     * @return The serial number.
     */
    public String getSerialNo()
    {
        return getIdentity().split(":")[1];
    }

    /**
     * Returns the banner.
     * 
     * @return The banner.
     */
    public String getBanner()
    {
        return getIdentity().split(":")[2];
    }

    @Override
    public String toString()
    {
        return String.format("CONNECT(0x%08x, %d, \"%s\")",
            getVersion(), getMaxData(), getIdentity()); 
    }
}
