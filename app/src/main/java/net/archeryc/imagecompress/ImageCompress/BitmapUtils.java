package net.archeryc.imagecompress.ImageCompress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by yc on 2017/8/20.
 * Bitmap压缩工具类
 */

public class BitmapUtils {


    /**
     * 主要的压缩方法
     * @param inputFile
     * @param outputFile
     * @param targetWidth
     * @param targetHeight
     * @param options
     * @return
     */
    public static String compressImage(String inputFile, String outputFile, int targetWidth, int targetHeight, int options) {
        log("targetWidth===>" + targetWidth + "targetHeight=====>" + targetHeight + "mineType====>" + getImageMineType(inputFile));
        Bitmap bitmap = getOriginBitmap(inputFile);
        boolean success=compressImageToFile(executeMatrix(inputFile,bitmap,targetWidth,targetHeight), new File(outputFile), getImageMineType(inputFile),options);
        if (success){
            return outputFile;
        }else{
            return inputFile;
        }
    }

    /**
     * 根据图片大小设置采样率，减少内存占用
     * @param inputFile
     * @return
     */
    private static Bitmap getOriginBitmap(String inputFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Size size = getImageSize(inputFile);
        int sampleSize=size.getWidth()>size.getHeight()?size.getWidth()/ImageCompressUtils.MAX_SIZE:size.getHeight()/ImageCompressUtils.MAX_SIZE;
        if (sampleSize<=0){
            sampleSize=1;
        }
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(inputFile, options);
    }

    /**
     * 对bitmap进行旋转和宽高变化
     * @param inputFile
     * @param bitmap
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    private static Bitmap executeMatrix(String inputFile,Bitmap bitmap,int targetWidth,int targetHeight){
        log("bitmap width===>"+bitmap.getWidth()+"bitmap height===>"+bitmap.getHeight());
        Matrix matrix = new Matrix();
        if (bitmap.getWidth()>targetWidth&&bitmap.getHeight()>targetHeight) {
            matrix.postScale((float)targetWidth/bitmap.getWidth(),(float) targetHeight/bitmap.getHeight());
        }
        try {
            ExifInterface exifReader = null;
            exifReader = new ExifInterface(inputFile);
            int orientation = exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;

                default: // ExifInterface.ORIENTATION_NORMAL
                    // Do nothing. The original image is fine.
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 压缩并保存到文件中
     * @param bmp
     * @param file
     * @param type
     * @param options
     * @return
     */
    private static boolean compressImageToFile(Bitmap bmp, File file, int type,int options) {
        // 0-100 100为不压缩
        boolean success = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 把压缩后的数据存放到baos中
        if (type == ImageType.TYPE_PNG) {
            bmp.compress(Bitmap.CompressFormat.PNG, options, baos);
        } else {
            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
            success = true;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }


    /**
     * 获取图片宽高
     * @param inputFile
     * @return
     */
    public static Size getImageSize(String inputFile) {
        Size size = new Size();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(inputFile, options);
        size.setWidth(options.outWidth);
        size.setHeight(options.outHeight);
        return size;
    }

    /**
     * 获取图片类型 gif,png,jpeg
     * @param path
     * @return
     */
    public static int getImageMineType(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        if (options.outMimeType.contains("gif")) {
            return ImageType.TYPE_GIF;
        } else if (options.outMimeType.contains("png")) {
            return ImageType.TYPE_PNG;
        } else {
            return ImageType.TYPE_JPEG;
        }
    }


    private static void log(String str) {
        Log.d("archeryc", str);
    }

    public static class Size {
        int width;
        int height;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public static class ImageType {
        public static final int TYPE_GIF = 1;
        public static final int TYPE_JPEG = 2;
        public static final int TYPE_PNG = 3;
    }
}
