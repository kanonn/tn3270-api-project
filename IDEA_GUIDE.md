# IntelliJ IDEA 完整操作步骤

## 📦 项目包含的内容

您已经获得了一个完整的 Spring Boot 项目压缩包 `tn3270-api-project.zip`，包含：

- ✅ 完整的项目结构
- ✅ 所有 Java 源代码
- ✅ Maven 配置文件 (pom.xml)
- ✅ 配置文件 (application.yml)
- ✅ API 测试文件 (test-api.http)
- ✅ 详细文档 (README.md, QUICKSTART.md)

## 🎯 在 IDEA 中导入和运行项目

### 第一步：解压项目

1. **下载** `tn3270-api-project.zip` 文件
2. **解压** 到您想要的位置，例如：
   - Windows: `C:\Projects\tn3270-api-project`
   - Mac/Linux: `~/Projects/tn3270-api-project`

### 第二步：在 IDEA 中打开项目

1. **启动 IntelliJ IDEA**

2. **打开项目**：
   - 方法 A：如果是首次打开 IDEA
     - 点击 **Open** 按钮
   
   - 方法 B：如果 IDEA 已经打开
     - 点击 **File → Open**
   
3. **选择项目文件夹**：
   - 浏览到解压后的 `tn3270-api-project` 文件夹
   - **重要**：选择包含 `pom.xml` 的文件夹
   - 点击 **OK**

4. **等待项目加载**：
   - IDEA 会自动识别这是一个 Maven 项目
   - 右下角会显示 "Importing Maven Projects..."
   - 第一次导入会下载依赖，可能需要几分钟
   - 完成后右下角会显示 "Maven Import Finished"

### 第三步：配置 JDK（如果需要）

1. **检查 JDK 配置**：
   - 点击 **File → Project Structure** (快捷键: `Ctrl+Alt+Shift+S`)
   - 左侧选择 **Project**
   - 确保 **Project SDK** 设置为 JDK 8 或更高版本
   
2. **如果没有 JDK**：
   - 点击 **Project SDK** 下拉框
   - 选择 **Add SDK → Download JDK**
   - 选择版本（推荐 JDK 11 或 17）
   - 点击 **Download**

3. **点击 OK** 保存设置

### 第四步：检查项目结构

在左侧项目树中，您应该看到：

```
tn3270-api-project
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.example.tn3270api
│   │   │       ├── Tn3270ApiApplication.java  ← 主程序
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── model/
│   │   │       ├── dto/
│   │   │       └── emulator/
│   │   └── resources
│   │       └── application.yml                ← 配置文件
│   └── test
├── pom.xml                                    ← Maven 配置
├── test-api.http                              ← API 测试
├── README.md                                  ← 详细文档
└── QUICKSTART.md                              ← 快速开始
```

### 第五步：修改配置（如果需要）

1. **打开配置文件**：
   - 在项目树中找到 `src/main/resources/application.yml`
   - 双击打开

2. **修改连接信息**（如果您的设置不同）：
   ```yaml
   tn3270:
     host: 127.0.0.1      # 大型机地址
     port: 3270           # TN3270 端口
     script-port: 4270    # ws3270 端口
     username: HERC01     # 用户名
     password: CUL8TR     # 密码
   ```

3. **保存文件**: `Ctrl+S` (Windows) 或 `Cmd+S` (Mac)

### 第六步：运行项目

1. **找到主程序**：
   - 展开 `src/main/java/com/example/tn3270api`
   - 找到 `Tn3270ApiApplication.java`
   - 双击打开

2. **运行应用** - 有三种方式：

   **方式 A：使用绿色箭头**
   - 在类名 `Tn3270ApiApplication` 左侧有一个绿色箭头 ▶️
   - 点击它，选择 **Run 'Tn3270ApiApplication'**

   **方式 B：右键菜单**
   - 在文件编辑区域右键点击
   - 选择 **Run 'Tn3270ApiApplication.main()'**

   **方式 C：快捷键**
   - 确保光标在文件中
   - 按 `Shift+F10` (Windows) 或 `Control+R` (Mac)

3. **查看启动日志**：
   - IDEA 底部会自动打开 **Run** 窗口
   - 您会看到 Spring Boot 的启动日志
   - 看到以下信息表示启动成功：
     ```
     Started Tn3270ApiApplication in X.XXX seconds
     ```

### 第七步：测试 API

#### 方法 1：使用 IDEA HTTP Client（推荐）

1. **打开测试文件**：
   - 在项目树中找到 `test-api.http`
   - 双击打开

2. **执行第一个测试 - 健康检查**：
   - 找到这一行：
     ```
     GET http://localhost:8080/api/tn3270/health
     ```
   - 点击左侧的绿色箭头 ▶️
   - 在底部会显示响应结果

3. **执行登录测试**：
   - 找到：
     ```
     POST http://localhost:8080/api/tn3270/login
     ```
   - 点击左侧的绿色箭头 ▶️
   - **重要**：在响应中找到 `sessionId`，复制它

4. **使用 sessionId 执行命令**：
   - 找到：
     ```
     POST http://localhost:8080/api/tn3270/menu/command
     X-Session-Id: YOUR_SESSION_ID
     ```
   - 将 `YOUR_SESSION_ID` 替换为刚才复制的真实 sessionId
   - 点击绿色箭头执行

#### 方法 2：使用浏览器（仅限 GET 请求）

1. 打开浏览器
2. 访问：`http://localhost:8080/api/tn3270/health`
3. 应该看到 JSON 响应

#### 方法 3：使用 Postman

1. **下载安装 Postman**：https://www.postman.com/downloads/

2. **测试登录**：
   - 创建新请求
   - 方法：POST
   - URL：`http://localhost:8080/api/tn3270/login`
   - 点击 **Send**
   - 复制响应中的 `sessionId`

3. **测试发送命令**：
   - 创建新请求
   - 方法：POST
   - URL：`http://localhost:8080/api/tn3270/menu/command`
   - Headers 添加：
     - Key: `X-Session-Id`
     - Value: `{您的sessionId}`
     - Key: `Content-Type`
     - Value: `application/json`
   - Body 选择 **raw** 和 **JSON**，输入：
     ```json
     {
       "command": "2"
     }
     ```
   - 点击 **Send**

### 第八步：查看日志

1. **查看控制台日志**：
   - IDEA 底部的 **Run** 窗口显示实时日志
   - 您可以看到：
     - 连接状态
     - 命令执行
     - 屏幕内容
     - 错误信息

2. **日志级别**：
   - `INFO`：正常操作信息
   - `DEBUG`：详细调试信息
   - `ERROR`：错误信息

### 第九步：停止应用

1. **停止运行**：
   - 在 Run 窗口顶部，点击红色停止按钮 ⬛
   - 或按快捷键 `Ctrl+F2`

2. **应用会自动清理所有会话连接**

## 🎨 IDEA 使用技巧

### 快捷键

- `Ctrl+Space`：代码补全
- `Ctrl+/`：注释/取消注释
- `Ctrl+Alt+L`：格式化代码
- `Shift+F10`：运行
- `Ctrl+F2`：停止
- `Ctrl+F9`：编译项目

### 查看 Maven 依赖

1. 右侧边栏点击 **Maven**
2. 展开 `Dependencies` 查看所有依赖库
3. 如果依赖下载失败，右键点击项目 → **Reload Project**

### 代码导航

- `Ctrl+B` 或 `Ctrl+Click`：跳转到定义
- `Ctrl+Alt+B`：跳转到实现
- `Ctrl+N`：查找类
- `Ctrl+Shift+N`：查找文件

## 🔍 故障排查

### 问题 1：Maven 依赖下载失败

**症状**：pom.xml 文件有红色波浪线，或者右侧有红色感叹号

**解决方案**：
1. 检查网络连接
2. 右键 `pom.xml` → **Maven → Reimport**
3. 或配置阿里云镜像：
   - File → Settings → Build, Execution, Deployment → Maven
   - User settings file → Edit
   - 添加阿里云镜像配置

### 问题 2：找不到 Java 类或包

**症状**：import 语句显示红色

**解决方案**：
1. File → Invalidate Caches → Invalidate and Restart
2. 等待 IDEA 重建索引

### 问题 3：端口 8080 被占用

**症状**：启动时报错 "Port 8080 is already in use"

**解决方案**：
在 `application.yml` 中修改端口：
```yaml
server:
  port: 8081  # 改成其他端口
```

### 问题 4：无法连接到大型机

**症状**：登录 API 返回连接失败

**检查清单**：
- ✅ 大型机是否正在运行
- ✅ IP 地址和端口是否正确
- ✅ 防火墙是否允许连接
- ✅ ws3270 或 s3270 是否已安装

## 📝 下一步建议

1. ✅ **熟悉项目结构**：
   - 阅读 `README.md` 了解详细架构
   - 查看各个 Java 类的代码和注释

2. ✅ **测试所有 API**：
   - 使用 `test-api.http` 完整测试流程
   - 理解每个 API 的功能

3. ✅ **修改和扩展**：
   - 尝试添加新的 API 接口
   - 修改业务逻辑

4. ✅ **准备 React 开发**：
   - 确保 API 工作正常
   - 理解 sessionId 的使用方式
   - 准备 API 文档给前端开发使用

## 💡 重要提示

- **sessionId 必须保存**：每次登录后返回的 sessionId 是后续所有操作的凭证
- **会话会超时**：默认 5 分钟不使用会自动清理
- **先测试再开发**：确保 API 正常工作后再进行 React 开发
- **查看日志**：遇到问题先查看 Run 窗口的日志

---

祝您开发顺利！如有问题，请查看详细文档或在 IDEA 的 Run 窗口查看日志。
