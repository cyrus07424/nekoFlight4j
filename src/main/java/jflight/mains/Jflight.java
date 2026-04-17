package jflight.mains;

import java.awt.Graphics;
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

	protected Thread mainThread = null;
	protected volatile boolean running = false;
	public Plane[] plane;
	protected boolean autoFlight = true;
	static protected CVector3[][] obj;

	protected CVector3[][] pos;

	public Jflight() {

		plane = new Plane[Commons.PMAX];
		for (int i = 0; i < Commons.PMAX; i++)
			plane[i] = new Plane();

		pos = new CVector3[Commons.GSCALE][Commons.GSCALE];
		for (int j = 0; j < Commons.GSCALE; j++)
			for (int i = 0; i < Commons.GSCALE; i++)
				pos[j][i] = new CVector3();

		objInit();

		plane[0].no = 0;
		plane[1].no = 1;
		plane[2].no = 2;
		plane[3].no = 3;
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

	public void paint(Graphics g) {
		super.paint(g);
	}

	public void draw() {

		clear();

		plane[0].checkTrans();

		writeGround();

		writePlane();

		flush();

		getToolkit().sync();
	}

	public void run() {
		while (running) {

			if (keyShoot)
				autoFlight = false;

			plane[0].move(this, autoFlight);
			for (int i = 1; i < Commons.PMAX; i++)
				plane[i].move(this, true);

			camerapos.set(plane[0].pVel);
			draw();

			try {
				Thread.sleep(10);
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
			if (plane[i].use) {

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

						drawPoly(s0, s1, s2);
					}
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

				if (cp.z < 400) {

					dm.x = bp.pVel.x + bp.vVel.x * 0.005;
					dm.y = bp.pVel.y + bp.vVel.y * 0.005;
					dm.z = bp.pVel.z + bp.vVel.z * 0.005;
					change3d(plane[0], dm, cp);
					dm.x = bp.pVel.x + bp.vVel.x * 0.04;
					dm.y = bp.pVel.y + bp.vVel.y * 0.04;
					dm.z = bp.pVel.z + bp.vVel.z * 0.04;
					change3d(plane[0], dm, dm2);
					drawBline(cp, dm2);
				}

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
					drawMline(dm, cp);
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

		double mx, my;
		int i, j;
		CVector3 p = new CVector3();

		double step = Commons.FMAX * 2 / Commons.GSCALE;

		int dx = (int) (plane[0].pVel.x / step);
		int dy = (int) (plane[0].pVel.y / step);
		double sx = dx * step;
		double sy = dy * step;

		my = -Commons.FMAX;
		for (j = 0; j < Commons.GSCALE; j++) {
			mx = -Commons.FMAX;
			for (i = 0; i < Commons.GSCALE; i++) {
				p.x = mx + sx;
				p.y = my + sy;
				p.z = gHeight(mx + sx, my + sy);
				change3d(plane[0], p, pos[j][i]);
				mx += step;
			}
			my += step;
		}

		for (j = 0; j < Commons.GSCALE; j++)
			for (i = 0; i < Commons.GSCALE - 1; i++)
				drawSline(pos[j][i], pos[j][i + 1]);
		for (i = 0; i < Commons.GSCALE; i++)
			for (j = 0; j < Commons.GSCALE - 1; j++)
				drawSline(pos[j][i], pos[j + 1][i]);
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
		JFrame frame = new JFrame("NekoFlight for Java");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setContentPane(app);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				app.stop();
			}

			public void windowClosed(WindowEvent e) {
				app.stop();
			}
		});
		frame.setVisible(true);
		app.requestFocusInWindow();
		app.start();
	}

}
