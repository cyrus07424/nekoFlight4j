package jflight.mains;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jflight.actors.Bullet;
import jflight.actors.Missile;
import jflight.actors.Plane;
import jflight.constants.Commons;
import jflight.swing.Applet3D;
import jflight.utils.CVector3;

public class Jflight extends Applet3D implements Runnable {

	private static final Color COLOR_GROUND = new Color(0, 96, 0);
	private static final Color COLOR_ENEMY = new Color(128, 128, 0);
	private static final Color COLOR_DARK_GREY = new Color(80, 80, 80);
	private static final Color COLOR_PANEL_SHADOW = new Color(48, 48, 48);
	private static final Color COLOR_CYAN = new Color(80, 220, 255);
	private static final Color COLOR_GREEN_YELLOW = new Color(192, 255, 96);
	private static final Color COLOR_ORANGE = new Color(255, 176, 48);
	private static final Color COLOR_OLIVE = new Color(128, 128, 0);
	private static final String[] UI_MENU_LABELS = {
			"Attitude HUD", "Reticle", "Lock Box", "Enemy Arrows", "Speed Tape",
			"Altitude Tape", "TGT Panel", "Header", "Mode Banner", "Footer"
	};

	protected Thread mainThread = null;
	protected volatile boolean running = false;
	protected volatile boolean needsRedraw = true;
	public Plane[] plane;
	protected boolean autoFlight = true;
	static protected CVector3[][] obj;

	protected CVector3[][] pos;
	protected boolean chromeVisible = true;
	protected boolean menuVisible = false;
	protected boolean uiAttitudeVisible = true;
	protected boolean uiReticleVisible = true;
	protected boolean uiLockBoxVisible = true;
	protected boolean uiEnemyArrowsVisible = true;
	protected boolean uiSpeedVisible = true;
	protected boolean uiAltitudeVisible = true;
	protected boolean uiTargetVisible = true;
	protected boolean uiHeaderVisible = true;
	protected boolean uiModeBannerVisible = true;
	protected boolean uiFooterVisible = true;
	protected int menuIndex = 0;
	protected int menuScroll = 0;

	private boolean prevToggleMenu = false;
	private boolean prevToggleAuto = false;
	private boolean prevReset = false;
	private boolean prevToggleChrome = false;
	private boolean prevMenuUp = false;
	private boolean prevMenuDown = false;

	public Jflight() {
		plane = new Plane[Commons.PMAX];
		for (int i = 0; i < Commons.PMAX; i++)
			plane[i] = new Plane();

		pos = new CVector3[Commons.GSCALE][Commons.GSCALE];
		for (int j = 0; j < Commons.GSCALE; j++)
			for (int i = 0; i < Commons.GSCALE; i++)
				pos[j][i] = new CVector3();

		objInit();
		resetStage();
	}

	public void init() {
	}

	public void start() {
		if (mainThread == null) {
			running = true;
			mainThread = new Thread(this, "Jflight-Main");
			mainThread.start();
		}
	}

	public void stop() {
		running = false;
		Thread thread = mainThread;
		mainThread = null;
		if (thread != null) {
			thread.interrupt();
			if (Thread.currentThread() != thread) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	private void resetStage() {
		for (int i = 0; i < Commons.PMAX; i++) {
			plane[i].posInit();
			plane[i].no = i;
		}

		plane[0].target = 2;
		plane[1].target = 2;
		plane[2].target = 1;
		plane[3].target = 1;
		plane[0].use = true;
		plane[1].use = true;
		plane[2].use = true;
		plane[3].use = true;
		plane[0].level = 20;
		plane[1].level = 10;
		plane[2].level = 20;
		plane[3].level = 30;

		autoFlight = true;
		keyShoot = false;
		keyLeft = false;
		keyRight = false;
		keyUp = false;
		keyDown = false;
		keyBoost = false;
		keyRudderLeft = false;
		keyRudderRight = false;
		camerapos.set(plane[0].pVel);
		needsRedraw = true;
	}

	public void resetStagePreserveUi() {
		boolean savedChromeVisible = chromeVisible;
		boolean savedMenuVisible = menuVisible;
		boolean savedUiAttitudeVisible = uiAttitudeVisible;
		boolean savedUiReticleVisible = uiReticleVisible;
		boolean savedUiLockBoxVisible = uiLockBoxVisible;
		boolean savedUiEnemyArrowsVisible = uiEnemyArrowsVisible;
		boolean savedUiSpeedVisible = uiSpeedVisible;
		boolean savedUiAltitudeVisible = uiAltitudeVisible;
		boolean savedUiTargetVisible = uiTargetVisible;
		boolean savedUiHeaderVisible = uiHeaderVisible;
		boolean savedUiModeBannerVisible = uiModeBannerVisible;
		boolean savedUiFooterVisible = uiFooterVisible;
		int savedMenuIndex = menuIndex;
		int savedMenuScroll = menuScroll;

		resetStage();

		chromeVisible = savedChromeVisible;
		menuVisible = savedMenuVisible;
		uiAttitudeVisible = savedUiAttitudeVisible;
		uiReticleVisible = savedUiReticleVisible;
		uiLockBoxVisible = savedUiLockBoxVisible;
		uiEnemyArrowsVisible = savedUiEnemyArrowsVisible;
		uiSpeedVisible = savedUiSpeedVisible;
		uiAltitudeVisible = savedUiAltitudeVisible;
		uiTargetVisible = savedUiTargetVisible;
		uiHeaderVisible = savedUiHeaderVisible;
		uiModeBannerVisible = savedUiModeBannerVisible;
		uiFooterVisible = savedUiFooterVisible;
		menuIndex = savedMenuIndex;
		menuScroll = savedMenuScroll;
		needsRedraw = true;
	}

	protected void objInit() {
		if (obj != null)
			return;

		obj = new CVector3[20][3];
		for (int j = 0; j < 20; j++)
			for (int i = 0; i < 3; i++)
				obj[j][i] = new CVector3();

		obj[0][0].set(-0.000000, -2.000000, 0.000000);
		obj[0][1].set(0.000000, 4.000000, 0.000000);
		obj[0][2].set(6.000000, -2.000000, 0.000000);

		obj[1][0].set(0.000000, -3.000000, 1.500000);
		obj[1][1].set(2.000000, -3.000000, 0.000000);
		obj[1][2].set(0.000000, 8.000000, 0.000000);

		obj[2][0].set(2.000000, 0.000000, 0.000000);
		obj[2][1].set(3.000000, 0.000000, -0.500000);
		obj[2][2].set(3.500000, 0.000000, 0.000000);

		obj[3][0].set(3.000000, 0.000000, 0.000000);
		obj[3][1].set(3.000000, -1.000000, -1.500000);
		obj[3][2].set(3.000000, 0.000000, -2.000000);

		obj[4][0].set(3.000000, -1.000000, -2.000000);
		obj[4][1].set(3.000000, 2.000000, -2.000000);
		obj[4][2].set(3.500000, 1.000000, -2.500000);

		obj[5][0].set(1.000000, 0.000000, -6.000000);
		obj[5][1].set(2.000000, 4.000000, -6.000000);
		obj[5][2].set(2.000000, -2.000000, 0.000000);

		obj[6][0].set(3.000000, 0.000000, -6.000000);
		obj[6][1].set(2.000000, 4.000000, -6.000000);
		obj[6][2].set(2.000000, -2.000000, 0.000000);

		obj[7][0].set(2.000000, 1.000000, 0.000000);
		obj[7][1].set(2.000000, -3.000000, 4.000000);
		obj[7][2].set(2.000000, -3.000000, -2.000000);

		obj[8][0].set(1.000000, 0.000000, 0.000000);
		obj[8][1].set(0.000000, 0.000000, -1.000000);
		obj[8][2].set(0.000000, 1.000000, 0.000000);

		obj[9][0].set(0.000000, -2.000000, 0.000000);
		obj[9][1].set(0.000000, 4.000000, 0.000000);
		obj[9][2].set(-6.000000, -2.000000, 0.000000);

		obj[10][0].set(0.000000, -3.000000, 1.500000);
		obj[10][1].set(-2.000000, -3.000000, 0.000000);
		obj[10][2].set(0.000000, 8.000000, 0.000000);

		obj[11][0].set(-2.000000, 0.000000, 0.000000);
		obj[11][1].set(-3.000000, 0.000000, -0.500000);
		obj[11][2].set(-3.500000, 0.000000, 0.000000);

		obj[12][0].set(-3.000000, 0.000000, 0.000000);
		obj[12][1].set(-3.000000, -1.000000, -1.500000);
		obj[12][2].set(-3.000000, 0.000000, -2.000000);

		obj[13][0].set(-3.000000, -1.000000, -2.000000);
		obj[13][1].set(-3.000000, 2.000000, -2.000000);
		obj[13][2].set(-3.500000, 1.000000, -2.500000);

		obj[14][0].set(-1.000000, 0.000000, -6.000000);
		obj[14][1].set(-2.000000, 4.000000, -6.000000);
		obj[14][2].set(-2.000000, -2.000000, 0.000000);

		obj[15][0].set(-3.000000, 0.000000, -6.000000);
		obj[15][1].set(-2.000000, 4.000000, -6.000000);
		obj[15][2].set(-2.000000, -2.000000, 0.000000);

		obj[16][0].set(-2.000000, 1.000000, 0.000000);
		obj[16][1].set(-2.000000, -3.000000, 4.000000);
		obj[16][2].set(-2.000000, -3.000000, -2.000000);

		obj[17][0].set(-1.000000, 0.000000, 0.000000);
		obj[17][1].set(0.000000, 0.000000, -1.000000);
		obj[17][2].set(0.000000, 1.000000, 0.000000);

		obj[18][0].set(3.000000, 0.000000, -2.000000);
		obj[18][1].set(3.000000, 0.000000, -1.500000);
		obj[18][2].set(3.000000, 7.000000, -2.000000);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}

	private double hudScale() {
		return Math.max(1.0, Math.min(sWidth / 240.0, sHeight / 135.0));
	}

	private void updateControls() {
		boolean up = inputUp;
		boolean down = inputDown;
		boolean left = inputLeft;
		boolean right = inputRight;
		boolean rudderLeft = inputRudderLeft;
		boolean rudderRight = inputRudderRight;
		boolean shoot = inputShoot;
		boolean boost = inputBoost;

		if (inputToggleMenu && !prevToggleMenu) {
			menuVisible = !menuVisible;
			needsRedraw = true;
		}
		prevToggleMenu = inputToggleMenu;

		boolean menuActive = menuVisible;
		keyUp = menuActive ? false : up;
		keyDown = menuActive ? false : down;
		keyLeft = menuActive ? false : left;
		keyRight = menuActive ? false : right;
		keyRudderLeft = menuActive ? false : rudderLeft;
		keyRudderRight = menuActive ? false : rudderRight;
		keyShoot = menuActive ? false : shoot;
		keyBoost = menuActive ? false : boost;

		if (menuActive) {
			if (up && !prevMenuUp) {
				menuIndex = (menuIndex + UI_MENU_LABELS.length - 1) % UI_MENU_LABELS.length;
				adjustMenuScroll();
				needsRedraw = true;
			}
			if (down && !prevMenuDown) {
				menuIndex = (menuIndex + 1) % UI_MENU_LABELS.length;
				adjustMenuScroll();
				needsRedraw = true;
			}
		} else if (inputReset && !prevReset) {
			resetStagePreserveUi();
		}
		prevReset = inputReset;
		prevMenuUp = up;
		prevMenuDown = down;

		if (inputToggleChrome && !prevToggleChrome) {
			chromeVisible = !chromeVisible;
			needsRedraw = true;
		}
		prevToggleChrome = inputToggleChrome;

		if (inputToggleAuto && !prevToggleAuto) {
			if (menuActive)
				toggleUiMenuValue(menuIndex);
			else
				autoFlight = !autoFlight;
			needsRedraw = true;
		}
		prevToggleAuto = inputToggleAuto;

		if (keyShoot || keyLeft || keyRight || keyUp || keyDown || keyRudderLeft || keyRudderRight || keyBoost)
			autoFlight = false;
	}

	private void adjustMenuScroll() {
		int visibleRows = getVisibleMenuRows();
		if (UI_MENU_LABELS.length <= visibleRows) {
			menuScroll = 0;
			return;
		}
		if (menuIndex < menuScroll)
			menuScroll = menuIndex;
		else if (menuIndex >= menuScroll + visibleRows)
			menuScroll = menuIndex - visibleRows + 1;
	}

	private double getPopupScale() {
		return Math.min(hudScale(), 1.25);
	}

	private int getVisibleMenuRows() {
		double popupScale = getPopupScale();
		int y = (int) Math.round(10 * popupScale);
		int rowStartOffset = (int) Math.round(28 * popupScale);
		int rowStep = (int) Math.round(12 * popupScale);
		int bottomPad = (int) Math.round(14 * popupScale);
		int availableHeight = sHeight - y - bottomPad;
		int availableRowSpace = availableHeight - rowStartOffset;
		if (availableRowSpace <= 0)
			return 1;
		return Math.max(1, Math.min(UI_MENU_LABELS.length, availableRowSpace / rowStep + 1));
	}

	private void toggleUiMenuValue(int index) {
		switch (index) {
		case 0:
			uiAttitudeVisible = !uiAttitudeVisible;
			break;
		case 1:
			uiReticleVisible = !uiReticleVisible;
			break;
		case 2:
			uiLockBoxVisible = !uiLockBoxVisible;
			break;
		case 3:
			uiEnemyArrowsVisible = !uiEnemyArrowsVisible;
			break;
		case 4:
			uiSpeedVisible = !uiSpeedVisible;
			break;
		case 5:
			uiAltitudeVisible = !uiAltitudeVisible;
			break;
		case 6:
			uiTargetVisible = !uiTargetVisible;
			break;
		case 7:
			uiHeaderVisible = !uiHeaderVisible;
			break;
		case 8:
			uiModeBannerVisible = !uiModeBannerVisible;
			break;
		case 9:
			uiFooterVisible = !uiFooterVisible;
			break;
		default:
			break;
		}
	}

	private boolean uiMenuValue(int index) {
		switch (index) {
		case 0:
			return uiAttitudeVisible;
		case 1:
			return uiReticleVisible;
		case 2:
			return uiLockBoxVisible;
		case 3:
			return uiEnemyArrowsVisible;
		case 4:
			return uiSpeedVisible;
		case 5:
			return uiAltitudeVisible;
		case 6:
			return uiTargetVisible;
		case 7:
			return uiHeaderVisible;
		case 8:
			return uiModeBannerVisible;
		case 9:
			return uiFooterVisible;
		default:
			return false;
		}
	}

	private void updateWorld() {
		plane[0].move(this, autoFlight);
		for (int i = 1; i < Commons.PMAX; i++)
			plane[i].move(this, true);
		camerapos.set(plane[0].pVel);
		needsRedraw = true;
	}

	public void draw() {
		synchronized (renderLock) {
			clear();

			plane[0].checkTrans();
			writeGround();
			writePlane();

			if (uiAttitudeVisible)
				drawAttitudeHud(plane[0]);
			if (uiEnemyArrowsVisible)
				drawEnemyDirectionArrows();
			if (uiReticleVisible || uiLockBoxVisible)
				drawReticle(plane[0]);

			drawHud();
			drawPopupMenu();

			flush();
			getToolkit().sync();
			needsRedraw = false;
		}
	}

	public void run() {
		long lastFrameMs = System.currentTimeMillis();
		while (running) {
			updateControls();

			long now = System.currentTimeMillis();
			if (now - lastFrameMs >= Commons.FRAME_INTERVAL_MS) {
				updateWorld();
				draw();
				lastFrameMs = now;
			} else if (needsRedraw) {
				draw();
			}

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		running = false;
		mainThread = null;
	}

	protected void writePlane() {
		CVector3 p0 = new CVector3();
		CVector3 p1 = new CVector3();
		CVector3 p2 = new CVector3();
		CVector3 s0 = new CVector3();
		CVector3 s1 = new CVector3();
		CVector3 s2 = new CVector3();

		for (int i = 0; i < Commons.PMAX; i++) {
			if (!plane[i].use)
				continue;

			writeGun(plane[i]);
			writeAam(plane[i]);

			plane[0].checkTransM(plane[i].aVel);
			if (i != 0) {
				for (int j = 0; j < 19; j++) {
					plane[0].change_ml2w(obj[j][0], p0);
					plane[0].change_ml2w(obj[j][1], p1);
					plane[0].change_ml2w(obj[j][2], p2);
					p0.add(plane[i].pVel);
					p1.add(plane[i].pVel);
					p2.add(plane[i].pVel);

					change3d(plane[0], p0, s0);
					change3d(plane[0], p1, s1);
					change3d(plane[0], p2, s2);

					drawPoly(s0, s1, s2, COLOR_ENEMY);
				}
			}
		}
	}

	protected void writeGun(Plane aplane) {
		CVector3 dm = new CVector3();
		CVector3 dm2 = new CVector3();
		CVector3 cp = new CVector3();

		for (int j = 0; j < Plane.BMAX; j++) {
			Bullet bp = aplane.bullet[j];

			if (bp.use > 0) {
				dm.x = bp.pVel.x + bp.vVel.x * 0.005;
				dm.y = bp.pVel.y + bp.vVel.y * 0.005;
				dm.z = bp.pVel.z + bp.vVel.z * 0.005;
				change3d(plane[0], dm, cp);
				dm.x = bp.pVel.x + bp.vVel.x * 0.04;
				dm.y = bp.pVel.y + bp.vVel.y * 0.04;
				dm.z = bp.pVel.z + bp.vVel.z * 0.04;
				change3d(plane[0], dm, dm2);
				drawBline(cp, dm2);

				change3d(plane[0], bp.pVel, cp);
				dm.x = bp.pVel.x + bp.vVel.x * 0.05;
				dm.y = bp.pVel.y + bp.vVel.y * 0.05;
				dm.z = bp.pVel.z + bp.vVel.z * 0.05;
				change3d(plane[0], dm, dm2);
				drawBlined(cp, dm2);
			}

			if (bp.bom > 0) {
				change3d(plane[0], bp.opVel, cp);
				fillBarc(cp);
				bp.bom--;
			}
		}
	}

	private Color grayColor(int level) {
		int clamped = Math.max(0, Math.min(255, level));
		return new Color(clamped, clamped, clamped);
	}

	protected void writeAam(Plane aplane) {
		CVector3 dm = new CVector3();
		CVector3 cp = new CVector3();
		for (int j = 0; j < Plane.MMMAX; j++) {
			Missile ap = aplane.aam[j];

			if (ap.use >= 0) {
				if (ap.bom <= 0) {
					dm.x = ap.pVel.x + ap.aVel.x * 4;
					dm.y = ap.pVel.y + ap.aVel.y * 4;
					dm.z = ap.pVel.z + ap.aVel.z * 4;
					change3d(plane[0], dm, cp);
					change3d(plane[0], ap.pVel, dm);
					drawAline(cp, dm);
				}

				int k = (ap.use + Missile.MOMAX + 1) % Missile.MOMAX;
				change3d(plane[0], ap.opVel[k], dm);
				for (int m = 0; m < ap.count; m++) {
					change3d(plane[0], ap.opVel[k], cp);
					int gray = ap.count > 1 ? 255 - (m * 175 / (ap.count - 1)) : 255;
					drawMline(dm, cp, grayColor(gray));
					k = (k + Missile.MOMAX + 1) % Missile.MOMAX;
					dm.set(cp);
				}
			}

			if (ap.bom > 0) {
				change3d(plane[0], ap.pVel, cp);
				fillBarc(cp);
			}
		}
	}

	protected void writeGround() {
		double step = Commons.FMAX * 2 / Commons.GSCALE;

		int dx = (int) (plane[0].pVel.x / step);
		int dy = (int) (plane[0].pVel.y / step);
		double sx = dx * step;
		double sy = dy * step;

		double my = -Commons.FMAX;
		CVector3 p = new CVector3();
		for (int j = 0; j < Commons.GSCALE; j++) {
			double mx = -Commons.FMAX;
			for (int i = 0; i < Commons.GSCALE; i++) {
				p.x = mx + sx;
				p.y = my + sy;
				p.z = gHeight(mx + sx, my + sy);
				change3d(plane[0], p, pos[j][i]);
				mx += step;
			}
			my += step;
		}

		for (int j = 0; j < Commons.GSCALE; j++)
			for (int i = 0; i < Commons.GSCALE - 1; i++)
				drawSline(pos[j][i], pos[j][i + 1], COLOR_GROUND);
		for (int i = 0; i < Commons.GSCALE; i++)
			for (int j = 0; j < Commons.GSCALE - 1; j++)
				drawSline(pos[j][i], pos[j + 1][i], COLOR_GROUND);
	}

	private void drawHudPanel(int x, int y, int w, int h, Color color) {
		if (bGraphics == null)
			return;
		bGraphics.setColor(color);
		bGraphics.drawRoundRect(x, y, w, h, 8, 8);
		bGraphics.setColor(COLOR_PANEL_SHADOW);
		bGraphics.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 8, 8);
	}

	private void drawModeBanner() {
		if (bGraphics == null)
			return;
		double scale = hudScale();
		String modeText = autoFlight ? "AUTO" : "MANUAL";
		FontMetrics metrics = bGraphics.getFontMetrics();
		int textX = sWidth / 2 - 20;
		int textBaselineY = (int) Math.round(4 * scale) + (int) Math.round(14 * scale) - 4;
		int padX = (int) Math.max(5, Math.round(6 * scale));
		int padY = (int) Math.max(3, Math.round(3 * scale));
		int bannerW = metrics.stringWidth(modeText) + padX * 2;
		int bannerH = metrics.getAscent() + metrics.getDescent() + padY * 2;
		int bannerX = textX - padX;
		int bannerY = textBaselineY - metrics.getAscent() - padY;
		Color modeColor = autoFlight ? Color.yellow : COLOR_CYAN;
		drawHudPanel(bannerX, bannerY, bannerW, bannerH, modeColor);
		bGraphics.setColor(modeColor);
		bGraphics.drawString(modeText, textX, textBaselineY);
	}

	private void drawPopupMenu() {
		if (!menuVisible || bGraphics == null)
			return;

		double popupScale = getPopupScale();
		int x = (int) Math.round(8 * popupScale);
		int y = (int) Math.round(10 * popupScale);
		int w = (int) Math.round(156 * popupScale);
		int rowStep = (int) Math.round(12 * popupScale);
		int visibleRows = getVisibleMenuRows();
		int visibleStart = UI_MENU_LABELS.length <= visibleRows ? 0 : menuScroll;
		int visibleEnd = Math.min(UI_MENU_LABELS.length, visibleStart + visibleRows);
		int renderedRows = visibleEnd - visibleStart;
		int h = (int) Math.round(34 * popupScale) + Math.max(0, renderedRows - 1) * rowStep
				+ (int) Math.round(14 * popupScale);
		int headerX = x + (int) Math.round(6 * popupScale);
		int headerY = y + (int) Math.round(14 * popupScale);
		int rowStartY = y + (int) Math.round(28 * popupScale);
		int labelX = x + (int) Math.round(6 * popupScale);
		int valueX = x + w - (int) Math.round(44 * popupScale);
		int selectionX = x + (int) Math.round(3 * popupScale);
		int selectionYPad = (int) Math.round(9 * popupScale);
		int selectionH = (int) Math.round(11 * popupScale);
		int maxY = sHeight - h - (int) Math.round(8 * popupScale);
		if (y > maxY)
			y = Math.max(0, maxY);
		headerY = y + (int) Math.round(14 * popupScale);
		rowStartY = y + (int) Math.round(28 * popupScale);
		bGraphics.setColor(Color.black);
		bGraphics.fillRoundRect(x, y, w, h, 10, 10);
		bGraphics.setColor(COLOR_DARK_GREY);
		bGraphics.drawRoundRect(x, y, w, h, 10, 10);
		bGraphics.setColor(COLOR_ORANGE);
		bGraphics.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 10, 10);
		bGraphics.drawString("UI MENU", headerX, headerY);

		for (int i = visibleStart; i < visibleEnd; i++) {
			int rowY = rowStartY + (i - visibleStart) * rowStep;
			boolean selected = i == menuIndex;
			if (selected) {
				bGraphics.setColor(COLOR_ORANGE);
				bGraphics.fillRect(selectionX, rowY - selectionYPad, w - (int) Math.round(10 * popupScale), selectionH);
			}
			bGraphics.setColor(selected ? Color.black : Color.white);
			bGraphics.drawString(UI_MENU_LABELS[i], labelX, rowY);
			String value = uiMenuValue(i) ? "ON" : "OFF";
			bGraphics.drawString(value, valueX, rowY);
		}
	}

	private void drawHudTape(int centerX, int centerY, int value, int step, int majorStep, int range,
			boolean leftSide, String label, Color color) {
		if (bGraphics == null)
			return;
		double scale = hudScale();
		int tapeH = (int) Math.round(58 * scale);
		int topY = centerY - tapeH / 2;
		String valueText = String.format("%4d", value);
		FontMetrics metrics = bGraphics.getFontMetrics();
		int padX = (int) Math.max(3, Math.round(3 * scale));
		int padY = (int) Math.max(2, Math.round(2 * scale));
		int boxW = metrics.stringWidth("0000") + padX * 2;
		int boxH = metrics.getAscent() + metrics.getDescent() + padY * 2;
		int tickOuterX = leftSide ? centerX + (int) Math.round(10 * scale) : centerX - (int) Math.round(10 * scale);
		int tickInnerX = leftSide ? tickOuterX - (int) Math.round(8 * scale) : tickOuterX + (int) Math.round(8 * scale);
		int boxGap = (int) Math.max(3, Math.round(4 * scale));
		int boxX = leftSide ? tickInnerX - boxGap - boxW : tickInnerX + boxGap;
		int pixelsPerStep = (int) Math.round(8 * scale);
		int textBaseY = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
		int textX = boxX + boxW - padX - metrics.stringWidth(valueText);

		bGraphics.setColor(color);
		bGraphics.drawString(label, boxX + 2, topY - 6);
		bGraphics.drawRect(boxX, centerY - boxH / 2, boxW, boxH);
		bGraphics.drawString(valueText, textX, textBaseY);

		int firstTick = (int) Math.floor((double) (value - range) / step) * step;
		int lastTick = (int) Math.ceil((double) (value + range) / step) * step;
		for (int tickValue = firstTick; tickValue <= lastTick; tickValue += step) {
			double offsetSteps = (double) (tickValue - value) / step;
			int y = centerY + (int) Math.round(offsetSteps * pixelsPerStep);
			if (y < topY || y > topY + tapeH)
				continue;

			boolean major = tickValue % majorStep == 0;
			int tickLen = major ? (int) Math.round(10 * scale) : (int) Math.round(4 * scale);
			int x1 = leftSide ? tickOuterX - tickLen : tickOuterX + tickLen;
			bGraphics.drawLine(tickOuterX, y, x1, y);
		}

		bGraphics.drawLine(leftSide ? boxX + boxW : boxX, centerY, tickInnerX, centerY);
		bGraphics.drawLine(tickInnerX, centerY, tickInnerX, centerY - (int) Math.round(10 * scale));
		bGraphics.drawLine(tickInnerX, centerY, tickInnerX, centerY + (int) Math.round(10 * scale));
	}

	private int rotateX(double x, double y, double angle) {
		return (int) Math.round(x * Math.cos(angle) - y * Math.sin(angle));
	}

	private int rotateY(double x, double y, double angle) {
		return (int) Math.round(x * Math.sin(angle) + y * Math.cos(angle));
	}

	private void drawPitchLadder(int cx, int cy, double pitchDeg, double rollDeg) {
		if (bGraphics == null)
			return;
		double scale = hudScale();
		double rollRad = Math.toRadians(rollDeg);
		double pixelsPerDeg = 1.5 * scale;

		for (int mark = -90; mark <= 90; mark += 10) {
			double yOffset = (pitchDeg - mark) * pixelsPerDeg;
			if (Math.abs(yOffset) > 36 * scale)
				continue;

			boolean horizon = mark == 0;
			int halfWidth = (int) Math.round((horizon ? 20 : 12) * scale);
			int gap = (int) Math.round((horizon ? 0 : 4) * scale);
			bGraphics.setColor(horizon ? COLOR_GREEN_YELLOW : COLOR_DARK_GREY);

			int rx0 = rotateX(-halfWidth, yOffset, rollRad);
			int ry0 = rotateY(-halfWidth, yOffset, rollRad);
			int rx1 = rotateX(-gap, yOffset, rollRad);
			int ry1 = rotateY(-gap, yOffset, rollRad);
			bGraphics.drawLine(cx + rx0, cy + ry0, cx + rx1, cy + ry1);

			rx0 = rotateX(gap, yOffset, rollRad);
			ry0 = rotateY(gap, yOffset, rollRad);
			rx1 = rotateX(halfWidth, yOffset, rollRad);
			ry1 = rotateY(halfWidth, yOffset, rollRad);
			bGraphics.drawLine(cx + rx0, cy + ry0, cx + rx1, cy + ry1);

			if (!horizon) {
				int tx = rotateX(-halfWidth - 14 * scale, yOffset - 2 * scale, rollRad);
				int ty = rotateY(-halfWidth - 14 * scale, yOffset - 2 * scale, rollRad);
				bGraphics.drawString(Integer.toString(-mark), cx + tx, cy + ty);
			}
		}
	}

	private void drawAttitudeHud(Plane player) {
		int cx = sCenterX;
		int cy = sCenterY;
		double pitchDeg = Math.toDegrees(player.aVel.x);
		double rollDeg = Math.toDegrees(player.aVel.y);
		drawPitchLadder(cx, cy, pitchDeg, rollDeg);
	}

	private void drawEnemyDirectionArrows() {
		if (bGraphics == null)
			return;
		Plane player = plane[0];
		double radius = 44.0 * hudScale();
		double arrowScale = hudScale() * Commons.HUD_ENEMY_ARROW_SCALE;
		CVector3 screen = new CVector3();
		CVector3 rel = new CVector3();
		CVector3 local = new CVector3();

		for (int i = 1; i < Commons.PMAX; i++) {
			if (!plane[i].use)
				continue;

			change3d(player, plane[i].pVel, screen);
			rel.setMinus(plane[i].pVel, player.pVel);
			player.change_w2l(rel, local);

			if (screen.x >= 0 && screen.x < sWidth && screen.y >= 0 && screen.y < sHeight)
				continue;

			double dirX = local.x;
			double dirY = -local.z;
			if (local.y < 0) {
				dirX = -dirX;
				dirY = -dirY;
			}

			double len = Math.sqrt(dirX * dirX + dirY * dirY);
			if (len < 1)
				continue;
			dirX /= len;
			dirY /= len;

			int tipX = (int) Math.round(sCenterX + dirX * radius);
			int tipY = (int) Math.round(sCenterY + dirY * radius);
			int baseX = (int) Math.round(sCenterX + dirX * (radius - 8 * arrowScale));
			int baseY = (int) Math.round(sCenterY + dirY * (radius - 8 * arrowScale));
			int leftX = (int) Math.round(baseX - dirY * 4 * arrowScale);
			int leftY = (int) Math.round(baseY + dirX * 4 * arrowScale);
			int rightX = (int) Math.round(baseX + dirY * 4 * arrowScale);
			int rightY = (int) Math.round(baseY - dirX * 4 * arrowScale);

			bGraphics.setColor(COLOR_ORANGE);
			bGraphics.fillPolygon(new Polygon(new int[] { tipX, leftX, rightX }, new int[] { tipY, leftY, rightY }, 3));
		}
	}

	private Rectangle getProjectedTargetBounds(Plane player) {
		if (player.gunTarget < 0 || player.gunTarget >= Commons.PMAX || !plane[player.gunTarget].use)
			return null;

		Plane targetPlane = plane[player.gunTarget];
		CVector3 p = new CVector3();
		CVector3 screen = new CVector3();
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		boolean found = false;

		player.checkTransM(targetPlane.aVel);
		for (int j = 0; j < 19; j++) {
			for (int k = 0; k < 3; k++) {
				player.change_ml2w(obj[j][k], p);
				p.add(targetPlane.pVel);
				change3d(player, p, screen);
				if (screen.x <= -1000 || screen.y <= -1000 || screen.x >= 30000 || screen.y >= 30000)
					continue;
				int sx = (int) Math.round(screen.x);
				int sy = (int) Math.round(screen.y);
				minX = Math.min(minX, sx);
				minY = Math.min(minY, sy);
				maxX = Math.max(maxX, sx);
				maxY = Math.max(maxY, sy);
				found = true;
			}
		}

		if (!found)
			return null;

		int padding = (int) Math.max(3, Math.round(3 * hudScale()));
		return new Rectangle(minX - padding, minY - padding,
				Math.max(8, maxX - minX + padding * 2),
				Math.max(8, maxY - minY + padding * 2));
	}

	private void drawReticle(Plane player) {
		if (bGraphics == null)
			return;
		double scale = hudScale();
		double centerCrossScale = scale * Commons.HUD_CENTER_CROSS_SCALE;
		double reticleScale = scale * Commons.HUD_RETICLE_SCALE;
		if (uiReticleVisible) {
			int reticleRadius = (int) Math.round(8 * reticleScale);
			int centerCrossRadius = (int) Math.round(3 * centerCrossScale);
			int centerCrossArm = (int) Math.round(6 * centerCrossScale);
			int reticleArm = (int) Math.round(14 * reticleScale);

			bGraphics.setColor(COLOR_DARK_GREY);
			bGraphics.drawLine(sCenterX - centerCrossArm, sCenterY, sCenterX + centerCrossArm, sCenterY);
			bGraphics.drawLine(sCenterX, sCenterY - centerCrossArm, sCenterX, sCenterY + centerCrossArm);

			int gunX = sCenterX + (int) Math.round(player.gunX * 0.36 * scale);
			int gunY = sCenterY - (int) Math.round((player.gunY - 20.0) * 0.36 * scale);
			Color reticleColor = autoFlight ? Color.yellow : COLOR_CYAN;
			bGraphics.setColor(reticleColor);
			bGraphics.drawOval(gunX - reticleRadius, gunY - reticleRadius, reticleRadius * 2, reticleRadius * 2);
			bGraphics.drawLine(gunX - reticleArm, gunY, gunX + reticleArm, gunY);
			bGraphics.drawLine(gunX, gunY - reticleArm, gunX, gunY + reticleArm);
			bGraphics.drawOval(gunX - 2, gunY - 2, 4, 4);
		}

		if (uiLockBoxVisible && player.targetSx > -1000) {
			bGraphics.setColor(Color.red);
			Rectangle targetBounds = getProjectedTargetBounds(player);
			if (targetBounds != null)
				bGraphics.drawRect(targetBounds.x, targetBounds.y, targetBounds.width, targetBounds.height);
		}
	}

	private void drawCenteredText(String text, int centerX, int baselineY) {
		FontMetrics metrics = bGraphics.getFontMetrics();
		bGraphics.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
	}

	private void drawHud() {
		if (bGraphics == null)
			return;
		Plane player = plane[0];
		double scale = hudScale();

		Stroke oldStroke = bGraphics.getStroke();
		bGraphics.setStroke(new BasicStroke((float) Math.max(1.0, scale * 0.7)));

		if (chromeVisible && uiModeBannerVisible)
			drawModeBanner();

		if (chromeVisible && uiHeaderVisible) {
			bGraphics.setColor(Color.green);
			bGraphics.drawString("NekoFlight4j", (int) Math.round(4 * scale), (int) Math.round(8 * scale));
			String right = "PC HUD";
			FontMetrics metrics = bGraphics.getFontMetrics();
			bGraphics.drawString(right, sWidth - metrics.stringWidth(right) - (int) Math.round(4 * scale),
					(int) Math.round(8 * scale));
		}

		if (uiSpeedVisible)
			drawHudTape(sCenterX - (int) Math.round(26 * scale), sCenterY,
					(int) Math.round(player.vpVel.abs()), 10, 50, 40, true, "SPD", COLOR_CYAN);
		if (uiAltitudeVisible)
			drawHudTape(sCenterX + (int) Math.round(26 * scale), sCenterY,
					(int) Math.round(player.height), 100, 500, 400, false, "ALT", COLOR_GREEN_YELLOW);

		if (uiTargetVisible && player.targetDis > 0.0) {
			FontMetrics metrics = bGraphics.getFontMetrics();
			String targetText = Integer.toString((int) Math.round(player.targetDis));
			int padX = (int) Math.max(4, Math.round(4 * scale));
			int topPad = (int) Math.max(3, Math.round(3 * scale));
			int bottomPad = (int) Math.max(3, Math.round(3 * scale));
			int lineGap = (int) Math.max(0, Math.round(1 * scale));
			int textW = Math.max(metrics.stringWidth("TGT"), metrics.stringWidth(targetText));
			int panelW = textW + padX * 2;
			int panelH = metrics.getAscent() * 2 + topPad + bottomPad + lineGap;
			int panelX = sWidth - panelW - (int) Math.round(16 * scale);
			int panelY = (int) Math.round(32 * scale);
			int textX = panelX + padX;
			int labelBaseY = panelY + topPad + metrics.getAscent();
			int valueBaseY = labelBaseY + metrics.getAscent() + lineGap;
			drawHudPanel(panelX, panelY, panelW, panelH, COLOR_ORANGE);
			bGraphics.setColor(COLOR_ORANGE);
			bGraphics.drawString("TGT", textX, labelBaseY);
			bGraphics.drawString(targetText, textX, valueBaseY);
		}

		if (chromeVisible && uiFooterVisible) {
			int footerY = sHeight - (int) Math.round(22 * scale);
			bGraphics.setColor(player.gunTemp > Plane.MAXT * 3 / 4 ? COLOR_ORANGE : Color.white);

			String line1 = "Move W/S A/D  Rudder Q/E  Fire Space  Boost Shift";
			String line2 = String.format("Reset R  Auto/Select Enter  Menu Tab  HUD H  Gun:%02d", player.gunTemp);
			if (sWidth >= 880) {
				drawCenteredText(line1 + "   " + line2, sCenterX, footerY + (int) Math.round(14 * scale));
			} else {
				drawCenteredText(line1, sCenterX, footerY);
				drawCenteredText(line2, sCenterX, footerY + (int) Math.round(14 * scale));
			}
		}

		bGraphics.setStroke(oldStroke);
	}

	public double gHeight(double px, double py) {
		return 0;
	}

	public void gGrad(double px, double py, CVector3 p) {
		p.x = 0;
		p.y = 0;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowUi();
			}
		});
	}

	private static void createAndShowUi() {
		final Jflight app = new Jflight();
		JFrame frame = new JFrame("NekoFlight4j");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setContentPane(app);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				app.stop();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				app.stop();
			}
		});
		frame.setVisible(true);
		app.requestFocusInWindow();
		app.start();
	}
}
