package model;

/**
 * this class contains several static method to be used for other classes
 * @author 170024030
 *
 */
public class Util {
	/**
	 * normalize a vector
	 * @param n
	 * @return
	 */
	public static double[] normalizeVector(double[] n) {
		double lenN = Math.sqrt(n[0] * n[0] + n[1] * n[1] + n[2] * n[2]);
		return new double[] {
				n[0] / lenN,
				n[1] / lenN,
				n[2] / lenN
		};
	}
	
	/**
	 * calculate diffusion given
	 * @param normal - the unit normal vector
	 * @param point - the position of the object
	 * @param light - the position of the light source
	 * @param intensity - the intensity of the light source
	 * @return
	 */
	public static int calcDiffusion(double[] normal, double[] point, double[] light, double intensity) {
		double[] ray = vectorSubtraction(light, point);
		ray = Util.normalizeVector(ray);
		double dotProduct = ray[0] * normal[0] + ray[1] * normal[1] + ray[2] * normal[2];
		int color = (int) (dotProduct * intensity * 255);
		if (color < 0) {
			color = 0;
		}
		return color;
	}
	
	/**
	 * calculate specular reflection given
	 * @param normal - the unit normal vector
	 * @param point - the position of the point
	 * @param light - the position of the light source
	 * @param intensity - the intensity of the light source
	 * @param camera - the position of the camera
	 * @return
	 */
	public static int calcReflection(double[] normal, double[] point, double[] light, double intensity, double[] camera) {
		double[] ray = vectorSubtraction(light, point);
		double n = 2 * dotProduct(ray, normal) / calcLength(normal) / calcLength(normal);
		double[] reflection = vectorSubtraction(ray, multVector(n, normal));
		reflection = normalizeVector(reflection);
		double[] observe = vectorSubtraction(point, camera);
		observe = normalizeVector(observe);
		double dotProduct = dotProduct(reflection, observe);
		int color = (int) (Math.pow(dotProduct, 1.5) * intensity * 255);
		if (color < 0) {
			color = 0;
		}
		return color;
	}
	
	/**
	 * calculate illumination given
	 * @param normal - the unit normal vector
	 * @param point - the position of the object
	 * @param light - the position of the light
	 * @param intensity - the intensity of the light
	 * @return
	 */
	public static int calcIllumination(double[] normal, double[] point, double[] light, double intensity, double[] camera, double diffCoe, double reflCoe) {
		int color = (int) (diffCoe * calcDiffusion(normal, point, light, intensity) + reflCoe * calcReflection(normal, point, light, intensity, camera));
		if (color > 255) return 255;
		return color;
	}
	
	/**
	 * calculate the distance between two points in the 3d Cartesian coordinate system
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double calcDistance(double[] p1, double[] p2) {
		double sum = 0;
		for(int i = 0; i < 3; i++) {
			sum += Math.pow(p1[i] - p2[i], 2);
		}
		return Math.sqrt(sum);
	}
	
	/**
	 * calculate the length of a vector
	 * @param v
	 * @return
	 */
	public static double calcLength(double[] v) {
		double sum = 0;
		for(int i = 0; i < 3; i++) {
			sum += Math.pow(v[i], 2);
		}
		return Math.sqrt(sum);
	}
	
	/**
	 * convert a 1d double array to String so that it can be used as key of hash map
	 * @param coord
	 * @return
	 */
	public static String stringifyCoordinate(double[] coord) {
		String str = "";
		for(int i = 0; i < coord.length; i++) {
			str += coord[i];
		}
		return str;
	}
	
	/**
	 * subtract two vectors
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double[] vectorSubtraction(double[] v1, double[] v2) {
		double[] v = new double[v1.length];
		for(int i = 0; i < v.length; i++) {
			v[i] = v1[i] - v2[i];
		}
		return v;
	}
	
	/**
	 * calculate the vector multiplied by a scaler
	 * @param n
	 * @param vector
	 * @return
	 */
	public static double[] multVector(double n, double[] vector) {
		double[] v = new double[vector.length];
		for(int i = 0; i < vector.length; i++) {
			v[i] = n * vector[i];
		}
		return v;
	}
	
	/**
	 * calculate the dot product of two vectors
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double dotProduct(double[] v1, double[] v2) {
		double product = 0;
		for(int i = 0; i < v1.length; i++) {
			product += v1[i] * v2[i];
		}
		return product;
	}
}
