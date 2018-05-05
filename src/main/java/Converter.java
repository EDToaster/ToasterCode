
import sun.security.util.BitArray;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class Converter {

	private static int BORDER_WIDTH = 5;

	private static int BLACK = 0xff000000;
	private static int TRANS = 0x00ffffff;
	private static int WHITE = 0xffffffff; // BLACK IS 1, WHITE IS 0

	public static BufferedImage convertToToaster(String strData, boolean useTransparency, int x) {
		BitSet bs = BitSet.valueOf(strData.getBytes());
		BitSet data = message(bs, x);

		int length = data.length();
		int gridWidth = (int) Math.ceil(Math.sqrt(length));

		gridWidth += gridWidth % 2 == 0 ? 0 : 1;

		int trueWidth = gridWidth + BORDER_WIDTH * 2;

		BufferedImage img = new BufferedImage(
				trueWidth,
				trueWidth,
				TYPE_INT_ARGB);

		int[] imageData = new int[trueWidth * trueWidth];
		fillBlank(imageData, useTransparency);
		createBorder(imageData, trueWidth, useTransparency);
		fillData(imageData, data, BORDER_WIDTH, BORDER_WIDTH, gridWidth, trueWidth, useTransparency);

		img.setRGB(0, 0, trueWidth, trueWidth, imageData, 0, trueWidth);
		return img;

	}


	private static void fillBlank(int[] pixelData, boolean useTransparency) {
		for (int i = 0; i < pixelData.length; i++) pixelData[i] = useTransparency ? TRANS : WHITE;
	}

	private static void fillData(int[] imageData, BitSet data, int startX, int startY, int gridWidth, int totalWidth, boolean useTransparency) {
		for (int i = 0; i < data.length(); i++) {
			int xBase = i % gridWidth;
			int yBase = i / gridWidth;
			boolean d = data.get(i);
			set(imageData, d, xBase + startX, yBase + startY, totalWidth, useTransparency);
		}
	}

	private static void createBorder(int[] pixelData, int totalWidth, boolean useTransparency) {
		int borderHalf = (BORDER_WIDTH - 1) / 2;
		Point[] points = {
				new Point(borderHalf, borderHalf),
				new Point(borderHalf, totalWidth - borderHalf - 1),
				new Point(totalWidth - borderHalf - 1, totalWidth - borderHalf - 1)
		};

		createFourCorners(pixelData, points, totalWidth, useTransparency);
		createAuxillaryBorder(pixelData, totalWidth, useTransparency);
		//TODO: fill formatting data
	}

	private static void createFourCorners(int[] pixelData, Point[] points, int totalWidth, boolean useTransparency) {
		for (Point p : points) {
			setBlackBorder(p.x - 2, p.y - 2, 5, 5, pixelData, totalWidth);
			set(pixelData, true, p.x, p.y, totalWidth, useTransparency); // set top row to black
		}
	}

	private static void createAuxillaryBorder(int[] pixelData, int totalWidth, boolean useTransparency) {
		int borderWidth = totalWidth - 4;

		setBlackBorder(0, 4, borderWidth, borderWidth, pixelData, totalWidth);
		setDottedBorder(4, 0, borderWidth, borderWidth, pixelData, totalWidth);
	}

	private static void setBlackBorder(int xOffset, int yOffset, int width, int height, int[] pixelData, int totalWidth) {
		for (int y = 0; y < height; y++) {
			if (y == 0) {
				for (int x = 0; x < width; x++) {
					set(pixelData, true, x + xOffset, y + yOffset, totalWidth, true); // set top row to black
				}
			} else if (y == height - 1) {
				for (int x = 0; x < width; x++) {
					set(pixelData, true, x + xOffset, y + yOffset, totalWidth, true); // set bottom row to black
				}
			} else {
				set(pixelData, true, xOffset, y + yOffset, totalWidth, true); // set left right to black
				set(pixelData, true, width - 1 + xOffset, y + yOffset, totalWidth, true);
			}
		}
	}

	private static void setDottedBorder(int xOffset, int yOffset, int width, int height, int[] pixelData, int totalWidth) {
		for (int y = 0; y < height; y++) {
			if (y == 0) {
				for (int x = 1; x < width; x += 2) {
					set(pixelData, true, x + xOffset, y + yOffset, totalWidth, true); // set top row to black
				}
			} else if (y == height - 1) {
				for (int x = 0; x < width; x += 2) {
					set(pixelData, true, x + xOffset, y + yOffset, totalWidth, true); // set bottom row to black
				}
			} else {
				if (y % 2 == 1) //odd
					set(pixelData, true, xOffset, y + yOffset, totalWidth, true); // set left right to black
				else set(pixelData, true, width - 1 + xOffset, y + yOffset, totalWidth, true);
			}
		}
	}

	private static void set(int[] pixelData, boolean data, int x, int y, int totalWidth, boolean useTransparency) {
		pixelData[getArrayIndex(x, y, totalWidth)] = data ? BLACK : (useTransparency ? TRANS : WHITE);
	}

	private static int getArrayIndex(int x, int y, int totalWidth) {
		return x + y * totalWidth;
	}

	private static class Point {
		public int x, y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	public static void main(String[] args) {

		String str = "This is a sample code of the Toastercode variety!";

		BufferedImage img = convertToToaster(str, false, 3);
		try {
			File outputFile = new File("saved.png");
			ImageIO.write(img, "png", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static BitSet message(BitSet data, int x) {
		BigInteger sum = BigInteger.ZERO;
		BigInteger xTerm = BigInteger.valueOf(x);

		int k = data.length();

		for (int i = 0; i < k; i++) {
			BigInteger term = xTerm.pow(i);
			if (data.get(i)) {
				sum = sum.add(term);
			}
		}
		return BitSet.valueOf(sum.toByteArray());
	}

}
