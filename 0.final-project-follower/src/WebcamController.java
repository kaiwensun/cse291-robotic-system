

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

/**
 * Webcam Controller.
 * @author Kaiwen Sun
 *
 */
public class WebcamController{
	
	private VideoCapture webcam;
	private String deviceName = "Astra Pro HD Camera";

	private volatile Boolean mWhileFetching = false;
	private final BlockingQueue<Mat> mJpegQueue = new ArrayBlockingQueue<Mat>(2);

	
	/**
	 * Take a picture. If fail, will try another 19 times.
	 * @param frame fill the taken picture to the frame. New frame will be created if frame is null.
	 * @return the frame of picture. null if finally fail.
	 */
	private Mat takePicture(Mat frame){
		if(frame==null)
			frame = new Mat();
		for(int i=0;i<20;i++){
			if (webcam.read(frame)){
				Imgproc.resize(frame, frame, new Size(frame.size().width/2,frame.size().height/2));
				return frame;
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				break;
			}
		}
		return null;
	}
	
	public int[] getResolution(){
		Mat f;
		if(mJpegQueue.size()>0){
			f = mJpegQueue.peek();
		}else{
			f = takePicture(null);
		}
		return new int[]{f.width(),f.height()};
	}
	
	/**
	 * Start live stream.
	 * @return true if success. false otherwise.
	 */
	public boolean startLive(){
		synchronized (mWhileFetching) {
			if (mWhileFetching) {
				System.err.println("webcam start() already starting.");
				return false;
			}
			mWhileFetching = true;
		}
		
		// A thread for retrieving liveview data from server.
		Thread thread = new Thread() {
			@Override
			public void run() {
				Mat frame = null;
				try {
					while (mWhileFetching) {
						frame = takePicture(null);
						if(frame==null){
							System.err.println("Fail to takePicture on Webcam");
						}
						else{
							if (mJpegQueue.size() == 2) {
								mJpegQueue.remove();
							}
							mJpegQueue.add(frame);
						}
					}
				} catch (Exception e) {
					System.err.println("Exception while fetching webcam image: " + e.getMessage());
				} finally {
					mJpegQueue.clear();
					mWhileFetching = false;
				}
			}
		};
		thread.start();
		return true;
	}
	
	/**
	 * get and remove the most recent frame from queue.
	 * waiting if necessary until there is at least one
	 * element in the queue.
	 * @return latest frame
	 */
	public Mat getLatestFrame(){
		try {
			return mJpegQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	/**
	 * Request to stop retrieving and drawing liveview data.
	 */
	public void stopLive() {
		synchronized (mWhileFetching) {
			mWhileFetching = false;
			mJpegQueue.clear();
		}
	}

	
	/**
	 * Judge if Webcam is on.
	 * @return true if Webcam is on, false otherwise.
	 */
	public boolean isOn(){
		return webcam.isOpened();
	}
	
	/**
	 * Try to let the webcam get ready by taking one picture.
	 * @return true if picture is successfully taken, inferring webcam is ready. false otherwise.
	 */
	private boolean getReady(){
		if(webcam==null)
			return false;
		Mat m = new Mat();		//trash bin
		for(int i=0;i<5;i++){
			webcam.read(m);
			if(m.width()!=0 && m.height()!=0){
				return true;
			}
		}
		return false;
		
	}
	
	/**
	 * Called when webcam is not found.
	 */
	private void onWebcamNotFound(){
		webcam = null;
		System.err.println("Webcam Not Found");
	}
	
	/**
	 * Constructor. Use webcam device according to Cfg.use_webcam_name and priority of webcam_name, video&lt;index&gt; and video0 
	 */
	public WebcamController(){
		String name = deviceName;
		try{
			webcam = new VideoCapture(name);
		}
		catch(Throwable e){
			webcam = null;
		}
		int index = -1;
		if(!getReady()){
			for(index=0;index<4;index++){
				webcam = discover(index);
				if(!getReady()){
					webcam = discover(0);
					if(!getReady()){
						onWebcamNotFound();
					}
				}else{
					break;
				}
			}
		}
	}
	
	
	/**
	 * Discover a Webcam device with the given index (usually /dev/video&lt;index&gt;)
	 * @param index Webcam index
	 * @return Webcam object matching the given index.
	 */
	public static VideoCapture discover(int index){
		try{
			return new VideoCapture(index);
		}
		catch(Throwable e){
			return null;
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

	/**
	 * Decide whether the webcam is valid. Check if webcam is null and isOpened.
	 * @return whether webcam is valid.
	 */
	public boolean isValid(){
		return webcam!=null && webcam.isOpened();
	}
	
	/**
	 * Release webcam.
	 */
	public void close(){
		stopLive();
		if(webcam!=null)
			webcam.release();
	}
	
	/**
	 * Decide whether images are being fetched from webcam.
	 * @return mWhileFetching
	 */
	public boolean isFetching(){
		return this.mWhileFetching;
	}
	
	/**
	 * Display mat image in a new window. Used only for debug. 
	 * @param mat image to show.
	 */
	public static void debug_imshow(Mat mat){
		Imgproc.resize(mat, mat, new Size(640,480));
		
		try{
			BufferedImage bufferedImage = mat2BufImg(mat);
			debug_imshow(bufferedImage);
		}
		catch(Exception e){
			if(mat==null)
				System.err.println("mat is null");
			else
				System.err.println("Unable to imshow mat "+mat.toString());
		}
		
	}
	
	/**
	 * Display BufferedImage image in a new window. Used only for debug. 
	 * @param img image to show.
	 */
	public static void debug_imshow(BufferedImage img){
		try{
			BufferedImage bufferedImage = img;
			JFrame frame = new JFrame();
			frame.getContentPane().add(new JLabel(new ImageIcon(bufferedImage)));
			frame.pack();
			frame.setVisible(true);
		}
		catch(Exception e){
			System.err.println("Unable to imshow img.");
		}
		
	}
	
	private static JFrame debug_frame = null;
	private static JLabel debug_label = null;
	
	/**
	 * Show video based on given images.
	 * @param mat a Mat frame of video.
	 */
	public static void debug_videoShow(Mat mat){
		Imgproc.resize(mat, mat, new Size(640,480));
		
		try{
			BufferedImage bufferedImage = mat2BufImg(mat);
			debug_videoShow(bufferedImage);
		}
		catch(Exception e){
			if(mat==null)
				System.err.println("mat is null");
			else
				System.err.println("Unable to imshow mat "+mat.toString());
		}		
	}
	
	/**
	 * Show video based on given images.
	 * @param img a BufferedImage frame of video.
	 */
	public static void debug_videoShow(BufferedImage img){
		try{
			BufferedImage bufferedImage = img;
			if(debug_frame==null){
				debug_frame = new JFrame();
				debug_label = new JLabel(new ImageIcon(bufferedImage));
				debug_frame.getContentPane().add(debug_label);
				debug_frame.pack();
				debug_frame.setVisible(true);
			}
			else{
				debug_label.setIcon(new ImageIcon(bufferedImage));
			}
		}
		catch(Exception e){
			System.err.println("Unable to imshow img.");
		}
		
	}
}

