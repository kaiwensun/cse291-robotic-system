import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;


/**
 * JPanel showing Webcam's picture and video.
 * @author Kaiwen Sun
 *
 */
public class VideoScreen extends JPanel{

	private static final long serialVersionUID = 4600820487844504218L;
	private int panelWidth;
	private int panelHeight;
	private int matWidth;
	private int matHeight;
	//private int matTargetWidth;
	//private int matTargetHeight;
	private JLabel label = new JLabel("No video stream to show.");
	private Mat frame;
	private Mat roi;
	private Rect roi_posi;
	private volatile boolean newRoiIsSet = false; 

	private java.awt.Point drag_start;
	private java.awt.Point drag_current;
	
	private Scalar TRACKED_COLOR = new Scalar(0,0,255);		//red
	private Scalar DRAGED_COLOR = new Scalar(255,0,0);	//blue
	
	public Mat getRoi(){
		return roi;
	}
	public Rect getRoiPosi(){
		return roi_posi;
	}
	public boolean newRoiIsSet(){
		return newRoiIsSet;
	}
	public void clearNewRoiSet(){
		newRoiIsSet = false;
	}
	
	
	/**
	 * Constructor
	 * @param dimension size of JPanel
	 */
	public VideoScreen(Dimension dimension){
		this.panelWidth = dimension.width;
		this.panelHeight = dimension.height;
		setAlignmentX(SwingConstants.CENTER);
		setAlignmentY(SwingConstants.CENTER);
		label.setSize(dimension);
		add(BorderLayout.CENTER, label);
		
		MouseAdapter mouseAdapter = new MouseAdapter() {

			@Override
			public void mouseDragged(MouseEvent e){
				drag_current = e.getPoint();
			}
			
			@Override
			public void mouseMoved(MouseEvent arg0) {
				// do something if needed.
				
			}
					
			@Override
			public void mousePressed(MouseEvent e){
				if(e.getClickCount()==1)
					drag_start = e.getPoint();
				else
					drag_start = null;
			}
			@Override
			public void mouseReleased(MouseEvent e){
				if(e.getClickCount()<=1){
					if(drag_start!=null && !drag_start.equals(e.getPoint())){
						Point tl = panel2FrameUnit(drag_start);
						Point br = panel2FrameUnit(e.getPoint());
						roi_posi = new Rect(tl,br);
						roi = frame.submat(roi_posi);
						drag_start = null;
						drag_current = null;
						e.consume();
						newRoiIsSet = true;
					}
				}
			}
		};
		label.addMouseMotionListener(mouseAdapter);
		label.addMouseListener(mouseAdapter);
	}
	
	/**
	 * show BufferedImage on the jpanel.
	 * @param frame image frame to show
	 * @param roi_position the Rect or RotatedRect position of ROI. If null, then don't draw rect.
	 */
	public void showImage(Mat frame, Object roi_posi){
		this.frame = frame.clone();
		updateFrameSize(frame);
		if((roi_posi instanceof Rect) || (roi_posi instanceof RotatedRect)){
			drawRoiRect(frame, roi_posi, TRACKED_COLOR);
		}
		drawDraged(frame, DRAGED_COLOR);
		revalidate();
		repaint();

		label.setText(null);
		BufferedImage image = Camera.mat2BufImg(frame);
		label.setIcon(new ImageIcon(image.getScaledInstance(panelWidth, panelHeight, Image.SCALE_SMOOTH)));
	}
	
	private void drawRoiRect(Mat frame, Object roi_posi, Scalar color){
		if(frame==null || roi_posi==null){
			return;
		}
		if(roi_posi instanceof Rect){
			Core.rectangle(frame, ((Rect) roi_posi).tl(), ((Rect) roi_posi).br(), color);
		}
		else if(roi_posi instanceof RotatedRect){
		    Point points[] = new Point[4];
		    ((RotatedRect) roi_posi).points(points);
		    for(int i=0; i<4; ++i){
		        Core.line(frame, points[i], points[(i+1)%4], color);
		    }
		}
	}
	
	private void drawDraged(Mat frame, Scalar color){
		if(drag_start!=null && drag_current!=null && !drag_start.equals(drag_current)){
			Core.rectangle(frame, panel2FrameUnit(drag_start),panel2FrameUnit(drag_current), color);
		}
	}
	
	@SuppressWarnings("unused")
	private java.awt.Point frame2PanelUnit(Point pt){
		int x = (int) (pt.x*panelWidth/matWidth);
		int y = (int) (pt.y*panelHeight/matHeight);
		return new java.awt.Point(x, y);
	}
	
	private Point panel2FrameUnit(java.awt.Point pt){
		int x = pt.x*matWidth/panelWidth;
		int y = pt.y*matHeight/panelHeight;
		return new Point(x, y);
	}

		
	/**
	 * Show image with a list of targets. Targets are marked by rectangles.
	 * @param image main image.
	 * @param targets a list of targets.
	 */
	/*
	public void showImageWithTargets(Mat image, List<Target> targets){
		if(!Cfg.enable_webcam)
			return;
		for(Target target : targets){
			drawTarget(image, target, Color.yellow);
		}
		showImage(image);
	}
	*/
	
	/**
	 * Show image with a list of targets and the best target. Targets are marked by rectangles.
	 * @param image main image.
	 * @param targets a list of targets.
	 * @param bestTarget the best target.
	 */
	/*
	public void showImageWithTargets(Mat image, List<Target>targets, Target bestTarget){
		if(!Cfg.enable_webcam)
			return;
		for(Target target : targets){
			drawTarget(image, target, Color.yellow);
		}
		if(bestTarget!=null){
			drawTarget(image, bestTarget, Color.green);
		}
		showImage(image);
		
	}
	*/
	
	/**
	 * Convert Color object to Scalar object.
	 * @param color color
	 * @return Scalar object.
	 */
	/*private Scalar color2Scalar(Color color){
		return new Scalar(color.getBlue(),color.getGreen(),color.getRed());
	}
	*/
	
	/**
	 * Update the size of this webcam pic according to a Mat frame.
	 * @param frame frame
	 */
	private void updateFrameSize(Mat frame){
		this.matWidth = frame.cols();
		this.matHeight = frame.rows();
	}
	
	/**
	 * Show image with a target in given color. Target is marked by rectangle.
	 * @param image main image.
	 * @param target target.
	 * @param color color
	 */
	/*private void drawTarget(Mat image, Target target, Color color){
		updateSizes(image);
		picMatcher.setTargetSize(matTargetWidth, matTargetHeight);
		Point pt1,pt2;
		if(target==null){
			pt1 = new Point((matWidth-matTargetWidth)/2,(matHeight-matTargetHeight)/2);
			pt2 = new Point((matWidth+matTargetWidth)/2,(matHeight+matTargetHeight)/2);
		}
		else{
			pt1 = new Point(target.x-matTargetWidth/2,target.y-matTargetHeight/2);
			pt2 = new Point(target.x+matTargetWidth/2,target.y+matTargetHeight/2);
			Core.putText(image, ""+target.confidence, pt1, Core.FONT_HERSHEY_PLAIN, 1, new Scalar(0,0,255));
		}
		Core.rectangle(image, pt1, pt2, color2Scalar(color));
	}
	*/
	
}
