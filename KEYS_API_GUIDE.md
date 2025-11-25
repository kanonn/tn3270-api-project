# 🎹 TN3270 API - 分离的按键操作

## 📋 更新内容

根据您的要求，已完成以下修改：

1. ✅ **API2 不再自动回车** - 输入命令后不自动按回车
2. ✅ **新增 API5** - 单独的回车键 API
3. ✅ **新增 API6** - 发送 Reset 的 API
4. ✅ **新增 API7** - 键入 Tab 的 API

---

## 🎯 完整 API 列表

### 核心 API（3个）

| API | 方法 | 路径 | 功能 |
|-----|------|------|------|
| API1 | POST | `/api/tn3270/login` | 登录到 TSO 主菜单 |
| API2 | POST | `/api/tn3270/menu/command` | 在指定位置输入命令（**不自动回车**） |
| API3 | POST | `/api/tn3270/logoff` | 执行 LOGOFF |

### 按键操作 API（3个）✨ 新增

| API | 方法 | 路径 | 功能 |
|-----|------|------|------|
| API5 | POST | `/api/tn3270/key/enter` | 发送回车键 |
| API6 | POST | `/api/tn3270/key/reset` | 发送 Reset |
| API7 | POST | `/api/tn3270/key/tab` | 发送 Tab 键 |

### 辅助 API（2个）

| API | 方法 | 路径 | 功能 |
|-----|------|------|------|
| API4 | POST | `/api/tn3270/menu/enter-level2` | 进入第2级菜单（已过时） |
| 额外 | GET | `/api/tn3270/screen` | 获取当前屏幕内容 |

---

## 📖 详细说明

### API2: 输入命令（不自动回车）

**接口**: `POST /api/tn3270/menu/command`

**功能**: 在指定位置输入命令，**不自动按回车**

**Headers**:
- `X-Session-Id`: 会话ID
- `Content-Type`: application/json

**Body**:
```json
{
  "row": 4,
  "column": 13,
  "command": "2"
}
```

**响应**:
```json
{
  "success": true,
  "message": "命令输入成功",
  "sessionId": "...",
  "data": {
    "screenLines": [...],
    "fullScreen": "..."
  }
}
```

**注意**: 
- ⚠️ **输入后不会自动回车**
- 需要单独调用 API5 发送回车

---

### API5: 发送回车键 ✨ 新增

**接口**: `POST /api/tn3270/key/enter`

**功能**: 发送回车键

**Headers**:
- `X-Session-Id`: 会话ID

**Body**: 无

**响应**:
```json
{
  "success": true,
  "message": "回车键发送成功",
  "sessionId": "...",
  "data": {
    "screenLines": [...],
    "fullScreen": "..."
  }
}
```

**使用场景**:
- 输入命令后确认
- 在菜单中选择选项
- 提交表单

---

### API6: 发送 Reset ✨ 新增

**接口**: `POST /api/tn3270/key/reset`

**功能**: 发送 Reset 键（清除错误状态）

**Headers**:
- `X-Session-Id`: 会话ID

**Body**: 无

**响应**:
```json
{
  "success": true,
  "message": "Reset 发送成功",
  "sessionId": "...",
  "data": {
    "screenLines": [...],
    "fullScreen": "..."
  }
}
```

**使用场景**:
- 清除键盘锁定
- 重置输入错误
- 解除屏幕锁定状态

---

### API7: 发送 Tab 键 ✨ 新增

**接口**: `POST /api/tn3270/key/tab`

**功能**: 发送 Tab 键（跳到下一个输入域）

**Headers**:
- `X-Session-Id`: 会话ID

**Body**: 无

**响应**:
```json
{
  "success": true,
  "message": "Tab 键发送成功",
  "sessionId": "...",
  "data": {
    "screenLines": [...],
    "fullScreen": "..."
  }
}
```

**使用场景**:
- 跳到下一个输入字段
- 快速导航表单
- 移动光标位置

---

## 💡 使用示例

### 示例 1：输入命令并按回车

```http
### 步骤1: 输入命令（不自动回车）
POST http://localhost:8080/api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: abc-123

{
  "row": 4,
  "column": 13,
  "command": "2"
}

### 步骤2: 发送回车键
POST http://localhost:8080/api/tn3270/key/enter
X-Session-Id: abc-123
```

### 示例 2：使用 Tab 在字段间导航

```http
### 步骤1: 在第一个字段输入
POST http://localhost:8080/api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: abc-123

{
  "row": 5,
  "column": 10,
  "command": "VALUE1"
}

### 步骤2: 按 Tab 跳到下一个字段
POST http://localhost:8080/api/tn3270/key/tab
X-Session-Id: abc-123

### 步骤3: 在第二个字段输入
POST http://localhost:8080/api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: abc-123

{
  "row": 5,
  "column": 30,
  "command": "VALUE2"
}

### 步骤4: 按回车提交
POST http://localhost:8080/api/tn3270/key/enter
X-Session-Id: abc-123
```

### 示例 3：错误恢复（使用 Reset）

```http
### 如果遇到键盘锁定或错误
POST http://localhost:8080/api/tn3270/key/reset
X-Session-Id: abc-123

### 然后继续操作
POST http://localhost:8080/api/tn3270/menu/command
Content-Type: application/json
X-Session-Id: abc-123

{
  "row": 4,
  "column": 13,
  "command": "2"
}
```

---

## 🔄 完整操作流程

### 场景：登录并进入菜单选项 2

```javascript
// 1. 登录
const loginResponse = await fetch('http://localhost:8080/api/tn3270/login', {
  method: 'POST'
});
const { sessionId } = await loginResponse.json();

// 2. 在第4行第13列输入 "2"
await fetch('http://localhost:8080/api/tn3270/menu/command', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-Session-Id': sessionId
  },
  body: JSON.stringify({ row: 4, column: 13, command: '2' })
});

// 3. 发送回车键确认
await fetch('http://localhost:8080/api/tn3270/key/enter', {
  method: 'POST',
  headers: { 'X-Session-Id': sessionId }
});

// 4. 查看屏幕内容
const screenResponse = await fetch('http://localhost:8080/api/tn3270/screen', {
  headers: { 'X-Session-Id': sessionId }
});
const screen = await screenResponse.json();
console.log(screen.data.fullScreen);
```

---

## 💻 React 组件示例

```jsx
import React, { useState } from 'react';

function TN3270Terminal() {
  const [sessionId, setSessionId] = useState(null);
  const [screen, setScreen] = useState('');

  // 输入命令（不自动回车）
  const inputCommand = async (row, column, command) => {
    const response = await fetch('http://localhost:8080/api/tn3270/menu/command', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Session-Id': sessionId
      },
      body: JSON.stringify({ row, column, command })
    });
    const data = await response.json();
    if (data.success) {
      setScreen(data.data.fullScreen);
    }
  };

  // 发送回车
  const sendEnter = async () => {
    const response = await fetch('http://localhost:8080/api/tn3270/key/enter', {
      method: 'POST',
      headers: { 'X-Session-Id': sessionId }
    });
    const data = await response.json();
    if (data.success) {
      setScreen(data.data.fullScreen);
    }
  };

  // 发送 Tab
  const sendTab = async () => {
    const response = await fetch('http://localhost:8080/api/tn3270/key/tab', {
      method: 'POST',
      headers: { 'X-Session-Id': sessionId }
    });
    const data = await response.json();
    if (data.success) {
      setScreen(data.data.fullScreen);
    }
  };

  // 发送 Reset
  const sendReset = async () => {
    const response = await fetch('http://localhost:8080/api/tn3270/key/reset', {
      method: 'POST',
      headers: { 'X-Session-Id': sessionId }
    });
    const data = await response.json();
    if (data.success) {
      setScreen(data.data.fullScreen);
    }
  };

  return (
    <div>
      <h2>TN3270 终端</h2>
      
      {/* 按键操作 */}
      <div>
        <button onClick={sendEnter}>回车 (Enter)</button>
        <button onClick={sendTab}>Tab</button>
        <button onClick={sendReset}>Reset</button>
      </div>
      
      {/* 输入命令 */}
      <div>
        <input id="row" type="number" placeholder="行" />
        <input id="column" type="number" placeholder="列" />
        <input id="command" type="text" placeholder="命令" />
        <button onClick={() => {
          const row = document.getElementById('row').value;
          const column = document.getElementById('column').value;
          const command = document.getElementById('command').value;
          inputCommand(parseInt(row), parseInt(column), command);
        }}>
          输入（不回车）
        </button>
      </div>
      
      {/* 快捷按钮 */}
      <div>
        <button onClick={async () => {
          await inputCommand(4, 13, '2');
          await sendEnter();
        }}>
          选择选项 2 并回车
        </button>
      </div>
      
      {/* 屏幕显示 */}
      <pre style={{ 
        background: 'black', 
        color: 'green', 
        padding: '10px',
        fontFamily: 'monospace'
      }}>
        {screen}
      </pre>
    </div>
  );
}

export default TN3270Terminal;
```

---

## 🔑 按键对照表

| 按键 | API 路径 | 说明 |
|------|---------|------|
| Enter | `/api/tn3270/key/enter` | 回车/确认 |
| Tab | `/api/tn3270/key/tab` | 跳到下一个字段 |
| Reset | `/api/tn3270/key/reset` | 重置/清除错误 |

---

## ⚠️ 重要变化

### 与之前版本的区别

**之前**:
- API2 输入命令后自动按回车
- 无法控制回车时机

**现在**:
- ✅ API2 输入命令**不自动回车**
- ✅ 使用 API5 单独控制回车时机
- ✅ 更灵活，可以在多个字段输入后再统一回车

---

## 📦 需要更新的文件

1. **Tn3270Service.java** - 添加 sendEnter、sendReset、sendTab 方法
2. **Tn3270Controller.java** - 添加 API5、API6、API7 接口
3. **test-api-keys.http** - 新的测试文件

---

## ✅ 优势

1. **更精细的控制** 🎯
   - 可以分步操作
   - 控制按键时机

2. **符合终端习惯** ⌨️
   - Tab 跳字段
   - Reset 清错误
   - Enter 确认

3. **便于调试** 🔍
   - 可以逐步观察屏幕变化
   - 更容易定位问题

4. **灵活组合** 🔧
   - 可以自由组合按键
   - 适应各种操作场景

---

## 🎉 现在可以实现

- ✅ 输入多个字段后再统一回车
- ✅ 使用 Tab 在字段间快速跳转
- ✅ 遇到错误用 Reset 恢复
- ✅ 完全控制每个按键的时机

更灵活、更强大的 TN3270 API！🚀
