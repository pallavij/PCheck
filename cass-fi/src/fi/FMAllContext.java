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

// This is just a class that covers all context


import org.fi.*;
import java.io.*;

public class FMAllContext {
  
  public FMJoinPoint fjp = null;
  public FMContext ctx = null;
  public FMStackTrace fst = null;


  // ********************************************
  public void write(DataOutputStream out) throws IOException {
    fjp.write(out);
    ctx.write(out);
    fst.write(out);
  }

  // ********************************************
  public void readFields(DataInputStream in) throws IOException {
    fjp = new FMJoinPoint();
    fjp.readFields(in);

    ctx = new FMContext();
    ctx.readFields(in);

    fst = new FMStackTrace();
    fst.readFields(in);

  }


  // ********************************************
  public FMAllContext() {

  }
  
  // ********************************************
  public FMAllContext(FMJoinPoint fjp, 
		      FMContext ctx, 
		      FMStackTrace fst) {

    this.fjp = fjp;
    this.ctx = ctx;
    this.fst = fst;
  }
  

  // ********************************************
  public String toString() {

    String buf = "";

    buf += "\nFM JOIN POINT:\n";
    buf += fjp.toString();

    buf += "\nFM CONTEXT:\n";
    buf += ctx.toString();

    buf += "\nFM STACK TRACE:\n";
    buf += fst.toString();

    buf += "\n-----------------------------------\n";

    return buf;

  }

  
}