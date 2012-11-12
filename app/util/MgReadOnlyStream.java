package util;

import java.io.InputStream;
import java.io.IOException;
import org.osgeo.mapguide.*;

public class MgReadOnlyStream extends InputStream {

    private MgByteReader _reader;
    private byte [] _buffer;
    private boolean _eos;

    public MgReadOnlyStream(MgByteReader byteReader) {
        _reader = byteReader;
        _buffer = new byte[1];
        _eos = false;
    }

    public void close() {
        _reader = null;
    }

    @Override
    public int read() throws IOException {
        int read = -1;
        if (_reader == null)
            return read;

        try {
            if (_reader.Read(_buffer, 1) == 0) {
                _eos = true;
                return -1;
            }
            return _buffer[0];
        } catch (MgException ex) {
            String msg = "";
            try {
                msg = ex.GetExceptionMessage();
            } catch (Exception e) {
                msg = "";
            }
            throw new IOException(msg);
        }
    }
}