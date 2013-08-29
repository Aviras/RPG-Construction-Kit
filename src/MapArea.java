import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.swing.*;


public class MapArea extends JPanel implements MouseListener {

	private static final long serialVersionUID = 4489659904448610752L;
	private int width,height;
	private double[][] elevation;
	private HashMap<String,boolean[][]> layers;
	private Color[] colors,colorsForest,colorsMountains;
	private Color mobColor,npcColor;
	private int radius,waterRadius;
	private double force;
	private Timer timer;
	private MouseEvent mouse;
	private boolean suppressTimer = false;
	
	public MapArea(int width,int height){
		setLayout(null);
		setBackground(Color.white);
		setPreferredSize(new Dimension(width,height));
		addMouseListener(this);
		colorsForest = new Color[10];
		colorsForest[0] = new Color(0,49,0);
		colorsForest[1] = new Color(40,90,0);
		colorsForest[2] = new Color(75,125,25);
		colorsForest[3] = new Color(100,175,30);
		colorsForest[4] = new Color(125,215,35);
		colorsForest[5] = new Color(215,225,30);
		colorsForest[6] = new Color(185,150,35);
		colorsForest[7] = new Color(185,100,0);
		colorsForest[8] = new Color(175,15,15);
		colorsForest[9] = new Color(255,30,15);
		
		colorsMountains = new Color[10];
		colorsMountains[0] = new Color(28,57,31);
		colorsMountains[1] = new Color(47,78,50);
		colorsMountains[2] = new Color(60,78,62);
		colorsMountains[3] = new Color(60,68,60);
		colorsMountains[4] = new Color(86,91,84);
		colorsMountains[5] = new Color(117,124,116);
		colorsMountains[6] = new Color(144,149,143);
		colorsMountains[7] = new Color(178,188,190);
		colorsMountains[8] = new Color(210,225,224);
		colorsMountains[9] = new Color(240,240,240);
		mobColor = new Color(255,40,255);
		npcColor = new Color(115,225,210);
	}
	public void init(int width, int height){
		this.width = width;
		this.height = height;
		elevation = new double[width][height];
		layers = new HashMap<String,boolean[][]>();
		radius = (int)(Math.min(width, height)/3);
		waterRadius = (int)(Math.min(WIDTH, HEIGHT)/40);
		force = 0.5;
		if(MainCanvas.type.equalsIgnoreCase("naaldbos")){
			//colors = colorsMountains;
			colors = colorsForest;
		}
		else if(MainCanvas.type.equalsIgnoreCase("loofbos")){
			colors = colorsForest;
		}
		repaint();
	}
	public void createLayer(int level, String name){
		boolean[][] layerMap = new boolean[width][height];
		layers.put(level + ": " + name, layerMap);
		MainCanvas.layerList.addItem(level + ": " + name);
		MainCanvas.layerList.setSelectedIndex(MainCanvas.layerList.getItemCount()-1);
		repaint();
	}
	public void addLayer(int level, String name, boolean[][] map){
		layers.put(level + ": " + name, map);
		MainCanvas.layerList.addItem(level + ": " + name);
		MainCanvas.layerList.setSelectedIndex(MainCanvas.layerList.getItemCount()-1);
		repaint();
	}
	public String getLayerKey(int level){
		for(String s: layers.keySet()){
			if(s.startsWith(level + "")){
				return s;
			}
		}
		return null;
	}
	public void removeLayer(String level){
		layers.remove(level);
	}
	public boolean[][] getLayer(int level){
		for(String s: layers.keySet()){
			if(Integer.parseInt(s.split(": ")[0]) == level){
				return layers.get(s);
			}
		}
		return null;
	}
	public String getLayerMap(int level){
		boolean[][] layerMap = layers.get(getLayerKey(level));
		StringBuilder sb = new StringBuilder();
		for(int j=0;j<height;j++){
			for(int k=0;k<width;k++){
				if(layerMap[k][j]){
					sb.append("-");
				}
				else{
					sb.append("o");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	public String getCurrentLayerName(){
		for(String s: layers.keySet()){
			if(Integer.parseInt(s.split(": ")[0]) == MainCanvas.terrainLevel){
				return s;
			}
		}
		return null;
	}
	public void setRadius(int x){
		radius = x;
	}
	public void setWaterRadius(int x){
		waterRadius = x;
	}
	public void setForce(double x){
		force = x;
	}
	public void setElevation(int x,int y,double value){
		elevation[x][y] = value;
	}
	public String getElevation(){
		StringBuilder sb = new StringBuilder();
		for(int j=0;j<height;j++){
			for(int k=0;k<width;k++){
				sb.append((int)elevation[k][j]);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	public HashMap<String,boolean[][]> getLayers(){
		return layers;
	}
	public int[] getNumLayerValues(){
		int[] v = new int[layers.size()];
		int index = 0;
		for(String s: layers.keySet()){
			v[index] = Integer.parseInt(s.split(": ")[0]);
			index++;
		}
		return v;
	}
	public void makeUniform(int y){
		for(int j=0;j<width;j++){
			for(int k=0;k<width;k++){
				elevation[j][k] = y;
			}
		}
		repaint();
	}
	public void randomize(int y,double maxDev,String mode){
		makeUniform(y);
		for(int j=0;j<width;j++){
			for(int k=0;k<height;k++){
				Random gen = new Random();
				double dev = 0;
				if(mode.equalsIgnoreCase("gaussian")){
					dev = maxDev*gen.nextGaussian();
				}
				else if(mode.equalsIgnoreCase("uniform")){
					dev = maxDev*(gen.nextDouble()-0.5)*2;
				}
				elevation[j][k]+=dev;
				elevation[j][k] = Math.min(9, elevation[j][k]);
				elevation[j][k] = Math.max(0, elevation[j][k]);
			}
		}
	}
	public void drawLayers(Graphics g){
		if(MainCanvas.terrainLevel != 0){
			boolean[][] currentLayer = layers.get(getLayerKey(MainCanvas.terrainLevel));
			boolean[][] upperLayer = layers.get(getLayerKey(MainCanvas.terrainLevel+1));
			boolean[][] lowerLayer = layers.get(getLayerKey(MainCanvas.terrainLevel-1));
			drawLayer(g,upperLayer,100);
			drawLayer(g,lowerLayer,120);
			drawLayer(g,currentLayer,255);
		}
	}
	public void drawLayer(Graphics g, boolean[][] layer, int alpha){
		if(layer != null){
			g.setColor(new Color(100,50,0,alpha));
			for(int j=0;j<width;j++){
				for(int k=0;k<height;k++){
					if(layer[j][k]){
						int x = Math.round(j*this.getWidth()/width);
						int y = Math.round(k*this.getHeight()/height);
						g.fillRect(x, y,(int)((double)this.getWidth()/(double)width), (int)((double)this.getHeight()/(double)height));
					}
				}
			}
			for(int[] i: MainCanvas.entrancePos.keySet()){
				if(i[2] == MainCanvas.terrainLevel || i[3] == MainCanvas.terrainLevel){
					g.setColor(new Color(0,0,200,alpha));
					int x = Math.round(i[0]*this.getWidth()/width);
					int y = Math.round(i[1]*this.getHeight()/height);
					g.fillRect(x, y, (int)((double)this.getWidth()/(double)width), (int)((double)this.getHeight()/(double)height));
				}
			}
		}
	}
	public void drawElevation(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		//Point2D center = new Point2D.Double((double)this.getWidth()/(double)width/2.0, (double)this.getHeight()/(double)height/2.0);
		/*
		 * Point2D center = new Point2D.Float(50, 50);
     float radius = 25;
     Point2D focus = new Point2D.Float(40, 40);
     float[] dist = {0.0f, 0.2f, 1.0f};
     Color[] colors = {Color.RED, Color.WHITE, Color.BLUE};
     RadialGradientPaint p =
         new RadialGradientPaint(center, radius, focus,
                                 dist, colors,
                                 CycleMethod.NO_CYCLE);
		 */
		for(int j=0;j<width;j++){
			for(int k=0;k<height;k++){
				int x = Math.round(j*this.getWidth()/width);
				int y = Math.round(k*this.getHeight()/height);
				/*double[] dev = calcFocusPoint(j,k);
				/**adjust scaling param
				Point2D center = new Point2D.Double((double)x + (double)this.getWidth()/(double)width/2.0, (double)y + (double)this.getHeight()/(double)height/2.0);
				Point2D focus = new Point2D.Double(center.getX() - dev[0]*10, center.getY() - dev[1]*10);
				Color[] c = {colors[(int)elevation[j][k]],colors[(int)elevation[j][k]]};
				try{
					c[1] = colors[(int)elevation[j-1][k]];
				}catch(ArrayIndexOutOfBoundsException exc){
				}
				float radius = (float)((double)this.getWidth()/(double)width);
				float[] dist = {0.0f,1.0f};
				RadialGradientPaint p = new RadialGradientPaint(center,radius,focus,dist,c,CycleMethod.NO_CYCLE);
				g2d.setPaint(p);
				g2d.fillRect(x, y, (int)((double)this.getWidth()/(double)width), (int)((double)this.getHeight()/(double)height));*/
				Color c = colors[(int)elevation[j][k]];
				g.setColor(c);
				if(MainCanvas.terrainLevel != 0){
					Color transparent = new Color(c.getRed(),c.getGreen(),c.getBlue(),150);
					g.setColor(transparent);
				}
				g.fillRect(x, y,(int)((double)this.getWidth()/(double)width), (int)((double)this.getHeight()/(double)height));
				for(int[] i: MainCanvas.entrancePos.keySet()){
					if(i[0] == j && i[1] == k && (i[2] == MainCanvas.terrainLevel || i[3] == MainCanvas.terrainLevel)){
						int w = (int)(this.getWidth()/width/2);
						int h = (int)(this.getHeight()/height/2);
						g.setColor(new Color(0,0,200));
						g.fillOval(x + w/4, y + h/4 , w, h);
					}
				}
			}
		}
	}
	public double[] calcFocusPoint(int j, int k){
		double[] dev = new double[2];
		try{
			dev[0] = (int)elevation[j-1][k] - (int)elevation[j+1][k];
			dev[1] = (int)elevation[j][k-1] - (int)elevation[j][k+1];
		} catch(ArrayIndexOutOfBoundsException exc){
		}
		return dev;
	}
	public void drawNPCs(Graphics g){
		g.setColor(mobColor);
		for(int j=0;j<MainCanvas.enemyPos.size();j++){
			if(MainCanvas.enemyPos.get(j)[2] == MainCanvas.terrainLevel){
				int x = Math.round(MainCanvas.enemyPos.get(j)[0]*this.getWidth()/width);
				int y = Math.round(MainCanvas.enemyPos.get(j)[1]*this.getHeight()/height);
				int z = MainCanvas.enemyPos.get(j)[2];
				int w = (int)(this.getWidth()/width/2);
				int h = (int)(this.getHeight()/height/2);
				g.fillOval(x + w/4, y + h/4 , w, h);
			}
		}
		g.setColor(npcColor);
		for(int j=0;j<MainCanvas.npcPos.size();j++){
			if(MainCanvas.npcPos.get(j)[2] == MainCanvas.terrainLevel){
				int x = Math.round(MainCanvas.npcPos.get(j)[0]*this.getWidth()/width);
				int y = Math.round(MainCanvas.npcPos.get(j)[1]*this.getHeight()/height);
				int z = MainCanvas.npcPos.get(j)[2];
				int w = (int)(this.getWidth()/width/2);
				int h = (int)(this.getHeight()/height/2);
				g.fillOval(x + w/4, y + h/4 , w, h);
			}
		}
	}
	public void drawWater(Graphics g){
		Color c = new Color(69,121,252);
		g.setColor(c);
		if(MainCanvas.terrainLevel != 0){
			Color transparent = new Color(c.getRed(),c.getGreen(),c.getBlue(),110);
			g.setColor(transparent);
		}
		for(int j=0;j<MainCanvas.seaPos.size();j++){
			int x = Math.round(MainCanvas.seaPos.get(j)[0]*this.getWidth()/width);
			int y = Math.round(MainCanvas.seaPos.get(j)[1]*this.getHeight()/height);
			int w = (int)(this.getWidth()/width);
			int h = (int)(this.getHeight()/height);
			g.fillRect(x, y, w, h);
		}
		for(int j=0;j<MainCanvas.lakePos.size();j++){
			int x = Math.round(MainCanvas.lakePos.get(j)[0]*this.getWidth()/width);
			int y = Math.round(MainCanvas.lakePos.get(j)[1]*this.getHeight()/height);
			int w = (int)(this.getWidth()/width);
			int h = (int)(this.getHeight()/height);
			g.fillRect(x, y, w, h);
		}
	}
	public void drawLocations(Graphics g){
		g.setColor(Color.black);
		for(int j=0;j<MainCanvas.townPos.size();j++){
			int x = Math.round(MainCanvas.townPos.get(j)[0]*this.getWidth()/width);
			int y = Math.round(MainCanvas.townPos.get(j)[1]*this.getHeight()/height);
			int z = MainCanvas.townPos.get(j)[2];
			int w = (int)(this.getWidth()/width/2);
			int h = (int)(this.getHeight()/height/2);
			if(z == MainCanvas.terrainLevel){
				g.fillOval(x + w/4, y + h/4 , w, h);
			}
		}
		g.setColor(Color.white);
		for(int j=0;j<MainCanvas.hostAPos.size();j++){
			int x = Math.round(MainCanvas.hostAPos.get(j)[0]*this.getWidth()/width);
			int y = Math.round(MainCanvas.hostAPos.get(j)[1]*this.getHeight()/height);
			int z = MainCanvas.hostAPos.get(j)[2];
			int w = (int)(this.getWidth()/width/2);
			int h = (int)(this.getHeight()/height/2);
			if(z == MainCanvas.terrainLevel){
				g.fillOval(x + w/4, y + h/4 , w, h);
			}
		}
	}
	public void drawEvents(Graphics g){
		g.setColor(Color.gray);
		Set<int[]> eventPos = MainCanvas.events.keySet();
		Iterator<int[]> i = eventPos.iterator();
		while(i.hasNext()){
			int[] el = i.next();
			int x = Math.round(el[0]*this.getWidth()/width);
			int y = Math.round(el[1]*this.getHeight()/height);
			int z = el[2];
			int w = (int)(this.getWidth()/width/2);
			int h = (int)(this.getHeight()/height/2);
			if(z == MainCanvas.terrainLevel){
				g.fillOval(x + w/4, y + h/4 , w, h);
			}
		}
	}
	public void drawRoster(Graphics g){
		g.setColor(Color.black);
		for(int j=0;j<width;j++){
			int pos = Math.round(j*this.getWidth()/width);
			g.drawLine(pos, 0, pos, this.getHeight());
		}
		for(int j=0;j<height;j++){
			int pos = Math.round(j*this.getHeight()/height);
			g.drawLine(0, pos, this.getWidth(), pos);
		}
	}
	public void drawHerbs(Graphics g){
		g.setColor(Color.ORANGE);
		for(int j=0;j<MainCanvas.herbPos.size();j++){
			int x = Math.round(MainCanvas.herbPos.get(j)[0]*this.getWidth()/width);
			int y = Math.round(MainCanvas.herbPos.get(j)[1]*this.getHeight()/height);
			int z = MainCanvas.herbPos.get(j)[2];
			int w = (int)(this.getWidth()/width/2);
			int h = (int)(this.getHeight()/height/2);
			if(z == MainCanvas.terrainLevel){
				g.fillOval(x + w/4, y + h/4 , w, h);
			}
		}
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		drawElevation(g);
		drawWater(g);
		drawLayers(g);
		drawNPCs(g);
		drawLocations(g);
		drawEvents(g);
		drawHerbs(g);
		drawRoster(g);
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {	
	}

	public void mouseExited(MouseEvent arg0) {
	}
	
	public void mousePressed(MouseEvent arg0) {
		mouse = arg0;
		interpretMouse();
		if(!suppressTimer){
			timer = new Timer(150, new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					interpretMouse();
				}
			});
			timer.start();
		}
		else{
			suppressTimer = false;
		}
	}

	public void mouseReleased(MouseEvent arg0) {
		if(timer != null){
			timer.stop();
		}
	}
	public void interpretMouse(){
		// get mouse position on screen and transform it to component
		PointerInfo a = MouseInfo.getPointerInfo();
		Point point = new Point(a.getLocation());
		SwingUtilities.convertPointFromScreen(point, this);
		double x = point.getX();
		double y = point.getY();
		int t = (int)(x/this.getWidth()*width);
		int u = (int)(y/this.getHeight()*height);
		switch(mouse.getModifiers()){
			case MouseEvent.BUTTON1_MASK:{
				// height increase is turned on
				if(MainCanvas.tabbedPane.getSelectedIndex() != 0 /*|| MainCanvas.terrainLevel != 0*/){
					MainCanvas.incrHeight = false;
					MainCanvas.toggleHeight.setText("Height increase: false");
				}
				
				//HEIGHT INCREASE
				if(MainCanvas.incrHeight){
					if(MainCanvas.terrainLevel != 0){
						String key = getLayerKey(MainCanvas.terrainLevel);
						boolean[][] map = layers.get(key);
						map[t][u] = true;
					}
					else{
						try{
							elevation[t][u]++;
							for(int j=-radius;j<=radius;j++){
								for(int k=-radius;k<=radius;k++){
									if(!(k==0 && j==0)){
										try{
											elevation[t+j][u+k]+=force/Math.sqrt(Math.pow(j, 2) + Math.pow(k, 2));
										}catch(ArrayIndexOutOfBoundsException e){}
									}
									try{
										elevation[t+j][u+k] = Math.min(9, elevation[t+j][u+k]);
									}catch(ArrayIndexOutOfBoundsException e){}
								}
							}
						} catch(ArrayIndexOutOfBoundsException e){
							
						}
					}
				}
				//NPC
				else if(MainCanvas.tabbedPane.getSelectedIndex() == 1){
					int[] info = new int[4];
					info[0] = t;
					info[1] = u;
					info[2] = MainCanvas.terrainLevel;
					if(((String)MainCanvas.npcTypeList.getSelectedItem()).equalsIgnoreCase("Enemies")){
						info[3] = MainCanvas.enemies.get(MainCanvas.npcNames.getSelectedItem());
						MainCanvas.enemyPos.add(info);
					}
					else if(((String)MainCanvas.npcTypeList.getSelectedItem()).equalsIgnoreCase("NPCs")){
						info[3] = MainCanvas.npcs.get(MainCanvas.npcNames.getSelectedItem());
						MainCanvas.npcPos.add(info);
					}
				}
				//LOCATIONS
				else if(MainCanvas.tabbedPane.getSelectedIndex() == 2){
					int[] info = new int[4];
					info[0] = t;
					info[1] = u;
					info[2] = MainCanvas.terrainLevel;
					if(((String)MainCanvas.locTypeList.getSelectedItem()).equalsIgnoreCase("City")){
						info[3] = MainCanvas.towns.get(MainCanvas.locNames.getSelectedItem());
						MainCanvas.townPos.add(info);
					}
					else if(((String)MainCanvas.locTypeList.getSelectedItem()).equalsIgnoreCase("Hostile Area")){
						info[3] = MainCanvas.hostileAreas.get(MainCanvas.locNames.getSelectedItem());
						MainCanvas.hostAPos.add(info);
					}
				}
				//WATER
				else if(MainCanvas.tabbedPane.getSelectedIndex() == 3){
					int[] info = new int[4];
					info[0] = t;
					info[1] = u;
					info[2] = MainCanvas.terrainLevel;
					if(((String)MainCanvas.waterTypeList.getSelectedItem()).equalsIgnoreCase("Sea")){
						MainCanvas.seaPos.add(info);
						elevation[t][u] = 0;
					}
					else if(((String)MainCanvas.waterTypeList.getSelectedItem()).equalsIgnoreCase("Lake")){
						//TODO algorithm for filling water according to heights, natural water flow
						for(int k=-waterRadius;k<=waterRadius;k++){
							for(int i=-waterRadius;i<=waterRadius;i++){
								if(Math.sqrt(k*k + i*i) <= waterRadius){
									boolean alreadyAdded = false;
									for(int j=0;j<MainCanvas.lakePos.size();j++){
										if(MainCanvas.lakePos.get(j)[0] == (info[0]+k) && MainCanvas.lakePos.get(j)[1] == (info[1]+i)){
											alreadyAdded = true;
											break;
										}
									}
									if(!alreadyAdded){
										MainCanvas.lakePos.add(new int[]{info[0]+k,info[1]+i,info[2],0});
									}
								}
							}
						}
					}
				}
				//EVENTS
				else if(MainCanvas.tabbedPane.getSelectedIndex() == 4){
					int[] info = new int[4];
					info[0] = t;
					info[1] = u;
					info[2] = MainCanvas.terrainLevel;
					String chosenType = (String)MainCanvas.eventItemTypeList.getSelectedItem();
					if(chosenType.equalsIgnoreCase("Enemy")){
						info[3] = MainCanvas.enemies.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("QNPC")){
						info[3] = MainCanvas.npcs.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("Quest Item")){
						info[3] = MainCanvas.qItems.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("Equipment")){
						info[3] = MainCanvas.equipment.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("Item")){
						info[3] = MainCanvas.items.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("Potion")){
						info[3] = MainCanvas.potions.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("Clothing")){
						info[3] = MainCanvas.clothes.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("Consumable")){
						info[3] = MainCanvas.consumables.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else{
						info[3] = -1;
					}
					MainCanvas.events.put(info, (String)MainCanvas.eventTypeList.getSelectedItem() + ";" + MainCanvas.eventDescription.getText() + ";" + MainCanvas.logbookPathField.getText());
				}
				//LAYER
				else if(MainCanvas.tabbedPane.getSelectedIndex() == 5){
					if(MainCanvas.chooseEntrance){
						MainCanvas.chooseEntrance = false;
						String[] poss = new String[layers.size()+1];
						poss[0] = "0: Ground Height Map";
						int i=1;
						for(String s: layers.keySet()){
							poss[i] = s;
							i++;
						}
						String layer1 = (String) JOptionPane.showInputDialog(null, "Choose the first layer for the entrance", "Selection", JOptionPane.OK_OPTION, null, poss, null);
						String layer2 = (String) JOptionPane.showInputDialog(null, "Choose the Second layer for the entrance", "Selection", JOptionPane.OK_OPTION, null, poss, null);
						if(!layer1.equalsIgnoreCase(layer2)){
							String description12 = (String) JOptionPane.showInputDialog(null, "Description going from " + layer1 + " to " + layer2 + ":");
							String description21 = (String) JOptionPane.showInputDialog(null, "Description going from " + layer2 + " to " + layer1 + ":");
							try{
								int level1 = Integer.parseInt(layer1.split(": ")[0]);
								int level2 = Integer.parseInt(layer2.split(": ")[0]);
								MainCanvas.entrancePos.put(new int[]{t,u,level1,level2}, new String[]{description12, description21});
								if(level1 != 0){
									String key = getLayerKey(level1);
									boolean[][] map = layers.get(key);
									map[t][u] = true;
								}
								if(level2 != 0){
									String key = getLayerKey(level2);
									boolean[][] map = layers.get(key);
									map[t][u] = true;
								}
							} catch(NullPointerException e){
								e.printStackTrace();
							}
						}
						else{
							JOptionPane.showMessageDialog(null, "Layer 1 can't be equal to layer 2.");
						}
						suppressTimer = true;
						MainCanvas.setEntrance.setText("Add entrance");
					}
				}
				// herbs
				else if(MainCanvas.tabbedPane.getSelectedIndex() == 6){
					int[] info = new int[4];
					info[0] = t;
					info[1] = u;
					info[2] = MainCanvas.terrainLevel;
					info[3] = MainCanvas.herbs.get(MainCanvas.herbList.getSelectedItem());
					MainCanvas.herbPos.add(info);
				}
				String message = "Current position:\n" + t + "," + u + "\nElevation:\n" + elevation[t][u] + "\nLayer: " + MainCanvas.terrainLevel;
				for(int j=0;j<MainCanvas.enemyPos.size();j++){
					if(MainCanvas.enemyPos.get(j)[0] == t && MainCanvas.enemyPos.get(j)[1] == u && MainCanvas.enemyPos.get(j)[2] == MainCanvas.terrainLevel){
						message+="\nEnemy present:\n" + getFirstKey(MainCanvas.enemyPos.get(j)[3],MainCanvas.enemies) + " (ID: " + MainCanvas.enemyPos.get(j)[3] + ")";
					}
				}
				for(int j=0;j<MainCanvas.npcPos.size();j++){
					if(MainCanvas.npcPos.get(j)[0] == t && MainCanvas.npcPos.get(j)[1] == u && MainCanvas.npcPos.get(j)[2] == MainCanvas.terrainLevel){
						message+="\nNPC present:\n" + getFirstKey(MainCanvas.npcPos.get(j)[3],MainCanvas.npcs) + " (ID: " + MainCanvas.npcPos.get(j)[3] + ")";
					}
				}
				for(int j=0;j<MainCanvas.townPos.size();j++){
					if(MainCanvas.townPos.get(j)[0] == t && MainCanvas.townPos.get(j)[1] == u && MainCanvas.townPos.get(j)[2] == MainCanvas.terrainLevel){
						message+="\nCurrent location set to: " + getFirstKey(MainCanvas.townPos.get(j)[3],MainCanvas.towns) + " (ID: " + MainCanvas.townPos.get(j)[3] + ")";
					}
				}
				for(int j=0;j<MainCanvas.hostAPos.size();j++){
					if(MainCanvas.hostAPos.get(j)[0] == t && MainCanvas.hostAPos.get(j)[1] == u && MainCanvas.hostAPos.get(j)[2] == MainCanvas.terrainLevel){
						message+="\nCurrent location set to: " + getFirstKey(MainCanvas.hostAPos.get(j)[3],MainCanvas.hostileAreas) + " (ID: " + MainCanvas.hostAPos.get(j)[3] + ")";
					}
				}
				Set<int[]> eventPos = MainCanvas.events.keySet();
				Iterator<int[]> i = eventPos.iterator();
				while(i.hasNext()){
					int[] el = i.next();
					if(el[0] == t && el[1] == u){
						String content = MainCanvas.events.get(el);
						String[] contentSplit = content.split(";");
						message+="\nDungeon event present: " + contentSplit[0];
					}
				}
				for(int[] pos: MainCanvas.entrancePos.keySet()){
					if(pos[0] == t && pos[1] == u && (pos[2] == MainCanvas.terrainLevel || pos[3] == MainCanvas.terrainLevel)){
						message+="\nPassage between layer " + pos[2] + " and " + pos[3];
					}
				}
				for(int j=0;j<MainCanvas.herbPos.size();j++){
					if(MainCanvas.herbPos.get(j)[0] == t && MainCanvas.herbPos.get(j)[1] == u && MainCanvas.herbPos.get(j)[2] == MainCanvas.terrainLevel){
						message+="\nHerb present:\n" + getFirstKey(MainCanvas.herbPos.get(j)[3],MainCanvas.herbs) + " (ID: " + MainCanvas.herbPos.get(j)[3] + ")";
					}
				}
				MainCanvas.updateInfo(message);
				break;
			}
			case MouseEvent.BUTTON3_MASK:{
				//height increase is turned on
				if(MainCanvas.incrHeight && MainCanvas.tabbedPane.getSelectedIndex() == 0){
					if(MainCanvas.terrainLevel == 0){
						elevation[t][u]--;
						for(int j=-radius;j<=radius;j++){
							for(int k=-radius;k<=radius;k++){
								if(!(k==0 && j==0)){
									try{
										elevation[t+j][u+k]-=force/Math.sqrt(Math.pow(j, 2) + Math.pow(k, 2));
									}catch(Exception e){}
								}
								try{
									elevation[t+j][u+k] = Math.max(0, elevation[t+j][u+k]);
								}catch(ArrayIndexOutOfBoundsException e){}
							}
						}
					}
					else{
						boolean[][] currentLayer = layers.get(getLayerKey(MainCanvas.terrainLevel));
						if(currentLayer != null){
							currentLayer[t][u] = false;
						}
					}
				}
				else{
					for(int j=0;j<MainCanvas.enemyPos.size();j++){
						if(MainCanvas.enemyPos.get(j)[0] == t && MainCanvas.enemyPos.get(j)[1] == u && MainCanvas.enemyPos.get(j)[2] == MainCanvas.terrainLevel){
							MainCanvas.enemyPos.remove(j);
						}
					}
					for(int j=0;j<MainCanvas.npcPos.size();j++){
						if(MainCanvas.npcPos.get(j)[0] == t && MainCanvas.npcPos.get(j)[1] == u && MainCanvas.npcPos.get(j)[2] == MainCanvas.terrainLevel){
							MainCanvas.npcPos.remove(j);
						}
					}
					for(int j=0;j<MainCanvas.townPos.size();j++){
						if(MainCanvas.townPos.get(j)[0] == t && MainCanvas.townPos.get(j)[1] == u && MainCanvas.townPos.get(j)[2] == MainCanvas.terrainLevel){
							MainCanvas.townPos.remove(j);
						}
					}
					for(int j=0;j<MainCanvas.hostAPos.size();j++){
						if(MainCanvas.hostAPos.get(j)[0] == t && MainCanvas.hostAPos.get(j)[1] == u && MainCanvas.hostAPos.get(j)[2] == MainCanvas.terrainLevel){
							MainCanvas.hostAPos.remove(j);
						}
					}
					for(int j=0;j<MainCanvas.lakePos.size();j++){
						if(MainCanvas.lakePos.get(j)[0] == t && MainCanvas.lakePos.get(j)[1] == u && MainCanvas.lakePos.get(j)[2] == MainCanvas.terrainLevel){
							MainCanvas.lakePos.remove(j);
						}
					}
					for(int j=0;j<MainCanvas.seaPos.size();j++){
						if(MainCanvas.seaPos.get(j)[0] == t && MainCanvas.seaPos.get(j)[1] == u && MainCanvas.seaPos.get(j)[2] == MainCanvas.terrainLevel){
							MainCanvas.seaPos.remove(j);
						}
					}
					for(int j=0;j<MainCanvas.herbPos.size();j++){
						if(MainCanvas.herbPos.get(j)[0] == t && MainCanvas.herbPos.get(j)[1] == u && MainCanvas.herbPos.get(j)[2] == MainCanvas.terrainLevel){
							MainCanvas.herbPos.remove(j);
						}
					}
					Set<int[]> eventPos = MainCanvas.events.keySet();
					Iterator<int[]> i = eventPos.iterator();
					ArrayList<int[]> toDelete = new ArrayList<int[]>();
					while(i.hasNext()){
						int[] el = i.next();
						if(el[0] == t && el[1] == u && el[2] == MainCanvas.terrainLevel){
							toDelete.add(el);
						}
					}
					for(int[] del: toDelete){
						MainCanvas.events.remove(del);
					}
				}
				String message = "Current position:\n" + t + "," + u + "\nElevation:\n" + elevation[t][u] + "\n";
				MainCanvas.updateInfo(message);
				break;
			}
		}
		repaint();
	}
	public Object getFirstKey(int t,Hashtable<String,Integer> h){
		Enumeration<String> en = h.keys();
		while(en.hasMoreElements()){
			Object el = en.nextElement();
			if(t == (h.get(el)).intValue()){
				return el;
			}
		}
		return null;
	}
}
