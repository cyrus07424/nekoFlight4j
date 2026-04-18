package jflight.constants;

public interface Configurations {

	/** 地表グリッドを描画する範囲の半径。 */
	double FMAX = 10000;

	/** 地表グリッドの分割数。 */
	int GSCALE = 128;

	/** 機体数の最大値。 */
	int PMAX = 4;

	/** 垂直尾翼のラダー偏向角度（度）。 */
	double RUDDER_DEFLECTION_DEG = 10.0;

	/** フレーム更新間隔（ミリ秒）。 */
	int FRAME_INTERVAL_MS = 33;

	/** 3D 描画時の透視投影スケール。 */
	double CAMERA_SCALE = 42.0;

	/** 弾丸の最大数。 */
	int PLANE_BMAX = 200;

	/** ミサイルの最大数。 */
	int PLANE_MMMAX = 10;

	/** 翼の数。 */
	int PLANE_WMAX = 6;

	/** 機銃の最大温度。 */
	int PLANE_MAXT = 50;

	/** ミサイルの煙の長さの最大値。 */
	int MISSILE_MOMAX = 100;
}
