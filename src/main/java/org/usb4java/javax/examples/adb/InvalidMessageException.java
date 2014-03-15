/*
 * Copyright (C) 2014 Klaus Reimer <k@ailis.de>
 * See LICENSE.md for licensing information. 
 */

package org.usb4java.javax.examples.adb;

/**
 * Thrown when an invalid ADB message has been received.
 * 
 * @author Klaus Reimer (k@ailis.de)
 */
public class InvalidMessageException extends RuntimeException
{
    /**
     * Constructor.
     * 
     * @param message
     *            The exception message.
     */
    public InvalidMessageException(String message)
    {
        super(message);

    }
}
