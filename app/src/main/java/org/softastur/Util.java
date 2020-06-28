package org.softastur;

import android.content.Context;

import org.softastur.asturiandictionary.R;
import org.softastur.asturianspellchecker.QuickFileReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Created by guifa on 9/15/17.
 */

public class Util {

    static public final int[] intArrayFromRawResource(Context context, int rawFileId, int endianess) {
        byte[] file = readRawResource(context, R.raw.fast_dict);
        IntBuffer ib = ByteBuffer.wrap(file).order(ByteOrder.BIG_ENDIAN).asIntBuffer();

        int[] data = new int[ib.limit()];
        ib.get(data);
        return data;
    }

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
                }catch(IOException f) {
                    System.out.println(f.getMessage());
                }
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
