import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.net.Socket;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

public class Camera {
	
	private static boolean DB_disablePostman = true;
	
	private Postman postman;
	private WebcamController webcamController;
		
	public Camera() throws Exception{
		this(4568);
	}
	public Camera(int port) throws Exception{
		if(!DB_disablePostman)
			postman = new Postman(new Socket("127.0.0.1",port));
		webcamController = new WebcamController();
		webcamController.startLive();
	}
	
	public int[] getWebcamResolution(){
		return webcamController.getResolution();
	}
	
	public Mat getColorImage() throws Exception{
		/*
		postman.send("getColorImage");
		
		Object obj = postman.recv();
		if(obj instanceof ColorfulImage){
			return ((ColorfulImage) obj).image;
		}else{
			throw new  InvalidClassException("The camera returns an "+obj.getClass().getName()+" object other than a colorful "+ColorfulImage.class.getSimpleName());
		}
		*/
		return webcamController.getLatestFrame();
	}
	
	public void close() throws Exception{
		if(webcamController!=null)
			webcamController.close();
		if(postman!=null)
			postman.send("close");
		
	}
	
	public DepthImage getDepthImage() throws Exception{
		if(DB_disablePostman)
			return new DepthImage(new byte[]{1,1,1,1},2, 2);
		postman.send("getDepthImage");
		Object obj = postman.recv();
		if(obj instanceof DepthImage){
			return ((DepthImage) obj);
		}else{
			throw new  InvalidClassException("The camera returns an "+obj.getClass().getName()+" object other than a depth "+DepthImage.class.getSimpleName());
		}
	}
	
		
	/**
	 * Convert BufferedImage to Mat.
	 * @param in BufferedImage
	 * @return Mat image
	 */
	public static Mat bufImg2Mat(BufferedImage in){
		 Mat out;
         byte[] data;
         int r, g, b;

         if(in.getType() == BufferedImage.TYPE_INT_RGB)
         {
             out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
             data = new byte[in.getWidth() * in.getHeight() * (int)out.elemSize()];
             int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
             for(int i = 0; i < dataBuff.length; i++)
             {
                 data[i*3] = (byte) ((dataBuff[i] >> 16) & 0xFF);
                 data[i*3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
                 data[i*3 + 2] = (byte) ((dataBuff[i] >> 0) & 0xFF);
             }
         }
         else if(in.getType() == BufferedImage.TYPE_3BYTE_BGR)
         {
             out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
             data = new byte[in.getWidth() * in.getHeight() * (int)out.elemSize()];
             int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
             for(int i = 0; i < dataBuff.length; i++)
             {
                 data[i*3 + 2] = (byte) ((dataBuff[i] >> 16) & 0xFF);
                 data[i*3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
                 data[i*3] = (byte) ((dataBuff[i] >> 0) & 0xFF);
             }
         }
         else
         {
             out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC1);
             data = new byte[in.getWidth() * in.getHeight() * (int)out.elemSize()];
             int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
             for(int i = 0; i < dataBuff.length; i++)
             {
               r = (byte) ((dataBuff[i] >> 16) & 0xFF);
               g = (byte) ((dataBuff[i] >> 8) & 0xFF);
               b = (byte) ((dataBuff[i] >> 0) & 0xFF);
               data[i] = (byte)((0.21 * r) + (0.71 * g) + (0.07 * b)); //luminosity
             }
          }
          out.put(0, 0, data);
          return out;
	}
	
	/**
	 * Convert Mat image to BufferedImage.
	 * @param mat Mat image
	 * @return BufferedImage
	 */
	public static BufferedImage mat2BufImg(Mat mat){
		MatOfByte matOfByte = new MatOfByte();
		Highgui.imencode(".jpg", mat, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		BufferedImage bufferedImage = null;
		try{
			InputStream in = new ByteArrayInputStream(byteArray);
			bufferedImage = ImageIO.read(in);
		}
		catch(Exception e){
			if(mat==null)
				System.err.println("Unable to convert null mat to BufferedImage.");
			else
				System.err.println("Unable to convert mat "+mat.toString()+" to BufferedImage.");
		}
		return bufferedImage;
	}

}

