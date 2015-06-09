
package com.jrdcom.example.layout;

import android.util.Log;
import android.view.View;

import com.jrdcom.mt.util.IInputStreamOpener;
import com.jrdcom.mt.util.StreamUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class FreePuzzleLayout extends Layout<FreePuzzleLayoutItem> {

    
    
    public FreePuzzleLayout(IInputStreamOpener pInputStreamOpener) {
        super(pInputStreamOpener);
    }

    @Override
    public void load() {

        if (null == this.mInputStreamOpener
                || this.mLayoutItemEntities.size() != 0)
            return;
        do {
            InputStream lInputStream = null;
            byte[] lFileDatas = null;
            try {
                lInputStream = this.mInputStreamOpener.open();

                if (null == lInputStream)
                    break;

                lFileDatas = StreamUtils.streamToBytes(lInputStream);
            } catch (IOException e) {
                break;

            } finally {
                StreamUtils.close(lInputStream);
            }
            if (lFileDatas == null
                    || (lFileDatas != null && lFileDatas.length == 0))
                break;

            byte szHeadData[] = new byte[5];
            int pos = 12; // 初始化12 为文件类型标识位
            int lItemCount = 0;
            Arrays.fill(szHeadData, (byte) 0);
            System.arraycopy(lFileDatas, pos, szHeadData, 0, 5);

            String strHead = new String(szHeadData);
            if (!strHead.equals("MZYPT")) {
                break;
            }
            pos = 32;
            // 版本信息
            // int nValue = getIntData(data,pos);
            pos += 4;
            // 图片区域个数
            lItemCount = getIntData(lFileDatas, pos);
            pos += 4;
            this.mWidth = getIntData(lFileDatas, pos);
            pos += 4;
            this.mHeight = getIntData(lFileDatas, pos);
            pos += 4;
            
            //640 * 896
//            Log.e("puzzle", "width: "+this.mWidth+"  height:"+this.mHeight);
            // 预留八个字节
            pos += 8;
            for (int i = 0; i < lItemCount; i++) {
                FreePuzzleLayoutItem item = new FreePuzzleLayoutItem();
                item.setX(getIntData(lFileDatas, pos));
                pos += 4;
                item.setY(getIntData(lFileDatas, pos));
                pos += 4;
                item.setWidth(getIntData(lFileDatas, pos));
                pos += 4;
                item.setHeight(getIntData(lFileDatas, pos));
                pos += 4;

                int from = getIntData(lFileDatas, pos);
                pos += 4;
                int to = getIntData(lFileDatas, pos);
                pos += 4;
                item.setX(item.getX() + AverageRandom(from, to));

                from = getIntData(lFileDatas, pos);
                pos += 4;
                to = getIntData(lFileDatas, pos);
                pos += 4;
                item.setY(item.getY() + AverageRandom(from, to));

                from = getIntData(lFileDatas, pos);
                pos += 4;
                to = getIntData(lFileDatas, pos);
                pos += 4;
                item.setRotation(AverageRandom(from, to));

                float dMin, dMax;

                byte byteFloat[] = new byte[4];
                arraycopy(lFileDatas, pos, byteFloat, 4);
                pos += 4;
                dMin = bytesToFloat(byteFloat);
                arraycopy(lFileDatas, pos, byteFloat, 4);
                pos += 4;
                dMax = bytesToFloat(byteFloat);

                dMax = AverageRandom(dMin, dMax);

                dMin = item.getWidth() * dMax;
                dMax = item.getHeight() * dMax;

                item.setX(item.getX() + (item.getWidth() - dMin) / 2);
                item.setY(item.getY() + (item.getHeight() - dMax) / 2);
                item.setWidth(dMin);
                item.setHeight(dMax);
                
                // //预留八个字节
                pos += 8;

                this.mLayoutItemEntities.add(item);
            }
            lFileDatas = null;
        } while (false);
    }

    public static float AverageRandom(float from, float to) {
        float n = (float) (from + (to - from) * Math.random());
        return n;
    }

    public static int arraycopy(byte[] src, int pos, byte dst[], int len) {
        try {
            if (src.length >= pos + len) {
                System.arraycopy(src, pos, dst, 0, len);
                return len;
            }
        } catch (Exception e) {
        }
        return -1;
    }

    public static int getIntData(byte[] bytes, int start) {
        int num = 0;
        try {
            num = bytes[start + 0] & 0xFF;
            num |= ((bytes[start + 1] << 8) & 0xFF00);
            num |= ((bytes[start + 2] << 16) & 0xFF0000);
            num |= ((bytes[start + 3] << 24) & 0xFF000000);
        } catch (Exception e) {
        }
        return num;
    }

    public static int AverageRandom(int from, int to) {
        int n = (int) (from + (to - from) * Math.random());
        return n;
    }

    public static float bytesToFloat(byte[] temp) {
        try {
            byte a = temp[0];
            temp[0] = temp[3];
            temp[3] = a;
            a = temp[1];
            temp[1] = temp[2];
            temp[2] = a;

            ByteBuffer bb = ByteBuffer.wrap(temp);
            FloatBuffer fb = bb.asFloatBuffer();
            return fb.get();
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public void updateLayoutOnScreenSizeChanged(View pView, int pWidth,
            int pHeight) {
        super.updateLayoutOnScreenSizeChanged(pView, pWidth, pHeight);
    }

}
