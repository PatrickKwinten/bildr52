/*
 * IMPORTANT
 * 
 * This class is also used by the ImportPictures agent. An exact copy of it should be maintained in the Code section of this database
 */
package org.openntf.bildr;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.ImageIO;

public class ScaledImage {
	
	public static final int MODE_RESIZE_MAX_VALUES_XY = 1;	//Enter max values for x and y
	public static final int MODE_RESIZE_MAX_LONGER = 2;		//Enter a max value for the longer side
	public static final int MODE_RESIZE_MAX_SHORTER = 3;	//Enter a max value for the shorter side
	public static final int MODE_RESIZE_PERCENT = 4;		//Resize by percent value
	private int resizeMode = 0;
	
	private static final int	DEFAULT_IMAGE_TYPE	= BufferedImage.TYPE_INT_RGB;
	private Image				image				= null;
	private int					srcImageHeight;
	private int					srcImageWidth;
	public String				srcFilePath			= null;
	public String				srcFileName			= null;	
	
	private int width_temp = 0;
	private int height_temp = 0;
	
	private int					targetImageHeight;
	private int					targetImageWidth;
	private String				targetFileName;
	private long				targetFileLength;
	private boolean				targetCrop			= false;		
	
	private String				outputFormat		= "jpg";

	private boolean				debug				= false;

	private boolean				higherQuality		= false;
	
	private Vector images = new Vector();
	
	public ScaledImage (InputStream in, String fileName) {
		try {
			image = ImageIO.read(in);
			srcFileName = fileName;
			this.getSourceImageParameters();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * path = path where the image can be found
	 * fileName = fileName of the image
	 */
	public ScaledImage(String path, String fileName) {
		try {
			this.srcFilePath = path;
			this.srcFileName = fileName;
			this.image = ImageIO.read(new File(this.srcFilePath + this.srcFileName));
			this.getSourceImageParameters();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * fileName = path and filename of the source image file
	 */
	public ScaledImage(String fileName) {
		try {
			//split path and fileName
			int lastDirSep = fileName.lastIndexOf(File.separator);
			
			this.srcFilePath = fileName.substring(0, lastDirSep+1);
			this.srcFileName = fileName.substring( lastDirSep + 1);
			image = ImageIO.read(new File(fileName));
			this.getSourceImageParameters();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void setResizeMode(int resizeMode, int x, int y) {
		
		//this.log("set resize mode to " + resizeMode + " (x: "+ x + ", y: " + y + ")");
		 
		switch( resizeMode) {
			case MODE_RESIZE_MAX_VALUES_XY:
				this.resizeMode = resizeMode;
				
				double scaleH = (double)y / (double)srcImageHeight;
				double scaleW = (double)x / (double)srcImageWidth;
				
				double scale = Math.min(scaleH, scaleW);
	
				width_temp = (int) (srcImageWidth * scale);
				height_temp = (int) (srcImageHeight * scale);
				
				//target size is square, but input file is not: use crop method
				if ( ((double)y/(double)x) == 1 && ((double)srcImageHeight/(double)srcImageWidth) != 1) {
					targetCrop = true;
				}
	
				break;
			case MODE_RESIZE_MAX_LONGER:		//max value for the longer side
				this.resizeMode = resizeMode;
				if (srcImageWidth > srcImageHeight) {
					width_temp = x;
					height_temp = (int) (srcImageHeight * ((double)x / (double)srcImageWidth));
				} else {
					width_temp = (int) (srcImageWidth * ((double)x / (double)srcImageHeight));
					height_temp = x;
				}
				break;
				
			case MODE_RESIZE_MAX_SHORTER:		//max value for the shorter side
				this.resizeMode = resizeMode;
				if (srcImageWidth < srcImageHeight) {	//tall image
					width_temp = x;
					height_temp = (int) (srcImageHeight * ((double)x / (double)srcImageWidth));;
				} else {		//width image
					width_temp = (int) (srcImageWidth * ((double)x / (double)srcImageHeight));
					height_temp = x;
				}
				break;
			
			case MODE_RESIZE_PERCENT:
				this.resizeMode = resizeMode;
				width_temp = (int) (srcImageWidth * ((double)x/(double)100));
				height_temp = (int) (srcImageHeight * ((double)x/(double)100));
				break;
		}
		
	}
	/*
	 * start the resize process, writes the output file
	 */
	public File resize(String outputFile) throws IOException {
		
		File saveFile = null;
		
		if (resizeMode>0) {
			saveFile = this.saveScaledInstance(width_temp, height_temp, outputFile);
		} 
		
		return saveFile;
	}
	
	/*
	 * rotate an image 90 degrees clockwise
	 */
	public void rotate(String outputFile) throws IOException {
		//this.log("rotating file " + outputFile);
		//this.log("source width: " + this.getSourceImageWidth() + ", height: " + this.getSourceImageHeight() );
		
		File saveFile = new File(outputFile);
		
		BufferedImage bufImageSrc = (BufferedImage) image;
		
		BufferedImage bufImage = new BufferedImage(this.getSourceImageHeight(), this.getSourceImageWidth(), DEFAULT_IMAGE_TYPE);
		Graphics2D g2 = bufImage.createGraphics();
		g2.rotate(Math.toRadians(90.0));
		g2.drawImage(bufImageSrc, 0, -bufImage.getWidth(null), null);
		g2.dispose();

		ImageIO.write(bufImage, this.outputFormat, saveFile);
		//this.log("rotated image written");
		
		this.images.add(saveFile);		//for cleanup purposes later
	}
	
	
	private void getSourceImageParameters() {
		this.srcImageWidth = image.getWidth(null);
		this.srcImageHeight = image.getHeight(null);
	}
	
	public int getSourceImageWidth() {
		return this.srcImageWidth;
	}
	public int getSourceImageHeight() {
		return this.srcImageHeight;
	}
	public int getTargetImageWidth() {
		return this.targetImageWidth;
	}
	public int getTargetImageHeight() {
		return this.targetImageHeight;
	}
	public String getTargetFileName() {
		return targetFileName;
	}

	public long getTargetFileLength() {
		return targetFileLength;
	}
	
	/*remove all created files from disk*/
	public void cleanup() {
		Iterator it = this.images.iterator();
		
		while (it.hasNext()) {
			File f = (File) it.next();
			f.delete();
			//this.log("cleanup - removed file " + f.toString());
		}
	}
	
	/**
	 * Shrinks or enlarges the current image so that the size of it (in pixels)
	 * is the lesser of the height and width dictated by the parameters.<p>
	 * For example, if the image has to be enlarged by a factor of 60% in order to be
	 * the given height, and it has to be enlarged by a factor of 80% in order to be 
	 * the given width, then the image will be enlarged by 60% (the lesser of the two).
	 * Use this method if you need to make sure that an image is <i>no larger</i> than the given
	 * height or width.
	 *
	 * @param  height    scale the image to at most this height
	 * @param  width    scale the image to at most this width
	 * @param	fileName	path and filename where the image will be stored
	 */
	public File saveScaledInstanceMinWidthHeight(int width, int height, String fileName) throws IOException
	{
		double scaleH = (double)height / (double)this.srcImageHeight;
		double scaleW = (double)width / (double)this.srcImageWidth;
		
		double scale = Math.min(scaleH, scaleW);
		
		targetImageWidth = (int) (srcImageWidth * scale);		
		targetImageHeight = (int) (srcImageHeight * scale);
		
		File saveFile = this.saveScaledInstance(targetImageWidth, targetImageHeight, fileName);
		
		return saveFile;
	}

	public File saveScaledInstance(int targetWidth, int targetHeight, String filePath) throws IOException {
		
		File saveFile = null;
		
		if (image != null) {
			targetImageWidth = targetWidth;
			targetImageHeight = targetHeight;
			saveFile = this.writeFile(filePath);
		}
	
		return saveFile;
	}
	
	private File writeFile(String filePath ) throws IOException {
		//this.log("writing file (target width:  " + targetImageWidth + ", target height: " + targetImageHeight + ")");
		
		File saveFile = new File(filePath);
		
		//this.log("target file: " + filePath);
		
		BufferedImage bufScaledImage = getScaledInstance(targetImageWidth, targetImageHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, this.higherQuality);

		if (bufScaledImage != null) {
			//this.log("buffered image retrieved, write file");
			ImageIO.write(bufScaledImage, this.outputFormat, saveFile);
			//this.log("file written");
		}
		
		this.images.add(saveFile);
		this.targetFileLength = saveFile.length();
		this.targetFileName = saveFile.getName();
		
		return saveFile;
	}

	/**
	 * Convenience method that returns a scaled instance of the provided {@code BufferedImage}.
	 * 
	 * @param img
	 *            the original image to be scaled
	 * @param targetWidth
	 *            the desired width of the scaled instance, in pixels
	 * @param targetHeight
	 *            the desired height of the scaled instance, in pixels
	 * @param hint
	 *            one of the rendering hints that corresponds to {@code RenderingHints.KEY_INTERPOLATION} (e.g. {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR}, {@code
	 *            RenderingHints.VALUE_INTERPOLATION_BILINEAR}, {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	 * @param higherQuality
	 *            if true, this method will use a multi-step scaling technique that provides higher quality than the usual one-step technique (only useful in downscaling cases,
	 *            where {@code targetWidth} or {@code targetHeight} is smaller than the original dimensions, and generally only when the {@code BILINEAR} hint is specified)
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	private BufferedImage getScaledInstance (int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
		
		BufferedImage bufScaledImage = null;
		
		try {
			int type = DEFAULT_IMAGE_TYPE;
	
			//this.log("source width: " + srcImageWidth + ", height: " + srcImageHeight );
				
			if (this.targetCrop && srcImageHeight != srcImageWidth) {	//crop non-square images
				
				//this.log("crop target image");
				
				int x, y, size;
				
				if (srcImageHeight > srcImageWidth) {		//tall image
					
					x = 0;
					y = ((srcImageHeight - srcImageWidth) / 2);
					size = srcImageWidth;
					
				} else {		//wide image
					
					x = ((srcImageWidth - srcImageHeight) / 2);
					y = 0;
					size = srcImageHeight;
				
				}
				
				targetWidth=Math.max(targetWidth, targetHeight);
				targetHeight=Math.max(targetWidth, targetHeight);
				
				targetImageWidth = targetWidth;
				targetImageHeight = targetHeight;
							
				bufScaledImage = ((BufferedImage) image).getSubimage(x,y,size,size);
				
			} else {
				
				bufScaledImage = (BufferedImage) image;
				
			}
			
			int w, h;

			if (higherQuality && (
				srcImageWidth > targetWidth ||
				srcImageHeight > targetHeight)) {
				
				// Use multi-step technique: start with original size, then
				// scale down in multiple passes with drawImage()
				// until the target size is reached
				w = srcImageWidth;
				h = srcImageWidth;
			} else {
				// Use one-step technique: scale directly from original
				// size to target size with a single drawImage() call
				w = targetWidth;
				h = targetHeight;
			}
	
			do {
				if (higherQuality && w > targetWidth) {
					w /= 2;
					if (w < targetWidth) {
						w = targetWidth;
					}
				}
	
				if (higherQuality && h > targetHeight) {
					h /= 2;
					if (h < targetHeight) {
						h = targetHeight;
					}
				}
	
				BufferedImage bufImage = new BufferedImage(w, h, type);
				Graphics2D g2 = bufImage.createGraphics();
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
				
				g2.drawImage(bufScaledImage, 0, 0, w, h, null);
				
				g2.dispose();
	
				bufScaledImage = bufImage;
			} while (w != targetWidth || h != targetHeight);
		
		} catch (Exception e) {
			
		} finally {
			
		}

		return bufScaledImage;
	}
	
	private void log(String message) {
		if (this.debug) {
			//System.out.println("ScaledImage: " + message);
		}
	}
	
	/*
	 * set to true to send debug messages to the system console
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/*
	 * set to true to increase the quality of the scaled image
	 */
	public void setHigherQuality(boolean higherQuality) {
		this.higherQuality = higherQuality;
	}
	
}