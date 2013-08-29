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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.swing.*;

public class MapArea extends JPanel implements MouseListener {

	private static final long serialVersionUID = 4489659904448610752L;
	private int width,height;
	private double[][] elevation;
	private Color[] colors;
	private Color mobColor,npcColor;
	private int radius;
	private double force;
	private Timer timer;
	private MouseEvent mouse;
	
	public MapArea(int width,int height){
		setLayout(null);
		setBackground(Color.white);
		setPreferredSize(new Dimension(width,height));
		addMouseListener(this);
		colors = new Color[10];
		colors[0] = new Color(0,49,0);
		colors[1] = new Color(40,90,0);
		colors[2] = new Color(75,125,25);
		colors[3] = new Color(100,175,30);
		colors[4] = new Color(125,215,35);
		colors[5] = new Color(215,225,30);
		colors[6] = new Color(185,150,35);
		colors[7] = new Color(185,100,0);
		colors[8] = new Color(175,15,15);
		colors[9] = new Color(255,30,15);
		mobColor = new Color(255,40,255);
		npcColor = new Color(115,225,210);
	}
	public void init(int width, int height){
		this.width = width;
		this.height = height;
		elevation = new double[width][height];
		radius = (int)(Math.min(width, height)/3);
		force = 0.5;
		repaint();
	}
	public void setRadius(int x){
		radius = x;
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
				
				g.setColor(colors[(int)elevation[j][k]]);
				g.fillRect(x, y,(int)((double)this.getWidth()/(double)width), (int)((double)this.getHeight()/(double)height));
				
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
			int x = Math.round(MainCanvas.enemyPos.get(j)[0]*this.getWidth()/width);
			int y = Math.round(MainCanvas.enemyPos.get(j)[1]*this.getHeight()/height);
			int w = (int)(this.getWidth()/width/2);
			int h = (int)(this.getHeight()/height/2);
			g.fillOval(x + w/4, y + h/4 , w, h);
		}
		g.setColor(npcColor);
		for(int j=0;j<MainCanvas.npcPos.size();j++){
			int x = Math.round(MainCanvas.npcPos.get(j)[0]*this.getWidth()/width);
			int y = Math.round(MainCanvas.npcPos.get(j)[1]*this.getHeight()/height);
			int w = (int)(this.getWidth()/width/2);
			int h = (int)(this.getHeight()/height/2);
			g.fillOval(x + w/4, y + h, w, h);
		}
	}
	public void drawWater(Graphics g){
		g.setColor(new Color(69,121,252));
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
			int w = (int)(this.getWidth()/width/2);
			int h = (int)(this.getHeight()/height/2);
			g.fillOval(x + w, y + h/4, w, h);
		}
		g.setColor(Color.white);
		for(int j=0;j<MainCanvas.hostAPos.size();j++){
			int x = Math.round(MainCanvas.hostAPos.get(j)[0]*this.getWidth()/width);
			int y = Math.round(MainCanvas.hostAPos.get(j)[1]*this.getHeight()/height);
			int w = (int)(this.getWidth()/width/2);
			int h = (int)(this.getHeight()/height/2);
			g.fillOval(x + w, y + h/4, w, h);
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
			int w = (int)(this.getWidth()/width/2);
			int h = (int)(this.getHeight()/height/2);
			g.fillOval(x + w, y + h, w, h);
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
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		drawElevation(g);
		drawWater(g);
		drawNPCs(g);
		drawLocations(g);
		drawEvents(g);
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
		timer = new Timer(150, new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				interpretMouse();
			}
		});
		timer.start();
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
				if(MainCanvas.tabbedPane.getSelectedIndex() != 0){
					MainCanvas.incrHeight = false;
				}
				
				//HEIGHT INCREASE
				if(MainCanvas.incrHeight){
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
				//NPC
				else if(MainCanvas.tabbedPane.getSelectedIndex() == 1){
					int[] info = new int[3];
					info[0] = t;
					info[1] = u;
					if(((String)MainCanvas.npcTypeList.getSelectedItem()).equalsIgnoreCase("Enemies")){
						info[2] = MainCanvas.enemies.get(MainCanvas.npcNames.getSelectedItem());
						MainCanvas.enemyPos.add(info);
					}
					else if(((String)MainCanvas.npcTypeList.getSelectedItem()).equalsIgnoreCase("Quest Givers")){
						info[2] = MainCanvas.npcs.get(MainCanvas.npcNames.getSelectedItem());
						MainCanvas.npcPos.add(info);
					}
				}
				//LOCATIONS
				else if(MainCanvas.tabbedPane.getSelectedIndex() == 2){
					int[] info = new int[3];
					info[0] = t;
					info[1] = u;
					if(((String)MainCanvas.locTypeList.getSelectedItem()).equalsIgnoreCase("City")){
						info[2] = MainCanvas.towns.get(MainCanvas.locNames.getSelectedItem());
						MainCanvas.townPos.add(info);
					}
					else if(((String)MainCanvas.locTypeList.getSelectedItem()).equalsIgnoreCase("Hostile Area")){
						info[2] = MainCanvas.hostileAreas.get(MainCanvas.locNames.getSelectedItem());
						MainCanvas.hostAPos.add(info);
					}
				}
				//WATER
				else if(MainCanvas.tabbedPane.getSelectedIndex() == 3){
					int[] info = new int[3];
					info[0] = t;
					info[1] = u;
					if(((String)MainCanvas.waterTypeList.getSelectedItem()).equalsIgnoreCase("Sea")){
						MainCanvas.seaPos.add(info);
						elevation[t][u] = 0;
					}
					else if(((String)MainCanvas.waterTypeList.getSelectedItem()).equalsIgnoreCase("Lake")){
						//TODO algorithm for filling water according to heights, natural water flow
						boolean alreadyAdded = false;
						for(int j=0;j<MainCanvas.lakePos.size();j++){
							if(MainCanvas.lakePos.get(j)[0] == info[0] && MainCanvas.lakePos.get(j)[1] == info[1]){
								alreadyAdded = true;
								break;
							}
						}
						if(!alreadyAdded){
							MainCanvas.lakePos.add(info);
						}
					}
				}
				//EVENTS
				else if(MainCanvas.tabbedPane.getSelectedIndex() == 4){
					int[] info = new int[3];
					info[0] = t;
					info[1] = u;
					String chosenType = (String)MainCanvas.eventItemTypeList.getSelectedItem();
					if(chosenType.equalsIgnoreCase("Enemy")){
						info[2] = MainCanvas.enemies.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("QNPC")){
						info[2] = MainCanvas.npcs.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("Quest Item")){
						info[2] = MainCanvas.qItems.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("Equipment")){
						info[2] = MainCanvas.equipment.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else if(chosenType.equalsIgnoreCase("Item")){
						info[2] = MainCanvas.items.get(MainCanvas.eventItemList.getSelectedItem());
					}
					else{
						info[2] = -1;
					}
					MainCanvas.events.put(info, (String)MainCanvas.eventTypeList.getSelectedItem() + ";" + MainCanvas.eventDescription.getText());
				}
				String message = "Current position:\n" + t + "," + u + "\nElevation:\n" + elevation[t][u] + "";
				for(int j=0;j<MainCanvas.enemyPos.size();j++){
					if(MainCanvas.enemyPos.get(j)[0] == t && MainCanvas.enemyPos.get(j)[1] == u){
						message+="\nEnemy present:\n" + getFirstKey(MainCanvas.enemyPos.get(j)[2],MainCanvas.enemies) + " (ID: " + MainCanvas.enemyPos.get(j)[2] + ")";
					}
				}
				for(int j=0;j<MainCanvas.npcPos.size();j++){
					if(MainCanvas.npcPos.get(j)[0] == t && MainCanvas.npcPos.get(j)[1] == u){
						message+="\nQuest Giver present:\n" + getFirstKey(MainCanvas.npcPos.get(j)[2],MainCanvas.npcs) + " (ID: " + MainCanvas.npcPos.get(j)[2] + ")";
					}
				}
				for(int j=0;j<MainCanvas.townPos.size();j++){
					if(MainCanvas.townPos.get(j)[0] == t && MainCanvas.townPos.get(j)[1] == u){
						message+="\nCurrent location set to: " + getFirstKey(MainCanvas.townPos.get(j)[2],MainCanvas.towns) + " (ID: " + MainCanvas.townPos.get(j)[2] + ")";
					}
				}
				for(int j=0;j<MainCanvas.hostAPos.size();j++){
					if(MainCanvas.hostAPos.get(j)[0] == t && MainCanvas.hostAPos.get(j)[1] == u){
						message+="\nCurrent location set to: " + getFirstKey(MainCanvas.hostAPos.get(j)[2],MainCanvas.hostileAreas) + " (ID: " + MainCanvas.hostAPos.get(j)[2] + ")";
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
				MainCanvas.updateInfo(message);
				break;
			}
			case MouseEvent.BUTTON3_MASK:{
				//height increase is turned on
				if(MainCanvas.incrHeight && MainCanvas.tabbedPane.getSelectedIndex() == 0){
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
					for(int j=0;j<MainCanvas.enemyPos.size();j++){
						if(MainCanvas.enemyPos.get(j)[0] == t && MainCanvas.enemyPos.get(j)[1] == u){
							MainCanvas.enemyPos.remove(j);
						}
					}
					for(int j=0;j<MainCanvas.npcPos.size();j++){
						if(MainCanvas.npcPos.get(j)[0] == t && MainCanvas.npcPos.get(j)[1] == u){
							MainCanvas.npcPos.remove(j);
						}
					}
					for(int j=0;j<MainCanvas.lakePos.size();j++){
						if(MainCanvas.lakePos.get(j)[0] == t && MainCanvas.lakePos.get(j)[1] == u){
							MainCanvas.lakePos.remove(j);
						}
					}
					for(int j=0;j<MainCanvas.seaPos.size();j++){
						if(MainCanvas.seaPos.get(j)[0] == t && MainCanvas.seaPos.get(j)[1] == u){
							MainCanvas.seaPos.remove(j);
						}
					}
					for(int j=0;j<MainCanvas.seaPos.size();j++){
						if(MainCanvas.seaPos.get(j)[0] == t && MainCanvas.seaPos.get(j)[1] == u){
							MainCanvas.seaPos.remove(j);
						}
					}
					Set<int[]> eventPos = MainCanvas.events.keySet();
					Iterator<int[]> i = eventPos.iterator();
					while(i.hasNext()){
						int[] el = i.next();
						if(el[0] == t && el[1] == u){
							MainCanvas.events.remove(el);
						}
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
