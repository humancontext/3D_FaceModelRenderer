import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import data.Triangle;
import model.Configs;
import model.Scene;
import model.Util;

/**
 * The main class of this program which also serves as the UI.
 * @author 170024030
 *
 */
public class Main extends JPanel {
	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private Scene scene;		//the model controller
	private int height = Configs.FRAME_HEIGHT;
	private int width = Configs.FRAME_WIDTH;
	public double[] initLightSource = new double[] {Configs.MIDX, Configs.MIDY, 2000000};
	public double[] lightSource = new double[] {Configs.MIDX, Configs.MIDY, 200000};
	private JTextField laAngleText;
	private JTextField liAngleText;
	private JTextField intensityText;
	private JTextField heightText;
	private JTextField caAngleText;
	private JTextField ciAngleText;
	private JTextField cRadiusText;
	private JToolBar toolBar1;
	private JToolBar toolBar2;
	private JToolBar toolBar3;
	private JPanel toolPanel;
	/**
	 * the entrance of the program
	 * @param args
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {
		new Main();
	}
	
	/**
	 * the constructor to initialise the program
	 * @throws URISyntaxException 
	 */
	public Main() throws URISyntaxException {
		frame = new JFrame("3D Rendering");
		scene = new Scene(width, height);
		setup();
	}

	
	/**
	 * Set the size of the frame.
	 */
	public void addNotify() {
		setPreferredSize(new Dimension(width, height));
	}
	
	/**
	 * set up the UI
	 */
	private void setup() {
		toolPanel = new JPanel();
		toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.Y_AXIS));
		
		setupToolbar();
		toolPanel.add(toolBar1);
		toolPanel.add(toolBar2);
		toolPanel.add(toolBar3);
		Container contentPane = frame.getContentPane();
		contentPane.add(toolPanel, BorderLayout.NORTH);
//		addKeyboardListener();
//		addMouseWheelListener();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.getContentPane().add(this);
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * paint method to refresh the canvas.
	 * shading calculation is included
	 * the pixels are painted with bilinear interpolation within the triangle
	 */
	public void paint(Graphics g) {
		// refresh the configuration according to the changed parameters
		laAngleText.setText(""+scene.lightAzimuth);
		liAngleText.setText(""+scene.lightInclination);
		intensityText.setText(""+scene.intensity);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.LIGHT_GRAY);
		// draw the background
		for (int i = 0; i <= width; i += 50) {
			g.drawLine(i, 0, i, Configs.FRAME_HEIGHT);
		}
		for (int i = 0; i <= height + Configs.MENU_HEIGHT; i += 50) {
			g.drawLine(0, i, Configs.FRAME_WIDTH + Configs.MENU_HEIGHT, i);
		}
		ArrayList<Triangle> mesh = scene.mesh;
		if(scene.shading == "gouraud") {
			for(String v : scene.vertexMap.keySet()) {
				scene.vertexMap.get(v).calcNormal();
				scene.vertexMap.get(v).calcIllumination(scene.lightSource, scene.camera, scene.intensity, scene.diffCoe, scene.reflCoe);
			}
		}
		// loop through every triangle mesh to render image
		for(int i = 0; i < mesh.size(); i++) {
			Triangle t = mesh.get(i);
			int illum;
			// shading with texture
			if (scene.shading == "texture") {
				illum = t.flatGray;
				for(double m = 0; m <= 1; m += 1/Util.calcLength(t.v12) * Configs.RATIO) {
					for(double n = 0; n <= 1 - m; n += 1/Util.calcLength(t.v13) * Configs.RATIO) {
						Color c = new Color(illum, illum, illum);
						g.setColor(c);
						int[] projection = scene.getProjection(t, m, n);
						g.drawRect(projection[0], projection[1], 1, 1);
					}
				}
			}
			// flat shading
			else if(scene.shading == "flat") {
				illum = Util.calcIllumination(t.nOut, t.centroid, scene.lightSource, scene.intensity, scene.camera, scene.diffCoe, scene.reflCoe);
				for(double m = 0; m <= 1; m += 1/Util.calcLength(t.v12) * Configs.RATIO) {
					for(double n = 0; n <= 1 - m; n += 1/Util.calcLength(t.v13) * Configs.RATIO) {
						Color c = new Color(illum, illum, illum);
						g.setColor(c);
						int[] projection = scene.getProjection(t, m, n);
						g.drawRect(projection[0], projection[1], 1, 1);
					}
				}
			}
			// gouraud shading
			else if(scene.shading == "gouraud") {
				//calculate the shading on the vertices
				int illumP1 = scene.vertexMap.get(Util.stringifyCoordinate(t.points[0])).illumination;
				int illumP2 = scene.vertexMap.get(Util.stringifyCoordinate(t.points[1])).illumination;
				int illumP3 = scene.vertexMap.get(Util.stringifyCoordinate(t.points[2])).illumination;
				int illumDiff12 = illumP2 - illumP1;
				int illumDiff13 = illumP3 - illumP1;
				// bilinearly interpolate the shading across the triangle
				for(double m = 0; m <= 1; m += 1/Util.calcLength(t.v12) * Configs.RATIO) {
					for(double n = 0; n <= 1 - m; n += 1/Util.calcLength(t.v13) * Configs.RATIO) {
						illum = (int) (illumP1 + m * illumDiff12 + n * illumDiff13);
						Color c = new Color(illum, illum, illum);
						g.setColor(c);
						int[] projection = scene.getProjection(t, m, n);
						g.drawRect(projection[0], projection[1], 1, 1);
					}
				}
			}
			// phong shading
			else if(scene.shading == "phong") {
				// interpolate the 3D location and normals across the triangle
				double[] locP1 = t.points[0];
				double[] locP2 = t.points[1];
				double[] locP3 = t.points[2];
				double[] locDiff12 = Util.vectorSubtraction(locP2, locP1);
				double[] locDiff13 = Util.vectorSubtraction(locP3, locP1);
				double[] normalP1 = scene.vertexMap.get(Util.stringifyCoordinate(t.points[0])).nOut;
				double[] normalP2 = scene.vertexMap.get(Util.stringifyCoordinate(t.points[1])).nOut;
				double[] normalP3 = scene.vertexMap.get(Util.stringifyCoordinate(t.points[2])).nOut;
				double[] normalDiff12 = Util.vectorSubtraction(normalP2, normalP1);
				double[] normalDiff13 = Util.vectorSubtraction(normalP3, normalP1);
				// bilinear interpolation
				for(double m = 0; m <= 1; m += 1/Util.calcLength(t.v12) * Configs.RATIO) {
					for(double n = 0; n <= 1 - m; n += 1/Util.calcLength(t.v13) * Configs.RATIO) {
						double[] normalD12Mul = Util.multVector(m, normalDiff12);
						double[] normalD13Mul = Util.multVector(n, normalDiff13);
						double[] normal = new double[3];
						double[] locD12Mult = Util.multVector(m, locDiff12);
						double[] locD13Mult = Util.multVector(m, locDiff13);
						double[] loc = new double[3];
						for(int j = 0; j < 3; j++) {
							normal[j] = normalD12Mul[j] + normalD13Mul[j] + normalP1[j];
							loc[j] = locD12Mult[j] + locD13Mult[j] + locP1[j];
						}
						illum = Util.calcIllumination(normal, loc , scene.lightSource, scene.intensity, scene.camera, scene.diffCoe, scene.reflCoe);
						Color c = new Color(illum, illum, illum);
						g.setColor(c);
						int[] projection = scene.getProjection(t, m, n);
						g.drawRect(projection[0], projection[1], 1, 1);
					}
				}
			}
		}
	}
	
	/**
	 * setting up the components in toolbar
	 */
	private void setupToolbar() {
		JLabel projectionLabel = new JLabel("Projection:");
//		JButton resetButton = new JButton("Reset");
//        resetButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//            		scene = new Scene(height, width);
//            		frame.repaint();
//            }
//        });
		
		JButton orthoButton = new JButton("O");
        orthoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            		scene.projection = "ortho";
            		frame.repaint();
            }
        });
        
        JButton persButton = new JButton("P");
        persButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            		scene.projection = "pers";
            		frame.repaint();
            }
        });
        
        JLabel shadingLabel = new JLabel("Shading:");
		JButton textureButton = new JButton("T");
        textureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            		scene.shading = "texture";
            		frame.repaint();
            }
        });
        
        JButton flatButton = new JButton("F");
        flatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            		scene.shading = "flat";
            		frame.repaint();
            }
        });
        
        JButton gouraudButton = new JButton("G");
        gouraudButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            		scene.shading = "gouraud";
            		frame.repaint();
            }
        });
        
        JButton phongButton = new JButton("P");
        phongButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            		scene.shading = "phong";
            		frame.repaint();
            }
        });
        
      
        
		JLabel lightAzimuthLabel = new JLabel("Light: Azimuth:");
        laAngleText = new JTextField("" + scene.getAzimuthAngle());
        laAngleText.addActionListener(new ActionListener() {
        	// Allow the user to change the number of samples.
        	public void actionPerformed(ActionEvent e) {
        		String text = laAngleText.getText();
        		scene.setAzimuthAngle(Double.parseDouble(text));
        		scene.modifyLightPos();
            	frame.repaint();
            }
        });
        
        
        JLabel lightInclinationLabel = new JLabel("Inclination:");
        liAngleText = new JTextField("" + scene.getInclinationAngle());
        liAngleText.addActionListener(new ActionListener() {
        	// Allow the user to change the number of samples.
        	public void actionPerformed(ActionEvent e) {
        		String text = liAngleText.getText();
        		scene.setInclinationAngle(Double.parseDouble(text));
        		scene.modifyLightPos();
            	frame.repaint();
            }
        });
        
        JLabel lightIntensityLabel = new JLabel("Intensity:");
        intensityText = new JTextField("" + scene.intensity);
        intensityText.addActionListener(new ActionListener() {
        	// Allow the user to change the number of samples.
        	public void actionPerformed(ActionEvent e) {
        		String text = intensityText.getText();
        		scene.intensity = Double.parseDouble(text);
        		scene.modifyLightPos();
            	frame.repaint();
            }
        });
        
        JLabel heightLabel = new JLabel("H:");
        heightText = new JTextField("" + (scene.lightRadius - Configs.MAXZ));
        heightText.addActionListener(new ActionListener() {
        	// Allow the user to change the number of samples.
        	public void actionPerformed(ActionEvent e) {
        		String text = heightText.getText();
        		scene.lightRadius = Double.parseDouble(text) + Configs.MAXZ;
        		scene.modifyLightPos();
            	frame.repaint();
            }
        });
        
        
        
        JLabel caAngleLabel = new JLabel("Camera: Azimuth:");
        caAngleText = new JTextField("" + (scene.camAzimuth));
        caAngleText.addActionListener(new ActionListener() {
        	// Allow the user to change the number of samples.
        	public void actionPerformed(ActionEvent e) {
        		String text = caAngleText.getText();
        		scene.camAzimuth = Double.parseDouble(text);
        		scene.modifyCamPos();
            	frame.repaint();
            }
        });
        
        JLabel ciAngleLabel = new JLabel("Inclination:");
        ciAngleText = new JTextField("" + (scene.camInclination));
        ciAngleText.addActionListener(new ActionListener() {
        	// Allow the user to change the number of samples.
        	public void actionPerformed(ActionEvent e) {
        		String text = ciAngleText.getText();
        		scene.camInclination = Double.parseDouble(text);
        		scene.modifyCamPos();
            	frame.repaint();
            }
        });
        
        JLabel cRadiusLabel = new JLabel("Raidus (x MaxZ):");
        cRadiusText = new JTextField("" + (scene.camRadius / Configs.MAXZ));
        cRadiusText.addActionListener(new ActionListener() {
        	// Allow the user to change the number of samples.
        	public void actionPerformed(ActionEvent e) {
        		String text = cRadiusText.getText();
        		scene.camRadius = Double.parseDouble(text) * Configs.MAXZ;
        		scene.modifyCamPos();
            	frame.repaint();
            }
        });
        
        JLabel cFLLabel = new JLabel("f:");
        JTextField cFLText = new JTextField("" + (scene.focalLength));
        cFLText.addActionListener(new ActionListener() {
        	// Allow the user to change the number of samples.
        	public void actionPerformed(ActionEvent e) {
        		String text = cFLText.getText();
        		scene.focalLength = Double.parseDouble(text);
            	frame.repaint();
            }
        });
        
        JLabel diffCoeLabel = new JLabel("DiffusionCoe.:");
        JTextField diffCoeText = new JTextField("" + (scene.diffCoe));
        diffCoeText.addActionListener(new ActionListener() {
        	// Allow the user to change the number of samples.
        	public void actionPerformed(ActionEvent e) {
        		String text = diffCoeText.getText();
        		scene.diffCoe = Double.parseDouble(text);
            	frame.repaint();
            }
        });
        
        JLabel reflCoeLabel = new JLabel("ReflectionCoe.:");
        JTextField reflCoeText = new JTextField("" + (scene.reflCoe));
        reflCoeText.addActionListener(new ActionListener() {
        	// Allow the user to change the number of samples.
        	public void actionPerformed(ActionEvent e) {
        		String text = reflCoeText.getText();
        		scene.reflCoe = Double.parseDouble(text);
            	frame.repaint();
            }
        });
        toolBar1 = new JToolBar();
        toolBar2 = new JToolBar();
        toolBar3 = new JToolBar();
        
//        toolBar1.add(resetButton);
        toolBar1.add(projectionLabel);
        toolBar1.add(orthoButton);
        toolBar1.add(persButton);
        toolBar1.add(shadingLabel);
        toolBar1.add(textureButton);
        toolBar1.add(flatButton);
        toolBar1.add(gouraudButton);
        toolBar1.add(phongButton);
        toolBar1.add(diffCoeLabel);
        toolBar1.add(diffCoeText);
        toolBar1.add(reflCoeLabel);
        toolBar1.add(reflCoeText);
        
        toolBar2.add(lightAzimuthLabel);
        toolBar2.add(laAngleText);
        toolBar2.add(lightInclinationLabel);
        toolBar2.add(liAngleText);
        toolBar2.add(lightIntensityLabel);
        toolBar2.add(intensityText);
        toolBar2.add(heightLabel);
        toolBar2.add(heightText);
        
        
        toolBar3.add(caAngleLabel);
        toolBar3.add(caAngleText);
        toolBar3.add(ciAngleLabel);
        toolBar3.add(ciAngleText);
        toolBar3.add(cRadiusLabel);
        toolBar3.add(cRadiusText);
        toolBar3.add(cFLLabel);
        toolBar3.add(cFLText);
        
	}
}