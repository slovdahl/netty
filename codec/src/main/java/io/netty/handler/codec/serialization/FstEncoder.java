/*
 * Copyright 2018 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.EncoderException;
import org.nustaq.serialization.FSTConfiguration;

public class FstEncoder extends ChannelOutboundHandlerAdapter {

    private final FSTConfiguration fstConfiguration;

    public FstEncoder(FSTConfiguration fstConfiguration) {
        this.fstConfiguration = fstConfiguration;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object obj, ChannelPromise promise) {
        ByteBuf buf = null;
        try {
            // allocates an integer array for the length of the serialized object
            int[] length = new int[1];
            // serialize the object
            byte[] serialized = fstConfiguration.asSharedByteArray(obj, length);
            int serializedObjectLength = length[0];

            // allocate an output buffer of the correct size
            // 4 bytes for length + length of serialized byte array
            buf = ctx.alloc().ioBuffer(4 + serializedObjectLength);
            buf.writeInt(serializedObjectLength);
            // need to explicitly specify how many bytes to write, because the byte array might be bigger
            buf.writeBytes(serialized, 0, serializedObjectLength);

            ctx.write(buf, promise);
            buf = null;
        } catch (EncoderException e) {
            throw e;
        } catch (Throwable e) {
            throw new EncoderException(e);
        } finally {
            if (buf != null) {
                buf.release();
            }
        }
    }
}
