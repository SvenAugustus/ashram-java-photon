/*
 * MIT License
 *
 * Copyright (c) 2020 SvenAugustus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.flysium.photon.rpc.remoting.portocol;

/**
 * @author zeno (Sven Augustus)
 * @version 1.0
 */
public class RpcMessage {

  private short type;
  private long mId;
  private int length;
  private byte[] body;

  public RpcMessage() {
  }

  public RpcMessage(short type, long mId, byte[] body) {
    this.type = type;
    this.mId = mId;
    this.setBody(body);
  }

  public short getType() {
    return type;
  }

  public void setType(short type) {
    this.type = type;
  }

  public long getId() {
    return mId;
  }

  public void setId(long mId) {
    this.mId = mId;
  }

  public int getLength() {
    return length;
  }

  public byte[] getBody() {
    return body;
  }

  public void setBody(byte[] body) {
    if (body == null) {
      body = new byte[0];
    }
    this.body = body;
    this.length = body.length;
  }

}
