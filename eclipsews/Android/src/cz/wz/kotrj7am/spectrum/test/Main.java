package cz.wz.kotrj7am.spectrum.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class Main {
	
	public static final String SOURCE = "testImages/";
	public static final String DEST = "decoded/";

	public static void main(String[] args) {
		System.out.println("test");
		File file = new File(SOURCE);
		if(file.isDirectory()){
			for(File f : file.listFiles()){
				if(f.getName().startsWith("img")){
					decode(f);
				}
			}
		} else {
			System.out.println("Source is not directory!");
		}
	}
	
	private static void decode(File file){

		String name = file.getName();
		int width = 640;
		int height = 480;
		
		try {
			byte[] data = readSmallBinaryFile(file.getPath());
			int[] rgbints1 = new int[width * height];
			int[] rgbints2 = new int[width * height];
			
			decodeYUV(rgbints1, data, width, height);
			// second looks better!
			YUV_NV21_TO_RGB(rgbints2, data, width, height);
			
			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			img.setRGB(0, 0, width, height, rgbints1, 0, width);
			

			BufferedImage img2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			img2.setRGB(0, 0, width, height, rgbints2, 0, width);
			
			
			ImageIO.write(img, "png", new File(DEST + name + "-1.png"));
			ImageIO.write(img2, "png", new File(DEST + name + "-2.png"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static byte[] readSmallBinaryFile(String aFileName) throws IOException {
	    Path path = Paths.get(aFileName);
	    return Files.readAllBytes(path);
	}
	

    /**
     * Decodes YUV frame to a buffer which can be use to create a bitmap. use
     * this for OS < FROYO which has a native YUV decoder decode Y, U, and V
     * values on the YUV 420 buffer described as YCbCr_422_SP by Android
     *
     * @param out
     *            the outgoing array of RGB bytes
     * @param fg
     *            the incoming frame bytes
     * @param width
     *            of source frame
     * @param height
     *            of source frame
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public static void decodeYUV(int[] out, byte[] fg, int width, int height) throws NullPointerException, IllegalArgumentException {
        int sz = width * height;
        if (out == null)
            throw new NullPointerException("buffer out is null");
        if (out.length < sz)
            throw new IllegalArgumentException("buffer out size " + out.length + " < minimum " + sz);
        if (fg == null)
            throw new NullPointerException("buffer 'fg' is null");
        if (fg.length < sz)
            throw new IllegalArgumentException("buffer fg size " + fg.length + " < minimum " + sz * 3 / 2);
        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = fg[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
                    Cb = fg[cOff];
                    if (Cb < 0)
                        Cb += 127;
                    else
                        Cb -= 128;
                    Cr = fg[cOff + 1];
                    if (Cr < 0)
                        Cr += 127;
                    else
                        Cr -= 128;
                }
                int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                if (R < 0)
                    R = 0;
                else if (R > 255)
                    R = 255;
                int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                if (G < 0)
                    G = 0;
                else if (G > 255)
                    G = 255;
                int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                if (B < 0)
                    B = 0;
                else if (B > 255)
                    B = 255;
                out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
            }
        }

    }

    public static void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height) {
        final int frameSize = width * height;

        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for (int i = 0, ci = ii; i < height; ++i, ci += di) {
            for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                int y = (0xff & ((int) yuv[ci * width + cj]));
                int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
                int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

}
