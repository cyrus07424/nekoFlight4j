package jflight.actors;

import jflight.constants.Commons;
import jflight.mains.Jflight;
import jflight.utils.CVector3;

//
// Bullet
// 弾丸クラス
//

public class Bullet {

	// 変数

	public CVector3 pVel; // 位置
	public CVector3 opVel; // １ステップ前の位置
	public CVector3 vVel; // 速度
	public int use; // 使用状態（0で未使用）
	public int bom; // 爆発状態（0で未爆）

	// テンポラリ用オブジェクト

	protected CVector3 m_a;
	protected CVector3 m_b;
	protected CVector3 m_vv;

	// コンストラクタ

	public Bullet() {
		pVel = new CVector3();
		opVel = new CVector3();
		vVel = new CVector3();
		m_a = new CVector3();
		m_b = new CVector3();
		m_vv = new CVector3();
	}

	// 弾丸移動、敵機とのあたり判定、地面との当たり判定を行う
	// 弾丸発射処理はJflightクラス側で行われている

	public void move(Jflight world, Plane plane) {
		int i, k;
		long cx, cy;

		// 重力加速
		vVel.z += Commons.G * Commons.DT;

		// 一つ前の位置を保存
		opVel.set(pVel);

		// 移動
		pVel.addCons(vVel, Commons.DT);
		use--;

		// 目標が定まっているのならターゲットとの当たり判定を行う
		// 目標以外との当たり判定は行わない

		if (plane.gunTarget > -1) {

			// 目標が存在している場合

			// ここでの当たり判定方法は、
			// 一つ前の位置と現在の位置との距離と
			// 一つ前の位置と目標の距離、現在の位置と目標の距離との和を比較することで
			// 行っている。弾丸速度が速いため、単に距離を求めても当たらない。
			// 点と直線の方程式で再接近距離を求めても良いが、面倒だったので手抜き 。

			// 現在の弾丸の位置と目標との差ベクトルを求める
			m_a.setMinus(pVel, world.plane[plane.gunTarget].pVel);

			// 一つ前の弾丸の位置と目標との差ベクトルを求める
			m_b.setMinus(opVel, world.plane[plane.gunTarget].pVel);

			// 一つ前の弾丸の位置と現在の弾丸の位置との差ベクトルを求める
			m_vv.setCons(vVel, Commons.DT);

			double v0 = m_vv.abs();
			double l = m_a.abs() + m_b.abs();

			if (l < v0 * 1.05) {
				// 命中
				bom = 1; // 爆発表示用にセット
				use = 10; // 直ぐには消さないで跳ね飛ばす

				// 現在位置と一つ前の位置の中間位置方向の速度成分を足して跳ね飛ばす
				m_vv.x = (m_a.x + m_b.x) / 2.0;
				m_vv.y = (m_a.y + m_b.y) / 2.0;
				m_vv.z = (m_a.z + m_b.z) / 2.0;
				l = m_vv.abs();
				m_vv.consInv(l);
				vVel.addCons(m_vv, v0 / 0.1);
				vVel.cons(0.1);
			}
		}

		// 地面との当たり判定

		double gh = world.gHeight(pVel.x, pVel.y);
		if (pVel.z < gh) {
			// 地面以下なら、乱反射させる
			vVel.z = Math.abs(vVel.z);
			pVel.z = gh;
			vVel.x += (Math.random() - 0.5) * 50;
			vVel.y += (Math.random() - 0.5) * 50;
			vVel.x *= 0.5;
			vVel.y *= 0.5;
			vVel.z *= 0.1;
		}
	}
}
