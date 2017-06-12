package org.softastur.asturianspellchecker;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class QuickFileReader {

    public static byte[] readRawResource(Context context, int id) {
        InputStream inStream = context.getResources().openRawResource(id);
        byte[] result;
        try {
            result = convertStreamToByteArray(inStream);
            inStream.close();
        }catch(IOException e) {
            result = new byte[0];
            try {
                inStream.close();
            }catch(IOException f) {}
        }
        return result;
    }

    public static byte[] convertStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[10240];
        int i;
        while ((i = is.read(buff, 0, buff.length)) > 0) {
            baos.write(buff, 0, i);
        }

        return baos.toByteArray(); // be sure to close InputStream in calling function
    }

}
