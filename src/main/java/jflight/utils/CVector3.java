package jflight.utils;

//
// CVector3
// ３次ベクトルクラス
//
// ガーベッジコレクションを避けるため、新規オブジェクト生成が必要となる
// 和や差を求めるメソッドを持たせてないので注意。
// 効率と利便性を重視して、メンバ変数もpublicにしてある
//

public class CVector3 {

	// 変数

	public double x, y, z;

	// コンストラクタ

	public CVector3() {
		x = y = z = 0.0;
	}

	public CVector3(double ax, double ay, double az) {
		set(ax, ay, az);
	}

	// 値設定

	public CVector3 set(double ax, double ay, double az) {
		x = ax;
		y = ay;
		z = az;
		return this;
	}

	public CVector3 set(CVector3 a) {
		x = a.x;
		y = a.y;
		z = a.z;
		return this;
	}

	// ベクトル加算

	public CVector3 add(CVector3 a) {
		x += a.x;
		y += a.y;
		z += a.z;
		return this;
	}

	public CVector3 setPlus(CVector3 a, CVector3 b) {
		x = a.x + b.x;
		y = a.y + b.y;
		z = a.z + b.z;
		return this;
	}

	// ベクトル定数倍加算

	public CVector3 addCons(CVector3 a, double c) {
		x += a.x * c;
		y += a.y * c;
		z += a.z * c;
		return this;
	}

	// ベクトル減算

	public CVector3 sub(CVector3 a) {
		x -= a.x;
		y -= a.y;
		z -= a.z;
		return this;
	}

	public CVector3 setMinus(CVector3 a, CVector3 b) {
		x = a.x - b.x;
		y = a.y - b.y;
		z = a.z - b.z;
		return this;
	}

	// ベクトル定数倍減算

	public CVector3 subCons(CVector3 a, double c) {
		x -= a.x * c;
		y -= a.y * c;
		z -= a.z * c;
		return this;
	}

	// 定数倍

	public CVector3 cons(double c) {
		x *= c;
		y *= c;
		z *= c;
		return this;
	}

	public CVector3 consInv(double c) {
		if (Math.abs(c) < 1e-9)
			return this;
		x /= c;
		y /= c;
		z /= c;
		return this;
	}

	public CVector3 setCons(CVector3 a, double c) {
		x = a.x * c;
		y = a.y * c;
		z = a.z * c;
		return this;
	}

	public CVector3 setConsInv(CVector3 a, double c) {
		if (Math.abs(c) < 1e-9) {
			x = 0;
			y = 0;
			z = 0;
			return this;
		}
		x = a.x / c;
		y = a.y / c;
		z = a.z / c;
		return this;
	}

	// ベクトル長の２乗

	public double abs2() {
		return x * x + y * y + z * z;
	}

	// ベクトル長

	public double abs() {
		return Math.sqrt(abs2());
	}

	// 内積

	public double inprod(CVector3 a) {
		return x * a.x + y * a.y + z * a.z;
	}
}
