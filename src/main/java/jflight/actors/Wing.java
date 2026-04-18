package jflight.actors;

import jflight.utils.CVector3;

//
// Wing
// 翼クラス
//
// 翼についたエンジンも表す
//

public class Wing {

	// 変数

	public CVector3 pVel; // 翼中心位置（機体座標）
	public CVector3 xVel; // 翼座標Ｘ単位ベクトル（機体座標）
	public CVector3 yVel; // 翼座標Ｙ単位ベクトル（機体座標）
	public CVector3 zVel; // 翼座標Ｚ単位ベクトル（機体座標）
	public double mass; // 翼の質量
	public double sVal; // 翼面積
	public CVector3 fVel; // 翼にかかっている力
	public double aAngle; // 翼のＸ軸ひねり角度（rad）
	public double bAngle; // 翼のＹ軸ひねり角度（rad）
	public CVector3 vVel; // 翼のひねりを考慮したＹ単位ベクトル（機体座標）
	public double tVal; // エンジンの推力（0で通常の翼）

	// テンポラリオブジェクト

	protected CVector3 m_pp, m_op, m_ti, m_ni, m_vp, m_vp2;
	protected CVector3 m_wx, m_wy, m_wz, m_qx, m_qy, m_qz;

	// コンストラクタ

	public Wing() {
		pVel = new CVector3();
		xVel = new CVector3();
		yVel = new CVector3();
		zVel = new CVector3();
		vVel = new CVector3();
		fVel = new CVector3();

		m_pp = new CVector3();
		m_op = new CVector3();
		m_ti = new CVector3();
		m_ni = new CVector3();
		m_vp = new CVector3();
		m_vp2 = new CVector3();
		m_wx = new CVector3();
		m_wy = new CVector3();
		m_wz = new CVector3();
		m_qx = new CVector3();
		m_qy = new CVector3();
		m_qz = new CVector3();
	}

	// 翼計算を行う
	// fVelに計算結果が求まる
	// veは空気密度、noは翼No.（迎角計算に使用）、boostはエンジンブースト

	public void calc(Plane plane, double ve, int no, boolean boost) {
		double v0, vv, t0, n, at, sin, cos, rr, cl, cd, ff, dx, dy, dz;

		// 機体の速度と回転率、翼の位置から翼における速度を求める（外積計算）

		m_vp.x = plane.vVel.x + pVel.y * plane.vaVel.z - pVel.z * plane.vaVel.y;
		m_vp.y = plane.vVel.y + pVel.z * plane.vaVel.x - pVel.x * plane.vaVel.z;
		m_vp.z = plane.vVel.z + pVel.x * plane.vaVel.y - pVel.y * plane.vaVel.x;

		// 翼のひねりを基に、基本座標ベクトルを回転

		sin = Math.sin(bAngle);
		cos = Math.cos(bAngle);

		m_qx.x = xVel.x * cos - zVel.x * sin;
		m_qx.y = xVel.y * cos - zVel.y * sin;
		m_qx.z = xVel.z * cos - zVel.z * sin;

		m_qy.set(yVel);

		m_qz.x = xVel.x * sin + zVel.x * cos;
		m_qz.y = xVel.y * sin + zVel.y * cos;
		m_qz.z = xVel.z * sin + zVel.z * cos;

		sin = Math.sin(aAngle);
		cos = Math.cos(aAngle);

		m_wx.set(m_qx);

		m_wy.x = m_qy.x * cos - m_qz.x * sin;
		m_wy.y = m_qy.y * cos - m_qz.y * sin;
		m_wy.z = m_qy.z * cos - m_qz.z * sin;

		m_wz.x = m_qy.x * sin + m_qz.x * cos;
		m_wz.y = m_qy.y * sin + m_qz.y * cos;
		m_wz.z = m_qy.z * sin + m_qz.z * cos;

		t0 = 0;

		fVel.set(0, 0, 0);

		if (sVal > 0) {

			// 翼計算

			vv = m_vp.abs();

			// 翼速度の単位ベクトルを求める(機体座標)

			if (vv < 1e-6)
				vv = 1e-6;
			m_ti.x = m_vp.x / vv;
			m_ti.y = m_vp.y / vv;
			m_ti.z = m_vp.z / vv;

			// 機体座標の翼速度を翼座標系に変換

			dx = m_wx.x * m_vp.x + m_wx.y * m_vp.y + m_wx.z * m_vp.z;
			dy = m_wy.x * m_vp.x + m_wy.y * m_vp.y + m_wy.z * m_vp.z;
			dz = m_wz.x * m_vp.x + m_wz.y * m_vp.y + m_wz.z * m_vp.z;

			// 揚力方向の速度成分を求める

			rr = Math.sqrt(dx * dx + dy * dy);

			if (rr > 0.001) {
				m_vp2.x = (m_wx.x * dx + m_wy.x * dy) / rr;
				m_vp2.y = (m_wx.y * dx + m_wy.y * dy) / rr;
				m_vp2.z = (m_wx.z * dx + m_wy.z * dy) / rr;
			} else {
				m_vp2.x = m_wx.x * dx + m_wy.x * dy;
				m_vp2.y = m_wx.y * dx + m_wy.y * dy;
				m_vp2.z = m_wx.z * dx + m_wy.z * dy;
			}

			m_ni.x = m_wz.x * rr - m_vp2.x * dz;
			m_ni.y = m_wz.y * rr - m_vp2.y * dz;
			m_ni.z = m_wz.z * rr - m_vp2.z * dz;

			vv = m_ni.abs();
			if (vv < 1e-6)
				vv = 1e-6;

			m_ni.consInv(vv);

			// 迎角を求める

			at = -Math.atan2(dz, dy);
			if (no == 0)
				plane.aoa = at;

			if (Math.abs(at) < 0.4) {
				//  揚力係数と抗力係数を迎角から求める
				cl = at * 4;
				cd = (at * at + 0.05);
			} else {
				//  迎角が0.4radを超えていたら失速
				cl = 0;
				cd = (0.4 * 0.4 + 0.05);
			}

			// 抗力を求める
			t0 = 0.5 * vv * vv * cd * ve * sVal;

			// 揚力を求める
			n = 0.5 * rr * rr * cl * ve * sVal;

			fVel.x = n * m_ni.x - t0 * m_ti.x;
			fVel.y = n * m_ni.y - t0 * m_ti.y;
			fVel.z = n * m_ni.z - t0 * m_ti.z;
		}

		if (tVal > 0) {

			// 推力計算

			// 推力を求める
			if (boost)
				ff = (5 * 10) / 0.9 * ve * 4.8 * tVal;
			else
				ff = plane.power / 0.9 * ve * 4.8 * tVal;

			// 地面に近い場合、見かけの推力を上げる
			if (plane.height < 20)
				ff *= (1 + (20 - plane.height) / 40);

			// 推力を加える

			fVel.addCons(m_wy, ff);
		}
		vVel.set(m_wy);
	}

}
