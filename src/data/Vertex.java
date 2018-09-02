package data;

import java.util.ArrayList;

import model.Util;

/**
 * data structure to store the vertex with corresponded triangles
 * @author 170024030
 *
 */
public class Vertex {
	public double[] coordinates;
	public double[] nIn = new double[3];
	public double[] nOut = new double[3];
	public int illumination;
	ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	
	/**
	 * constructor
	 * @param c
	 */
	public Vertex(double[] c) {
		this.coordinates = c;
	}
	
	/**
	 * add adjacent triangle
	 * @param t
	 */
	public void addTriangle(Triangle t) {
		this.triangles.add(t);
	}
	
	/**
	 * calculate the normal of the vertex as the averaged normal of the triangles
	 */
	public void calcNormal() {
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < triangles.size(); j++) {
				nIn[i] += triangles.get(j).nIn[i];
				nOut[i] += triangles.get(j).nOut[i];
			}
			nIn[i] /= 3;
			nOut[i] /= 3;
		}
		nIn = Util.normalizeVector(nIn);
		nOut = Util.normalizeVector(nOut);
	}
	
	/**
	 * calculate the shading on the vertex
	 * @param lightSource
	 * @param intensity
	 */
	public void calcIllumination(double[] lightSource, double[] camera, double intensity, double diffCoe, double reflCoe) {
		this.illumination = Util.calcIllumination(nOut, coordinates, lightSource, intensity, camera, diffCoe, reflCoe);
	}
}
