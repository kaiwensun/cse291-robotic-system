/*****************************************************************************
*                                                                            *
*  OpenNI 2.x Alpha                                                          *
*  Copyright (C) 2012 PrimeSense Ltd.                                        *
*                                                                            *
*  This file is part of OpenNI.                                              *
*                                                                            *
*  Licensed under the Apache License, Version 2.0 (the "License");           *
*  you may not use this file except in compliance with the License.          *
*  You may obtain a copy of the License at                                   *
*                                                                            *
*      http://www.apache.org/licenses/LICENSE-2.0                            *
*                                                                            *
*  Unless required by applicable law or agreed to in writing, software       *
*  distributed under the License is distributed on an "AS IS" BASIS,         *
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
*  See the License for the specific language governing permissions and       *
*  limitations under the License.                                            *
*                                                                            *
*****************************************************************************/
package org.openni.Samples.SimpleViewer;

import org.openni.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.*;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import java.io.IOException;
import java.net.ServerSocket;  
import java.net.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SimpleViewerApplication implements ItemListener {
    private static SingleThreadServer stServer;
    private JFrame mFrame;
    private JPanel mPanel;
    private SimpleViewer mViewer;
    private boolean mShouldRun = true;
    private Device mDevice;
    private VideoStream mVideoStream;
    private ArrayList<SensorType> mDeviceSensors;
    private ArrayList<VideoMode> mSupportedModes;

    private JComboBox mComboBoxStreams;
    private JComboBox mComboBoxVideoModes;
	
	private int threshold;
	private SensorType type;
	private VideoMode mode;

    public SimpleViewerApplication(Device device) {
        stServer = new SingleThreadServer(4568);
        mDevice = device;
	threshold = 10;
	type = SensorType.DEPTH;
        if (mVideoStream != null) {
            mVideoStream.stop();
            mViewer.setStream(null);
            mVideoStream.destroy();
            mVideoStream = null;
        }
        mVideoStream = VideoStream.create(mDevice, type);
	List<VideoMode> supportedModes = mVideoStream.getSensorInfo().getSupportedVideoModes();
	for(VideoMode m : supportedModes){
		if(m.getPixelFormat() == PixelFormat.DEPTH_1_MM){
			mode = m;
		}
	}
	if(mode == null)
		System.err.println("Mode initialization error");
	mVideoStream.setVideoMode(mode);	
        mViewer = new SimpleViewer();
        mViewer.setStream(mVideoStream);
	mVideoStream.start();
        /*
        mFrame = new JFrame("OpenNI Simple Viewer");
        mPanel = new JPanel();
        
        // register to key events
        mFrame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent arg0) {}
            
            @Override
            public void keyReleased(KeyEvent arg0) {}
            
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    mShouldRun = false;
                }
            }
        });
        
        // register to closing event
        mFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mShouldRun = false;
            }
        });

        mComboBoxStreams = new JComboBox();
        mComboBoxVideoModes = new JComboBox();
        
        mComboBoxStreams.addItem("<Stream Type>");
        mDeviceSensors = new ArrayList<SensorType>();
        
        if (device.getSensorInfo(SensorType.COLOR) != null) {
            mDeviceSensors.add(SensorType.COLOR);
            mComboBoxStreams.addItem("Color");
        }
        
        if (device.getSensorInfo(SensorType.DEPTH) != null) {
            mDeviceSensors.add(SensorType.DEPTH);
            mComboBoxStreams.addItem("Depth");
        }
        
        if (device.getSensorInfo(SensorType.IR) != null) {
            mDeviceSensors.add(SensorType.IR);
            mComboBoxStreams.addItem("IR");
        }
        
        mComboBoxStreams.addItemListener(this);
        mComboBoxVideoModes.addItemListener(this);
        mViewer.setSize(800,600);
        
        mPanel.add("West", mComboBoxStreams);
        mPanel.add("East", mComboBoxVideoModes);
        mFrame.add("North", mPanel);
        mFrame.add("Center", mViewer);
        mFrame.setSize(mViewer.getWidth() + 20, mViewer.getHeight() + 80);
        mFrame.setVisible(true);*/
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.DESELECTED)
            return;
            
    }
    
    void videoStreamStart() {

        
        //mSupportedModes = new ArrayList<VideoMode>();

        // now only keep the ones that our application supports
		/*
        for (VideoMode mode : supportedModes) {
            switch (mode.getPixelFormat()) {
                case DEPTH_1_MM:
                case DEPTH_100_UM:
                case SHIFT_9_2:
                case SHIFT_9_3:
                case RGB888:
                case GRAY8:
                case GRAY16:
                    mSupportedModes.add(mode);
                    break;
            }
        }

        // and add them to combo box
        //mComboBoxVideoModes.removeAllItems();
        //mComboBoxVideoModes.addItem("<Video Mode>");
        for (VideoMode mode : mSupportedModes) {
            mComboBoxVideoModes.addItem(String.format(
                "%d x %d @ %d FPS (%s)",
                mode.getResolutionX(),
                mode.getResolutionY(), 
                mode.getFps(),
                pixelFormatToName(mode.getPixelFormat())));
        }*/
    }
    
    private String pixelFormatToName(PixelFormat format) {
        switch (format) {
            case DEPTH_1_MM:    return "1 mm";
            case DEPTH_100_UM:  return "100 um";
            case SHIFT_9_2:     return "9.2";
            case SHIFT_9_3:     return "9.3";
            case RGB888:        return "RGB";
            case GRAY8:         return "Gray8";
            case GRAY16:        return "Gray16";
            default:            return "UNKNOWN";
        }
    }
    /*
    void selectedVideoModeChanged() {
        mVideoStream.stop();
        
        int modeIndex = mComboBoxVideoModes.getSelectedIndex() - 1;
        if (modeIndex == -1) {
            return;
        }
        
        VideoMode mode = mSupportedModes.get(modeIndex);
        mVideoStream.setVideoMode(mode);
        mViewer.setStream(mVideoStream);
        //mViewer.setSize(mode.getResolutionX(), mode.getResolutionY());
        //mFrame.setSize(mViewer.getWidth() + 20, mViewer.getHeight() + 80);
        mVideoStream.start();
    }*/
    
    void run() {
        while (mShouldRun) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mFrame.dispose();
    }
	
	public int getDepth(){
		int min = Integer.MAX_VALUE;
		int[] depthList = mViewer.getDepth();
//System.err.println("==========================");
//System.err.println(Arrays.toString(depthList));
//System.err.println("==========================");
		int zeroCnt = 0;
		for(int i = 0;i<depthList.length; ++i){
			if(depthList[i]==0){
				zeroCnt++;
			}else if(depthList[i] < min){
				min = depthList[i];
			}
			if(zeroCnt>depthList.length*0.7){
				return 0;
			}
		}
		return min;
	}
	
	public boolean hasObstacle(int distRef){
		if(getDepth()<distRef)
			return true;
		else
			return false;
	}
	
	public void changeSensorType(SensorType Type){
		type = Type;
	}

    public static void main(String s[]) throws Exception{
        // initialize OpenNI
        OpenNI.initialize();

        String uri;
        
        if (s.length > 0) {
            uri = s[0];
        } else {
            List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
            if (devicesInfo.isEmpty()) {
                //JOptionPane.showMessageDialog(null, "No device is connected", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            uri = devicesInfo.get(0).getUri();
        }
        
        Device device = Device.open(uri);
	if(device == null)
		System.err.println("device wrong!");			
        final SimpleViewerApplication app = new SimpleViewerApplication(device);
        //app.videoStreamStart();
		Scanner scanner = new Scanner(System.in);
		try{
		while(true){
			//String str = scanner.nextLine();
			String str = "";
			Object obj = stServer.getPostman().recv();
			if(str instanceof String){
				str = (String) obj;
			}
			switch(str){
				case "depth": //System.out.println(app.getDepth()); break;
						stServer.getPostman().send(app.getDepth()+""); break;
				case "obstacle":
					int i = 0;
					if(app.hasObstacle(400)) i = 1;
					else i = 0;  
					//System.out.println(i); break;
					stServer.getPostman().send(i+"");break;
				default: //System.out.println("invalid command"); break;
					stServer.getPostman().send("incalid command");
			}
		}
		}finally{
			if(stServer!=null && stServer.getPostman()!=null)
				stServer.getPostman().close();
		}
    }
}
class SingleThreadServer {
	Postman postman;
	public Postman getPostman(){
		return postman;
	}
	public SingleThreadServer(int port){
		ServerSocket listener = null;
		try {
			listener = new ServerSocket(port);
			Socket incoming = listener.accept();
			postman = new Postman(incoming);
		} catch (IOException e1) {
			System.err.println("Can't open port "+port);
			return;
		}
		catch(Throwable throwable){
			throwable.printStackTrace();
		}
	}
}

/**
 * A socket wrapper to send and receive serializable objects.
 * @author Kaiwen Sun
 *
 */
class Postman {
	private Socket socket;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;

	protected OutputStream getOutStram(){
		return outStream;
	}
	protected InputStream getInStream(){
		return inStream;
	}

	
	/**
	 * Constructor.
	 * @param socket socket
	 * @throws IOException fail to open I/O stream.
	 */
	public Postman(Socket socket) throws IOException{
		this.socket = socket;
		init();
	}
	
	/**
	 * Initialize I/O stream on socket.
	 * @throws IOException fail to init
	 */
	private void init() throws IOException{
		 outStream = new ObjectOutputStream(socket.getOutputStream());
		 //outStream.flush();
		 InputStream inputStream = socket.getInputStream();
		 inStream = new ObjectInputStream(inputStream);
	}

	/**
	 * Receive serializable object. 
	 * @return received serializable object.
	 * @throws ClassNotFoundException fail to receive
	 * @throws IOException fail to receive
	 */
	public Object recv() throws ClassNotFoundException, IOException{
		synchronized (inStream) {
			return inStream.readObject();	
		}
	}
	
	/**
	 * Send serializable object.
	 * @param obj serializable object to be sent
	 * @throws IOException fail to send
	 */
	public  void send(Object obj) throws IOException{
		synchronized (outStream) {
			outStream.writeObject(obj);
			outStream.flush();
			outStream.reset();
		}
	}
	
	/**
	 * Close the I/O stream and socket. The postman should not be used anymore after being closed.
	 */
	public void close(){
		if(inStream!=null)
			try {
				inStream.close();
				inStream = null;
			} catch (IOException e) {
			}
		if(outStream!=null)
			try {
				outStream.close();
				outStream = null;
			} catch (IOException e) {
			}
		if(socket!=null)
			try {
				System.out.println("Postman at "+socket+" closed.");
				socket.close();
				socket = null;
			} catch (IOException e) {
			}
	}

	/**
	 * Convert the postman to a string containing its socket information.
	 */
	@Override
	public String toString(){
		try{
			return "postman at "+socket.toString();
		}
		catch(NullPointerException e){
			return "postman at unknown socket";
		}
	}
}
