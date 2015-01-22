package com.tues.sando.equalizator;

import android.graphics.Bitmap;

public class HistogramEqualizator {
    public static Bitmap histogram_equalization(Bitmap image2) {
        int wt = (image2.getWidth()) / 10;
        int ht = (image2.getHeight()) / 10;
        Bitmap image = Bitmap.createScaledBitmap(image2, wt, ht, false);

        int width = image.getWidth();
        int height = image.getHeight();

        final int LEVEL = 256;
        int[] lred = new int[LEVEL];
        int[] lgreen = new int[LEVEL];
        int[] lblue = new int[LEVEL];
        double[] pr = new double[LEVEL];
        double[] pg = new double[LEVEL];
        double[] pb = new double[LEVEL];

        double[] sr = new double[LEVEL];
        double[] sg = new double[LEVEL];
        double[] sb = new double[LEVEL];

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int pix = 0;
                int alpha = 0xff & (image.getPixel(w, h) >> 24);
                int red = 0xff & (image.getPixel(w, h) >> 16);
                int green = 0xff & (image.getPixel(w, h) >> 8);
                int blue = 0xff & image.getPixel(w, h);
                //mounting a histogram to red
                //we gonna go to count the number of each gray level
                lred[red]++;
                lgreen[green]++;
                lblue[blue]++;
            }//w
        }//h

        //get all PDF = probability distribution function
        //of the normalized histogram
        for (int h = 0; h < height; h++) {
            pr[h] = (double) lred[h] / ((double) height * (double) width);
            pg[h] = (double) lgreen[h] / ((double) height * (double) width);
            pb[h] = (double) lblue[h] / ((double) height * (double) width);
        }

        //now we need to map to s domain
        for (int h = 0; h < height; h++) {
            //mapping the red color
            sr[h] = 0;
            for (int j = 0; j < 256; j++) {
                sr[h] = sr[h] + pr[j];
            }
            sr[h] = (LEVEL - 1) * (sr[h]);

            //mapping the green color
            sg[h] = 0;
            for (int j = 0; j < 256; j++) {
                sg[h] = sg[h] + pg[j];
            }
            sg[h] = (LEVEL - 1) * (sg[h]);

            //mapping the blue color
            sb[h] = 0;
            for (int j = 0; j < 256; j++) {
                sb[h] = sb[h] + pb[j];
            }
            sb[h] = (LEVEL - 1) * (sb[h]);
        }

        //digital levels so round the values
        for (int h = 0; h < height; h++) {
            sr[h] = Math.round(sr[h]);
            sg[h] = Math.round(sg[h]);
            sb[h] = Math.round(sb[h]);

        }


        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int pix = 0;
                int alpha = 0xff & (image.getPixel(w, h) >> 24);
                int red = 0xff & (image.getPixel(w, h) >> 16);
                int green = 0xff & (image.getPixel(w, h) >> 8);
                int blue = 0xff & image.getPixel(w, h);

                //if (red == l)
                {
                    red = (int) sr[red];
                }
                //if (green == l)
                {
                    green = (int) sg[green];
                }
                //if(blue == l)
                {
                    blue = (int) sb[blue];
                }

                pix = pix | blue;
                pix = pix | (green << 8 );
                pix = pix | (red << 16 );
                pix = pix | (alpha << 24 );
                image.setPixel(w, h, pix);
                pix = 0;
            }
        }
        return image;
    }//histogram equalization
}
