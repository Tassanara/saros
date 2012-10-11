package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.NetTransferMode;
import de.fu_berlin.inf.dpp.util.Utils;

public class BinaryChannelTransferObject implements IncomingTransferObject {

    private BinaryChannel binaryChannel;

    private TransferDescription transferDescription;

    private int fragmentId;
    private int chunkCount;
    private long transferredSize;
    private long uncompressedSize;
    private BlockingQueue<byte[]> chunks;

    private AtomicBoolean accepted = new AtomicBoolean(false);

    public BinaryChannelTransferObject(BinaryChannel binaryChannel,
        TransferDescription transferDescription, int fragmentId,
        int chunkCount, BlockingQueue<byte[]> chunks) {

        this.binaryChannel = binaryChannel;
        this.transferDescription = transferDescription;
        this.fragmentId = fragmentId;
        this.chunkCount = chunkCount;
        this.chunks = chunks;
        transferredSize = 0;
        uncompressedSize = 0;
    }

    @Override
    public byte[] accept() throws IOException {

        try {

            if (!accepted.compareAndSet(false, true))
                throw new IllegalStateException(
                    "This IncomingTransferObject has already been accepted");

            List<byte[]> resultList = new LinkedList<byte[]>();

            while (chunkCount > 0) {
                if (!binaryChannel.isConnected())
                    throw new IOException("data connection lost");

                byte[] payload;

                try {
                    payload = chunks.poll(5, TimeUnit.SECONDS);
                    if (payload == null)
                        continue;

                    chunkCount--;

                } catch (InterruptedException e) {
                    Thread.interrupted();
                    throw new InterruptedIOException(
                        "interrupted while reading stream data");
                }

                resultList.add(payload);
            }

            int length = 0;

            for (byte[] payload : resultList)
                length += payload.length;

            // OOM Exception incoming at least here if the binary channel not
            // thrown it already !
            byte[] data = new byte[length];

            int offset = 0;

            for (byte[] payload : resultList) {
                System.arraycopy(payload, 0, data, offset, payload.length);
                offset += payload.length;
            }

            transferredSize = data.length;

            /*
             * HOW cool is that, got 50 MB compressed data ... deflate it ..
             * trash the heap !
             */

            // OOM Exception !
            if (transferDescription.compressContent())
                data = Utils.inflate(data, null);

            uncompressedSize = data.length;

            return data;
        } finally {
            binaryChannel.removeFragments(fragmentId);
        }
    }

    public TransferDescription getTransferDescription() {
        return transferDescription;
    }

    public NetTransferMode getTransferMode() {
        return binaryChannel.getTransferMode();
    }

    public long getTransferredSize() {
        return transferredSize;
    }

    public long getUncompressedSize() {
        return uncompressedSize;
    }

}