import org.opencv.core.Rect;


public interface VelocityComputerInterface {
	/**
	 * Compute linear and angular velocity of Kobuki based on image information.
	 * @param imgWidth image width in pixel
	 * @param imgHeight image height in pixel
	 * @param roi range of interest in pixel
	 * @param depth depth of roi center in mm
	 * @return a length-2 float array. {linear_velocity, angular_velocity}
	 */
	public float[] getVelocity(int imgWidth, int imgHeight, Rect roi);
}

