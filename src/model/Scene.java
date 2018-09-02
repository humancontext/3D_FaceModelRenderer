package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

import data.Triangle;
import data.Vertex;

/**
 * class that contains the model of the scene
 * @author 170024030
 *
 */
public class Scene {
	public ArrayList<Triangle> mesh = new ArrayList<Triangle>();	//the collection of triangles to render
	int width;	//width of the frame
	int height;	//height of the frame
	public HashMap<String, Vertex> vertexMap = new HashMap<String, Vertex>();	//the collection of the vertices, key is the Stringified array of coordinates
	public double[] lightSource = new double[3];	//location of the light source
	public double lightInclination = 0;	//initial inclination of the light source
	public double lightAzimuth = 0;	//initial azimuth of the light source
	public double lightRadius = Configs.MAXZ + 1000;	//the radius of the light source
	public double camInclination = 0;	//initial inclination of the camera
	public double camAzimuth = 0;	//initial azimuth of the camera
	public double camRadius = Configs.MAXZ * 2;	//initial radius of the camera
	public double intensity = 1;	//initial light intensity
	public String shading = "texture";	//initial shading
	public String projection = "ortho";	//initial projection
	public double focalLength = 150000;	//initial focal length
	public double[] camera = new double[3]; //camera location
	public double diffCoe = 0.5;
	public double reflCoe = 0.5;
	
	public void setLightSource(double[] lightSource) {
		this.lightSource = lightSource;
	}
	
	/**
	 * update the light source position by the member variables
	 */
	public void modifyLightPos() {
		this.lightSource[0] = Configs.MIDX + lightRadius * Math.sin(lightInclination / 180 * Math.PI) * Math.cos(lightAzimuth / 180 * Math.PI);
		this.lightSource[1] = Configs.MIDY + lightRadius * Math.sin(lightInclination / 180 * Math.PI) * Math.sin(lightAzimuth / 180 * Math.PI);
		this.lightSource[2] = lightRadius * Math.cos(lightInclination / 180 * Math.PI);
	}
	
	/**
	 * update the camera position by the member variables
	 */
	public void modifyCamPos() {
		this.camera[0] = Configs.MIDX + camRadius * Math.sin(camInclination / 180 * Math.PI) * Math.cos(camAzimuth / 180 * Math.PI);
		this.camera[1] = Configs.MIDY + camRadius * Math.sin(camInclination / 180 * Math.PI) * Math.sin(camAzimuth / 180 * Math.PI);
		this.camera[2] = camRadius * Math.cos(camInclination / 180 * Math.PI);
	}
	
	/**
	 * constructor
	 * @param width
	 * @param height
	 * @throws URISyntaxException 
	 */
	public Scene(int width, int height) throws URISyntaxException {
		Scanner sc1;
		Scanner sc2;

		File file1 = new File("face-shape.txt");
		File file2 = new File("face-texture.txt");

		try {
			// reading data from file
			sc1 = new Scanner(file1);
			sc2 = new Scanner(file2);
			while(sc1.hasNextDouble() && sc2.hasNextDouble()) {
				ArrayList<double[]> vList= new ArrayList<double[]>();
				ArrayList<Double> gList = new ArrayList<Double>();
				for(int i = 0; i < 3; i++) {
					double[] list = new double[3];
					for(int j = 0; j < 3; j++) {
						list[j] = sc1.nextDouble();
					}
					vList.add(list);
					double g = sc2.nextDouble();
					gList.add(g);
				}
				Triangle t = new Triangle(vList, gList, lightSource);
				mesh.add(t);
				for(int i = 0; i < 3; i++) {
					double[] coor = vList.get(i);
					if(!vertexMap.containsKey(Util.stringifyCoordinate(coor))) {
						Vertex v = new Vertex(coor);
						v.addTriangle(t);
						vertexMap.put(Util.stringifyCoordinate(coor), v);
					}
					else {
						vertexMap.get(Util.stringifyCoordinate(coor)).addTriangle(t);
					}
				}
			}
			for(String v : vertexMap.keySet()) {
				vertexMap.get(v).calcNormal();
				vertexMap.get(v).calcIllumination(lightSource, camera, intensity, diffCoe, reflCoe);
			}
			//sort the triangle by the distance of the centroid
			Collections.sort(mesh, new SortByDistance());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		modifyLightPos();
		modifyCamPos();
	}
	
	public void reset() {
		lightInclination = 0;	//initial inclination of the light source
		lightAzimuth = 0;	//initial azimuth of the light source
		lightRadius = Configs.MAXZ + 1000;	//the radius of the light source
		camInclination = 0;	//initial inclination of the camera
		camAzimuth = 0;	//initial azimuth of the camera
		camRadius = Configs.MAXZ * 2;	//initial radius of the camera
		intensity = 1;	//initial light intensity
		shading = "texture";	//initial shading
		projection = "ortho";	//initial projection
		focalLength = 150000;	//initial focal length
		diffCoe = 0.5;
		reflCoe = 0.5;
		modifyCamPos();
		modifyLightPos();
	}
	
	public double getInclinationAngle() {
		return lightInclination;
	}

	public void setInclinationAngle(double inclinationAngle) {
		this.lightInclination = inclinationAngle;
	}

	public double getAzimuthAngle() {
		return lightAzimuth;
	}

	public void setAzimuthAngle(double azimuthAngle) {
		this.lightAzimuth = azimuthAngle;
	}
	
	/**
	 * calculate the projection both orthographic and perspective
	 * @param t
	 * @param m
	 * @param n
	 * @return
	 */
	public int[] getProjection (Triangle t, double m, double n) {
		double x = t.points[0][0] + m * t.v12[0] + n * t.v13[0];
		double y = t.points[0][1] + m * t.v12[1] + n * t.v13[1];
		double z = t.points[0][2] + m * t.v12[2] + n * t.v13[2];
		if(projection == "ortho") {
			x = (x - Configs.MINX) / Configs.XRANGE * Configs.FRAME_WIDTH;
			y = Configs.FRAME_HEIGHT - (y - Configs.MINY) / Configs.YRANGE * Configs.FRAME_HEIGHT;
		}
		else {
			x =  (x - camera[0]) * (focalLength / (z - camera[2])) + camera[0];
			y =  (y - camera[1]) * (focalLength / (z - camera[2])) + camera[1];
			x = (x - Configs.MINX) / Configs.XRANGE * Configs.FRAME_WIDTH;
			y = (y - Configs.MINY) / Configs.YRANGE * Configs.FRAME_HEIGHT;
		}
		return new int[] {(int) x, (int) y};
	}
}

class SortByDistance implements Comparator<Triangle> {
	// sorting in descending order of distance
	public int compare(Triangle t1, Triangle t2) {
		return (int) (t1.centroid[2] - t2.centroid[2]);
	}
}




