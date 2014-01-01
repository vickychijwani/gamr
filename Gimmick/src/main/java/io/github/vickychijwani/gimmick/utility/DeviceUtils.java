package io.github.vickychijwani.gimmick.utility;

import android.os.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DeviceUtils {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Checks if {@link android.os.Environment}.MEDIA_MOUNTED is returned by {@code getExternalStorageState()}
     * and therefore external storage is read- and writeable.
     */
    public static boolean isExtStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Copies data from one input stream to the other using a buffer of 8 kilobyte in size.
     *
     * @param input  {@link java.io.InputStream}
     * @param output {@link java.io.OutputStream}
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
