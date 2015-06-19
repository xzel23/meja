/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.io;

/**
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class DataException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Construct instance with message.
     * @param message the message to use
     */
    public DataException(String message) {
        super(message);
    }

    /**
     * Construct instance with message and cause.
     * @param message the message to use
     * @param cause the throwable that caused the exception
     */
    public DataException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct instance with a cause.
     * @param cause the throwable that caused the exception
     */
    public DataException(Throwable cause) {
        super(cause);
    }
 
}
