/*
 * Copyright 2015 Axel Howind.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.meja.util;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

/**
 * Wrap Appendable as Writer.
 */
class AppendableWriter extends Writer {

    protected Appendable app;

    /**
     * Create instance of Writer that writes to the Appendable given as
     * parameter.
     *
     * @param app
     *            an instance of
     */
    protected AppendableWriter(Appendable app) {
        this.app = app;
    }

    @Override
    public Writer append(char c) throws IOException {
        app.append(c);
        return this;
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        app.append(csq);
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        app.append(csq, start, end);
        return this;
    }

    @Override
    public void close() throws IOException {
        if (app instanceof Closeable) {
            ((Closeable) app).close();
        }
    }

    @Override
    public void flush() throws IOException {
        if (app instanceof Flushable) {
            ((Flushable) app).flush();
        }
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        append(new String(cbuf));
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        append(new String(cbuf, off, len));
    }

    @Override
    public void write(int c) throws IOException {
        append((char) c);
    }

    @Override
    public void write(String str) throws IOException {
        app.append(str);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        app.append(str, off, off + len);
    }

}
