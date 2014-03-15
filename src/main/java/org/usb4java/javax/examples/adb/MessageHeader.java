/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package org.usb4java.javax.examples.adb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The header of an ADB message.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class MessageHeader
{
    /** Command for SYNC message. */
    public static final int CMD_SYNC = 0x434e5953;

    /** Command for CONNECT message. */
    public static final int CMD_CNXN = 0x4e584e43;

    /** Command for AUTH message. */
    public static final int CMD_AUTH = 0x48545541;

    /** Command for OPEN message. */
    public static final int CMD_OPEN = 0x4e45504f;

    /** Command for OKAY message. */
    public static final int CMD_OKAY = 0x59414b4f;

    /** Command for CLOSE message. */
    public static final int CMD_CLSE = 0x45534c43;

    /** Command for WRITE message. */
    public static final int CMD_WRTE = 0x45545257;

    /** The ADB message header size in bytes. */
    public static final int SIZE = 24;

    /** The ADB message command. */
    private final int command;

    /** First argument. */
    private final int arg0;

    /** Second argument. */
    private final int arg1;

    /** Length of payload (0 is allowed). */
    private final int dataLength;

    /** Checksum of data payload (Sum of all bytes). */
    private final int dataChecksum;

    /** Inverted command. */
    private final int magic;

    /**
     * Constructs a new ADB message.
     * 
     * @param command
     *            The command.
     * @param arg0
     *            The first argument.
     * @param arg1
     *            The second argument.
     * @param dataLength
     *            The data length in bytes.
     * @param dataChecksum
     *            The data checksum. According to the documentation this is a
     *            CRC32 checksum but in reality it is just the sum of all data
     *            bytes.
     * @param magic
     *            The inverted command. Can be used for validating the message
     *            header.
     */
    public MessageHeader(int command, int arg0, int arg1, int dataLength, 
        int dataChecksum, int magic)
    {
        this.command = command;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.dataLength = dataLength;
        this.dataChecksum = dataChecksum;
        this.magic = command ^ 0xffffffff;
    }

    /**
     * Constructs a new ADB message header from the specified byte array.
     * 
     * @param bytes
     *            The ADB message header as bytes.
     */
    public MessageHeader(byte[] bytes)
    {
        if (bytes.length != SIZE)
            throw new IllegalArgumentException("ADB message header must be "
                + SIZE + " bytes large, not " + bytes.length + " bytes");
        ByteBuffer buffer = ByteBuffer.wrap(bytes).
            order(ByteOrder.LITTLE_ENDIAN);
        this.command = buffer.getInt();
        this.arg0 = buffer.getInt();
        this.arg1 = buffer.getInt();
        this.dataLength = buffer.getInt();
        this.dataChecksum = buffer.getInt();
        this.magic = buffer.getInt();
    }
    
    /**
     * Returns the command.
     * 
     * @return The command.
     */
    public int getCommand()
    {
        return this.command;
    }

    /**
     * Returns the first argument.
     * 
     * @return The first argument.
     */
    public int getArg0()
    {
        return this.arg0;
    }

    /**
     * Returns the second argument.
     * 
     * @return The second argument.
     */
    public int getArg1()
    {
        return this.arg1;
    }

    /**
     * Returns the data checksum.
     * 
     * @return The data checksum.
     */
    public int getDataChecksum()
    {
        return this.dataChecksum;
    }

    /**
     * Returns the data length.
     * 
     * @return The data length.
     */
    public int getDataLength()
    {
        return this.dataLength;
    }

    /**
     * Returns the inverted command.
     * 
     * @return The inverted command.
     */
    public int getMagic()
    {
        return this.magic;
    }

    /**
     * Check if this message header is valid. This simply checks if the header
     * magic value is the inverted value of the command value.
     * 
     * @return True if message header is valid, false if not.
     */
    public boolean isValid()
    {
        return this.magic == (this.command ^ 0xffffffff);
    }

    /**
     * Returns the message header as a byte array.
     * 
     * @return The message header as a byte array.
     */
    public byte[] getBytes()
    {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(this.command);
        buffer.putInt(this.arg0);
        buffer.putInt(this.arg1);
        buffer.putInt(this.dataLength);
        buffer.putInt(this.dataChecksum);
        buffer.putInt(this.magic);
        return buffer.array();
    }
}
