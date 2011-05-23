package com.android.util;

import java.io.IOException;
import java.io.OutputStream;

public class DummyOutputStream extends OutputStream {

    private int size = 0;
    
    @Override
    public void write(int b) throws IOException {
        size += 1;
    }
    @Override
    public void write(byte[] bytes) throws IOException {
        size += bytes.length;
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        size += len;
    }
    
    public int getSize(){
        return this.size;
    }
}
