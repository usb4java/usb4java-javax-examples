/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package org.usb4java.javax.examples.adb;

import java.nio.charset.Charset;

import javax.xml.bind.DatatypeConverter;

/**
 * ADB WRITE message.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class WriteMessage extends Message
{
    /**
     * Constructs a new WRITE message.
     * 
     * @param header
     *            The ADB message header.
     * @param data
     *            The ADB message data.
     */
    public WriteMessage(MessageHeader header, byte[] data)
    {
        super(header, data);
    }
    
    /**
     * Constructs a new WRITE message.
     * 
     * @param remoteId
     *            The remote ID.
     * @param data
     *            The destination.
     */
    public WriteMessage(int remoteId, byte[] data)
    {
        super(MessageHeader.CMD_WRTE, remoteId, 0, data);
    }
    
    /**
     * Constructs a new WRITE message.
     * 
     * @param remoteId
     *            The local ID.
     * @param data
     *            The data.
     */
    public WriteMessage(int remoteId, String data)
    {
        this(remoteId, (data + '\0').getBytes(Charset.forName("UTF-8")));
    }
    
    /**
     * Returns the remote ID.
     * 
     * @return The remote ID.
     */
    public int getRemoteId()
    {
        return this.header.getArg1();
    }
    
    /**
     * Returns the destination.
     * 
     * @return The destination.
     */
    public String getDataAsString()
    {
        int len = this.data.length;
        while (len > 0 && this.data[len - 1] == 0) len--;
        return new String(this.data, 0, len, Charset.forName("UTF-8"));
    }

    @Override
    public String toString()
    {
        return String.format("WRITE(%d, %s)", getRemoteId(), 
            DatatypeConverter.printHexBinary(getData()));
    }
}
