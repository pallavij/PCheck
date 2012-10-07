/**
 * Copyright (c) 2012,
 * Thanh Do  <thanhdo@cs.wisc.edu>
 * Haryadi S. Gunawi  <haryadi@cs.uchicago.edu>
 * Pallavi Joshi  <pallavi@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */



package org.fi;


import org.fi.*;


import java.io.*;
import java.util.*;
import java.lang.*;
import java.net.*;
import java.nio.channels.*;

public aspect weavert {

  declare parents: File implements ClassWC;
  declare parents: DataInput implements ClassWC;
  declare parents: DataOutput implements ClassWC;
  declare parents: InputStream implements ClassWC;
  declare parents: OutputStream implements ClassWC;
  declare parents: RandomAccessFile implements ClassWC;
  declare parents: FileChannel implements ClassWC;
  declare parents: Reader implements ClassWC;  
  declare parents: Writer implements ClassWC;  
  declare parents: FileLock implements ClassWC;
  declare parents: Properties implements ClassWC; // pallavi-07

  declare parents: Channel implements ClassWC; 

 
  // WARNING: you should declare something that implements
  // ClassWc ONLY IF you want to magically transfer the context.
  // But for sockets, we want to somewhat manually transfer the
  // context for now.
  // For example: for, DataOutputStream.new(Socket)
  // we want to transfer them ourselves .. 
  declare parents: SocketChannel implements ClassWC;
  declare parents: Socket implements ClassWC;
  
  public Context File.context = null;
  public Context File.getContext() { return context; }
  public void File.setContext(Context x) { context = x; }

  public Context InputStream.context = null;
  public Context InputStream.getContext() { return context; }
  public void InputStream.setContext(Context x) { context = x; }

  public Context OutputStream.context = null;
  public Context OutputStream.getContext() { return context; }
  public void OutputStream.setContext(Context x) { context = x; }
  
  public Context RandomAccessFile.context = null;
  public Context RandomAccessFile.getContext() { return context; }
  public void RandomAccessFile.setContext(Context x) { context = x; }

  public Context Reader.context = null;
  public Context Reader.getContext() { return context; }
  public void Reader.setContext(Context x) { context = x; }  

  public Context Writer.context = null;
  public Context Writer.getContext() { return context; }
  public void Writer.setContext(Context x) { context = x; }  

  public Context FileLock.context = null;
  public Context FileLock.getContext() { return context; }
  public void FileLock.setContext(Context x) { context = x; }  


  public Context SocketChannel.context = null;
  public Context SocketChannel.getContext() { return context; }
  public void SocketChannel.setContext(Context x) { context = x; }  
  
  public Context FileChannel.context = null;
  public Context FileChannel.getContext() { return context; }
  public void FileChannel.setContext(Context x) { context = x; }


  public Context Socket.context = null;
  public Context Socket.getContext() { return context; }
  public void Socket.setContext(Context x) { context = x; }  

  // pallavi-07
  public Context Properties.context = null;
  public Context Properties.getContext() { return context; }
  public void Properties.setContext(Context x) { context = x; }
  
  
}



