# 🔄 项目更新 - 新增第2级菜单专用 API

## 更新内容

新增 **API4: 进入第2级菜单**，固定在第4行第13列输入"2"。

---

## 📥 需要更新的文件

### 1. Tn3270Service.java
**位置**: `src/main/java/com/example/tn3270api/service/Tn3270Service.java`

**下载**: [Tn3270Service.java](computer:///mnt/user-data/outputs/Tn3270Service.java)

**变更**: 添加了 `enterSecondLevelMenu()` 方法

### 2. Tn3270Controller.java
**位置**: `src/main/java/com/example/tn3270api/controller/Tn3270Controller.java`

**下载**: [Tn3270Controller.java](computer:///mnt/user-data/outputs/Tn3270Controller.java)

**变更**: 添加了 `POST /api/tn3270/menu/enter-level2` 接口

### 3. test-api-updated.http
**位置**: 项目根目录 `test-api-updated.http`（新文件）

**下载**: [test-api-updated.http](computer:///mnt/user-data/outputs/test-api-updated.http)

**用途**: 更新后的 API 测试文件

### 4. API4_GUIDE.md
**位置**: 文档目录（可选）

**下载**: [API4_GUIDE.md](computer:///mnt/user-data/outputs/API4_GUIDE.md)

**用途**: 新 API 的详细说明文档

---

## 🔧 如何更新

### 方法 1：直接替换文件（推荐）

1. **备份原文件**（以防万一）
   ```
   备份 Tn3270Service.java
   备份 Tn3270Controller.java
   ```

2. **下载并替换**
   - 下载上面的 `Tn3270Service.java`，替换原文件
   - 下载上面的 `Tn3270Controller.java`，替换原文件

3. **添加测试文件**
   - 下载 `test-api-updated.http`，放到项目根目录

4. **刷新项目**
   - 在 IDEA 中：Build → Rebuild Project

5. **重启应用**
   - 停止当前运行的应用
   - 重新运行 `Tn3270ApiApplication`

### 方法 2：手动添加代码

如果您想手动添加，请查看 [API4_GUIDE.md](computer:///mnt/user-data/outputs/API4_GUIDE.md) 中的"代码实现细节"部分。

---

## 🧪 测试新 API

### 使用 IDEA HTTP Client

1. 打开 `test-api-updated.http`
2. 执行以下步骤：

```http
### 1. 登录
POST http://localhost:8080/api/tn3270/login

### 2. 使用新 API 进入第2级菜单
POST http://localhost:8080/api/tn3270/menu/enter-level2
X-Session-Id: YOUR_SESSION_ID
```

### 使用 curl

```bash
# 1. 登录
curl -X POST http://localhost:8080/api/tn3270/login

# 2. 进入第2级菜单（替换 YOUR_SESSION_ID）
curl -X POST http://localhost:8080/api/tn3270/menu/enter-level2 \
  -H "X-Session-Id: YOUR_SESSION_ID"
```

---

## 📋 新 API 详情

### API 4: 进入第2级菜单

**接口**: `POST /api/tn3270/menu/enter-level2`

**功能**: 固定在第4行第13列输入"2"并按回车

**请求**:
```http
POST /api/tn3270/menu/enter-level2
X-Session-Id: {sessionId}
```

**响应**:
```json
{
  "success": true,
  "message": "成功进入第2级菜单",
  "sessionId": "your-session-id",
  "data": {
    "screenLines": [...],
    "fullScreen": "..."
  }
}
```

**特点**:
- ✅ 不需要 Body
- ✅ 位置写死（第4行第13列）
- ✅ 命令写死（"2"）
- ✅ 专门用于进入第2级菜单

---

## 🆚 API 对比

### 原有的 API2（通用菜单命令）
```http
POST /api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: {sessionId}

{
  "command": "2"
}
```
- 需要 Body 指定命令
- 灵活，可以发送任何命令

### 新增的 API4（第2级菜单专用）
```http
POST /api/tn3270/menu/enter-level2
X-Session-Id: {sessionId}
```
- 不需要 Body
- 固定位置、固定命令
- 更简单、更专用

---

## 📚 完整 API 列表

更新后，您的项目共有 **5 个 API**：

1. **API1**: `POST /api/tn3270/login` - 登录
2. **API2**: `POST /api/tn3270/menu/command` - 通用菜单命令
3. **API3**: `POST /api/tn3270/logoff` - 登出
4. **API4**: `POST /api/tn3270/menu/enter-level2` - 进入第2级菜单（新增）
5. **额外**: `GET /api/tn3270/screen` - 获取屏幕内容

---

## ✅ 更新检查清单

更新完成后，检查以下内容：

- [ ] `Tn3270Service.java` 已更新
- [ ] `Tn3270Controller.java` 已更新
- [ ] 项目能成功编译（无红色错误）
- [ ] 应用能成功启动
- [ ] 新 API 能成功调用
- [ ] 返回了正确的屏幕内容

---

## 💡 提示

1. **如果您需要为其他固定位置的操作创建专用 API**，可以参照这个模式：
   - 在 Service 中创建专用方法
   - 在 Controller 中创建对应接口
   - 固定写死位置和命令

2. **API2 和 API4 的选择**：
   - 用 API2：需要灵活发送不同命令时
   - 用 API4：专门进入第2级菜单时（更简单）

---

## 🆘 如果遇到问题

1. 确认文件替换正确
2. 确认项目编译无误（Build → Rebuild Project）
3. 查看 IDEA Run 窗口的启动日志
4. 测试 API 时查看日志中的详细信息

---

**推荐阅读**: [API4_GUIDE.md](computer:///mnt/user-data/outputs/API4_GUIDE.md) 获取更详细的说明和示例。
