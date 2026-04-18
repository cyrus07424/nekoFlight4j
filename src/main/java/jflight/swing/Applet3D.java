package jflight.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import jflight.constants.Configurations;
import jflight.actors.Plane;
import jflight.constants.Commons;
import jflight.constants.Colors;
import jflight.utils.CVector3;

public class Applet3D extends JPanel {

	public int sWidth, sHeight;
	public int sCenterX, sCenterY;

	public CVector3 camerapos;
	public BufferedImage backImage = null;
	int bWidth, bHeight;
	protected Graphics2D bGraphics = null;
	protected final Object renderLock = new Object();

	public boolean keyShoot;
	public boolean keyLeft;
	public boolean keyRight;
	public boolean keyUp;
	public boolean keyDown;
	public boolean keyBoost;
	public boolean keyRudderLeft;
	public boolean keyRudderRight;

	protected boolean inputShoot;
	protected boolean inputLeft;
	protected boolean inputRight;
	protected boolean inputUp;
	protected boolean inputDown;
	protected boolean inputBoost;
	protected boolean inputRudderLeft;
	protected boolean inputRudderRight;
	protected boolean inputReset;
	protected boolean inputToggleMenu;
	protected boolean inputToggleChrome;
	protected boolean inputToggleAuto;

	public Applet3D() {
		camerapos = new CVector3();
		sWidth = 960;
		sHeight = 540;
		sCenterX = sWidth / 2;
		sCenterY = sHeight / 2;
		setPreferredSize(new Dimension(sWidth, sHeight));
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		bgInit();
	}

	public void bgInit() {
		synchronized (renderLock) {
			backImage = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_RGB);
			bWidth = sWidth;
			bHeight = sHeight;
			if (bGraphics != null)
				bGraphics.dispose();
			bGraphics = backImage.createGraphics();
			bGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			bGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
	}

	public void clear() {
		synchronized (renderLock) {
			if (backImage == null || bWidth != sWidth || bHeight != sHeight)
				bgInit();
			if (bGraphics != null) {
				bGraphics.setColor(Colors.BLACK);
				bGraphics.fillRect(0, 0, bWidth, bHeight);
			}
		}
	}

	public void flush() {
		synchronized (renderLock) {
			if (backImage == null || bWidth != sWidth || bHeight != sHeight)
				bgInit();
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		synchronized (renderLock) {
			if (backImage == null || bWidth != sWidth || bHeight != sHeight)
				bgInit();
			g.drawImage(backImage, 0, 0, null);
		}
	}

	public void change3d(Plane plane, CVector3 sp, CVector3 cp) {
		double x = sp.x - camerapos.x;
		double y = sp.y - camerapos.y;
		double z = sp.z - camerapos.z;

		double x1 = x * plane.y00 + y * plane.y01 + z * plane.y02;
		double y1 = x * plane.y10 + y * plane.y11 + z * plane.y12;
		double z1 = x * plane.y20 + y * plane.y21 + z * plane.y22;

		if (y1 > 10) {
			double perspective = Configurations.CAMERA_SCALE / (y1 / 10.0);
			cp.x = x1 * perspective + sCenterX;
			cp.y = -z1 * perspective + sCenterY;
			cp.z = y1 * 10;
		} else {
			cp.x = -10000;
			cp.y = -10000;
			cp.z = 1;
		}
	}

	public void drawSline(CVector3 p0, CVector3 p1) {
		drawSline(p0, p1, Colors.WHITE);
	}

	public void drawSline(CVector3 p0, CVector3 p1, Color color) {
		if (bGraphics != null && p0.x > -10000 && p0.x < 30000 && p0.y > -10000 && p0.y < 30000 &&
				p1.x > -10000 && p1.x < 30000 && p1.y > -10000 && p1.y < 30000) {
			bGraphics.setColor(color);
			bGraphics.drawLine((int) p0.x, (int) p0.y, (int) p1.x, (int) p1.y);
		}
	}

	public void drawBlined(CVector3 p0, CVector3 p1) {
		if (p0.x > -1000 && p1.x > -1000)
			drawSline(p0, p1, Colors.YELLOW);
	}

	public void drawBline(CVector3 p0, CVector3 p1) {
		if (p0.x > -1000 && p1.x > -1000) {
			drawSline(p0, p1, Colors.YELLOW);
			drawSline(new CVector3(p0.x + 1, p0.y, 0), new CVector3(p1.x + 1, p1.y, 0), Colors.YELLOW);
		}
	}

	public void drawMline(CVector3 p0, CVector3 p1) {
		drawMline(p0, p1, Colors.LIGHT_GRAY);
	}

	public void drawMline(CVector3 p0, CVector3 p1, Color color) {
		if (p0.x > -1000 && p1.x > -1000)
			drawSline(p0, p1, color);
	}

	public void drawAline(CVector3 p0, CVector3 p1) {
		if (p0.x > -1000 && p1.x > -1000) {
			drawSline(p0, p1, Colors.WHITE);
			drawSline(new CVector3(p0.x + 1, p0.y, 0), new CVector3(p1.x + 1, p1.y, 0), Colors.WHITE);
		}
	}

	public void drawPoly(CVector3 p0, CVector3 p1, CVector3 p2) {
		drawPoly(p0, p1, p2, Colors.WHITE);
	}

	public void drawPoly(CVector3 p0, CVector3 p1, CVector3 p2, Color color) {
		drawSline(p0, p1, color);
		drawSline(p1, p2, color);
		drawSline(p2, p0, color);
	}

	public void fillBarc(CVector3 p) {
		if (p.x >= -100 && bGraphics != null) {
			int rr = (int) (2000 / p.z) + 2;
			if (rr > 40)
				rr = 40;
			bGraphics.setColor(Colors.ORANGE);
			bGraphics.fillOval((int) p.x - rr / 2, (int) p.y - rr / 2, rr, rr);
		}
	}

	private void jbInit() throws Exception {
		setBackground(Colors.BLACK);
		addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				this_keyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				this_keyReleased(e);
			}
		});
		addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				this_componentResized(e);
			}
		});
		setForeground(Colors.WHITE);
	}

	void this_componentResized(ComponentEvent e) {
		sWidth = getSize().width;
		sHeight = getSize().height;
		sCenterX = sWidth / 2;
		sCenterY = sHeight / 2;
		synchronized (renderLock) {
			bgInit();
		}
	}

	void this_keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			inputShoot = true;
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			inputLeft = true;
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			inputRight = true;
			break;
		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			inputUp = true;
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			inputDown = true;
			break;
		case KeyEvent.VK_SHIFT:
			inputBoost = true;
			break;
		case KeyEvent.VK_Q:
			inputRudderLeft = true;
			break;
		case KeyEvent.VK_E:
			inputRudderRight = true;
			break;
		case KeyEvent.VK_R:
			inputReset = true;
			break;
		case KeyEvent.VK_TAB:
			inputToggleMenu = true;
			e.consume();
			break;
		case KeyEvent.VK_H:
			inputToggleChrome = true;
			break;
		case KeyEvent.VK_ENTER:
			inputToggleAuto = true;
			break;
		}
	}

	void this_keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			inputShoot = false;
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			inputLeft = false;
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			inputRight = false;
			break;
		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			inputUp = false;
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			inputDown = false;
			break;
		case KeyEvent.VK_SHIFT:
			inputBoost = false;
			break;
		case KeyEvent.VK_Q:
			inputRudderLeft = false;
			break;
		case KeyEvent.VK_E:
			inputRudderRight = false;
			break;
		case KeyEvent.VK_R:
			inputReset = false;
			break;
		case KeyEvent.VK_TAB:
			inputToggleMenu = false;
			e.consume();
			break;
		case KeyEvent.VK_H:
			inputToggleChrome = false;
			break;
		case KeyEvent.VK_ENTER:
			inputToggleAuto = false;
			break;
		}
	}
}
