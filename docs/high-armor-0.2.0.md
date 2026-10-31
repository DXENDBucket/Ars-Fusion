# 四、五级护甲 · Ars Fusion 0.2.0

## 最终范围

原版Sorcerer／Arcanist／Battlemage三套护甲各自继续升级到四、五级。保留原版一、二、三级数据，不把三套护甲当作三个等阶。只接入这十二件物品及其正向／反向融合结果；本次不扩展Elemental等附属护甲的升级路线。

| 升级 | 每件消耗的基座材料 | 魔源 |
| --- | --- | ---: |
| 三级→四级 | 荒野精华×1、高等经验宝石×1 | 7500 |
| 四级→五级 | 下界之星碎片×1、龙息×1 | 10000 |

荒野供物无序拆为四个`ars_fusion:wilden_essence`；下界之星无序拆为四个`ars_fusion:nether_star_fragment`。没有新增反向合并配方。五级配方接受`ars_fusion:nether_star_fragments`，默认只包括本模组的四分之一星碎片。

检索没有找到可确认统一等价量的公共碎片标签。已有碎片的份量不同：Silent Gear的[原配方](https://raw.githubusercontent.com/SilentChaos512/Silent-Gear/master/src/generated/resources/data/silentgear/recipe/nether_star_fragment.json)为一星拆九片；Mystical Agradditions的[原配方](https://raw.githubusercontent.com/BlakeBr0/MysticalAgradditions/26.1/src/main/resources/data/mysticalagradditions/recipe/nether_star.json)为三片合一星。2026-11-01检查时，本实例安装模组JAR内没有检出下界之星碎片标签。没有把猜测的`c:`或`forge:`命名包装成社区标准，也没有自动互换不同份量的碎片。整合包可以显式扩充Fusion标签。

## 槽位

四级取下表前四槽，五级取全部五槽；原版前三个装备等阶保留原始分配。

| 部位 | 轻甲 | 中甲 | 重甲 |
| --- | --- | --- | --- |
| 头 | I／II／III／IV／V | I／I／III／III／IV | I／I／II／II／III |
| 胸 | II／II／III／IV／V | I／II／III／IV／V | I／I／III／IV／V |
| 腿 | II／II／III／IV／V | I／II／III／IV／V | I／I／III／III／IV |
| 靴 | I／II／III／IV／V | I／II／II／III／IV | I／I／II／II／III |

Ⅳ／Ⅴ是注册到原生PerkSlot体系的真实数值。法力容量、法强、韧性、速度、抗性、持续时间等继续走原生数值公式，保留属性本身的上下限、原生重复丝线规则与效果适用条件。原版Whirlisprig饱和属性在Ⅲ后封顶；本次保持Ⅰ／Ⅱ／Ⅲ的0.3／0.6／1.0，在Ⅳ／Ⅴ延续为1.3／1.6。滑翔、不死等机制门槛保留，不凭空新增四／五级机制。

原版基础魔力和回复按真实装备等阶自行成长；Fusion可选等阶韧性以及反向融合的基础魔力转移也直接读取当前等阶。没有第二套隐藏等级或独立存档字段。

## 实现边界

- `ArmorLayouts`：共享四／五级槽位表与受支持原版物品识别。
- `HighArmorPlatform`：加载完成阶段注册物品、Ⅳ／Ⅴ槽以及扩展provider。Neo追加原生矩阵；Forge包装原provider，先保留可正常读取的原生／其他附属等阶，仅在原provider越界的四／五级补入新表。
- `HighArmorRecipe`：数据化`ars_fusion:armor_upgrade`序列化器，继承原生ArmorUpgradeRecipe，沿用其recipe type、魔源消费与结果升级方法。只限制本模组自己的配方，不限制其他模组的高阶配方。Forge提供自己的标准Ingredient JSON读取，避免旧原生序列化器的嵌套Ingredient格式差异。
- 改造台仍使用原生装卸和保存流程，固定“三根”上限改为实际槽数。客户端保留原生渲染器和材质，将额外物品行压缩到现有显示范围，复用相同的骨骼pivot，不索取不存在的`top_4`／`top_5`。Neo继续原生文字等级显示；Forge的旧渲染器使用三行模型变体，并会对超过三槽的数据执行越界subList，因此高阶时改为独立绘制紧凑罗马数字图例，保留原生物品展示。没有复制或修改原版方块模型、贴图或渲染实现。
- JEI继续使用Ars护甲升级页面；预览过滤不支持目的等阶的provider和本模组配方范围之外的物品。没有JEI硬依赖；Mixin插件先通过加载器的mod列表判断JEI是否存在，再应用对应客户端注入，同时使用Pseudo和Object-Coerce避免入口引用JEI接口。
- Neo原生手册的槽位示意图只有三列标头，保留其原始三级示意，避免新增provider导致列重叠。四／五级配方和装备数据由JEI、物品说明和改造台展示。

## 贴图与来源

两件物品均使用内置imagegen生成，再通过已有`pixel_assets.py`流程整理为真实16×16、硬透明、最多12色的PNG。原始大图保留在本地忽略的`art/pixel/work`中，不进入发行包或Git。

| 资源 | 最终贴图 | 实际生成提示词 | 导出记录 |
| --- | --- | --- | --- |
| 荒野精华 | [PNG](../common/src/main/resources/assets/ars_fusion/textures/item/wilden_essence.png) | [prompt](../art/pixel/prompts/wilden_essence.txt) | [record](../art/pixel/records/ars_fusion.wilden_essence.json) |
| 下界之星碎片 | [PNG](../common/src/main/resources/assets/ars_fusion/textures/item/nether_star_fragment.png) | [prompt](../art/pixel/prompts/nether_star_fragment.txt) | [record](../art/pixel/records/ars_fusion.nether_star_fragment.json) |

`tools/pixel_assets.py`与`requirements-pixel.txt`沿用本作者项目Goety Charms中的流程。两个7×7罗马数字槽位标记是代码定义的UI字形。没有复制Ars本体贴图；检查原生供物和槽图标时提取的参考文件只留在忽略的`build/inspect`内。

## 验证

新增两端原生路径GameTest源码：真实加载的升级配方／序列化器、十二件护甲的四／五级provider、正反融合升级保留、数值丝线读取Ⅳ／Ⅴ，以及改造台加入五根丝线、拒绝第六根、存储与网络数据往返。

按本次节约测试和内存的约定，只编译回归源码与构建发行包，没有启动游戏、服务端或GameTest。编译不代表上述运行时用例已经通过；尤其改造台实际布局、JEI页面与附属共存仍待人工验证。最终构建结果、静态注入核对和产物哈希见`high-armor-0.2.0-build.json`。
