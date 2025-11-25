# 快速开始 - TN3270 Spring Boot API

## 🚀 5分钟快速启动

### 前提条件
- ✅ 已安装 JDK 8 或更高版本
- ✅ 已安装 IntelliJ IDEA
- ✅ 已安装 ws3270 (Windows) 或 x3270 (Linux/Mac)
- ✅ 大型机正在运行 (127.0.0.1:3270)

### 步骤 1: 在 IDEA 中打开项目

1. 打开 IntelliJ IDEA
2. 选择 **File → Open**
3. 选择 `tn3270-api-project` 文件夹
4. 点击 **OK**
5. 等待 IDEA 加载项目和下载依赖（右下角会显示进度）

### 步骤 2: 配置连接信息（如需要）

打开 `src/main/resources/application.yml`，修改配置：

```yaml
tn3270:
  host: 127.0.0.1      # 大型机地址
  port: 3270           # TN3270 端口
  username: HERC01     # 用户名
  password: CUL8TR     # 密码
```

### 步骤 3: 运行应用

1. 在 IDEA 中找到 `Tn3270ApiApplication.java`
2. 右键点击文件
3. 选择 **Run 'Tn3270ApiApplication'**
4. 看到以下日志表示启动成功：
   ```
   Started Tn3270ApiApplication in X.XXX seconds
   ```

### 步骤 4: 测试 API

#### 方法 A: 使用 IDEA 内置 HTTP Client

1. 打开项目根目录的 `test-api.http` 文件
2. 点击每个请求旁边的绿色箭头 ▶️ 执行请求
3. 查看响应结果

#### 方法 B: 使用浏览器

访问健康检查接口：
```
http://localhost:8080/api/tn3270/health
```

#### 方法 C: 使用 curl

```bash
# 登录
curl -X POST http://localhost:8080/api/tn3270/login

# 注意保存返回的 sessionId
```

## 📋 API 接口快速参考

### 1. 登录
```
POST http://localhost:8080/api/tn3270/login
```

### 2. 发送菜单命令
```
POST http://localhost:8080/api/tn3270/menu/command
Headers: X-Session-Id: {sessionId}
Body: {"command": "2"}
```

### 3. 执行 LOGOFF
```
POST http://localhost:8080/api/tn3270/logoff
Headers: X-Session-Id: {sessionId}
```

## 🔍 如何测试完整流程

1. **第一步：登录**
   - 请求：`POST /api/tn3270/login`
   - 保存响应中的 `sessionId`

2. **第二步：发送命令**
   - 请求：`POST /api/tn3270/menu/command`
   - 在 Header 中添加：`X-Session-Id: {你的sessionId}`
   - Body: `{"command": "2"}`

3. **第三步：查看屏幕**
   - 请求：`GET /api/tn3270/screen`
   - Header: `X-Session-Id: {你的sessionId}`

4. **第四步：退出**
   - 请求：`POST /api/tn3270/logoff`
   - Header: `X-Session-Id: {你的sessionId}`

## 🐛 常见问题

### 问题1: 端口 8080 已被占用
**解决**: 修改 `application.yml` 中的端口号
```yaml
server:
  port: 8081  # 改成其他端口
```

### 问题2: Maven 依赖下载慢
**解决**: 配置阿里云镜像
1. File → Settings → Build → Maven
2. 在 User settings file 中添加阿里云镜像配置

### 问题3: 找不到 ws3270
**解决**: 
- Windows: 下载并安装 wc3270
- Linux: `sudo apt-get install x3270`
- Mac: `brew install x3270`

### 问题4: 连接主机失败
**检查**:
- 大型机是否正在运行
- 端口 3270 是否开放
- IP 地址是否正确

## 📚 详细文档

完整文档请查看 `README.md`

## 💡 提示

- **sessionId 很重要**: 每次登录后都要保存 sessionId
- **会话有超时**: 默认 5 分钟，可在配置文件中修改
- **日志查看**: 在 IDEA 底部 Run 窗口查看详细日志
- **API 响应**: 所有 API 都返回统一的 JSON 格式

## 🎯 下一步

1. ✅ 测试所有 API 接口
2. ⏳ 开发 React 前端界面
3. ⏳ 添加更多功能（如文件传输、屏幕刷新等）

---

祝您使用愉快！如有问题，请查看详细文档或检查日志。
