/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information.
 */

package org.usb4java.javax.examples.adb;


/**
 * ADB Message. The abstract base class for all commands.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public abstract class Message
{
    /** The ADB message header. */
    protected final MessageHeader header;

    /** The data payload. */
    protected final byte[] data;

    /**
     * Constructs a new ADB message.
     * 
     * @param command
     *            The command.
     * @param arg0
     *            The first argument.
     * @param arg1
     *            The second argument.
     * @param data
     *            The data payload.
     */
    protected Message(int command, int arg0, int arg1, byte[] data)
    {
        this.data = data;
        int checksum = 0;
        for (byte b: data)
            checksum += b & 0xff;
        this.header = new MessageHeader(command, arg0, arg1,
            data.length, checksum, command ^ 0xffffffff);
    }

    /**
     * Constructs a new ADB message.
     * 
     * @param header
     *            The ADB message header.
     * @param data
     *            The ADB message data.
     */
    public Message(MessageHeader header, byte[] data)
    {
        this.header = header;
        this.data = data;
    }

    /**
     * Returns the message header.
     * 
     * @return The message header.
     */
    public MessageHeader getHeader()
    {
        return this.header;
    }

    /**
     * Returns the payload data.
     * 
     * @return The payload data.
     */
    public byte[] getData()
    {
        return this.data;
    }

    /**
     * Checks if this ADB message is valid. First it checks the validity of the
     * header and then it checks the data checksum.
     * 
     * @return True if ADB message is valid, false if not.
     */
    public boolean isValid()
    {
        if (!this.header.isValid()) return false;
        int checksum = 0;
        for (byte b: this.data)
            checksum += b & 0xff;
        return checksum == this.header.getDataChecksum();
    }

    /**
     * Creates an ADB message.
     * 
     * @param header
     *            The ADB message header.
     * @param data
     *            The ADB message data.
     * @return The parsed ADB message.
     */
    public static Message create(MessageHeader header, byte[] data)
    {
        int command = header.getCommand();
        switch (command)
        {
            case MessageHeader.CMD_CNXN:
                return new ConnectMessage(header, data);

            case MessageHeader.CMD_AUTH:
                return new AuthMessage(header, data);
                
            case MessageHeader.CMD_OPEN:
                return new OpenMessage(header, data);

            case MessageHeader.CMD_CLSE:
                return new CloseMessage(header, data);

            case MessageHeader.CMD_OKAY:
                return new OkayMessage(header, data);

            case MessageHeader.CMD_WRTE:
                return new WriteMessage(header, data);

            default:
                throw new UnsupportedOperationException(String.format(
                    "Parsing of command 0x%08x not implemented yet",
                    command));
        }
    }
}
