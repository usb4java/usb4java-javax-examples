/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package org.usb4java.javax.examples.adb;


/**
 * ADB CLOSE message.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class CloseMessage extends Message
{
    /**
     * Constructs a new CLOSE message.
     * 
     * @param header
     *            The ADB message header.
     * @param data
     *            The ADB message data.
     */
    public CloseMessage(MessageHeader header, byte[] data)
    {
        super(header, data);
    }
    
    /**
     * Constructs a new CLOSE message.
     * 
     * @param localId
     *            The local ID.
     * @param remoteId
     *            The remote ID.
     */
    public CloseMessage(int localId, int remoteId)
    {
        super(MessageHeader.CMD_CLSE, localId, remoteId, new byte[0]);
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
     * Returns the remote ID.
     * 
     * @return The remote ID.
     */
    public int getRemoteId()
    {
        return this.header.getArg1();
    }
    
    @Override
    public String toString()
    {
        return String.format("CLOSE(%d, %d)", getLocalId(), getRemoteId()); 
    }
}
