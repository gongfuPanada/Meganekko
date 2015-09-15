/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eje_c.meganekko.utility;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A {@link FileInputStream} that supports {@link InputStream#mark(int)} and
 * {@link InputStream#reset()}
 */
public class MarkingFileInputStream extends FileInputStream {

    private static final long NO_MARK = -1;
    private long mark = NO_MARK;

    public MarkingFileInputStream(String path) throws FileNotFoundException {
        super(path);
    }

    public MarkingFileInputStream(File file) throws FileNotFoundException {
        super(file);
    }

    @Override
    public void mark(int readlimit) {
        try {
            mark = getChannel().position();
        } catch (IOException e) {
            e.printStackTrace();
            mark = NO_MARK;
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void reset() throws IOException {
        getChannel().position(mark);
    }

}