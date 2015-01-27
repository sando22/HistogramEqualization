package com.tues.sando.equalizator;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Arrays;

public class HistogramEqualizator {
    public static Bitmap histogram_equalization(Bitmap originalBitmap) {

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        Bitmap processedImage = Bitmap.createBitmap(width, height, originalBitmap.getConfig());

        int A = 0, R, G, B;
        int pixel;
        float[][] Y = new float[width][height];
        float[][] U = new float[width][height];
        float[][] V = new float[width][height];
        int[] histogram = new int[256];
        Arrays.fill(histogram, 0);

        int[] cdf = new int[256];
        Arrays.fill(cdf, 0);
        float min = 257;
        float max = 0;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                pixel = originalBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                Y[x][y] = 0.299f * R + 0.587f * G + 0.114f * B;
                U[x][y] = 0.565f * (B - Y[x][y]);
                V[x][y] = 0.713f * (R - Y[x][y]);
                histogram[(int) Y[x][y]] += 1;
                if (Y[x][y] < min) {
                    min = Y[x][y];
                }
                if (Y[x][y] > max) {
                    max = Y[x][y];
                }
            }
        }

        cdf[0] = histogram[0];
        for (int i = 1; i <= 255; i++) {
            cdf[i] = cdf[i - 1] + histogram[i];
        }

        float minCDF = cdf[(int) min];
        float denominator = width * height - minCDF;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                pixel = originalBitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                Y[x][y] = ((cdf[(int) Y[x][y]] - minCDF) / (denominator)) * 255;

                R = minMaxCalc(Y[x][y] + 1.140f * V[x][y]);
                G = minMaxCalc(Y[x][y] - 0.344f * U[x][y] - 0.714f * V[x][y]);
                B = minMaxCalc(Y[x][y] + 1.77f * U[x][y]);
                processedImage.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return processedImage;
    }

    private static int minMaxCalc(float current) {
        if (current < 0){
            current = 0;
        }else if (current > 255){
            current = 255;
        }
        return (int) current;
    }
}
