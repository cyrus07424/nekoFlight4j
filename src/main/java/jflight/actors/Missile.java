package jflight.actors;

import jflight.constants.Commons;
import jflight.constants.Configurations;
import jflight.mains.Jflight;
import jflight.utils.CVector3;

//
// missile
// ミサイルクラス
//

public class Missile {

	// 変数

	public CVector3 pVel; // 位置
	public CVector3[] opVel; // 昔の位置（煙の位置）
	public CVector3 vpVel; // 速度
	public CVector3 aVel; // 向き（単位ベクトル）
	public int use; // 使用状態（0で未使用）
	public int bom; // 爆発状態（0で未爆）
	public int bomm; // 破裂状態（0で未爆）
	public int count; // リングバッファ長（煙の長さ）
	public int targetNo; // ターゲットNO（0>でロックOFF）

	// テンポラリオブジェクト

	protected CVector3 m_a0;

	public Missile() {
		pVel = new CVector3();
		vpVel = new CVector3();
		aVel = new CVector3();

		opVel = new CVector3[Configurations.MISSILE_MOMAX];
		for (int i = 0; i < Configurations.MISSILE_MOMAX; i++)
			opVel[i] = new CVector3();

		m_a0 = new CVector3();
	}

	// ミサイルのホーミング処理

	public void horming(Jflight world, Plane plane) {

		// ロックONされていて、残りステップが85以下ならホーミングする

		if (targetNo >= 0 && use < 100 - 15) {

			// 自分の速度を求める
			double v = vpVel.abs();
			if (Math.abs(v) < 1)
				v = 1;

			// 追尾目標
			Plane tp = world.plane[targetNo];

			// 追尾目標との距離を求める
			m_a0.setMinus(tp.pVel, pVel);
			double l = m_a0.abs();
			if (l < 0.001)
				l = 0.001;

			// 追尾目標との速度差を求める
			m_a0.setMinus(tp.vpVel, vpVel);
			double m = m_a0.abs();

			// 衝突予想時間を修正ありで求める
			double t0 = l / v * (1.0 - m / (800 + 1));

			// 衝突予想時間を０から５に丸める
			if (t0 < 0)
				t0 = 0;
			if (t0 > 5)
				t0 = 5;

			// 衝突予想時間時のターゲットの位置と自分の位置の差を求める
			m_a0.x = tp.pVel.x + tp.vpVel.x * t0 - (pVel.x + vpVel.x * t0);
			m_a0.y = tp.pVel.y + tp.vpVel.y * t0 - (pVel.y + vpVel.y * t0);
			m_a0.z = tp.pVel.z + tp.vpVel.z * t0 - (pVel.z + vpVel.z * t0);

			double tr = ((100 - 15) - use) * 0.02 + 0.5;
			if (tr > 0.1)
				tr = 0.1;
			if (tr < 1) {
				// 発射直後は、派手な機動をしない
				l = m_a0.abs();
				aVel.addCons(m_a0, l * tr * 10);
			} else {
				// そうでない場合、追尾方向へミサイル機種を向ける
				aVel.set(m_a0);
			}

			// 向きを単位ベクトルに補正
			aVel.consInv(aVel.abs());
		}

	}

	// ミサイルモーター計算

	public void calcMotor(Jflight world, Plane plane) {

		// 発射直後はモーターOFF
		if (use < 100 - 5) {
			double aa = 1.0 / 20;
			double bb = 1 - aa;

			// 現在の速度成分と向き成分を合成して新たな速度成分とする
			double v = vpVel.abs();
			vpVel.x = aVel.x * v * aa + vpVel.x * bb;
			vpVel.y = aVel.y * v * aa + vpVel.y * bb;
			vpVel.z = aVel.z * v * aa + vpVel.z * bb;

			// ミサイル加速
			vpVel.addCons(aVel, 10.0);
		}
	}

	// ミサイル移動、敵機とのあたり判定、地面との当たり判定を行う
	// ミサイル発射処理はJflightクラス側で行われている

	public void move(Jflight world, Plane plane) {

		// 爆発中ならカウンタ減少
		if (bom > 0) {

			// 煙を消す
			count = 0;

			bom--;
			if (bom < 0)
				use = 0;

			return;
		}

		// 重力加速
		vpVel.z += Commons.G * Commons.DT;

		// ホーミング計算
		horming(world, plane);

		// ミサイルモーター計算
		calcMotor(world, plane);

		// リングバッファに位置を保存
		opVel[use % Configurations.MISSILE_MOMAX].set(pVel);

		// ミサイル移動
		pVel.addCons(vpVel, Commons.DT);
		use--;

		// ターゲットとの当たり判定
		// ロックしている対象とのみ当たり判定する

		if (targetNo >= 0) {

			// 追尾目標
			Plane tp = world.plane[targetNo];

			// ターゲットとの距離を求めて、ある程度以下なら当たり（接触信管のみ使用）
			m_a0.setMinus(pVel, tp.pVel);
			if (m_a0.abs() < 10) {
				bom = 10;
			}
		}

		// 地面との当たり判定

		double gh = world.gHeight(pVel.x, pVel.y);
		if (pVel.z < gh) {
			bom = 10;
			pVel.z = gh + 3;
		}

		// リングバッファ長（煙の長さ）を設定
		if (count < Configurations.MISSILE_MOMAX)
			count++;
	}

}
