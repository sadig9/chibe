package org.gvt.util.onotoa;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * @author Ozgun Babur
 */
public class ImageConverter {

	private static final PaletteData PALETTE_DATA = new PaletteData(0xFF0000, 0xFF00, 0xFF);

	/**
	 * Converts an AWT based buffered image into an SWT <code>Image</code>.  This will always return an
	 * <code>Image</code> that has 24 bit depth regardless of the type of AWT buffered image that is
	 * passed into the method.
	 *
	 * @param srcImage the {@link java.awt.image.BufferedImage} to be converted to an <code>Image</code>
	 * @return an <code>Image</code> that represents the same image data as the AWT
	 * <code>BufferedImage</code> type.
	 */
	public static Image convert( BufferedImage srcImage) {
		// We can force bitdepth to be 24 bit because BufferedImage getRGB allows us to always
		// retrieve 24 bit data regardless of source color depth.
		ImageData swtImageData =
			new ImageData(srcImage.getWidth(), srcImage.getHeight(), 24, PALETTE_DATA);

		// ensure scansize is aligned on 32 bit.
		int scansize = (((srcImage.getWidth() * 3) + 3) * 4) / 4;

		WritableRaster alphaRaster = srcImage.getAlphaRaster();
		byte[] alphaBytes = new byte[srcImage.getWidth()];

		for (int y=0; y<srcImage.getHeight(); y++) {
			int[] buff = srcImage.getRGB(0, y, srcImage.getWidth(), 1, null, 0, scansize);
			swtImageData.setPixels(0, y, srcImage.getWidth(), buff, 0);

			// check for alpha channel
			if (alphaRaster != null) {
				int[] alpha = alphaRaster.getPixels(0, y, srcImage.getWidth(), 1, (int[])null);
				for (int i=0; i<srcImage.getWidth(); i++)
					alphaBytes[i] = (byte)alpha[i];
				swtImageData.setAlphas(0, y, srcImage.getWidth(), alphaBytes, 0);
			}
		}

		return new Image(Display.getCurrent(), swtImageData);
	}

	/**
	 * Converts an swt based image into an AWT <code>BufferedImage</code>.  This will always return a
	 * <code>BufferedImage</code> that is of type <code>BufferedImage.TYPE_INT_ARGB</code> regardless of
	 * the type of swt image that is passed into the method.
	 *
	 * @param srcImage the {@link org.eclipse.swt.graphics.Image} to be converted to a <code>BufferedImage</code>
	 * @return a <code>BufferedImage</code> that represents the same image data as the swt <code>Image</code>
	 */
	public static BufferedImage convert( Image srcImage ) {

		ImageData imageData = srcImage.getImageData();
		int width = imageData.width;
		int height = imageData.height;
		ImageData maskData = null;
		int alpha[] = new int[1];

		if (imageData.alphaData == null)
			maskData = imageData.getTransparencyMask();

		// now we should have the image data for the bitmap, decompressed in imageData[0].data.
		// Convert that to a Buffered Image.
		BufferedImage image = new BufferedImage( imageData.width, imageData.height, BufferedImage.TYPE_INT_ARGB );

		WritableRaster alphaRaster = image.getAlphaRaster();

		// loop over the imagedata and set each pixel in the BufferedImage to the appropriate color.
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				RGB color = imageData.palette.getRGB(imageData.getPixel(x, y));
				image.setRGB( x, y, new java.awt.Color(color.red, color.green, color.blue).getRGB());

				// check for alpha channel
				if (alphaRaster != null) {
					if( imageData.alphaData != null) {
						alpha[0] = imageData.getAlpha( x, y );
						alphaRaster.setPixel( x, y, alpha );
					}
					else {
						// check for transparency mask
						if( maskData != null) {
							alpha[0] = maskData.getPixel( x, y ) == 0 ? 0 : 255;
							alphaRaster.setPixel( x, y, alpha );
						}
					}
				}
			}
		}

		return image;
	}

}
