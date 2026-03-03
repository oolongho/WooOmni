# WooOmni

🍵一款高性能、模块化的 Minecraft 基础管理插件

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

### 🔧 已实现功能

#### 飞行模块 (Fly)
- `/fly` - 切换飞行状态
- `/fly <玩家>` - 切换其他玩家飞行状态
- `/flyspeed <1-10>` - 设置飞行速度
- `/flyspeed <速度> <玩家>` - 设置其他玩家飞行速度
- 状态持久化保存

#### 无敌模块 (God)
- `/god` - 切换无敌状态
- `/god <玩家>` - 切换其他玩家无敌状态
- 自动取消伤害事件
- 状态持久化保存

## 环境

- Minecraft 1.21+
- Java 21+
- Paper 核心（推荐）

## 命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/wooomni` | 主命令 | `wooomni.use` |
| `/wooomni reload [模块]` | 重载配置 | `wooomni.reload` |
| `/fly [玩家]` | 切换飞行状态 | `wooomni.fly` |
| `/flyspeed <速度> [玩家]` | 设置飞行速度 | `wooomni.fly.speed` |
| `/god [玩家]` | 切换无敌状态 | `wooomni.god` |

## 权限

| 权限 | 描述 | 默认 |
|------|------|------|
| `wooomni.use` | 基础使用权限 | true |
| `wooomni.reload` | 重载权限 | op |
| `wooomni.fly` | 飞行命令权限 | op |
| `wooomni.fly.others` | 对他人使用飞行权限 | op |
| `wooomni.fly.speed` | 飞行速度权限 | op |
| `wooomni.fly.bypass.disabled` | 绕过模块禁用 | op |
| `wooomni.god` | 无敌命令权限 | op |
| `wooomni.god.others` | 对他人使用无敌权限 | op |
| `wooomni.god.bypass.disabled` | 绕过模块禁用 | op |


❤️ 主包是开发新手，如果有做得不好的地方，欢迎指正。希望能和大家一起交流！

⭐ 觉得有用请给个 Star 爱你哟
