import java.io.Serializable;
import java.security.InvalidParameterException;

public class DepthImage implements Serializable{
	
	private static final long serialVersionUID = 6799535892814208036L;
	private byte[] byteArray;
	private int width;
	private int height;
	public DepthImage(byte[] byteArray, int width, int height){
		if(width<=0 || height<=0 || width*height!=byteArray.length){
			throw new InvalidParameterException("byteArray.length="+byteArray.length+", width="+width+", height="+height);
		}
		this.width = width;
		this.height = height;
		this.byteArray = byteArray;
	}
	public byte[] getByteArray(){
		return this.byteArray;
	}
	public int getWidth(){
		return this.width;
	}
	public int getHeight(){
		return this.height;
	}
	public byte[][] getMatrix(){
		byte[][] matrix = new byte[height][width];
		int pointer = 0;
		for(int h = 0;h<height;h++){
			for(int w=0;w<width;w++){
				matrix[h][w] = byteArray[pointer++];
			}
		}
		return matrix;
	}
}
