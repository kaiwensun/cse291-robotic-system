import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.NoSuchPortException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 
public class MbedSocketTranslater {
    private String portName;
    private InputStream in;
    private OutputStream out;
    
    
    public MbedSocketTranslater(String portName) throws Exception{
        this.portName = portName;
        init();
    }
    void init() throws Exception{
        CommPortIdentifier portIdentifier = CommPortIdentifier
                .getPortIdentifier( portName );
        if( portIdentifier.isCurrentlyOwned() ) {
            System.err.println("Port is currently in use");
            throw new PortInUseException(); 
        }
        int timeout = 2000;
        CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );
        
        if( commPort instanceof SerialPort ) {
            SerialPort serialPort = ( SerialPort )commPort;
            serialPort.setSerialPortParams( 9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE );
            in = serialPort.getInputStream();
            out = serialPort.getOutputStream();
        }else{
            System.err.println("The port is not a serial port");
            throw new UnsupportedCommOperationException("The port is not a serial port");
        }
    }
    
    
    public static class SerialReader implements Runnable {
        private InputStream in;
        public SerialReader( InputStream in ) {
            this.in = in;
        }
     
        public void run() {
            byte[] buffer = new byte[ 10240 ];
            int len = -1;
            try {
            while( ( len = this.in.read( buffer ) ) > 0){//-1 ) {
                System.out.print( new String( buffer, 0, len ));
                System.out.flush();
            }
        } catch( IOException e ) {
            e.printStackTrace();
        }
        }
    }
    
    public static class SerialWriter implements Runnable {
    
        private OutputStream out;
    
        public SerialWriter( OutputStream out ) {
            this.out = out;
        }
     
        public void run() {
            try {
                int c = 0;
                while( ( c = System.in.read() ) > -1 ) {
                    this.out.write( c );
                }
            } catch( IOException e ) {
                e.printStackTrace();
            }
        }
    }
    public InputStream getInputStream(){
        return in;
    }
    public OutputStream getOutputStream(){
        return out;
    }
    
    public static void main(String[] argv) throws Exception{
        MbedSerial comm;
            try{
                comm = new MbedSerial("/dev/ttyACM0");
            }catch(NoSuchPortException e){
                comm = new MbedSerial("/dev/ttyACM1");
            }
        new Thread( new MbedSerial.SerialReader(comm.getInputStream())).start();
        new Thread( new MbedSerial.SerialWriter(comm.getOutputStream())).start();
    }
}
