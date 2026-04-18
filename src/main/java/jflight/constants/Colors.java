package jflight.constants;

import java.awt.Color;

public interface Colors {

	/** 背景や塗りつぶしに使う黒。 */
	Color BLACK = Color.black;

	/** 文字や通常ラインに使う白。 */
	Color WHITE = Color.white;

	/** 選択中表示などに使う黄。 */
	Color YELLOW = Color.yellow;

	/** ミサイル軌跡に使う明るい灰色。 */
	Color LIGHT_GRAY = Color.lightGray;

	/** 警告やロック枠に使う赤。 */
	Color RED = Color.red;

	/** ヘッダー文字に使う緑。 */
	Color GREEN = Color.green;

	/** 爆発マーカーや HUD 強調に使う橙。 */
	Color ORANGE = new Color(255, 176, 48);

	/** 地表グリッドに使う緑。 */
	Color GROUND = new Color(0, 96, 0);

	/** HUD 補助線に使う濃い灰色。 */
	Color DARK_GREY = new Color(80, 80, 80);

	/** HUD パネル内側の影色。 */
	Color PANEL_SHADOW = new Color(48, 48, 48);

	/** 手動操縦や低温時 HUD に使うシアン。 */
	Color CYAN = new Color(80, 220, 255);

	/** 水平線や中温時 HUD に使う黄緑。 */
	Color GREEN_YELLOW = new Color(192, 255, 96);

	/** 敵機描画に使うオリーブ色。 */
	Color OLIVE = new Color(128, 128, 0);

	/** 機銃冷却待ち表示に使うマゼンタ。 */
	Color HEAT_WAIT = new Color(255, 96, 255);

	/** 半透明 HUD 背景に使う黒。 */
	Color HUD_BACKGROUND = new Color(0, 0, 0, 72);

	/** ヒートバー目盛りに使う淡い白。 */
	Color HUD_TICK = new Color(255, 255, 255, 40);
}
