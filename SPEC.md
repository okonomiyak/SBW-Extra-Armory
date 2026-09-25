# SBW Extra Armory 仕様書

最終更新: 2026-09-25 (mod_version 0.2.2時点、次期0.2.3向け未リリース分を含む)

HANDOVER.md(開発経緯・詰まった箇所のメモ)、README.md(利用者向け概要)とは別に、実装されている全アイテム/エンティティ/エフェクト/設定値を機能単位でまとめた技術仕様書。

---

## 1. 擲弾・投射武器

### 1.1 クラスター擲弾 (`cluster_grenade`)
- 手投げ。構えて離すと投げる(`UseAnim.SPEA`, `releaseUsing`)。
- `GunGrenadeEntity`継承。着弾約8tick前を検知して、通常のグレネード3発に分裂して着弾する。
- ランチャー専用弾薬版 `cluster_grenade_ammo` あり(手投げ不可、`grenade_carbine`のアンダースラング用)。

### 1.2 スモーククラスター擲弾 (`smoke_cluster_grenade`)
- クラスター擲弾と同じ分裂挙動。分裂先が`M18SmokeGrenadeEntity`(発煙弾)。
- ランチャー専用弾薬版 `cluster_smoke_ammo` あり。

### 1.3 スプリンググレネード (`spring_grenade`)
- `HandGrenadeEntity`継承。構えて離すと投げる。
- フューズ30tick。爆発4tick前に真上へ1.5ブロック瞬間移動してから起爆(S字地雷方式)。
- ダメージ/爆発ダメージはベース値の2/3。

### 1.4 投げナイフ (`throwing_knife`)
- SuperbWarfareの汎用弾`ProjectileEntity`継承。ヘッドショット倍率(`.headShot()`)・脚ショット倍率(`.legShot(1f)`)を明示設定。
- `.velocity()`を実際の投擲速度と一致させてあるため、速度依存ダメージ減衰の影響を受けない。
- 無重力(まっすぐ飛ぶ)。

### 1.5 昏睡グレネード (`coma_grenade`)
- `HandGrenadeEntity`継承。ダメージ・爆発ダメージともに0(非殺傷)。
- 着弾(`onHit`)時に半径5ブロック以内の`LivingEntity`全員に**昏睡効果を1秒(20tick)** 付与。
- `super.onHit()`は呼ばない — ダメージ0だとSBW側の爆発処理が「不発弾」扱いにして自壊しないため、`discard()`を自前で呼んでいる。
- 見た目はスモーククラスター擲弾のモデルを流用し、テクスチャ全体を青系に色相統一したもの(`textures/item/coma_grenade.png`)。専用の`GeoEntityRenderer`/`GeoModel`で描画(理由は1.6参照)。

### 1.6 実装上の注意: `HandGrenadeEntity`系のレンダリング
`HandGrenadeEntity`は`getModel()`/`getModelInstance()`等を自前でオーバーライドしており、SBW汎用の`BasicProjectileRenderer`(エンティティ登録名からモデルパスを自動導出する仕組み)を使うと**SBWの素の手榴弾モデルが表示されてしまう**(`GunGrenadeEntity`系にはこの問題はない)。`HandGrenadeEntity`を継承する自作エンティティ(`spring_grenade`, `coma_grenade`)は、GeckoLib純正の`GeoEntityRenderer`+専用`GeoModel`を使うこと。

---

## 2. 銃火器

### 2.1 グレネードランチャー (`grenade_carbine`)
- データ駆動銃(`assets|data/sbwarmory/sbw/guns/grenade_carbine.json`)。
- 通常ライフル弾 + アンダースラング「グレネード」枠(クラスター擲弾弾薬を発射)の2種装填。

### 2.2 SRAW / SRAWミサイル (`sraw_launcher` / `sraw_missile`)
- `GunGrenadeEntity`継承、`GuidedMissile`マーカーインターフェース実装(4章参照)。
- 誘導方式: 発射5tick後から、毎tick撃った本人の視線方向(64ブロック先)へ8%(`TURN_FACTOR=0.08`)ずつ機体を曲げる。
- 描画は素のSBW `BasicProjectileRenderer`のまま(専用モデルは手持ちアイテムのみ)。
- **未解決の既知バグ**: GeckoLibの`GeoModel.getAnimation`で`NullPointerException`が発生することがある。再現条件未特定、調査は中断中。

---

## 3. 巡航ミサイル要請システム

### 3.1 巡航ミサイル指示機 (`laser_designator`, 表示名「巡航ミサイル指示機」)
- 右クリック: 照準トグル(ON/OFF、`Aiming`タグ)。`CONSUME`を返し腕振りモーションは出さない。
- 照準中: スクロールでズーム(1.5〜8.0倍、`ZOOM_STEP=0.5`)。スコープ演出は`superbwarfare:textures/overlay/spyglass/spyglass.png`(横長楕円ビネット、`SCOPE_SCALE=1.35`, `SCOPE_ASPECT=21:9`)+距離表示(`hud.sbwarmory.distance`)。
- 照準中に左クリック長押し(既定`LOCK_TICKS=30` = 1.5秒)で発射要求。長押し中は進捗バー(赤)を表示、左クリックの通常動作(採掘/攻撃)は照準中のみ無効化。
- 発射: サーバー側で再検証後、`CruiseMissileEntity`を着弾地点上空150ブロック(`SPAWN_HEIGHT_ABOVE_TARGET`)に生成。使用したスタックは1消費(クリエイティブ除く)。全プレイヤーへ`chat.sbwarmory.cruise_missile_inbound`(赤太字)をブロードキャスト。
- モデル: 独自3Dモデル(通称"soflam")。ネットワーク: `FireCruiseMissilePacket`(クライアント長押し完了→サーバーへ発射要求)。

### 3.2 巡航ミサイル (`cruise_missile`)
- 着弾地点上空150ブロックに出現 → `warmupSeconds`秒(既定5)待機 → `fallSpeed`(既定4.0ブロック/tick)で落下 → 着弾。
- `FastThrowableProjectile`が独自に毎tick raycastして`onHit()`を呼ぶため、`onHit`を直接オーバーライドして確実に起爆させている(`noPhysics`だけでは不十分)。水没・`VehicleEntity`/`LivingEntity`との接触でも即起爆。
- ダメージ判定: 着弾点中心に一辺`footprintSize`(既定30)×高さ`damageHeight`(既定24)の直方体。`LivingEntity`に`damage`(既定114514)、`VehicleEntity`(乗り物、LivingEntityではないので別枠)に`vehicleDamage`(既定114514)。
- ブロック破壊は`explosionRadius`(既定15)の別枠爆発。
- 空襲サイレン: 発射1.5秒後(`SIREN_DELAY_TICKS=30`)から`sirenLoopTicks()`(既定60tick=3秒)ごとに再生。音源は**着弾地点の地上**(待機中は上空150ブロックにいるため、そこを音源にすると距離減衰でほぼ聞こえない)。音量は`sirenVolume`(既定1.0、0でミュート)×16。
- 着弾演出: `MushroomCloudEntity`(6フレームのビルボードアニメ、80tick寿命)+`MUSHROOM_PUFF`パーティクル+爆発パーティクル群。

### 3.3 野獣ミサイル指示機 / 野獣ミサイル (`beast_missile_designator` / `beast_missile`)
- `LaserDesignatorItem`/`CruiseMissileEntity`のサブクラス。操作系・ダメージ設定は共通。
- 差分: サイレンがSuperbWarfareの「senpai」モブの唸り声(`growl.ogg`、約26秒)。`sirenLoopTicks()`を520(約26秒)にして基本1回しか鳴らないようにしてある。
- 飛来時間が2倍(`flightTimeScale()=2.0`、待機時間・落下速度の両方に効く)。
- 着弾演出: キノコ雲の代わりに岩の欠片(`rock_debris`パーティクル、重力あり、60個放射状)+`yarimasune.ogg`(SBWのsenpai待機ボイスの1つ、専用`SoundEvent`として再登録)。
- 見た目は巡航ミサイルと同一モデルの流用(専用アセットなし)。

### 3.4 誤爆防止: `GuidedMissile`マーカーインターフェース
`SrawMissileEntity`と`CruiseMissileEntity`(→`BeastMissileEntity`も継承で自動的に)は`GunGrenadeEntity`を継承しているため、グレネード判定を行う仕組み(3.5のアクティブ防御システム等)が誤って対象にしてしまう。個別クラス名での除外は今後増える missile 種類のたびに漏れるので、`GuidedMissile`インターフェースを実装させて一括除外する設計にしてある。

### 3.5 アクティブ防御システム (`active_defense_system`)
- 設置型(アイテムを地面に右クリック)。専用GeckoLibモデル(トライポッド+八角柱本体+迎撃弾クラスター6基)。
- 毎tick、`radius`(既定5ブロック)以内の以下を索敵し、即座に`discard()`で無力化:
  - `GunGrenadeEntity` / `HandGrenadeEntity` / `M18SmokeGrenadeEntity`(ただし`GuidedMissile`実装エンティティは除外)
  - `PrimedTnt`(無条件)
  - `Creeper`(**膨張中**、`getSwellDir() > 0`のみ。ただ近くにいるだけの個体は対象外)
- 迎撃は`cooldownSeconds`(既定5秒)に1回まで。
- 装置自体は`health`(既定1)で脆く、`isPickable`/`isAttackable`/`hurt`を実装して殴って壊せる。
- **ソフト依存対応**: 「LesRaisins Tactical Equipments」(`me.xjqsh.lrtactical`)導入時、そのMod製の投擲グレネード全種(通常/スモーク/スティッキー/スタン/エフェクトクラウド)も迎撃対象になる。`Class.forName("me.xjqsh.lrtactical.entity.ThrowableItemEntity")`(共通基底クラス)をリフレクションで解決する形で、build.gradleに依存追加はしていない(未導入でもクラッシュしない)。爆発後に残るガス雲エンティティ(`SpEffectCloudEntity`、バニラ`AreaEffectCloud`継承)はこの基底を継承しないため、対象から自然に除外される。

---

## 4. 頭防具

### 4.1 暗視ゴーグル (`night_vision_goggles` / `night_vision_goggles_red`)
- **本物の`ArmorItem`(HEADスロット)**。素材はレザー相当(防御力1)を流用。
- GeckoLibの`GeoArmorRenderer`+`armorHead`ボーン(`pivot=[0,24,0]`必須、プレイヤーモデルの頭座標系に合わせる)で専用3Dモデルを描画。5%大きめにスケール(`withScale(1.05F)`)してバニラの頭とのZファイティングを回避。
- 装備中(頭スロットに入っている間)、`inventoryTick`でバニラ`NIGHT_VISION`効果を継続付与(220tickごとに再付与)。
- **Nキー**(`ModKeyMappings.TOGGLE_NIGHT_VISION`)でON/OFFトグル可能。NBTの`Active`フラグ(既定true)で管理、`ToggleNightVisionPacket`でサーバーに反映。
- 緑エフェクトは**一人称視点かつ効果ON**の時だけ有効。当初はバニラPostChain(`GameRenderer#loadEffect`)を使う専用シェーダーJSONだったが、Rubidium/Oculus系の軽量化Modを併用する実機環境でPostChainがシェーダーパック未選択でも黙って無効化される事例が確認されたため、**HUDレイヤーへの乗算ブレンド描画方式に切り替え済み**(`ClientForgeEvents#drawGreenMultiplyOverlay`)。画面全体に不透明な緑(`0,255,0`)を`DST_COLOR × ZERO`ブレンドで描画することで、既存シーンの緑チャンネルの明るさをそのまま残しつつ赤/青を0にする(平坦な半透明トーンではなく、実際の明暗差が残るFLIR/サーマルっぽい見た目)。スコープのビネットと同じ`RenderGuiEvent.Post`経路を使うため、Sodium系Modの影響を受けない。旧`shaders/post/night_vision.json`は削除済み。
- `night_vision_goggles_red`は`NightVisionGogglesItem`のサブクラスで、`createRenderer()`だけオーバーライドしてモデル/テクスチャを差し替えている(ロジックは全部継承)。`createRenderer()`には`@OnlyIn(Dist.CLIENT)`が必須(付け忘れるとアイテム登録時に専用サーバーがクラッシュする、詳細は10章)。

### 4.2 ヘルメット (`helmet_blue` / `helmet_red`)
- ゴーグル無し、効果無しの見た目だけ版。汎用`HelmetItem`クラス1つでモデル/テクスチャだけコンストラクタ引数で差し替え。

---

## 5. ステータス効果・消費アイテム

### 5.1 昏睡 (`coma`)
- 移動速度 **-60%**(`MULTIPLY_TOTAL`)。攻撃力への影響なし。
- 画面はバニラ`Blindness`を毎tick(`isDurationEffectTick`を常時trueに)再付与し続けることで暗闇状態を維持(専用の画面シェーダーは作っていない)。
- 効果アイコン: `textures/mob_effect/coma.png`(ユーザー提供、16x16)。

### 5.2 アイスティー (`iced_tea`)
- バニラの`FoodProperties`機構をそのまま使用(専用Itemクラス不要)。
- 食べると**確定で昏睡10秒(200tick)** を付与(`effect(..., 1.0F)`)。
- 早食い(`fast()`)。アイコンはユーザー提供の専用テクスチャ。

---

## 6. ブロック

### 6.1 コンクリートバリア (`concrete_barrier`)
- ジャージー型防護壁。1ブロックで2形状:
  - `top=false`(下が空気/他ブロック): 太い5段土台
  - `top=true`(下が同じバリア): 細い直線壁
- 向き(`axis`: x/z)は設置方向に追従、積むときは下と同じ向きを継承。下を壊すと自動で`top=false`に戻る。
- 当たり判定はJavaの`VoxelShape`とモデルJSONの`from`/`to`を手動で一致させてある。

### 6.2 コンクリートブロック (`concrete_block`)
- バリアと同じ風化コンクリートテクスチャを全面に貼った普通の立方体。バニラ`Block`クラスそのまま。

共通: 耐久5.0/爆発耐性30、`requiresCorrectToolForDrops`、石音、自身をドロップ。

---

## 7. 鍛冶台設計図 (現状クリエイティブのみ入手可)

| 設計図 | base | addition | 生成物 |
|---|---|---|---|
| `sraw_missile_blueprint` | `superbwarfare:epic_material_pack` | `superbwarfare:ancient_cpu` | `sraw_missile` |
| `grenade_carbine_blueprint` | `superbwarfare:rare_material_pack` | `minecraft:dispenser` | `grenade_carbine` |

いずれも`minecraft:smithing_transform`。設計図自体を入手するレシピは未設定。

---

## 8. クラフトレシピ一覧

| 生成物 | レシピ内容 |
|---|---|
| `cluster_grenade_ammo` | 形状(XYX): `superbwarfare:hand_grenade` + `superbwarfare:grenade_40mm` |
| `cluster_smoke_ammo` | 形状(XYX): `superbwarfare:m18_smoke_grenade` + `superbwarfare:grenade_40mm` |
| `cluster_grenade`(手投げ版) | 無形: `cluster_grenade_ammo` + `minecraft:gunpowder` |
| `smoke_cluster_grenade`(手投げ版) | 無形: `cluster_smoke_ammo` + `minecraft:gunpowder` |
| `spring_grenade` | 無形: `superbwarfare:hand_grenade` + `minecraft:gunpowder` |
| `throwing_knife` | 無形: `minecraft:diamond_sword` 単品 |
| `laser_designator` | 無形: `minecraft:spyglass` + `superbwarfare:large_anti_ground_missile` |
| `sraw_missile` | 鍛冶台(7章参照) |
| `grenade_carbine` | 鍛冶台(7章参照) |
| `helmet_blue` | 無形: `minecraft:leather_helmet` + `minecraft:blue_dye`、または`helmet_red` + `minecraft:blue_dye` |
| `helmet_red` | 無形: `minecraft:leather_helmet` + `minecraft:red_dye`、または`helmet_blue` + `minecraft:red_dye` |
| `night_vision_goggles` | 無形: `helmet_blue` + `superbwarfare:thermal_imaging_goggles` |
| `night_vision_goggles_red` | 無形: `helmet_red` + `superbwarfare:thermal_imaging_goggles` |
| `concrete_block` | 形状(XXX/X_Y/YYY): `minecraft:sand`×4 + `minecraft:gravel`×4 |
| `concrete_barrier` | 無形: `concrete_block` 単品 → **2個** |

未設定: `beast_missile_designator`, `active_defense_system`, `coma_grenade`, `iced_tea`, 各種設計図アイテム自体、9章の無限弾薬・給電アイテム3種(すべてクリエイティブのみ入手可)。

---

## 9. 無限弾薬・給電アイテム (すべてクリエイティブ専用、レシピ未設定)

SuperbWarfareの弾薬/乗り物系リソースを「持っているだけ」で無限化する管理者/検証用アイテム群。SBW本体のCreative Ammo Boxと同じ「携帯するだけで効く」思想だが、対象を用途別に3アイテムへ分割してある。

### 9.1 一種類無限弾薬箱 (`infinite_ammo_box`)
- 対象: プレイヤー自身の持ち弾プール(SBWの`Ammo`列挙型: `HANDGUN`/`RIFLE`/`SHOTGUN`/`SNIPER`/`HEAVY`のうち**選択した1種類のみ**)。
- `inventoryTick`で毎tick、選択中の弾種が上限(`Ammo#getLimit()`)未満なら上限まで補充(`Ammo#set`)。手に持っていなくてもインベントリのどこかにあれば効く。
- 弾種選択: TACZの単一種弾薬箱と同じUX。アイテムをカーソルで持ち上げ、インベントリ内のSBW弾薬/弾薬箱アイテム(`AmmoSupplierItem`のインスタンス)をクリック(`overrideStackedOnOther`)すると、そのアイテムの弾種(`AmmoSupplierItem#getType()`)に設定される。**クリエイティブモードのプレイヤーのみ**この切り替えが効く(サバイバルプレイヤーがクリックしても無反応)。
- 選択中の弾種はNBT `AmmoType`(文字列)で保持。ツールチップに現在の弾種名(該当色)を表示。

### 9.2 ビークル無限弾薬箱 (`vehicle_ammo_box`)
- 対象: 現在騎乗中のSuperbWarfare `VehicleEntity`が持つ**全武器スロット**+デコイ(スモーク/チャフ)。弾種選択UIは無し(無条件で全部補充)。
- 武器: `VehicleEntity#getGunDataMap()`の全キーに対し`modifyGunData`で`GunData.ammo`(弾倉、`GunProp.MAGAZINE`が上限)と`GunData.backupAmmoCount`(予備弾、上限プロパティが無いため`999999`固定)の両方を補充。`VehicleEntity#getAmmo(GunData)`は弾倉が無い武器(`MAGAZINE<=0`)だと`backupAmmoCount`だけを表示/判定に使うため、両方触らないと「補充されない」ように見える(実際にハマった箇所)。
- デコイ: `VehicleEntity#getDecoyCount()`/`setDecoyCount()`(乗り物本体の同期int値、GunDataとは別管理)を`DefaultVehicleData#getDecoyMagazineSize()`まで補充。デコイ非搭載の乗り物は上限0のため自動的に何もしない。

### 9.3 無限給電バッテリーパック (`vehicle_battery_pack`)
- 対象: 現在騎乗中の乗り物のエネルギー残量。`VehicleEntity#hasEnergyStorage()`で対応乗り物か判定し、`getEnergy() < getMaxEnergy()`なら`setEnergy(getMaxEnergy())`。

いずれも実装はリフレクション不要の通常のSBW公開APIで完結しており、`build.gradle`の追加依存も不要(既存のSBW `modImplementation`のみで完結)。

---

## 10. サーバー設定 (`config/sbwarmory-server.toml`)

| セクション | キー | 既定値 | 内容 |
|---|---|---|---|
| `cruiseMissile` | `footprintSize` | 30.0 | 確殺範囲の一辺(m) |
| | `damageHeight` | 24.0 | ダメージ判定の高さ(m) |
| | `damage` | 114514.0 | 対生物ダメージ |
| | `vehicleDamage` | 114514.0 | 対乗り物ダメージ |
| | `explosionRadius` | 15.0 | ブロック破壊半径 |
| | `warmupSeconds` | 5 | 着弾までの待機秒数 |
| | `fallSpeed` | 4.0 | 落下速度(ブロック/tick) |
| `activeDefenseSystem` | `radius` | 5.0 | 索敵半径(m) |
| | `cooldownSeconds` | 5 | 迎撃間隔(秒) |
| | `health` | 1 | 装置自体のHP |
| (直下) | `sirenVolume` | 1.0 | 空襲サイレン音量倍率(0でミュート) |

`beast_missile_designator`は`cruiseMissile`の設定をそのまま継承する(`flightTimeScale`/サイレン内容だけ独自のJava定数)。

---

## 11. 既知の未解決事項

- SRAWランチャーの`GeoModel.getAnimation`NPEクラッシュ(2.2節参照、調査中断中)。
- `beast_missile_designator`/`active_defense_system`/`coma_grenade`/`iced_tea`/各種設計図アイテム/9章の無限系アイテム3種のクラフトレシピ未設定(現状クリエイティブのみ)。
- `coma_grenade`のダメージ0仕様は意図的(スタン専用武器として設計)。ダメージも欲しい場合は要相談。
- ADSのLesRaisins Tactical Equipments対応(3.5節)は実装済みだがMDG(このプロジェクトのdevビルド環境)側で動作未確認 — TACZ+LesRaisinsをMDGのdevクライアント(`gradlew runClient`)に入れるとLesRaisins自身のmixin(`LivingEntityMixin`の`getUseItem`シャドウ)がMDGのrefmap再マッピングと噛み合わずクラッシュするため、実機(パッケージ済みjar+通常のForgeランチャー環境)での確認待ち。

## 12. 解決済みの既知バグ (参考)

- **専用サーバー起動時クラッシュ**(v0.2.2で修正): `NightVisionGogglesItem.createRenderer()`が`@OnlyIn(Dist.CLIENT)`無しでクライアント専用の`GeoArmorRenderer`を返していたため、アイテム登録時(サーバーでも実行される)にForgeの`RuntimeDistCleaner`が検知してクラッシュしていた。メソッド本体に`@OnlyIn`を付与して解決(サブクラスのオーバーライド側だけでは不十分)。
- **暗視ゴーグルの緑エフェクトが実機の専用サーバーで掛からない**: 4.1節参照。PostChainベースの実装からHUD乗算ブレンド方式へ全面的に作り直して解決。
