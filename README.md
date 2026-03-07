# WooOmni

🍵一款现代化的 Minecraft 基础管理插件

## 特色

### 🧩 模块化架构
- **独立模块**：每个功能独立模块，可单独开关
- **热重载**：支持运行时热重载单个模块
- **统一存储**：全局存储配置，简化管理

### ⚡ 性能优化
- **按需监听**：禁用的模块不注册事件监听器
- **异步处理**：数据库操作在异步线程执行
- **智能缓存**：Guava Cache 管理，自动过期回收
- **自动保存**：定时保存数据，防止意外丢失
- **脏数据检测**：仅保存变更的数据

### 🔐 安全特性
- **细粒度权限**：完整的权限节点控制
- **Bypass 权限**：模块禁用时仍可使用基本功能

### 📊 数据持久化
- **双存储支持**：SQLite / MySQL 自由切换
- **HikariCP 连接池**：高性能数据库连接
- **离线玩家支持**：支持查看和编辑离线玩家数据

## 环境

- Minecraft 1.21+
- Java 21+
- Paper 核心（推荐）

## 命令

### 主命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/wooomni` `/omni` `/wo` | 主命令 | `wooomni.use` |
| `/omni help` | 查看帮助 | `wooomni.use` |
| `/omni reload [模块]` | 重载配置 | `wooomni.reload` |

### 飞行模块

| 命令 | 描述 | 权限 |
|------|------|------|
| `/fly` | 切换自己的飞行状态 | `wooomni.fly` |
| `/fly <玩家>` | 切换其他玩家的飞行状态 | `wooomni.fly.others` |
| `/flyspeed <速度>` | 设置自己的飞行速度 (0.1-10) | `wooomni.fly.speed` |
| `/flyspeed <速度> <玩家>` | 设置其他玩家的飞行速度 | `wooomni.fly.speed` + `wooomni.fly.others` |

### 无敌模块

| 命令 | 描述 | 权限 |
|------|------|------|
| `/god` | 切换自己的无敌状态 | `wooomni.god` |
| `/god <玩家>` | 切换其他玩家的无敌状态 | `wooomni.god.others` |

### 隐身模块

| 命令 | 描述 | 权限 |
|------|------|------|
| `/vanish` | 切换自己的隐身状态 | `wooomni.vanish` |
| `/vanish <玩家>` | 切换其他玩家的隐身状态 | `wooomni.vanish.others` |
| `/vanishlist` | 查看隐身玩家列表 | `wooomni.vanish.list` |
| `/vanishedit` | 打开自己的隐身设置GUI | `wooomni.vanish.edit` |
| `/vanishedit <玩家>` | 打开其他玩家的隐身设置GUI | `wooomni.vanish.edit.other` |

### 背包模块

| 命令 | 描述 | 权限 |
|------|------|------|
| `/inv <玩家>` `/inventory <玩家>` | 查看玩家的背包 | `wooomni.inv.view` |
| `/ender` | 打开自己的末影箱 | `wooomni.ender.view` |
| `/ender <玩家>` `/enderchest <玩家>` | 查看玩家的末影箱 | `wooomni.ender.view` |

## 模块功能

### 飞行模块 (Fly)

| 功能 | 描述 |
|------|------|
| 飞行状态持久化 | 重启服务器后保持飞行状态 |
| 飞行速度控制 | 支持 0.1-10 的速度范围 |
| 离线玩家支持 | 保存离线玩家的飞行数据 |

### 无敌模块 (God)

| 功能 | 描述 |
|------|------|
| 无敌状态持久化 | 重启服务器后保持无敌状态 |
| 伤害免疫 | 自动取消所有伤害事件 |
| 饥饿保护 | 无敌时饥饿值不下降 |
| 氧气保护 | 水下自动恢复氧气值 |

### 隐身模块 (Vanish)

| 功能 | 描述 |
|------|------|
| 完全隐身 | 其他玩家无法看见 |
| Tab列表隐藏 | 从在线玩家列表中隐藏 |
| BossBar提示 | 显示当前隐身状态 |
| 夜视效果 | 隐身时自动获得夜视 |
| 静默开箱 | 无声音、无动画打开容器，支持编辑 |
| 生物忽略 | 怪物不会发现隐身玩家 |
| 不计入刷怪机制 | 隐身玩家不影响周围怪物生成计数 |
| 假消息 | 隐藏加入/退出消息 |
| 自动隐身 | 特定权限玩家自动隐身加入 |

### 背包模块 (Inventory)

| 功能 | 描述 |
|------|------|
| 在线/离线背包查看 | 支持查看离线玩家背包 |
| 在线/离线末影箱 | 支持查看离线玩家末影箱 |
| 完整物品数据保存 | 使用 Paper API 保存附魔、Lore 等完整数据 |
| 视图切换 | 背包/末影箱快速切换 |
| 玩家信息 | 显示玩家详细数据（IP、在线时长、死亡次数等） |
| 批量操作 | 复制、清空背包 |

## 隐身设置GUI

通过 `/vanishedit` 打开隐身设置界面，可配置以下选项：

| 选项 | 描述 |
|------|------|
| 隐身状态 | 开启/关闭隐身 |
| 夜视效果 | 隐身时是否获得夜视 |
| 禁止拾起物品 | 是否禁止拾起地上物品 |
| 禁止受伤 | 是否禁止受到伤害 |
| 禁止攻击 | 是否禁止伤害其他玩家 |
| 禁用物理碰撞 | 是否禁用物理碰撞 |
| 静默开箱 | 打开容器是否静默 |
| 不计入刷怪机制 | 是否不参与怪物生成计数 |
| 隐藏登入消息 | 是否隐藏加入消息 |
| 隐藏登出消息 | 是否隐藏退出消息 |
| BOSSBAR提示 | 是否显示BossBar |
| Tab列表隐藏 | 是否从Tab列表隐藏 |
| 自动隐身加入 | 加入服务器时自动隐身 |

## 权限

### 基础权限

| 权限 | 描述 | 默认 |
|------|------|------|
| `wooomni.use` | 基础使用权限 | true |
| `wooomni.admin` | 管理员权限 | op |
| `wooomni.reload` | 重载权限 | op |

### 飞行权限

| 权限 | 描述 | 默认 |
|------|------|------|
| `wooomni.fly` | 使用飞行命令 | op |
| `wooomni.fly.others` | 对他人使用飞行 | op |
| `wooomni.fly.speed` | 设置飞行速度 | op |
| `wooomni.fly.bypass.disabled` | 模块禁用时仍可使用 | op |

### 无敌权限

| 权限 | 描述 | 默认 |
|------|------|------|
| `wooomni.god` | 使用无敌命令 | op |
| `wooomni.god.others` | 对他人使用无敌 | op |
| `wooomni.god.bypass.disabled` | 模块禁用时仍可使用 | op |

### 隐身权限

| 权限 | 描述 | 默认 |
|------|------|------|
| `wooomni.vanish` | 使用隐身命令 | op |
| `wooomni.vanish.others` | 对他人使用隐身 | op |
| `wooomni.vanish.list` | 查看隐身列表 | op |
| `wooomni.vanish.see` | 看见隐身玩家 | op |
| `wooomni.vanish.edit` | 编辑隐身设置 | op |
| `wooomni.vanish.edit.other` | 编辑他人隐身设置 | op |
| `wooomni.vanish.autojoin` | 自动隐身加入 | op |
| `wooomni.vanish.bypass.disabled` | 模块禁用时仍可使用 | op |

### 背包权限

| 权限 | 描述 | 默认 |
|------|------|------|
| `wooomni.inv.view` | 查看玩家背包 | op |
| `wooomni.inv.edit` | 编辑玩家背包 | op |
| `wooomni.ender.view` | 查看玩家末影箱 | op |
| `wooomni.ender.edit` | 编辑玩家末影箱 | op |


## API 使用示例

```java
// 获取模块管理器
ModuleManager moduleManager = plugin.getModuleManager();

// 获取飞行模块
FlyModule flyModule = (FlyModule) moduleManager.getModule("fly");
FlyDataManager dataManager = flyModule.getDataManager();

// 获取玩家飞行数据
FlyData flyData = dataManager.getFlyData(player.getUniqueId());
boolean isFlying = flyData.isFlying();

// 监听状态变更事件
@EventHandler
public void onFlyChange(FlyStatusChangeEvent event) {
    UUID uuid = event.getPlayerUUID();
    boolean newState = event.getNewState();
    Player initiator = event.getInitiator();
    // 你的逻辑
}

@EventHandler
public void onGodChange(GodStatusChangeEvent event) {
    // 处理无敌状态变更
}

@EventHandler
public void onVanishChange(VanishStatusChangeEvent event) {
    // 处理隐身状态变更
}
```

❤️ 主包是开发新手，如果有做得不好的地方，欢迎指正。希望能和大家一起交流！

⭐ 觉得有用请给个 Star 爱你哟
