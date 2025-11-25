# TN3270 Spring Boot API 项目 - 完整操作指南

## 📁 项目结构

```
tn3270-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── tn3270api/
│   │   │               ├── Tn3270ApiApplication.java       # 主应用类
│   │   │               ├── config/
│   │   │               │   └── Tn3270Properties.java       # 配置类
│   │   │               ├── controller/
│   │   │               │   └── Tn3270Controller.java       # REST 控制器
│   │   │               ├── service/
│   │   │               │   └── Tn3270Service.java          # 业务逻辑
│   │   │               ├── model/
│   │   │               │   └── Tn3270Session.java          # 会话模型
│   │   │               ├── dto/
│   │   │               │   ├── ApiResponse.java            # 响应封装
│   │   │               │   ├── ScreenResponse.java         # 屏幕响应
│   │   │               │   └── CommandRequest.java         # 命令请求
│   │   │               └── emulator/
│   │   │                   └── ExtendedEmulator.java       # 扩展模拟器
│   │   └── resources/
│   │       └── application.yml                             # 配置文件
│   └── test/
│       └── java/
└── pom.xml                                                 # Maven 配置
```

## 🔧 在 IDEA 中的详细操作步骤

### 步骤 1：导入文件到项目

1. **打开您的 IDEA 项目** `tn3270-api`

2. **创建包结构**：
   - 在左侧项目树中，右键点击 `src/main/java`
   - 选择 **New → Package**
   - 输入：`com.example.tn3270api`
   - 继续创建子包：
     - `com.example.tn3270api.config`
     - `com.example.tn3270api.controller`
     - `com.example.tn3270api.service`
     - `com.example.tn3270api.model`
     - `com.example.tn3270api.dto`
     - `com.example.tn3270api.emulator`

3. **复制文件到对应包**：
   - 将 `Tn3270ApiApplication.java` 放到 `com.example.tn3270api`
   - 将 `Tn3270Properties.java` 放到 `config` 包
   - 将 `Tn3270Controller.java` 放到 `controller` 包
   - 将 `Tn3270Service.java` 放到 `service` 包
   - 将 `Tn3270Session.java` 放到 `model` 包
   - 将 `ApiResponse.java`, `ScreenResponse.java`, `CommandRequest.java` 放到 `dto` 包
   - 将 `ExtendedEmulator.java` 放到 `emulator` 包

4. **放置配置文件**：
   - 右键点击 `src/main/resources`
   - 选择 **New → File**
   - 命名为 `application.yml`
   - 复制配置内容

5. **更新 pom.xml**：
   - 直接替换项目根目录的 `pom.xml` 文件

### 步骤 2：刷新 Maven 项目

1. 右键点击 `pom.xml`
2. 选择 **Maven → Reload Project**
3. 或者点击 IDEA 右上角的 Maven 刷新图标

### 步骤 3：启动应用

1. **找到主类**：
   - 打开 `Tn3270ApiApplication.java`

2. **运行应用**：
   - 方法 1：点击类名左侧的绿色三角按钮 ▶️，选择 **Run**
   - 方法 2：右键点击文件，选择 **Run 'Tn3270ApiApplication'**
   - 方法 3：使用快捷键 `Shift + F10`

3. **查看启动日志**：
   - 在 IDEA 底部的 **Run** 窗口查看日志
   - 看到 `Started Tn3270ApiApplication` 表示启动成功

### 步骤 4：测试 API

1. **使用 IDEA 内置 HTTP Client**：
   - 在项目根目录创建文件 `test-api.http`
   - 内容见下方 API 测试部分

2. **或使用 Postman**（推荐新手）：
   - 下载安装 Postman
   - 导入下方的 API 请求示例

## 📡 API 接口说明

### API 1: 登录（Login to TSO Main Menu）

**接口名称**: `POST /api/tn3270/session/login`

**功能**: 连接到主机，执行登录，直到显示 TSO 主菜单

**请求**:
```http
POST http://localhost:8080/api/tn3270/login
Content-Type: application/json
```

**响应**:
```json
{
  "success": true,
  "message": "登录成功",
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "data": {
    "screenLines": ["行01: ...", "行02: ...", ...],
    "fullScreen": "行01: ...\n行02: ...\n..."
  }
}
```

**保存 sessionId**，后续请求需要使用！

---

### API 2: 发送菜单命令（Send Menu Command）

**接口名称**: `POST /api/tn3270/menu/command`

**功能**: 向 TSO 主菜单发送命令（例如选项 2），然后按回车

**请求**:
```http
POST http://localhost:8080/api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: {从API1获取的sessionId}

{
  "command": "2"
}
```

**响应**:
```json
{
  "success": true,
  "message": "命令执行成功",
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "data": {
    "screenLines": ["行01: ...", "行02: ...", ...],
    "fullScreen": "..."
  }
}
```

---

### API 3: 执行 LOGOFF

**接口名称**: `POST /api/tn3270/logoff`

**功能**: 执行 LOGOFF 命令并关闭会话

**请求**:
```http
POST http://localhost:8080/api/tn3270/logoff
X-Session-Id: {sessionId}
```

**响应**:
```json
{
  "success": true,
  "message": "LOGOFF 成功",
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "data": {
    "screenLines": ["行01: ...", "行02: ...", ...],
    "fullScreen": "..."
  }
}
```

---

### 额外接口: 获取屏幕内容

**接口名称**: `GET /api/tn3270/screen`

**功能**: 获取当前屏幕内容（不发送任何命令）

**请求**:
```http
GET http://localhost:8080/api/tn3270/screen
X-Session-Id: {sessionId}
```

---

### 健康检查

**接口名称**: `GET /api/tn3270/health`

**功能**: 检查 API 是否运行正常

**请求**:
```http
GET http://localhost:8080/api/tn3270/health
```

## 🧪 API 测试示例

### 方法 1：使用 IDEA HTTP Client

在项目根目录创建 `test-api.http` 文件：

```http
### 1. 健康检查
GET http://localhost:8080/api/tn3270/health

### 2. 登录到 TSO 主菜单
POST http://localhost:8080/api/tn3270/login
Content-Type: application/json

# 注意：保存响应中的 sessionId

### 3. 发送菜单命令（将 {sessionId} 替换为实际值）
POST http://localhost:8080/api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: {sessionId}

{
  "command": "2"
}

### 4. 获取屏幕内容
GET http://localhost:8080/api/tn3270/screen
X-Session-Id: {sessionId}

### 5. 执行 LOGOFF
POST http://localhost:8080/api/tn3270/logoff
X-Session-Id: {sessionId}
```

### 方法 2：使用 curl 命令

```bash
# 1. 登录
curl -X POST http://localhost:8080/api/tn3270/login \
  -H "Content-Type: application/json"

# 2. 发送菜单命令（替换 SESSION_ID）
curl -X POST http://localhost:8080/api/tn3270/menu/command \
  -H "Content-Type: application/json" \
  -H "X-Session-Id: SESSION_ID" \
  -d '{"command":"2"}'

# 3. LOGOFF
curl -X POST http://localhost:8080/api/tn3270/logoff \
  -H "X-Session-Id: SESSION_ID"
```

## ⚙️ 配置说明

在 `application.yml` 中可以修改以下配置：

```yaml
server:
  port: 8080  # API 服务端口

tn3270:
  host: 127.0.0.1        # 大型机地址
  port: 3270             # TN3270 端口
  script-port: 4270      # ws3270 脚本端口
  username: HERC01       # 登录用户名
  password: CUL8TR       # 登录密码
  session-timeout: 300000 # 会话超时时间（毫秒）
```

## 🚨 常见问题

### 问题 1：Maven 依赖下载失败
**解决**：
1. 检查网络连接
2. 在 IDEA 中：File → Settings → Build → Maven
3. 配置国内镜像（阿里云）

### 问题 2：找不到 ws3270
**解决**：
1. 确保已安装 x3270 套件
2. Windows: 下载 wc3270
3. Linux: `sudo apt-get install x3270`

### 问题 3：端口 8080 被占用
**解决**：
在 `application.yml` 中修改端口：
```yaml
server:
  port: 8081  # 改为其他端口
```

### 问题 4：连接主机超时
**解决**：
1. 检查大型机是否运行
2. 检查防火墙设置
3. 增加超时时间

## 🔄 后续 React 集成

将来在 React 中调用示例：

```javascript
// 登录
const login = async () => {
  const response = await fetch('http://localhost:8080/api/tn3270/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    }
  });
  const data = await response.json();
  const sessionId = data.sessionId;
  // 保存 sessionId 到 state
  return sessionId;
};

// 发送命令
const sendCommand = async (sessionId, command) => {
  const response = await fetch('http://localhost:8080/api/tn3270/menu/command', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Session-Id': sessionId
    },
    body: JSON.stringify({ command })
  });
  return await response.json();
};

// LOGOFF
const logoff = async (sessionId) => {
  const response = await fetch('http://localhost:8080/api/tn3270/logoff', {
    method: 'POST',
    headers: {
      'X-Session-Id': sessionId
    }
  });
  return await response.json();
};
```

## 📝 下一步

1. ✅ 完成基础 API 开发
2. ⏳ 测试所有 API 接口
3. ⏳ 开发 React 前端
4. ⏳ 添加错误处理和日志
5. ⏳ 实现会话管理和超时处理

## 💡 提示

- 每次调用 API 后都会返回当前屏幕内容
- sessionId 在 LOGOFF 后会失效
- 建议在 React 中使用 Context 或 Redux 管理 sessionId
- 可以添加 WebSocket 支持实时屏幕更新

---

如有问题，请查看日志文件或联系开发者。
