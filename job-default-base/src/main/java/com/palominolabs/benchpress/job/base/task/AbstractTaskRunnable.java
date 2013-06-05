package com.palominolabs.benchpress.job.base.task;

import com.google.common.base.Charsets;
import com.palominolabs.benchpress.job.key.KeyGenerator;
import com.palominolabs.benchpress.job.value.ValueGenerator;
import com.palominolabs.benchpress.logging.MdcKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.UUID;

@NotThreadSafe
public abstract class AbstractTaskRunnable implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int DEFAULT_KEY_BUF_LENGTH = 64;

    private final KeyGenerator keyGenerator;
    private final UUID workerId;
    private final CharsetEncoder encoder = Charsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.IGNORE)
        .onUnmappableCharacter(CodingErrorAction.IGNORE);
    private final long threadId;
    private CharBuffer keyCharBuf = CharBuffer.allocate(DEFAULT_KEY_BUF_LENGTH);
    private ByteBuffer byteBuf = getByteBuffer(keyCharBuf);

    private final int partitionId;
    private final int numQuanta;
    private final int batchSize;
    private final UUID jobId;
    private final ValueGenerator valueGenerator;

    protected AbstractTaskRunnable(
        KeyGenerator keyGenerator, UUID workerId, int partitionId, int numQuanta, int batchSize, UUID jobId,
        ValueGenerator valueGenerator) {
        this.keyGenerator = keyGenerator;
        threadId = Thread.currentThread().getId();
        this.workerId = workerId;
        this.partitionId = partitionId;
        this.numQuanta = numQuanta;
        this.batchSize = batchSize;
        this.jobId = jobId;
        this.valueGenerator = valueGenerator;
    }

    @Override
    public final void run() {
        MDC.put(MdcKeys.JOB_ID, jobId.toString());

        try {
            doRun();
        } finally {
            MDC.remove(MdcKeys.JOB_ID);
        }
    }

    protected abstract void onBatchCompletion() throws IOException;

    protected abstract void onCompletion() throws IOException;

    /**
     * // TODO pass in the buffers directly?
     *
     * @param keyBytes key bytes
     * @param valueBytes value bytes. Do not modify -- this byte array is owned by the value generator.
     */
    protected abstract void onQuanta(byte[] keyBytes, byte[] valueBytes);

    protected abstract void onBatchStart();

    private void doRun() {
        logger.info("Starting");

        byte[] value = valueGenerator.getValue();

        int counter = 0;

        while (counter < numQuanta) {
            int todo = Math.min(numQuanta - counter, batchSize);

            onBatchStart();

            for (int i = 0; i < todo; i++) {
                onQuanta(getKeyBytes(counter), value);

                counter++;
            }

            try {
                onBatchCompletion();
            } catch (IOException e) {
                logger.warn("Error in onBatchCompletion", e);
            }
        }

        try {
            onCompletion();
        } catch (IOException e) {
            logger.warn("Error in onCompletion", e);
        }

        logger.info("Done");
    }

    private byte[] getKeyBytes(int counter) {
        keyCharBuf.clear();
        byteBuf.clear();

        // retry keygen until we have a big enough buffer
        boolean retry = true;
        while (retry) {
            try {
                keyGenerator.writeKey(keyCharBuf, workerId, threadId, partitionId, counter);
                retry = false;
            } catch (BufferOverflowException e) {
                keyCharBuf = CharBuffer.allocate(keyCharBuf.capacity() * 2);
                byteBuf = getByteBuffer(keyCharBuf);
            }
        }

        encoder.reset();
        keyCharBuf.flip();
        encoder.encode(keyCharBuf, byteBuf, true);
        encoder.flush(byteBuf);

        byteBuf.flip();

        // TODO don't allocate a new byte[] all the time
        byte[] rowBytes = new byte[byteBuf.remaining()];
        byteBuf.get(rowBytes);

        return rowBytes;
    }

    /**
     * @param keyCharBuf buf for the characters of a key
     * @return a byte buffer big enough to hold the capacity of keyCharBuf with the current encoder
     */
    private ByteBuffer getByteBuffer(CharBuffer keyCharBuf) {
        return ByteBuffer.allocate(keyCharBuf.capacity() * ((int) Math.ceil(encoder.maxBytesPerChar()) + 1));
    }
}
