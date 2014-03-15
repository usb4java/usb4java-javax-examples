/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package org.usb4java.javax.examples.adb;

import javax.xml.bind.DatatypeConverter;

/**
 * ADB AUTH message.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class AuthMessage extends Message
{
    /** The auth message transmits a authentification token. */
    public static final int TYPE_TOKEN = 1;

    /** The auth message transmits a signature. */
    public static final int TYPE_SIGNATURE = 2;

    /** The auth message transmits a public key. */
    public static final int TYPE_RSAPUBLICKEY = 3;

    /**
     * Constructs a new AUTH message.
     * 
     * @param header
     *            The ADB message header.
     * @param data
     *            The ADB message data.
     */
    public AuthMessage(MessageHeader header, byte[] data)
    {
        super(header, data);
    }
    
    /**
     * Constructs a new AUTH message.
     * 
     * @param type
     *            The auth message type. Can be {@link #TYPE_TOKEN},
     *            {@link #TYPE_SIGNATURE} or {@link #TYPE_RSAPUBLICKEY}.
     * @param data
     *            The auth data as a UTF-8 encoded character array. Can be a
     *            token, a signature or a public key. Depends on the auth
     *            message type.
     */
    public AuthMessage(int type, byte[] data)
    {
        super(MessageHeader.CMD_AUTH, type, 0, data);
    }

    /**
     * Returns the AUTH type. This is either {@link #TYPE_TOKEN},
     * {@link #TYPE_SIGNATURE} or {@link #TYPE_RSAPUBLICKEY}.
     * 
     * @return The AUTH type.
     */
    public int getType()
    {
        return this.header.getArg0();
    }

    @Override
    public String toString()
    {
        return String.format("AUTH(%d, 0x%s)", getType(),
            DatatypeConverter.printHexBinary(getData()));
    }
}
