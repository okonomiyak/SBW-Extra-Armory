# バイク モデル/テクスチャ 作業用コピー

ここはビルドに含まれない作業用フォルダ。編集が終わったら下の元の場所へ上書きで戻す。

| ファイル | 戻し先 |
|---|---|
| `motorcycle.geo.json` | `src/main/resources/assets/sbwarmory/models/bedrock/vehicle/motorcycle.geo.json` |
| `motorcycle.png` | `src/main/resources/assets/sbwarmory/textures/bedrock/vehicle/motorcycle.png` |

## 守ること
- 形式: Blockbench の Bedrock Model (`.geo.json`)。identifier は `geometry.motorcycle` のまま。
- テクスチャ: 現在 512×512。サイズを変えたら geo.json の `texture_width` / `texture_height` も合わせる。
- ボーン名は SBW が名前で探して動かすので、変えない・消さない:
  `root`, `body`, `steering`, `engine`, `rear`, `lights`, `Wheel`, `wheelL0Turn`(前輪・操舵), `wheelL1`(後輪)
- 向き: モデル上は前方が −Z、`wheelL*` が +X 側。
- 当たり判定・座席位置は `data/sbwarmory/sbw/vehicles/motorcycle.json` 側にあるので、
  車体の大きさや座る位置を大きく変えたらそちらの `OBB` / `Seats` も要調整。
