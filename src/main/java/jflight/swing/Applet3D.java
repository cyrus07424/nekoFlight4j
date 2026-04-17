package jflight.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import jflight.actors.Plane;
import jflight.utils.CVector3;

public class Applet3D extends JPanel {

	public int sWidth, sHeight;
	public int sCenterX, sCenterY;

	public CVector3 camerapos;
	public BufferedImage backImage = null;
	int bWidth, bHeight;
	Graphics bGraphics = null;

	public boolean keyShoot;
	public boolean keyLeft;
	public boolean keyRight;
	public boolean keyUp;
	public boolean keyDown;
	public boolean keyBoost;

	public Applet3D() {
		camerapos = new CVector3();
		sWidth = 600;
		sHeight = 400;
		sCenterX = 300;
		sCenterY = 200;
		setPreferredSize(new Dimension(sWidth, sHeight));
		setFocusable(true);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		bgInit();
	}

	public void bgInit() {
		backImage = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_RGB);
		bWidth = sWidth;
		bHeight = sHeight;
		if (bGraphics != null)
			bGraphics.dispose();
		bGraphics = backImage.getGraphics();
	}

	public void clear() {

		if (backImage == null || bWidth != sWidth || bHeight != sHeight)
			bgInit();
		if (bGraphics != null) {
			bGraphics.setColor(Color.black);
			bGraphics.fillRect(0, 0, bWidth, bHeight);
		}
	}

	public void flush() {
		if (backImage == null || bWidth != sWidth || bHeight != sHeight)
			bgInit();
		repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (backImage == null || bWidth != sWidth || bHeight != sHeight)
			bgInit();
		g.drawImage(backImage, 0, 0, null);
	}

	public void change3d(Plane plane, CVector3 sp, CVector3 cp) {

		double x, y, z;
		double x1, y1, z1;

		x = sp.x - camerapos.x;
		y = sp.y - camerapos.y;
		z = sp.z - camerapos.z;

		x1 = x * plane.y00 + y * plane.y01 + z * plane.y02;
		y1 = x * plane.y10 + y * plane.y11 + z * plane.y12;
		z1 = x * plane.y20 + y * plane.y21 + z * plane.y22;

		if (y1 > 10) {

			y1 /= 10;
			cp.x = x1 * 50 / y1 + sCenterX;
			cp.y = -z1 * 50 / y1 + sCenterY;
			cp.z = y1 * 10;
		} else {

			cp.x = -10000;
			cp.y = -10000;
			cp.z = 1;
		}
	}

	public void drawSline(CVector3 p0, CVector3 p1) {
		if (bGraphics != null && p0.x > -10000 && p0.x < 30000 && p0.y > -10000 && p0.y < 30000 &&
				p1.x > -10000 && p1.x < 30000 && p1.y > -10000 && p1.y < 30000) {
			bGraphics.setColor(Color.white);
			bGraphics.drawLine((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
		}
	}

	public void drawBlined(CVector3 p0, CVector3 p1) {
		if (p0.x > -1000 && p1.x > -1000) {
			bGraphics.setColor(Color.yellow);
			bGraphics.drawLine((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
		}
	}

	public void drawBline(CVector3 p0, CVector3 p1) {
		if (p0.x > -1000 && p1.x > -1000) {
			bGraphics.setColor(Color.yellow);
			bGraphics.drawLine((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
			bGraphics.drawLine((int) p0.x + 1, (int) p0.y, (int) p1.x + 1, (int) p1.y);
			bGraphics.drawLine((int) p0.x, (int) p0.y + 1, (int) p1.x, (int) p1.y + 1);
			bGraphics.drawLine((int) p0.x + 1, (int) p0.y + 1, (int) p1.x + 1, (int) p1.y + 1);
		}
	}

	public void drawMline(CVector3 p0, CVector3 p1) {
		if (p0.x > -1000 && p1.x > -1000) {
			bGraphics.setColor(Color.lightGray);
			bGraphics.drawLine((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
		}
	}

	public void drawAline(CVector3 p0, CVector3 p1) {
		if (p0.x > -1000 && p1.x > -1000) {
			bGraphics.setColor(Color.white);
			bGraphics.drawLine((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
			bGraphics.drawLine((int) p0.x + 1, (int) p0.y, (int) p1.x + 1, (int) p1.y);
			bGraphics.drawLine((int) p0.x, (int) p0.y + 1, (int) p1.x, (int) p1.y + 1);
			bGraphics.drawLine((int) p0.x + 1, (int) p0.y + 1, (int) p1.x + 1, (int) p1.y + 1);
		}
	}

	public void drawPoly(CVector3 p0, CVector3 p1, CVector3 p2) {
		drawSline(p0, p1);
		drawSline(p1, p2);
		drawSline(p2, p0);
	}

	public void fillBarc(CVector3 p) {
		if (p.x >= -100) {

			int rr = (int) (2000 / p.z) + 2;
			if (rr > 40)
				rr = 40;
			bGraphics.setColor(Color.orange);
			bGraphics.fillArc((int) p.x, (int) p.y, rr, rr, 0, 360);
		}
	}

	private void jbInit() throws Exception {
		this.setBackground(Color.black);
		this.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				this_keyPressed(e);
			}

			public void keyReleased(KeyEvent e) {
				this_keyReleased(e);
			}
		});
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				this_componentResized(e);
			}
		});
		this.setForeground(Color.white);
	}

	void this_componentResized(ComponentEvent e) {
		sWidth = getSize().width;
		sHeight = getSize().height;
		sCenterX = (int) (sWidth / 2);
		sCenterY = (int) (sHeight / 2);
		bgInit();
	}

	void this_keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			keyShoot = true;
			break;
		case KeyEvent.VK_LEFT:
			keyLeft = true;
			break;
		case KeyEvent.VK_RIGHT:
			keyRight = true;
			break;
		case KeyEvent.VK_UP:
			keyUp = true;
			break;
		case KeyEvent.VK_DOWN:
			keyDown = true;
			break;
		case KeyEvent.VK_B:
			keyBoost = true;
			break;
		}
	}

	void this_keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			keyShoot = false;
			break;
		case KeyEvent.VK_LEFT:
			keyLeft = false;
			break;
		case KeyEvent.VK_RIGHT:
			keyRight = false;
			break;
		case KeyEvent.VK_UP:
			keyUp = false;
			break;
		case KeyEvent.VK_DOWN:
			keyDown = false;
			break;
		case KeyEvent.VK_B:
			keyBoost = false;
			break;
		}
	}
}
