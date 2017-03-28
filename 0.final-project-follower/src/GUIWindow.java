import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;

public class GUIWindow extends JFrame implements Runnable{
	
	private static final long serialVersionUID = 3790637446303044182L;
	private static GUIWindow mWindow;
	private volatile boolean isRunning;//when this is set to false, all threads should stop.
	
	private Camera camera;
	private Tracker tracker;
	private VideoScreen mVideo;
	private VelocityComputer velocityComp;
	private Kobuki kobuki = new Kobuki("./kobukiCpp");
	private Button videoSwitchBtn;
	
	
	private Mat colorfulImg;	//real time colorfulImg
	private DepthImage depthImg;
	private RotatedRect roi_rotated_posi;
	/**
	 * Constructor of MyWindow
	 * @param name the title of the window
	 */
	public GUIWindow(String name){
		super(name);
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		isRunning = true;
		initMyWindow();
		new Thread(){
			@Override
			public void run(){
				mWindow.startMainLoop();
			}
		}.start();
	}
	
	/**
	 * Initialize the GUI app. 
	 */
	private void initMyWindow(){
		setVisible(true);
		setAlwaysOnTop(true);
		Dimension screensz = Toolkit.getDefaultToolkit().getScreenSize();
	System.err.println(""+screensz.getHeight()+","+screensz.getWidth());
		setMinimumSize(screensz);
		setMaximumSize(screensz);
		setExtendedState(getExtendedState()|JFrame.MAXIMIZED_BOTH);
		//setResizable(false);

		videoSwitchBtn = new Button("STOP LIVESTREAM");
		videoSwitchBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(videoSwitchBtn.getLabel().equals("STOP LIVESTREAM")){
					videoSwitchBtn.setLabel("RESUME LIVESTREAM");
				}else{
					videoSwitchBtn.setLabel("STOP LIVESTREAM");
				}
				
			}
			});
		this.add(videoSwitchBtn,BorderLayout.NORTH);
		try {
			camera = new Camera();
		} catch (Exception e3) {
			System.err.println("Fail to create camera object");
			e3.printStackTrace();
		}
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mVideo = new VideoScreen(new Dimension((int)(screensz.getWidth()*0.9), (int)(screensz.getHeight()*0.9)));
		int[] webcamReso = camera.getWebcamResolution();
		tracker = new Tracker(new Rect(webcamReso[0], webcamReso[1], webcamReso[0], webcamReso[1]), true);
		this.add(mVideo,BorderLayout.CENTER);
		velocityComp = new VelocityComputer();
		velocityComp.setStopRatio(0.05f);
		
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e){
				isRunning = false;
				try {
					camera.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				try{
					kobuki.shutdown();
				}catch(Exception e2){
					e2.printStackTrace();
				}
				System.exit(0);
			}
		});
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		try {
			colorfulImg = camera.getColorImage();
		} catch (Exception e1) {
			System.err.println("Fail to get colorImage");
			e1.printStackTrace();
		}
		
		try{
			mVideo.showImage(colorfulImg, null);
		}catch(Exception e2){
			System.err.println("Fail to show colorImage");
			e2.printStackTrace();
		}

		Thread videoThread = new Thread(){
			@Override
			public void run(){
				try{
					while(isRunning){
						if(videoSwitchBtn.getLabel().equals("STOP LIVESTREAM") && mVideo!=null && colorfulImg!=null){
							mVideo.showImage(colorfulImg, roi_rotated_posi==null?null:roi_rotated_posi.boundingRect());
						}
						Thread.sleep(50); 
					}
				}catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		videoThread.start();
		
		setVisible(true);
	}
	
	
	
	public static void main(String[] argv){
		mWindow = new GUIWindow("GUIWindow");
		SwingUtilities.invokeLater(mWindow);
	}

	@Override
	public void run() {
	}
	
	private void startMainLoop(){
		while(isRunning){
			// if new ROI is selected by user, update to tracker.
			if(mVideo.newRoiIsSet()){
	System.err.println("mVideo.newRoiIsSet()=true");
				Mat roi = mVideo.getRoi();
				Rect roi_posi = mVideo.getRoiPosi();
				tracker.setRoi(roi, roi_posi);
				mVideo.clearNewRoiSet();
			}
			// get colorful image
			try {
				colorfulImg = camera.getColorImage();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// get depth image
			try {
				depthImg = camera.getDepthImage();
			} catch (Exception e) {
				e.printStackTrace();
			}
			//update colorful image to tracker, get latest roi_posi
			roi_rotated_posi = tracker.updateFrame(colorfulImg);
			
			
			Rect roi_posi = roi_rotated_posi==null?null:roi_rotated_posi.boundingRect();
			//if tracked roi successfully, control kobuki.
			if(roi_posi!=null){
				double center_x = (roi_posi.tl().x+roi_posi.br().x)/2*depthImg.getWidth()/colorfulImg.width();
				double center_y = (roi_posi.tl().y+roi_posi.br().y)/2*depthImg.getHeight()/colorfulImg.height();
				int depth = 0;
				try{
					depth = depthImg.getByteArray()[(int)(center_y)+(int)(center_x)];
					//throw new Exception();
				}catch(Exception exception){
					exception.printStackTrace();
					System.err.println("center_x="+center_x);
					System.err.println("center_y="+center_y);
					System.err.println("roi_posi.tl().x="+roi_posi.tl().x);
					System.err.println("roi_posi.tl().y="+roi_posi.tl().y);
					System.err.println("roi_posi.br().x="+roi_posi.br().x);
					System.err.println("roi_posi.br().y="+roi_posi.br().y);
					System.err.println("depthImg.getHeight()="+depthImg.getHeight());
					System.err.println("depthImg.getwidth()="+depthImg.getWidth());
					System.err.println("depthImg.height()="+colorfulImg.height());
					System.err.println("depthImg.width()="+colorfulImg.width());
				}
				float[] velocity = velocityComp.getVelocity(colorfulImg.width(), colorfulImg.height(), roi_posi);
				velocity[0]*=3;
				System.err.println("velocity=["+velocity[0]+","+velocity[1]+"]");
				//kobuki.setBaseControl(velocity[1]==0f?velocity[0]:0f, velocity[1]);
				kobuki.setBaseControl(velocity[0], velocity[1]);
				kobuki.sendBaseControlCommand();
			}else{
				System.out.println("No target");
				kobuki.setBaseControl(0.0, 0.0);
				kobuki.sendBaseControlCommand();
			}
		}
	}
}
