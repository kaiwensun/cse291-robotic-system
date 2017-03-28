

import java.awt.Window.Type;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;


/**
 * Video roi tracker.
 * @author Kaiwen Sun
 *
 */
public class Tracker {
	
	private Mat roi;	//region of interest
	private Mat frame;	//video full frame
	private Rect roi_posi;	//start to iteratively search roi from roi_posi
	private final Rect default_roi_posi;	//roi default initial position
	private final Mat disc;	//filter core
	private final float thre1 = 100f;
	private final float thre2 = 20f;
	private final float thre3 = 30f;
	private boolean useHsv;
	
	private Mat roihistR;
	private Mat roihistG;
	private Mat roihistB;
	
	
	/**
	 * Constructor. The initRect is the default position from which the
	 * tracker starts to iteratively search the region of interest when
	 * the ROI was lost.
	 * @param initRect default initial position of ROI
	 */
	public Tracker(final Rect initRect, boolean useHsv){
		this.useHsv = useHsv;
		default_roi_posi = initRect;
		disc = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
		disc.convertTo(disc, CvType.CV_32F);
		Core.divide(1/21f, disc, disc);	//normalize to sum to 1.
	}
	
	public Rect getRoiRect(){
		return roi_posi;
	}
	/**
	 * Set the interesting target object and initial position starting to search.
	 * @param target interesting target
	 * @param position initial position of searching. If null, use the default position set by constructor.
	 * @return region of interest rectangle. null if not found.
	 */
	public RotatedRect setRoi(Mat target, Rect position){
		roi = target;
		if(position!=null)
			roi_posi = position;
		else
			roi_posi = default_roi_posi.clone();
		Mat hsvt = new Mat();
		if(useHsv)
			Imgproc.cvtColor(roi, hsvt, Imgproc.COLOR_RGB2HLS);
		else
			hsvt = roi;
		
		List<Mat> lRgb = new ArrayList<Mat>(3);
		Core.split(hsvt, lRgb);
		Mat mR = lRgb.get(0);
		Mat mG = lRgb.get(1);
		Mat mB = lRgb.get(2);
		roihistR = new Mat();
		roihistG = new Mat();
		roihistB = new Mat();
		LinkedList<Mat> list = new LinkedList<Mat>();
		list.add(mR);
		Imgproc.calcHist(list, new MatOfInt(0), new Mat(), roihistR, new MatOfInt(256), new MatOfFloat(0,255));
		Imgproc.GaussianBlur(roihistR, roihistR, new Size(1, 9), 0, 10, Imgproc.BORDER_CONSTANT);
		Core.normalize(roihistR, roihistR, 0, 255, Core.NORM_MINMAX);
		
		//list.set(0, mG);
		//Imgproc.calcHist(list, new MatOfInt(0), new Mat(), roihistG, new MatOfInt(256), new MatOfFloat(0,255));
		//Imgproc.GaussianBlur(roihistG, roihistG, new Size(1, 9), 0, 10, Imgproc.BORDER_CONSTANT);
		//Core.normalize(roihistG, roihistG, 0, 255, Core.NORM_MINMAX);
		
		list.set(0, mB);
		Imgproc.calcHist(list, new MatOfInt(0), new Mat(), roihistB, new MatOfInt(256), new MatOfFloat(0,255));
		Imgproc.GaussianBlur(roihistB, roihistB, new Size(1, 9), 0, 10, Imgproc.BORDER_CONSTANT);
		Core.normalize(roihistB, roihistB, 0, 255, Core.NORM_MINMAX);		
		//System.err.println("roihistR="+roihistR.dump()+";");
		//System.err.println("roihistG="+roihistG.dump()+";");
		//System.err.println("roihistB="+roihistB.dump()+";");
		return track();
	}

	/**
	 * Tell the latest video frame to the tracker and update the ROI position. 
	 * @param frame the latest video frame
	 * @return ROI position
	 */
	public RotatedRect updateFrame(final Mat frame){
		this.frame = frame;
		return track();
	}
	
	/**
	 * Update ROI's position in the frame.
	 * @return ROI's position. return null if ROI's position is not detected, or if either frame or roi target is null; 
	 */
	private RotatedRect track(){
		if(frame==null || roi==null){
			return null;
		}
		if(roi_posi.width<=1 || roi_posi.height<=1){
			roi_posi = default_roi_posi.clone();
		}
		if(frame==null || frame.width()==0)
			return null;
		Mat hsv = new Mat();
		if(useHsv)
			Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HLS);
		else
			hsv = frame;
		
		List<Mat> lRgb = new ArrayList<Mat>(3);
		Core.split(hsv, lRgb);
		Mat hsvR = lRgb.get(0);
		//Mat hsvG = lRgb.get(1);
		Mat hsvB = lRgb.get(2);
		
		Mat dstR = new Mat();
		//Mat dstG = new Mat();
		Mat dstB = new Mat();
		
		List<Mat> list = new ArrayList<Mat>(0); 
		list.add( hsvR);
		Imgproc.calcBackProject(list, new MatOfInt(0), roihistR, dstR, new MatOfFloat(0,255), 1);
		//list.set(0, hsvG);
		//Imgproc.calcBackProject(list, new MatOfInt(0), roihistG, dstG, new MatOfFloat(0,255), 1);
		list.set(0, hsvB);
		Imgproc.calcBackProject(list, new MatOfInt(0), roihistB, dstB, new MatOfFloat(0,255), 1);
		//blur
		
		Mat avg =Mat.ones(new Size(5,5), CvType.CV_32F);
		Core.divide(1/25f, avg, avg);	//normalize to sum to 1.
		Imgproc.filter2D(dstR, dstR, -1, avg);
	 	//Imgproc.filter2D(dstG, dstG, -1, avg);
	 	Imgproc.filter2D(dstB, dstB, -1, avg);
		
		Imgproc.filter2D(dstR, dstR, -1, disc);
	 	//Imgproc.filter2D(dstG, dstG, -1, disc);
	 	Imgproc.filter2D(dstB, dstB, -1, disc);
	 	
	 	
	 	Imgproc.GaussianBlur(dstR, dstR, new Size(5, 5), 5, 5, Imgproc.BORDER_CONSTANT);
	 	Imgproc.GaussianBlur(dstB, dstB, new Size(5, 5), 5, 5, Imgproc.BORDER_CONSTANT);
	 	
	 	
	 	Imgproc.threshold(dstR, dstR, thre1, 255, Imgproc.THRESH_BINARY);
	 	//Imgproc.threshold(dstG, dstG, thre2, 255, Imgproc.THRESH_BINARY);
	 	Imgproc.threshold(dstB, dstB, thre3, 255, Imgproc.THRESH_BINARY);
	 	
	 	Mat dst = new Mat();
	 	Core.bitwise_and(dstR, dstB, dst);
	 	//Core.bitwise_and(dst,dstB, dst);
	 	
	 	
	 	// debug start
	 	//Mat m = dst.clone();
	 	//VideoPlayer.debug_videoShow(m);
	 	// debug stop
	 	
	 	//old cretia 0.001
	 	RotatedRect rotate_roi_posi = Video.CamShift(dst, roi_posi, new TermCriteria(TermCriteria.MAX_ITER|TermCriteria.EPS, 50, 0.001));
	 	
		roi_posi = rotate_roi_posi.boundingRect();
		//Core.rectangle(frame, roi_posi.tl(), roi_posi.br(), new Scalar(0, 0, 255));
		if(roiIsValid())
			//return roi_posi;
			return rotate_roi_posi;
		return null;
	}
	private boolean roiIsValid(){
		if(roi_posi==null)
			return false;
		if(roi_posi.x==0 && roi_posi.y==0 && roi_posi.width==1 && roi_posi.height==1)
			return false;
		return true;
	}
	
	/*
	public static void main(String[] argv){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture v = new VideoCapture(0);
		Mat target = Highgui.imread("target.jpg");
		Mat f = new Mat();
		for(int i=0;i<10;i++)
		{
			v.read(f);
			if(f!=null && f.width()>=1)
				break;
		}
		Tracker tracker = new Tracker(new Rect(f.width()/3, f.height()/3, f.width()/3, f.height()/3));
		tracker.setRoi(target, null);
		try{
			while(true){
				v.read(f);
				Rect r = tracker.updateFrame(f);
				if(r!=null)
					Core.rectangle(f, r.tl(), r.br(), new Scalar(0, 0, 255));
				VideoPlayer.debug_videoShow(f);
			}
		}
		finally{
			v.release();
		}
	}*/
}

class VideoPlayer{
	private static JFrame debug_frame;
	private static JLabel debug_label;
	
	private static BufferedImage mat2BufImg(final Mat mat) throws IOException{
		MatOfByte matOfByte = new MatOfByte();
		Highgui.imencode(".jpg", mat, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		BufferedImage bufferedImage = null;
		InputStream in = new ByteArrayInputStream(byteArray);
		bufferedImage = ImageIO.read(in);
		return bufferedImage;
	}
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
				
				debug_frame.addWindowListener(new WindowAdapter(){
					@Override
					public void windowClosing(WindowEvent e){
						System.exit(0);
					}
				});
			}
			else{
				debug_label.setIcon(new ImageIcon(bufferedImage));
			}
		}
		catch(Exception e){
			System.err.println("Unable to video img.");
		}
	}
}
