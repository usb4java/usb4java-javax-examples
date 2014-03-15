/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package org.usb4java.javax.examples.adb;


/**
 * ADB OKAY message.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class OkayMessage extends Message
{
    /**
     * Constructs a new OKAY message.
     * 
     * @param header
     *            The ADB message header.
     * @param data
     *            The ADB message data.
     */
    public OkayMessage(MessageHeader header, byte[] data)
    {
        super(header, data);
    }
    
    /**
     * Constructs a new OKAY message.
     * 
     * @param localId
     *            The local ID.
     * @param remoteId
     *            The remote ID.
     */
    public OkayMessage(int remoteId, int localId)
    {
        super(MessageHeader.CMD_OKAY, remoteId, localId, new byte[0]);
    }
    
    /**
     * Returns the local ID.
     * 
     * @return The local ID.
     */
    public int getLocalId()
    {
        return this.header.getArg1();
    }
    
    /**
     * Returns the remote ID.
     * 
     * @return The remote ID.
     */
    public int getRemoteId()
    {
        return this.header.getArg0();
    }
    
    @Override
    public String toString()
    {
        return String.format("OKAY(%d, %d)", getRemoteId(), getLocalId()); 
    }
}
