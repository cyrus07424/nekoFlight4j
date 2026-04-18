package jflight.constants;

public interface DrawScales {

	/** HUD スケール計算に使う基準幅。 */
	double HUD_BASE_WIDTH = 240.0;

	/** HUD スケール計算に使う基準高さ。 */
	double HUD_BASE_HEIGHT = 135.0;

	/** ポップアップメニューに適用する最大スケール。 */
	double POPUP_MAX_SCALE = 1.25;

	/** 画面中央の固定十字に適用するスケール。 */
	double HUD_CENTER_CROSS_SCALE = 0.5;

	/** 可動照準に適用するスケール。 */
	double HUD_RETICLE_SCALE = 0.25;

	/** 視界外敵機マーカーに適用するスケール。 */
	double HUD_ENEMY_ARROW_SCALE = 0.5;
}
