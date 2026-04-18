package jflight.actors;

import jflight.constants.Commons;
import jflight.mains.Jflight;
import jflight.utils.CVector3;

//
// Plane
// 機体クラス
//
// 各弾丸やミサイルを動かしているのもこのクラス
//

public class Plane {

	// 定数

	final static public int BMAX = 20; // 弾丸の最大数
	final static public int MMMAX = 4; // ミサイルの最大数
	final static public int WMAX = 6; // 翼の数
	final static public int MAXT = 50; // 機銃の最大温度

	// 変数

	// ワールド座標→機体座標への変換行列

	protected double cosa, cosb, cosc, sina, sinb, sinc;
	public double y00;
	public double y01;
	public double y02;
	public double y10, y11, y12;
	public double y20, y21, y22;

	// ワールド座標→機体座標への変換行列（ワーク用）

	public double mcosa, mcosb, mcosc, msina, msinb, msinc;
	public double my00, my01, my02;
	public double my10, my11, my12;
	public double my20, my21, my22;

	// 機体

	public boolean use; // この機体を使用するか
	public int no; // 機体No.
	public Wing[] wing; // 各翼(0,1-主翼,2-水平尾翼,3-垂直尾翼,4,5-エンジン)
	public CVector3 pVel; // 機体位置（ワールド座標系）
	public CVector3 vpVel; // 機体速度（ワールド座標系）
	public CVector3 vVel; // 機体速度（機体座標系）
	public CVector3 gVel; // 機体加速度（ワールド座標系）
	public CVector3 aVel; // 機体向き（オイラー角）
	public CVector3 vaVel; // 機体回転速度（オイラー角）
	public CVector3 gcVel; // 弾丸の将来予想位置
	public double height; // 機体の高度
	public double gHeight; // 機体直下の地面の高さ
	public double mass; // 機体質量
	public CVector3 iMass; // 機体各軸の慣性モーメント
	public boolean onGround; // 地面上にいるかどうか
	public double aoa; // 機体の迎角

	// 操縦系

	public CVector3 stickPos; // 操縦系位置（x,y-スティック,z-ペダル）
	public CVector3 stickVel; // 操縦系変化率
	public double stickR, stickA; // 操縦系の感度（R-センターへの減衰率,A-変化率）
	public int power; // エンジン推力比率（ミリタリー時で9）
	public int throttle; // スロットル位置（ミリタリー時で9）
	public boolean boost; // ブースト
	public boolean gunShoot; // 機銃トリガー
	public boolean aamShoot; // ミサイルトリガー
	public int level, target; // 自動操縦時のレベルと目標

	// 機銃系

	public Bullet[] bullet; // 各弾丸オブジェクト
	public int gunTarget; // 主目標の機体No.
	public int targetSx, targetSy; // 主目標の位置（スクリーン座標）
	public double targetDis; // 主目標までの距離
	public double gunTime; // 弾丸衝突予想時間（機銃の追尾に使用）
	public double gunX, gunY; // 機銃の向き（上下左右稼働範囲で-1～1）
	public double gunVx, gunVy; // 機銃向きの変化率
	public int gunTemp; // 機銃の温度（0からMAXTまで）
	public boolean heatWait; // 自動操縦時の機銃オーバーヒート解除待ち

	// ミサイル系

	public Missile[] aam; // 各ミサイルオブジェクト
	public int[] aamTarget; // 各ミサイルのロック目標

	// コンストラクタ

	public Plane() {
		pVel = new CVector3();
		vpVel = new CVector3();
		gVel = new CVector3();
		aVel = new CVector3();
		vaVel = new CVector3();
		vVel = new CVector3();
		gcVel = new CVector3();
		iMass = new CVector3();
		stickPos = new CVector3();
		stickVel = new CVector3();

		bullet = new Bullet[BMAX];
		for (int i = 0; i < BMAX; i++)
			bullet[i] = new Bullet();

		aam = new Missile[MMMAX];
		for (int i = 0; i < MMMAX; i++)
			aam[i] = new Missile();

		wing = new Wing[WMAX];
		for (int i = 0; i < WMAX; i++)
			wing[i] = new Wing();

		aamTarget = new int[MMMAX];

		posInit();
	}

	// 各変数を初期化する

	public void posInit() {
		int i, j;

		pVel.x = (Math.random() - 0.5) * 1000 - 8000;
		pVel.y = (Math.random() - 0.5) * 1000 - 1100;
		pVel.z = 5000;
		gHeight = 0;
		height = 5000;
		vpVel.x = 200.0;
		aVel.set(0, 0, Math.PI / 2);
		vpVel.y = 0.0;
		vpVel.z = 0.0;
		gVel.set(0, 0, 0);
		vaVel.set(0, 0, 0);
		vVel.set(0, 0, 0);
		power = 5;
		throttle = 5;
		heatWait = false;
		gunTemp = 0;
		gcVel.set(pVel);
		target = -2;
		onGround = false;
		gunX = 0;
		gunY = 100;
		gunVx = 0;
		gunVy = 0;
		boost = false;
		aoa = 0;
		stickPos.set(0, 0, 0);
		stickVel.set(0, 0, 0);
		stickR = 0.1;
		stickA = 0.1;
		gunTarget = -1;
		targetSx = -1000;
		targetSy = 0;
		targetDis = 0;
		gunTime = 1.0;

		double wa = 45 * Math.PI / 180;
		double wa2 = 0 * Math.PI / 180;

		// 各翼の位置と向きをセット

		// 左翼
		wing[0].pVel.set(3, 0.1, 0);
		wing[0].xVel.set(Math.cos(wa), -Math.sin(wa), Math.sin(wa2));
		wing[0].yVel.set(Math.sin(wa), Math.cos(wa), 0);
		wing[0].zVel.set(0, 0, 1);

		// 右翼
		wing[1].pVel.set(-3, 0.1, 0);
		wing[1].xVel.set(Math.cos(wa), Math.sin(wa), -Math.sin(wa2));
		wing[1].yVel.set(-Math.sin(wa), Math.cos(wa), 0);
		wing[1].zVel.set(0, 0, 1);

		// 水平尾翼
		wing[2].pVel.set(0, -10, 2);
		wing[2].xVel.set(1, 0, 0);
		wing[2].yVel.set(0, 1, 0);
		wing[2].zVel.set(0, 0, 1);

		// 垂直尾翼
		wing[3].pVel.set(0, -10, 0);
		wing[3].xVel.set(0, 0, 1);
		wing[3].yVel.set(0, 1, 0);
		wing[3].zVel.set(1, 0, 0);

		// 右エンジン
		wing[4].pVel.set(5, 0, 0);
		wing[4].xVel.set(1, 0, 0);
		wing[4].yVel.set(0, 1, 0);
		wing[4].zVel.set(0, 0, 1);

		// 左エンジン
		wing[5].pVel.set(-5, 0, 0);
		wing[5].xVel.set(1, 0, 0);
		wing[5].yVel.set(0, 1, 0);
		wing[5].zVel.set(0, 0, 1);

		// 各翼の質量をセット

		wing[0].mass = 400 / 2;
		wing[1].mass = 400 / 2;
		wing[2].mass = 50;
		wing[3].mass = 50;
		wing[4].mass = 300;
		wing[5].mass = 300;

		// 各翼の面積をセット

		wing[0].sVal = 60 / 2;
		wing[1].sVal = 60 / 2;
		wing[2].sVal = 2;
		wing[3].sVal = 2;
		wing[4].sVal = 0;
		wing[5].sVal = 0;

		// エンジンの推力をセット

		wing[0].tVal = 0.1;
		wing[1].tVal = 0.1;
		wing[2].tVal = 0.1;
		wing[3].tVal = 0.1;
		wing[4].tVal = 1000;
		wing[5].tVal = 1000;

		// 総質量と慣性モーメントを求めておく

		mass = 0;
		iMass.set(1000, 1000, 4000);
		double m_i = 1;
		for (i = 0; i < WMAX; i++) {
			mass += wing[i].mass;
			wing[i].aAngle = 0;
			wing[i].bAngle = 0;
			wing[i].vVel.set(0, 0, 1);
			iMass.x += wing[i].mass * (Math.abs(wing[i].pVel.x) + 1) * m_i * m_i;
			iMass.y += wing[i].mass * (Math.abs(wing[i].pVel.y) + 1) * m_i * m_i;
			iMass.z += wing[i].mass * (Math.abs(wing[i].pVel.z) + 1) * m_i * m_i;
		}

		for (i = 0; i < BMAX; i++) {
			bullet[i].use = 0;
			bullet[i].bom = 0;
		}
		for (i = 0; i < MMMAX; i++) {
			aam[i].use = -1;
			aam[i].bom = 0;
			aam[i].count = 0;
			aamTarget[i] = -1;
		}
	}

	// 機体のローカル座標→ワールド座標変換行列を求める

	public void checkTrans() {

		double sasc, sacc, x;

		x = aVel.x;

		sina = Math.sin(x);
		cosa = Math.cos(x);
		if (cosa < 1e-9 && cosa > 0)
			cosa = 1e-9;
		if (cosa > -1e-9 && cosa < 0)
			cosa = -1e-9;
		sinb = Math.sin(aVel.y);
		cosb = Math.cos(aVel.y);
		sinc = Math.sin(aVel.z);
		cosc = Math.cos(aVel.z);
		sasc = sina * sinc;
		sacc = sina * cosc;

		y00 = cosb * cosc - sasc * sinb;
		y01 = -cosb * sinc - sacc * sinb;
		y02 = -sinb * cosa;
		y10 = cosa * sinc;
		y11 = cosa * cosc;
		y12 = -sina;
		y20 = sinb * cosc + sasc * cosb;
		y21 = -sinb * sinc + sacc * cosb;
		y22 = cosb * cosa;
	}

	// 機体のローカル座標→ワールド座標変換行列（ワーク用）を求める
	// 自機のオイラー座標ではなく、指定されたオイラー座標を用いる

	public void checkTransM(CVector3 p) {

		double mcosa, msina, mcosb, msinb, mcosc, msinc;
		double msasc, msacc;

		msina = Math.sin(p.x);
		mcosa = Math.cos(p.x);
		if (mcosa < 1e-9 && mcosa > 0)
			mcosa = 1e-9;
		if (mcosa > -1e-9 && mcosa < 0)
			mcosa = -1e-9;
		msinb = Math.sin(p.y);
		mcosb = Math.cos(p.y);
		msinc = Math.sin(p.z);
		mcosc = Math.cos(p.z);
		msasc = msina * msinc;
		msacc = msina * mcosc;

		my00 = mcosb * mcosc - msasc * msinb;
		my01 = -mcosb * msinc - msacc * msinb;
		my02 = -msinb * mcosa;
		my10 = mcosa * msinc;
		my11 = mcosa * mcosc;
		my12 = -msina;
		my20 = msinb * mcosc + msasc * mcosb;
		my21 = -msinb * msinc + msacc * mcosb;
		my22 = mcosb * mcosa;
	}

	// ワールド座標を機体座標へ変換する（１次変換のみ）

	public void change_w2l(CVector3 pw, CVector3 pl) {

		pl.x = pw.x * y00 + pw.y * y01 + pw.z * y02;
		pl.y = pw.x * y10 + pw.y * y11 + pw.z * y12;
		pl.z = pw.x * y20 + pw.y * y21 + pw.z * y22;
	}

	// 機体座標をワールド座標へ変換する（１次変換のみ）

	public void change_l2w(CVector3 pl, CVector3 pw) {

		pw.x = pl.x * y00 + pl.y * y10 + pl.z * y20;
		pw.y = pl.x * y01 + pl.y * y11 + pl.z * y21;
		pw.z = pl.x * y02 + pl.y * y12 + pl.z * y22;
	}

	// ワーク用行列を用いてワールド座標を機体座標へ変換する（１次変換のみ）

	public void change_mw2l(CVector3 pw, CVector3 pl) {

		pl.x = pw.x * my00 + pw.y * my01 + pw.z * my02;
		pl.y = pw.x * my10 + pw.y * my11 + pw.z * my12;
		pl.z = pw.x * my20 + pw.y * my21 + pw.z * my22;
	}

	// ワーク用行列を用いて機体座標をワールド座標へ変換する （１次変換のみ）

	public void change_ml2w(CVector3 pl, CVector3 pw) {

		pw.x = pl.x * my00 + pl.y * my10 + pl.z * my20;
		pw.y = pl.x * my01 + pl.y * my11 + pl.z * my21;
		pw.z = pl.x * my02 + pl.y * my12 + pl.z * my22;
	}

	// 機銃やミサイルのロック処理

	public void lockCheck(Jflight world) {
		CVector3 a = new CVector3();
		CVector3 b = new CVector3();
		int nno[] = new int[MMMAX]; // 機体No.
		double dis[] = new double[MMMAX]; // 機体と自機との距離

		for (int m = 0; m < MMMAX; m++) {
			dis[m] = 1e30;
			nno[m] = -1;
		}

		for (int m = 0; m < Commons.PMAX; m++) {

			// 目標が存在していればロックリストに追加
			if (m != no && world.plane[m].use) {

				// 目標との距離を求める
				a.setMinus(pVel, world.plane[m].pVel);
				double near_dis = a.abs2();

				if (near_dis < 1e8) {

					// 目標との位置関係を機体座標系に変換
					change_w2l(a, b);

					// 武器サークル内ならロック
					if (b.y <= 0 && Math.sqrt(b.x * b.x + b.z * b.z) < -b.y * 0.24) {

						// 既にロックされているのなら、他のロックと近い順に置き換える
						for (int m1 = 0; m1 < MMMAX; m1++) {
							if (near_dis < dis[m1]) {
								for (int m2 = MMMAX - 1; m2 > m1; m2--) {
									dis[m2] = dis[m2 - 1];
									nno[m2] = nno[m2 - 1];
								}
								dis[m1] = near_dis;
								nno[m1] = m;
								break;
							}
						}
					}
				}
			}
		}

		// ロック目標が見つからない場合、一番近い目標にロック

		for (int m1 = 1; m1 < 4; m1++)
			if (nno[m1] < 0) {
				nno[m1] = nno[0];
				dis[m1] = dis[0];
			}

		// ４以降のミサイルは、同一ポッドのミサイルに合わせる

		for (int m1 = 4; m1 < MMMAX; m1++) {
			nno[m1] = nno[m1 % 4];
			dis[m1] = dis[m1 % 4];
		}

		for (int m1 = 0; m1 < MMMAX; m1++)
			aamTarget[m1] = nno[m1];

		// 機銃の目標（主目標）は、最も近い敵機にセット
		gunTarget = nno[0];
		targetDis = dis[0] < 1e20 ? Math.sqrt(dis[0]) : 0.0;
	}

	// 機体を動かす
	// 自機の弾丸なども移動

	public void move(Jflight world, boolean autof) {

		checkTrans(); // 座標変換用の行列再計算
		lockCheck(world); // ミサイルロック処理

		if (no == 0 && !autof) // 手動操縦
			keyScan(world);
		else
			autoFlight(world); // 自動操縦

		moveCalc(world);
		moveBullet(world);
		moveAam(world);
	}

	// キー状態をもとに、スティックやトリガーをセット
	// 実際のキースキャンを処理しているのは、Applet3Dクラス

	protected void keyScan(Jflight world) {
		stickVel.set(0, 0, 0);
		boost = false;
		gunShoot = world.keyShoot;
		aamShoot = world.keyShoot;

		if (world.keyBoost)
			boost = true;

		// スティックを急激に動かすとまずいので、
		// スティック自身に慣性を持たせて滑らかに動かしている。

		if (world.keyUp)
			stickVel.x = 1;
		if (world.keyDown)
			stickVel.x = -1;
		if (world.keyLeft)
			stickVel.y = -1;
		if (world.keyRight)
			stickVel.y = 1;
		if (world.keyRudderLeft)
			stickVel.z = -1;
		if (world.keyRudderRight)
			stickVel.z = 1;

		stickPos.addCons(stickVel, stickA);
		stickPos.subCons(stickPos, stickR);

		if (stickPos.z > 1)
			stickPos.z = 1;
		if (stickPos.z < -1)
			stickPos.z = -1;

		// スティック位置を距離１以内に丸めておく

		double r = Math.sqrt(stickPos.x * stickPos.x + stickPos.y * stickPos.y);
		if (r > 1) {
			stickPos.x /= r;
			stickPos.y /= r;
		}
	}

	// 機体計算

	public void moveCalc(Jflight world) {
		int i;
		long cx, cy;
		double ve;
		CVector3 dm = new CVector3();

		// 主目標の見かけの位置を求めておく（機銃の追尾で用いる）

		targetSx = -1000;
		targetSy = 0;
		if (gunTarget >= 0 && world.plane[gunTarget].use) {

			// 主目標の座標をスクリーン座標に変換
			world.change3d(this, world.plane[gunTarget].pVel, dm);

			// スクリーン内なら
			if (dm.x > 0 && dm.x < world.sWidth && dm.y > 0 && dm.y < world.sHeight) {
				targetSx = (int) dm.x;
				targetSy = (int) dm.y;
			}
		}

		// 自機の位置から、地面の高さを求め、高度を求める

		gHeight = world.gHeight(pVel.x, pVel.y);
		height = pVel.z - gHeight;

		// 空気密度の計算

		if (pVel.z < 5000)
			ve = 0.12492 - 0.000008 * pVel.z;
		else
			ve = (0.12492 - 0.04) - 0.000002 * (pVel.z - 5000);
		if (ve < 0)
			ve = 0;

		// 各翼を操縦系に合わせてひねっておく

		wing[0].aAngle = -stickPos.y * 1.5 / 180 * Math.PI;
		wing[0].bAngle = 0;
		wing[1].aAngle = stickPos.y * 1.5 / 180 * Math.PI;
		wing[1].bAngle = 0;
		wing[2].aAngle = -stickPos.x * 6 / 180 * Math.PI;
		wing[2].bAngle = 0;
		wing[3].aAngle = stickPos.z * Commons.RUDDER_DEFLECTION_DEG / 180 * Math.PI;
		wing[3].bAngle = 0;
		wing[4].aAngle = 0;
		wing[4].bAngle = 0;
		wing[5].aAngle = 0;
		wing[5].bAngle = 0;

		change_w2l(vpVel, vVel);
		onGround = false;

		if (height < 5)
			onGround = true;

		// af→機体にかかる力
		// am→機体にかかるモーメント

		CVector3 af = new CVector3();
		CVector3 am = new CVector3();
		af.set(0, 0, 0);
		am.set(0, 0, 0);

		// 各翼に働く力とモーメントを計算

		aoa = 0;
		for (int m = 0; m < WMAX; m++) {
			wing[m].calc(this, ve, m, boost);

			// 力
			af.x += (wing[m].fVel.x * y00 + wing[m].fVel.y * y10 + wing[m].fVel.z * y20);
			af.y += (wing[m].fVel.x * y01 + wing[m].fVel.y * y11 + wing[m].fVel.z * y21);
			af.z += (wing[m].fVel.x * y02 + wing[m].fVel.y * y12 + wing[m].fVel.z * y22) + wing[m].mass * Commons.G;

			// モーメント（力と翼位置との外積）
			am.x -= (wing[m].pVel.y * wing[m].fVel.z - wing[m].pVel.z * wing[m].fVel.y);
			am.y -= (wing[m].pVel.z * wing[m].fVel.x - wing[m].pVel.x * wing[m].fVel.z);
			am.z -= (wing[m].pVel.x * wing[m].fVel.y - wing[m].pVel.y * wing[m].fVel.x);
		}

		// 角度変化を積分

		vaVel.x += am.x / iMass.x * Commons.DT;
		vaVel.y += am.y / iMass.y * Commons.DT;
		vaVel.z += am.z / iMass.z * Commons.DT;

		aVel.x += (vaVel.x * cosb + vaVel.z * sinb) * Commons.DT;
		aVel.y += (vaVel.y + (vaVel.x * sinb - vaVel.z * cosb) * sina / cosa) * Commons.DT;
		aVel.z += (-vaVel.x * sinb + vaVel.z * cosb) / cosa * Commons.DT;

		// 機体の角度を一定範囲に丸めておく

		int q;
		for (q = 0; q < 3 && aVel.x >= Math.PI / 2; q++) {
			aVel.x = Math.PI - aVel.x;
			aVel.y += Math.PI;
			aVel.z += Math.PI;
		}
		for (q = 0; q < 3 && aVel.x < -Math.PI / 2; q++) {
			aVel.x = -Math.PI - aVel.x;
			aVel.y += Math.PI;
			aVel.z += Math.PI;
		}

		for (q = 0; q < 3 && aVel.y >= Math.PI; q++)
			aVel.y -= Math.PI * 2;
		for (q = 0; q < 3 && aVel.y < -Math.PI; q++)
			aVel.y += Math.PI * 2;
		for (q = 0; q < 3 && aVel.z >= Math.PI * 2; q++)
			aVel.z -= Math.PI * 2;
		for (q = 0; q < 3 && aVel.z < 0; q++)
			aVel.z += Math.PI * 2;

		// 加速度を決定

		gVel.setConsInv(af, mass);

		// 機体で発生する抵抗を擬似的に生成

		vpVel.x -= vpVel.x * vpVel.x * 0.00002;
		vpVel.y -= vpVel.y * vpVel.y * 0.00002;
		vpVel.z -= vpVel.z * vpVel.z * 0.00002;

		// 地面の傾き処理

		world.gGrad(pVel.x, pVel.y, dm);
		if (onGround) {
			gVel.x -= dm.x * 10;
			gVel.y -= dm.y * 10;
			double vz = dm.x * vpVel.x + dm.y * vpVel.y;
			if (vpVel.z < vz)
				;
			vpVel.z = vz;
		}

		// ブースト時には、機体を振動させる

		if (boost) {
			gVel.x += (Math.random() - 0.5) * 5;
			gVel.y += (Math.random() - 0.5) * 5;
			gVel.z += (Math.random() - 0.5) * 5;
		}

		// 機体の位置を積分して求める

		vpVel.addCons(gVel, Commons.DT);
		pVel.addCons(vpVel, Commons.DT);

		// 念のため、地面にめり込んだかどうかチェック
		if (height < 2) {
			pVel.z = gHeight + 2;
			height = 2;
			vpVel.z *= -0.1;
		}

		// 地面にある程度以上の速度か、無理な体勢で接触した場合、機体を初期化
		if (height < 5
				&& (Math.abs(vpVel.z) > 50 || Math.abs(aVel.y) > 20 * Math.PI / 180 || aVel.x > 10 * Math.PI / 180)) {
			world.resetStagePreserveUi();
		}
	}

	// 自動操縦

	public void autoFlight(Jflight world) {
		double a, m, mm;
		int c;

		gunShoot = false;
		aamShoot = false;

		if (target < 0 || !world.plane[target].use)
			return;

		power = 4;
		throttle = power;
		stickPos.z = 0;

		if (level < 0)
			level = 0;

		CVector3 dm_p = new CVector3();
		CVector3 dm_a = new CVector3();

		// 目標と自機の位置関係を求め、機体座標に変換しておく
		dm_p.setMinus(pVel, world.plane[target].pVel);
		change_w2l(dm_p, dm_a);

		// mmは、スティックの移動限界量

		if (level >= 20)
			mm = 1;
		else
			mm = (level + 1) * 0.05;

		stickVel.x = 0;
		stickVel.y = 0;
		m = Math.sqrt(dm_a.x * dm_a.x + dm_a.z * dm_a.z);

		// スロットルの位置は、目標にあわせる

		if (level > 8 && gunTime < 1)
			power = world.plane[target].power;
		else
			power = 9;

		// 目標が上方に見える場合、スティックを引く
		if (dm_a.z < 0)
			stickVel.x = dm_a.z / m * mm;

		// 目標の左右見かけ位置に合わせて、スティックを左右に動かす
		stickVel.y = -dm_a.x / m * mm * 0.4;

		if (stickVel.y > 1)
			stickVel.y = 1;
		if (stickVel.y < -1)
			stickVel.y = -1;

		// スティックの慣性処理
		stickPos.x += stickVel.x;
		stickPos.y += stickVel.y;

		if (stickPos.x > 1)
			stickPos.x = 1;
		if (stickPos.x < -1)
			stickPos.x = -1;
		if (stickPos.y > 1)
			stickPos.y = 1;
		if (stickPos.y < -1)
			stickPos.y = -1;

		// 機体高度が低いか、8秒以内に地面にぶつかりそうな場合、空に向ける
		if (height < 1000 || height + vpVel.z * 8 < 0) {
			stickPos.y = -aVel.y;
			if (Math.abs(aVel.y) < Math.PI / 2)
				stickPos.x = -1;
			else
				stickPos.x = 0;
		}

		// スティック位置を１以内に丸めておく

		m = Math.sqrt(stickPos.x * stickPos.x + stickPos.y * stickPos.y);
		if (m > mm) {
			stickPos.x *= mm / m;
			stickPos.y *= mm / m;
		}

		// 主目標として選ばれているのなら、機銃を撃つ
		if (gunTarget == target && gunTime < 1) {
			// 機銃がオーバーヒートしている場合、温度が下がるまで待つ
			if (!heatWait && gunTemp < MAXT - 1)
				gunShoot = true;
			else
				heatWait = true;
		}

		if (gunTemp < 2)
			heatWait = false;

		// 主目標として選ばれているのなら、ミサイルを撃つ

		if (gunTarget == target)
			aamShoot = true;

		// 失速しそうな場合、スティックを離す

		if (Math.abs(aoa) > 0.35)
			stickPos.x = 0;
	}

	// 機銃の弾丸移動と発射処理

	public void moveBullet(Jflight world) {
		int i;
		double v, l, aa;
		long cx, cy;

		CVector3 sc = new CVector3();
		CVector3 a = new CVector3();
		CVector3 b = new CVector3();
		CVector3 c = new CVector3();
		CVector3 dm = new CVector3();
		CVector3 oi = new CVector3();
		CVector3 ni = new CVector3();

		// 弾丸の初期速度を求めておく
		dm.set(gunX * 400 / 200, 400, gunY * 400 / 200);
		change_l2w(dm, oi);
		oi.add(vpVel);
		gunTime = 1.0;

		// 弾丸の初期位置を求めておく
		dm.set(4 * 2, 10.0, 4 * -0.5);
		change_l2w(dm, ni);

		// 弾丸の到達予想時間を求めておく
		if (gunTarget >= 0)
			gunTime = targetDis / (oi.abs() * 1.1);
		if (gunTime > 1.0)
			gunTime = 1.0;

		// 弾丸の到着予想位置を求める
		gcVel.x = pVel.x + ni.x + (oi.x - gVel.x * gunTime) * gunTime;
		gcVel.y = pVel.y + ni.y + (oi.y - gVel.y * gunTime) * gunTime;
		gcVel.z = pVel.z + ni.z + (oi.z + (-9.8 - gVel.z) * gunTime / 2) * gunTime;

		world.change3d(this, gcVel, sc);

		// 機銃を目標へ向ける
		if (gunTarget >= 0) {
			c.set(world.plane[gunTarget].pVel);
			c.addCons(world.plane[gunTarget].vpVel, gunTime);
			world.change3d(this, c, a);
			world.change3d(this, world.plane[gunTarget].pVel, b);
			sc.x += b.x - a.x;
			sc.y += b.y - a.y;
		}

		if (targetSx > -1000) {
			double xx = (targetSx - sc.x);
			double yy = (targetSy - sc.y);
			double mm = Math.sqrt(xx * xx + yy * yy);
			if (mm > 20) {
				xx = xx / mm * 20;
				yy = yy / mm * 20;
			}
			gunVx += xx;
			gunVy -= yy;
		}
		gunX += gunVx * 100 / 300;
		gunY += gunVy * 100 / 300;
		gunVx -= gunVx * 0.3;
		gunVy -= gunVy * 0.3;

		// 機銃稼働限界内かチェック

		double y = gunY - 20;
		double r = Math.sqrt(gunX * gunX + gunY * gunY);
		if (r > 100) {
			double x = gunX;
			x *= 100 / r;
			y *= 100 / r;
			gunX = x;
			gunY = y + 20;
			gunVx = 0;
			gunVy = 0;
		}

		// 弾丸移動

		for (i = 0; i < BMAX; i++)
			if (bullet[i].use != 0)
				bullet[i].move(world, this);

		// 弾丸発射処理

		if (gunShoot && gunTemp++ < Plane.MAXT) {
			for (i = 0; i < BMAX; i++) {
				if (bullet[i].use == 0) {
					bullet[i].vVel.setPlus(vpVel, oi);
					aa = Math.random();
					bullet[i].pVel.setPlus(pVel, ni);
					bullet[i].pVel.addCons(bullet[i].vVel, 0.1 * aa);
					bullet[i].opVel.set(bullet[i].pVel);
					bullet[i].bom = 0;
					bullet[i].use = 15;
					break;
				}
			}
		} else if (gunTemp > 0)
			gunTemp--;
	}

	// ミサイル移動と発射処理

	public void moveAam(Jflight world) {
		CVector3 dm = new CVector3();
		CVector3 ni = new CVector3();
		CVector3 oi = new CVector3();

		for (int k = 0; k < MMMAX; k++) {

			// 各ミサイル移動
			if (aam[k].use > 0) {
				aam[k].move(world, this);
			}

			// タイムアウトしたら消去
			if (aam[k].use == 0)
				aam[k].use = -1;
		}

		// ミサイル発射処理
		// ただし、目標が近すぎると撃てない

		if (aamShoot && targetDis > 50) {

			// 使われていないミサイルを探す

			int k;
			for (k = 0; k < MMMAX; k++)
				if (aam[k].use < 0 && aamTarget[k] >= 0)
					break;

			if (k != MMMAX) {
				Missile ap = aam[k];

				//  発射位置を決める

				switch (k % 4) {
				case 0:
					dm.x = 6;
					dm.z = 1;
					break;
				case 1:
					dm.x = -6;
					dm.z = 1;
					break;
				case 2:
					dm.x = 6;
					dm.z = -1;
					break;
				case 3:
					dm.x = -6;
					dm.z = -1;
					break;
				}
				dm.y = 2;
				change_l2w(dm, ni);

				//  発射速度を決める

				double v2 = 0;
				double v3 = 5;
				double vx = Math.random() * v3;
				double vy = Math.random() * v3;
				v2 *= (k / 4) + 1;
				vx *= (k / 4) + 1;
				vy *= (k / 4) + 1;
				switch (k % 4) {
				case 0:
					dm.x = vx;
					dm.z = vy - v2;
					break;
				case 1:
					dm.x = -vx;
					dm.z = vy - v2;
					break;
				case 2:
					dm.x = vx;
					dm.z = -vy - v2;
					break;
				case 3:
					dm.x = -vx;
					dm.z = -vy - v2;
					break;
				}
				dm.y = 40;
				change_l2w(dm, oi);

				ap.pVel.setPlus(pVel, ni);
				ap.vpVel.setPlus(vpVel, oi);

				// 発射向きを決める

				switch (k % 4) {
				case 0:
					dm.x = 8;
					dm.z = 1 + 10;
					break;
				case 1:
					dm.x = -8;
					dm.z = 1 + 10;
					break;
				case 2:
					dm.x = 5;
					dm.z = -1 + 10;
					break;
				case 3:
					dm.x = -5;
					dm.z = -1 + 10;
					break;
				}
				dm.y = 50.0;
				dm.z += (k / 4) * 5;
				change_l2w(dm, oi);
				double v = oi.abs();
				ap.aVel.setConsInv(oi, v);

				// 各種初期化

				ap.use = 100;
				ap.count = 0;
				ap.bom = 0;
				ap.targetNo = aamTarget[k];
			}
		}
	}

}
