# 🆕 快速更新：新增字符串输入 API

## ✅ 新增内容

新增 **API8**：在当前光标位置输入字符串，**不需要指定行列坐标**。

---

## 📥 需要的文件

### 1. StringRequest.java（新增）
**下载**: [StringRequest.java](computer:///mnt/user-data/outputs/StringRequest.java)

**位置**: `src/main/java/com/example/tn3270api/dto/StringRequest.java`

**作用**: 字符串请求 DTO

### 2. Tn3270Service-v4.java（更新）
**下载**: [Tn3270Service-v4.java](computer:///mnt/user-data/outputs/Tn3270Service-v4.java)

**重命名**: `Tn3270Service.java`

**位置**: `src/main/java/com/example/tn3270api/service/Tn3270Service.java`

**新增**: `sendString(sessionId, text)` 方法

### 3. Tn3270Controller-v4.java（更新）
**下载**: [Tn3270Controller-v4.java](computer:///mnt/user-data/outputs/Tn3270Controller-v4.java)

**重命名**: `Tn3270Controller.java`

**位置**: `src/main/java/com/example/tn3270api/controller/Tn3270Controller.java`

**新增**: API8 接口

### 4. test-api-complete.http（测试）
**下载**: [test-api-complete.http](computer:///mnt/user-data/outputs/test-api-complete.http)

**位置**: 项目根目录

### 5. API8_STRING_GUIDE.md（文档）
**下载**: [API8_STRING_GUIDE.md](computer:///mnt/user-data/outputs/API8_STRING_GUIDE.md)

---

## 🎯 API8 使用方式

### 接口

```http
POST http://localhost:8080/api/tn3270/send/string
Content-Type: application/json
X-Session-Id: YOUR_SESSION_ID

{
  "text": "Hello"
}
```

### 特点

- ✅ **不需要指定行列** - 只需要字符串
- ✅ **在当前光标位置输入** - 光标在哪就输入到哪
- ✅ **配合 Tab 键完美** - Tab 跳字段，API8 输入内容

---

## 💡 典型使用场景

### 场景：配合 Tab 填写表单

```http
### 1. 定位到第一个字段（使用 API2）
POST /api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: abc-123

{
  "row": 5,
  "column": 10,
  "command": "FirstValue"
}

### 2. Tab 到第二个字段
POST /api/tn3270/key/tab
X-Session-Id: abc-123

### 3. 使用 API8 输入（不需要知道坐标）✨
POST /api/tn3270/send/string
Content-Type: application/json
X-Session-Id: abc-123

{
  "text": "SecondValue"
}

### 4. Tab 到第三个字段
POST /api/tn3270/key/tab
X-Session-Id: abc-123

### 5. 继续用 API8 输入 ✨
POST /api/tn3270/send/string
Content-Type: application/json
X-Session-Id: abc-123

{
  "text": "ThirdValue"
}

### 6. 回车提交
POST /api/tn3270/key/enter
X-Session-Id: abc-123
```

---

## 🆚 API2 vs API8

| 特性 | API2 | API8 |
|------|------|------|
| 路径 | `/menu/command` | `/send/string` |
| 参数 | row, column, command | text |
| 定位 | 指定精确坐标 | 当前光标位置 |
| 适用 | 需要定位时 | 配合 Tab 时 |

---

## 💻 JavaScript 示例

```javascript
// API8: 在当前位置输入字符串
const sendString = async (sessionId, text) => {
  const response = await fetch('http://localhost:8080/api/tn3270/send/string', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Session-Id': sessionId
    },
    body: JSON.stringify({ text })
  });
  return await response.json();
};

// 使用示例：填写多个字段
const sessionId = 'abc-123';

// 假设光标已在第一个字段
await sendString(sessionId, 'Field1');

// Tab 并输入
await sendTab(sessionId);
await sendString(sessionId, 'Field2');

await sendTab(sessionId);
await sendString(sessionId, 'Field3');

// 提交
await sendEnter(sessionId);
```

---

## 🔧 更新步骤

1. **添加新文件**: `StringRequest.java`
2. **替换旧文件**: `Tn3270Service.java` 和 `Tn3270Controller.java`
3. **重新构建**: Build → Rebuild Project
4. **重启应用**: 停止并重新运行

---

## 📊 完整 API 列表（9个）

| API | 路径 | 参数 | 说明 |
|-----|------|------|------|
| API1 | `/login` | 无 | 登录 |
| API2 | `/menu/command` | row, column, command | 指定位置输入 |
| API3 | `/logoff` | 无 | 登出 |
| API5 | `/key/enter` | 无 | 回车键 |
| API6 | `/key/reset` | 无 | Reset |
| API7 | `/key/tab` | 无 | Tab 键 |
| **API8** | `/send/string` | **text** | **当前位置输入** ✨ |
| 额外 | `/screen` | 无 | 获取屏幕 |

---

## ✅ 优势

1. **更简单** - 不需要计算坐标
2. **更灵活** - 配合 Tab 自然导航
3. **更高效** - 快速填写多字段
4. **更直观** - 符合人类操作习惯

---

## 🎯 推荐用法

```
第一个字段: API2 (精确定位)
后续字段: API7 (Tab) + API8 (输入) 循环
最后: API5 (回车提交)
```

---

**详细说明**: [API8_STRING_GUIDE.md](computer:///mnt/user-data/outputs/API8_STRING_GUIDE.md)

现在您有 **9 个强大的 API** 可以使用！🚀
