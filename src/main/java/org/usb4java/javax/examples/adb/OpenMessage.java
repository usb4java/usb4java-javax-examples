/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package org.usb4java.javax.examples.adb;

import java.nio.charset.Charset;

/**
 * ADB OPEN message.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class OpenMessage extends Message
{
    /**
     * Constructs a new OPEN message.
     * 
     * @param header
     *            The ADB message header.
     * @param data
     *            The ADB message data.
     */
    public OpenMessage(MessageHeader header, byte[] data)
    {
        super(header, data);
    }
    
    /**
     * Constructs a new OPEN message.
     * 
     * @param localId
     *            The local ID.
     * @param destination
     *            The destination.
     */
    public OpenMessage(int localId, byte[] destination)
    {
        super(MessageHeader.CMD_OPEN, localId, 0, destination);
    }
    
    /**
     * Constructs a new OPEN message.
     * 
     * @param localId
     *            The local ID.
     * @param destination
     *            The destination.
     */
    public OpenMessage(int localId, String destination)
    {
        this(localId, (destination + '\0').getBytes(Charset.forName("UTF-8")));
    }
    
    /**
     * Returns the local ID.
     * 
     * @return The local ID.
     */
    public int getLocalId()
    {
        return this.header.getArg0();
    }
    
    /**
     * Returns the destination.
     * 
     * @return The destination.
     */
    public String getDestination()
    {
        int len = this.data.length;
        while (len > 0 && this.data[len - 1] == 0) len--;
        return new String(this.data, 0, len, Charset.forName("UTF-8"));
    }

    @Override
    public String toString()
    {
        return String.format("OPEN(%d, \"%s\")", getLocalId(), 
            getDestination());
    }
}
