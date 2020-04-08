package cn.bertsir.zbar.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import cn.bertsir.zbar.Qr.Config;
import cn.bertsir.zbar.Qr.Image;
import cn.bertsir.zbar.Qr.ImageScanner;
import cn.bertsir.zbar.Qr.Symbol;
import cn.bertsir.zbar.Qr.SymbolSet;

/**
 * Created by Bert on 2017/9/20.
 */

public class QRUtils {

    private static QRUtils instance;
    private Bitmap scanBitmap;
    private Context mContext;


    public static QRUtils getInstance() {
        if (instance == null) {
            instance = new QRUtils();
        }
        return instance;
    }

    /**
     * 识别本地二维码
     *
     * @param path
     * @return
     */
    public String decodeQRcode(String path) throws Exception {
        //对图片进行灰度处理，为了兼容彩色二维码
        Bitmap qrbmp = compressImage(path);
        qrbmp = toGrayscale(qrbmp);
        if (qrbmp != null) {
            return decodeQRcode(qrbmp);
        } else {
            return "";
        }

    }

    public String decodeQRcode(ImageView iv) throws Exception {
        Bitmap qrbmp = ((BitmapDrawable) (iv).getDrawable()).getBitmap();
        if (qrbmp != null) {
            return decodeQRcode(qrbmp);
        } else {
            return "";
        }
    }

    public String decodeQRcode(Bitmap barcodeBmp) throws Exception {
        int width = barcodeBmp.getWidth();
        int height = barcodeBmp.getHeight();
        int[] pixels = new int[width * height];
        barcodeBmp.getPixels(pixels, 0, width, 0, 0, width, height);
        Image barcode = new Image(width, height, "RGB4");
        barcode.setData(pixels);
        ImageScanner reader = new ImageScanner();
        reader.setConfig(Symbol.NONE, Config.ENABLE, 0);
        reader.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
        int result = reader.scanImage(barcode.convert("Y800"));
        String qrCodeString = null;
        if (result != 0) {
            SymbolSet syms = reader.getResults();
            for (Symbol sym : syms) {
                qrCodeString = sym.getData();
            }
        }
        barcodeBmp.recycle();
        return qrCodeString;
    }

    /**
     * 扫描二维码图片的方法
     * @param path
     * @return
     */
    public String decodeQRcodeByZxing(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;

        }
        Hashtable<DecodeHintType, String> hints = new Hashtable();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8"); // 设置二维码内容的编码
        Bitmap scanBitmap = compressImage(path);
        int[] data = new int[scanBitmap.getWidth() * scanBitmap.getHeight()];
        scanBitmap.getPixels(data, 0, scanBitmap.getWidth(), 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight());
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(scanBitmap.getWidth(),scanBitmap.getHeight(),data);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(rgbLuminanceSource));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (NotFoundException e) {

        }catch (ChecksumException e){

        }catch(FormatException e){

        }
        if(result == null){
            return "";
        }else {
            return result.getText();
        }
    }

    /**
     * 扫描二维码图片的方法
     * @return
     */
    public String decodeQRcodeByZxing(Bitmap bitmap) {
        Hashtable<DecodeHintType, String> hints = new Hashtable();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8"); // 设置二维码内容的编码
        scanBitmap =bitmap;
        int[] data = new int[scanBitmap.getWidth() * scanBitmap.getHeight()];
        scanBitmap.getPixels(data, 0, scanBitmap.getWidth(), 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight());
        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(scanBitmap.getWidth(),scanBitmap.getHeight(),data);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(rgbLuminanceSource));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (NotFoundException e) {

        }catch (ChecksumException e){

        }catch(FormatException e){

        }
        if(result == null){
            return "";
        }else {
            return result.getText();
        }

    }

    /**
     * 识别本地条形码
     *
     * @param url
     * @return
     */
    public String decodeBarcode(String url)  {
        Bitmap qrbmp = BitmapFactory.decodeFile(url);
        if (qrbmp != null) {
            return decodeBarcode(qrbmp);
        } else {
            return "";
        }

    }

    public String decodeBarcode(ImageView iv) {
        Bitmap qrbmp = ((BitmapDrawable) (iv).getDrawable()).getBitmap();
        if (qrbmp != null) {
            return decodeBarcode(qrbmp);
        } else {
            return "";
        }
    }

    public String decodeBarcode(Bitmap barcodeBmp) {
        int width = barcodeBmp.getWidth();
        int height = barcodeBmp.getHeight();
        int[] pixels = new int[width * height];
        barcodeBmp.getPixels(pixels, 0, width, 0, 0, width, height);
        Image barcode = new Image(width, height, "RGB4");
        barcode.setData(pixels);
        ImageScanner reader = new ImageScanner();
        reader.setConfig(Symbol.NONE, Config.ENABLE, 0);
        reader.setConfig(Symbol.CODE128, Config.ENABLE, 1);
        reader.setConfig(Symbol.CODE39, Config.ENABLE, 1);
        reader.setConfig(Symbol.EAN13, Config.ENABLE, 1);
        reader.setConfig(Symbol.EAN8, Config.ENABLE, 1);
        reader.setConfig(Symbol.UPCA, Config.ENABLE, 1);
        reader.setConfig(Symbol.UPCE, Config.ENABLE, 1);
        int result = reader.scanImage(barcode.convert("Y800"));
        String qrCodeString = null;
        if (result != 0) {
            SymbolSet syms = reader.getResults();
            for (Symbol sym : syms) {
                qrCodeString = sym.getData();
            }
        }
        return qrCodeString;
    }


    /**
     * 生成二维码
     *
     * @param content
     * @return
     */
    public Bitmap createQRCode(String content) {
        return createQRCode(content, 300, 300);
    }

    /**
     * 生成二维码
     *
     * @param content
     * @return
     */
    public Bitmap createQRCode(String content, int width, int height) {
        Bitmap bitmap = null;
        BitMatrix result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//这里调整二维码的容错率
            hints.put(EncodeHintType.MARGIN, 1);   //设置白边取值1-4，值越大白边越大
            result = multiFormatWriter.encode(new String(content.getBytes("UTF-8"), "ISO-8859-1"), BarcodeFormat
                    .QR_CODE, width, height, hints);
            int w = result.getWidth();
            int h = result.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * 生成带logo的二维码
     *
     * @param content
     * @param logo
     * @return
     */
    public Bitmap createQRCodeAddLogo(String content, Bitmap logo) {
        Bitmap qrCode = createQRCode(content);
        int qrheight = qrCode.getHeight();
        int qrwidth = qrCode.getWidth();
        int waterWidth = (int) (qrwidth * 0.3);//0.3为logo占二维码大小的倍数 建议不要过大，否则二维码失效
        float scale = waterWidth / (float) logo.getWidth();
        Bitmap waterQrcode = createWaterMaskCenter(qrCode, zoomImg(logo, scale));
        return waterQrcode;
    }


    public Bitmap createQRCodeAddLogo(String content, int width, int height, Bitmap logo) {
        Bitmap qrCode = createQRCode(content, width, height);
        int qrheight = qrCode.getHeight();
        int qrwidth = qrCode.getWidth();
        int waterWidth = (int) (qrwidth * 0.3);//0.3为logo占二维码大小的倍数 建议不要过大，否则二维码失效
        float scale = waterWidth / (float) logo.getWidth();
        Bitmap waterQrcode = createWaterMaskCenter(qrCode, zoomImg(logo, scale));
        return waterQrcode;
    }

    /**
     * 生成条形码
     *
     * @param context
     * @param contents
     * @param desiredWidth
     * @param desiredHeight
     * @return
     */
    @Deprecated
    public Bitmap createBarcode(Context context, String contents, int desiredWidth, int desiredHeight) {
        if (TextUtils.isEmpty(contents)) {
            throw new NullPointerException("contents not be null");
        }
        if (desiredWidth == 0 || desiredHeight == 0) {
            throw new NullPointerException("desiredWidth or desiredHeight not be null");
        }
        Bitmap resultBitmap;
        /**
         * 条形码的编码类型
         */
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

        resultBitmap = encodeAsBitmap(contents, barcodeFormat,
                desiredWidth, desiredHeight);
        return resultBitmap;
    }

    /**
     * 生成条形码
     *
     * @param context
     * @param contents
     * @param desiredWidth
     * @param desiredHeight
     * @return
     */
    public Bitmap createBarCodeWithText(Context context, String contents, int desiredWidth,
                                        int desiredHeight) {
        return createBarCodeWithText(context, contents, desiredWidth, desiredHeight, null);
    }

    public Bitmap createBarCodeWithText(Context context, String contents, int desiredWidth,
                                        int desiredHeight, TextViewConfig config) {
        if (TextUtils.isEmpty(contents)) {
            throw new NullPointerException("contents not be null");
        }
        if (desiredWidth == 0 || desiredHeight == 0) {
            throw new NullPointerException("desiredWidth or desiredHeight not be null");
        }
        Bitmap resultBitmap;

        /**
         * 条形码的编码类型
         */
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

        Bitmap barcodeBitmap = encodeAsBitmap(contents, barcodeFormat,
                desiredWidth, desiredHeight);

        Bitmap codeBitmap = createCodeBitmap(contents, barcodeBitmap.getWidth(),
                barcodeBitmap.getHeight(), context, config);

        resultBitmap = mixtureBitmap(barcodeBitmap, codeBitmap, new PointF(
                0, desiredHeight));
        return resultBitmap;
    }

    private Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int desiredWidth, int desiredHeight) {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        try {
            result = writer.encode(contents, format, desiredWidth,
                    desiredHeight, null);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;

    }


    private Bitmap createCodeBitmap(String contents, int width, int height, Context context,
                                    TextViewConfig config) {
        if (config == null) {
            config = new TextViewConfig();
        }
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
        tv.setTextSize(config.size == 0 ? tv.getTextSize() : config.size);
        tv.setHeight(height);
        tv.setGravity(config.gravity);
        tv.setMaxLines(config.maxLines);
        tv.setWidth(width);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(config.color);
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

        tv.buildDrawingCache();
        return tv.getDrawingCache();
    }

    public static class TextViewConfig {

        private int gravity = Gravity.CENTER;
        private int maxLines = 1;
        private int color = Color.BLACK;
        private float size;

        public TextViewConfig() {
        }

        public void setGravity(int gravity) {
            this.gravity = gravity;
        }

        public void setMaxLines(int maxLines) {
            this.maxLines = maxLines;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public void setSize(float size) {
            this.size = size;
        }
    }

    /**
     * 将两个Bitmap合并成一个
     *
     * @param first
     * @param second
     * @param fromPoint 第二个Bitmap开始绘制的起始位置（相对于第一个Bitmap）
     * @return
     */
    private Bitmap mixtureBitmap(Bitmap first, Bitmap second, PointF fromPoint) {
        if (first == null || second == null || fromPoint == null) {
            return null;
        }

        int width = Math.max(first.getWidth(), second.getWidth());
        Bitmap newBitmap = Bitmap.createBitmap(
                width,
                first.getHeight() + second.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas cv = new Canvas(newBitmap);
        cv.drawBitmap(first, 0, 0, null);
        cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
        cv.save();
        cv.restore();

        return newBitmap;
    }

    /**
     * 设置水印图片到中间
     *
     * @param src
     * @param watermark
     * @return
     */
    private Bitmap createWaterMaskCenter(Bitmap src, Bitmap watermark) {
        return createWaterMaskBitmap(src, watermark,
                (src.getWidth() - watermark.getWidth()) / 2,
                (src.getHeight() - watermark.getHeight()) / 2);
    }

    private Bitmap createWaterMaskBitmap(Bitmap src, Bitmap watermark, int paddingLeft, int paddingTop) {
        if (src == null) {
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap newb = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas canvas = new Canvas(newb);
        canvas.drawBitmap(src, 0, 0, null);
        canvas.drawBitmap(watermark, paddingLeft, paddingTop, null);
        canvas.save();
        canvas.restore();
        return newb;
    }

    /**
     * 缩放Bitmap
     *
     * @param bm
     * @param f
     * @return
     */
    private Bitmap zoomImg(Bitmap bm, float f) {

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = f;
        float scaleHeight = f;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }


    public boolean isMIUI() {
        String manufacturer = Build.MANUFACTURER;
        if ("xiaomi".equalsIgnoreCase(manufacturer)) {
            return true;
        }
        return false;
    }

    /**
     * Return the width of screen, in pixel.
     *
     * @return the width of screen, in pixel
     */
    public int getScreenWidth(Context mContext) {
        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //noinspection ConstantConditions
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            //noinspection ConstantConditions
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    /**
     * Return the height of screen, in pixel.
     *
     * @return the height of screen, in pixel
     */
    public int getScreenHeight(Context mContext) {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //noinspection ConstantConditions
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            //noinspection ConstantConditions
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

    /**
     * 返回当前屏幕是否为竖屏。
     * @param context
     * @return 当且仅当当前屏幕为竖屏时返回true,否则返回false。
     */
    public  boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }



    /**
     * 对bitmap进行灰度处理
     * @param bmpOriginal
     * @return
     */
    private Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * 压缩图片
     * @param path
     * @return
     */
    private Bitmap compressImage(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        scanBitmap = BitmapFactory.decodeFile(path,options);
        options.inJustDecodeBounds = false;
        int sampleSizeH = (int) (options.outHeight / (float) 800);
        int sampleSizeW = (int) (options.outWidth / (float) 800);
        int sampleSize = Math.max(sampleSizeH,sampleSizeW);
        if (sampleSize <= 0) {
            sampleSize = 1;
        }
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap qrbmp = BitmapFactory.decodeFile(path,options);
        return qrbmp;
    }


    public boolean deleteTempFile(String delFile) {
        File file = new File(delFile);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //震动提醒
    public void getVibrator(Context mContext){
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 50, 0, 0};
        vibrator.vibrate(pattern, -1);
    }

    /**
     * bitmap转base64
     *
     * @param @param  bitmap
     * @param @return 设定文件
     * @return String    返回类型
     * @throws
     * @Title: bitmapToBase64
     */
    @SuppressLint("NewApi")
    public static String bitmapToBase64(Bitmap bitmap) {

        // 要返回的字符串
        String reslut = null;

        ByteArrayOutputStream baos = null;

        try {

            if (bitmap != null) {

                baos = new ByteArrayOutputStream();
                /**
                 * 压缩只对保存有效果bitmap还是原来的大小
                 */
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);

                baos.flush();
                baos.close();
                // 转换为字节数组
                byte[] byteArray = baos.toByteArray();

                // 转换为字符串
                reslut = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return reslut;

    }

    /**
     * base64转bitmap
     *
     * @param @param  base64String
     * @param @return 设定文件
     * @return Bitmap    返回类型
     * @throws
     * @Title: base64ToBitmap
     */
    public static Bitmap base64ToBitmap(String base64String) {

        byte[] decode = Base64.decode(base64String, Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);

        return bitmap;
    }

}