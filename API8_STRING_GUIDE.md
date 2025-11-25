# 🆕 API8: 发送字符串（不指定位置）

## 📋 新增功能

新增 **API8**，允许在**当前光标位置**输入字符串，无需指定行列坐标。

---

## 🎯 API8 详情

### 接口信息

**方法**: `POST`  
**路径**: `/api/tn3270/send/string`  
**功能**: 在当前光标位置输入字符串

### 请求

**Headers**:
- `X-Session-Id`: 会话ID（必填）
- `Content-Type`: application/json

**Body**:
```json
{
  "text": "Hello"
}
```

### 响应

```json
{
  "success": true,
  "message": "字符串输入成功",
  "sessionId": "...",
  "data": {
    "screenLines": [...],
    "fullScreen": "..."
  }
}
```

---

## 🆚 API2 vs API8 对比

### API2: 指定位置输入

```http
POST /api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: YOUR_SESSION_ID

{
  "row": 4,
  "column": 13,
  "command": "2"
}
```

**特点**:
- ✅ 需要指定行和列
- ✅ 精确控制输入位置
- ✅ 适合已知坐标的场景

### API8: 当前位置输入 ✨ 新增

```http
POST /api/tn3270/send/string
Content-Type: application/json
X-Session-Id: YOUR_SESSION_ID

{
  "text": "Hello"
}
```

**特点**:
- ✅ 不需要指定行列
- ✅ 在当前光标位置输入
- ✅ 适合配合 Tab 导航的场景

---

## 💡 使用场景

### 场景 1：配合 Tab 键填写表单

```http
### 第一个字段（指定位置）
POST /api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: abc-123

{
  "row": 5,
  "column": 10,
  "command": "FirstValue"
}

### 按 Tab 跳到第二个字段
POST /api/tn3270/key/tab
X-Session-Id: abc-123

### 在当前位置输入（不需要知道坐标）✨
POST /api/tn3270/send/string
Content-Type: application/json
X-Session-Id: abc-123

{
  "text": "SecondValue"
}

### 按 Tab 跳到第三个字段
POST /api/tn3270/key/tab
X-Session-Id: abc-123

### 继续在当前位置输入 ✨
POST /api/tn3270/send/string
Content-Type: application/json
X-Session-Id: abc-123

{
  "text": "ThirdValue"
}

### 按回车提交
POST /api/tn3270/key/enter
X-Session-Id: abc-123
```

### 场景 2：输入用户名密码

```http
### 假设光标已经在用户名字段
POST /api/tn3270/send/string
Content-Type: application/json
X-Session-Id: abc-123

{
  "text": "HERC01"
}

### Tab 到密码字段
POST /api/tn3270/key/tab
X-Session-Id: abc-123

### 输入密码
POST /api/tn3270/send/string
Content-Type: application/json
X-Session-Id: abc-123

{
  "text": "MyPassword"
}

### 回车提交
POST /api/tn3270/key/enter
X-Session-Id: abc-123
```

### 场景 3：逐字段填写（不需要知道坐标）

```http
### 定位到第一个字段（使用 API2）
POST /api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: abc-123
{ "row": 5, "column": 10, "command": "START" }

### 接下来全部使用 Tab + API8
POST /api/tn3270/key/tab
X-Session-Id: abc-123

POST /api/tn3270/send/string
Content-Type: application/json
X-Session-Id: abc-123
{ "text": "Field1" }

POST /api/tn3270/key/tab
X-Session-Id: abc-123

POST /api/tn3270/send/string
Content-Type: application/json
X-Session-Id: abc-123
{ "text": "Field2" }

POST /api/tn3270/key/tab
X-Session-Id: abc-123

POST /api/tn3270/send/string
Content-Type: application/json
X-Session-Id: abc-123
{ "text": "Field3" }

POST /api/tn3270/key/enter
X-Session-Id: abc-123
```

---

## 💻 JavaScript/React 示例

```javascript
// 发送字符串（不指定位置）
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

// 发送 Tab
const sendTab = async (sessionId) => {
  const response = await fetch('http://localhost:8080/api/tn3270/key/tab', {
    method: 'POST',
    headers: { 'X-Session-Id': sessionId }
  });
  return await response.json();
};

// 发送回车
const sendEnter = async (sessionId) => {
  const response = await fetch('http://localhost:8080/api/tn3270/key/enter', {
    method: 'POST',
    headers: { 'X-Session-Id': sessionId }
  });
  return await response.json();
};

// 使用示例：填写多个字段
const fillForm = async (sessionId, values) => {
  // 输入第一个值（假设光标已在第一个字段）
  await sendString(sessionId, values[0]);
  
  // Tab 并输入后续字段
  for (let i = 1; i < values.length; i++) {
    await sendTab(sessionId);
    await sendString(sessionId, values[i]);
  }
  
  // 提交
  await sendEnter(sessionId);
};

// 调用
const sessionId = 'abc-123';
await fillForm(sessionId, ['Field1', 'Field2', 'Field3', 'Field4']);
```

### React 组件示例

```jsx
import React, { useState } from 'react';

function TN3270FormFiller() {
  const [sessionId, setSessionId] = useState(null);
  const [fields, setFields] = useState(['', '', '']);

  const sendString = async (text) => {
    await fetch('http://localhost:8080/api/tn3270/send/string', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Session-Id': sessionId
      },
      body: JSON.stringify({ text })
    });
  };

  const sendTab = async () => {
    await fetch('http://localhost:8080/api/tn3270/key/tab', {
      method: 'POST',
      headers: { 'X-Session-Id': sessionId }
    });
  };

  const sendEnter = async () => {
    await fetch('http://localhost:8080/api/tn3270/key/enter', {
      method: 'POST',
      headers: { 'X-Session-Id': sessionId }
    });
  };

  const fillAllFields = async () => {
    // 输入第一个字段
    await sendString(fields[0]);
    
    // Tab 并输入后续字段
    for (let i = 1; i < fields.length; i++) {
      await sendTab();
      await sendString(fields[i]);
    }
    
    // 提交
    await sendEnter();
  };

  return (
    <div>
      <h3>表单填写助手</h3>
      
      {fields.map((field, index) => (
        <input
          key={index}
          value={field}
          onChange={(e) => {
            const newFields = [...fields];
            newFields[index] = e.target.value;
            setFields(newFields);
          }}
          placeholder={`字段 ${index + 1}`}
        />
      ))}
      
      <button onClick={fillAllFields}>
        一键填写所有字段
      </button>
      
      <div>
        <h4>单独操作</h4>
        <input id="singleText" placeholder="输入文本" />
        <button onClick={() => {
          const text = document.getElementById('singleText').value;
          sendString(text);
        }}>
          在当前位置输入
        </button>
        <button onClick={sendTab}>Tab</button>
        <button onClick={sendEnter}>回车</button>
      </div>
    </div>
  );
}
```

---

## 📋 完整 API 列表（9个）

| API | 路径 | 说明 |
|-----|------|------|
| API1 | `POST /api/tn3270/login` | 登录 |
| API2 | `POST /api/tn3270/menu/command` | 指定位置输入 |
| API3 | `POST /api/tn3270/logoff` | 登出 |
| API5 | `POST /api/tn3270/key/enter` | 回车键 |
| API6 | `POST /api/tn3270/key/reset` | Reset |
| API7 | `POST /api/tn3270/key/tab` | Tab 键 |
| API8 | `POST /api/tn3270/send/string` | 当前位置输入字符串 ✨ |
| 额外 | `GET /api/tn3270/screen` | 获取屏幕 |
| API4 | `POST /api/tn3270/menu/enter-level2` | （已过时） |

---

## 🎯 最佳实践

### 何时使用 API2（指定位置）

- ✅ 第一次定位到某个字段
- ✅ 需要跳过某些字段直接定位
- ✅ 已知精确坐标

### 何时使用 API8（当前位置）

- ✅ 配合 Tab 键连续填写多个字段
- ✅ 不需要知道精确坐标
- ✅ 光标已经在正确位置

### 推荐组合

```
API2 (定位到第一个字段) 
  → API8 (输入内容)
  → API7 (Tab)
  → API8 (输入内容)
  → API7 (Tab)
  → API8 (输入内容)
  → API5 (回车)
```

---

## 📦 需要更新的文件

1. **StringRequest.java** (新增)
   - 位置：`src/main/java/com/example/tn3270api/dto/StringRequest.java`

2. **Tn3270Service.java** (更新)
   - 添加 `sendString(sessionId, text)` 方法

3. **Tn3270Controller.java** (更新)
   - 添加 API8 接口

4. **test-api-complete.http** (新增)
   - 包含所有 API 的测试

---

## ✅ 优势

1. **更简单** 📝
   - 不需要计算坐标
   - 代码更简洁

2. **更灵活** 🔄
   - 配合 Tab 自然导航
   - 适应不同的屏幕布局

3. **更高效** ⚡
   - 快速填写多字段表单
   - 减少定位时间

4. **更直观** 👁️
   - 符合人类操作习惯
   - 类似真实终端输入

---

## 🎉 总结

**API8** 让字符串输入更加简单：
- ✅ 不需要指定行列
- ✅ 在当前光标位置输入
- ✅ 完美配合 Tab 键
- ✅ 适合连续填写表单

现在您有两种方式输入内容：
- **API2**: 精确定位 + 输入
- **API8**: 当前位置输入（新增）

更灵活、更强大！🚀
