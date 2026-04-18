package jflight.constants;

public interface Configurations {

	/** 地表グリッドを描画する範囲の半径。 */
	double FMAX = 50000;

	/** 地表グリッドの分割数。 */
	int GSCALE = 128;

	/** 機体数の最大値。 */
	int PMAX = 4;

	/** 描画の最大フレームレート。 */
	int MAX_FPS = 60;

	/** 描画フレーム間隔（ナノ秒）。 */
	long FRAME_INTERVAL_NANOS = 1_000_000_000L / MAX_FPS;

	/** シミュレーション更新レート。 */
	int SIMULATION_HZ = 30;

	/** シミュレーション更新間隔（ナノ秒）。 */
	long SIMULATION_STEP_NANOS = 1_000_000_000L / SIMULATION_HZ;

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
