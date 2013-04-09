package util;

import java.io.InputStream;
import java.io.IOException;
import org.osgeo.mapguide.*;

public class MgReadOnlyStream extends InputStream {

    private MgByteReader _reader;
    private byte [] _buffer;
    private int _internalBufferSize;
    private int _internalBufferPosition;
    private boolean _eos;

    public MgReadOnlyStream(MgByteReader byteReader) {
        _reader = byteReader;
        _buffer = new byte[2048];
        _internalBufferPosition = -1;
        _internalBufferSize = -1;
        _eos = false;
    }

    public void close() {
        _reader = null;
    }

    private int advanceInternalBuffer() throws MgException {
        _internalBufferSize = _reader.read(_buffer, _buffer.length);
        _internalBufferPosition = -1;
        return _internalBufferSize;
    }

    @Override
    public int read() throws IOException {
        if (_reader == null)
            return -1;

        try {
            //Un-initialized internal buffer
            if (_internalBufferSize == -1) {
                //Bail if we get nothing from the MgByteReader
                if (advanceInternalBuffer() <= 0)
                    return -1;
            }

            //Advance our internal buffer position
            _internalBufferPosition++;
            //End of internal buffer?
            if (_internalBufferPosition == _internalBufferSize) {
                //Bail if we get nothing from the MgByteReader
                if (advanceInternalBuffer() <= 0)
                    return -1;
                _internalBufferPosition++; //Re-position to start
            }

            return _buffer[_internalBufferPosition];
        } catch (MgException ex) {
            String msg = "";
            try {
                msg = ex.getExceptionMessage();
            } catch (Exception e) {
                msg = "";
            }
            throw new IOException(msg);
        }
    }
}