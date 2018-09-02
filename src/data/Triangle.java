package data;

import java.util.ArrayList;
import model.Util;

/**
 * The data structure for triangle mesh
 * @author 170024030
 *
 */
public class Triangle {
	public double points[][] = new double[3][3];	//location of the three vertice
	public double centroid[];	//location of the centroid
	public double[] texture = new double[3];	//loaded from the texture file
	public int flatGray = 0;	//flat shading
	public double[] nOut;	//outward pointing normal vector
	public double[] nIn;		//inward pointing normal vector
	public double[] v12;		//vector from the first point to the second point
	public double[] v13; 	//vector from the first point to the third point
	public double lightDistance;		//distance between the light source and the centroid
	
	/**
	 * constructor of triangle mesh
	 * @param vs
	 * @param g
	 * @param lightSource
	 */
 	public Triangle(ArrayList<double[]> vs, ArrayList<Double> g, double[] lightSource) {
		for(int i = 0; i < 3; i++) {
			this.points[i] = vs.get(i);
			this.texture[i] = g.get(i);
			flatGray += (double) g.get(i);
		}
		this.centroid = new double[] {
				(points[0][0] + points[1][0] + points[2][0]) / 3,
				(points[0][1] + points[1][1] + points[2][1]) / 3,
				(points[0][2] + points[1][2] + points[2][2]) / 3
		};
		flatGray = (int) Math.max(flatGray / 3, 0);
		calcNormal();
		this.lightDistance = Util.calcDistance(this.centroid, lightSource);
	}
	
 	/**
 	 * calculate the normals of the triangle
 	 */
	private void calcNormal() {
		this.v12 = new double[] {
				points[1][0] - points[0][0], 
				points[1][1] - points[0][1], 
				points[1][2] - points[0][2]
		};
		this.v13 = new double[] {
				points[2][0] - points[0][0], 
				points[2][1] - points[0][1], 
				points[2][2] - points[0][2]
		};
		double[] n = new double[] {
				v12[1] * v13[2] - v12[2] * v13[1],
				v12[2] * v13[0] - v12[0] * v13[2],
				v12[0] * v13[1] - v12[1] * v13[0]
		};
		n = Util.normalizeVector(n);
		if(n[2] < 0) {
			this.nIn = n;
			this.nOut = new double[] {
					-n[0],
					-n[1],
					-n[2]
			};
		}
		else {
			this.nOut = n;
			this.nIn = new double[] {
					-n[0],
					-n[1],
					-n[2]
			};
		}
	}
	
}
