# 用户接口文档

## 1. 用户登录

### 接口信息
- 请求路径：`/api/user/login`
- 请求方式：POST
- 接口描述：用户登录接口，验证用户邮箱和密码

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| email | String | 是 | 用户邮箱 |
| password | String | 是 | 用户密码 |

### 响应格式
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "id": 1,
        "email": "example@email.com",
        // 其他用户信息字段
    }
}
```

### Cookie设置
登录成功后会设置名为 `login_token` 的Cookie，用于后续接口的身份验证。Cookie属性如下：
- HttpOnly: true
- Secure: true（生产环境）
- SameSite: Lax
- Path: /
- MaxAge: 1800（30分钟）

### 错误响应
```json
{
    "code": 1,
    "message": "邮箱或密码错误",
    "data": null
}
```

## 2. 发送验证码

### 接口信息
- 请求路径：`/api/user/register/code`
- 请求方式：POST
- 接口描述：发送注册验证码到指定邮箱

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| email | String | 是 | 用户邮箱 |

### 响应格式
```json
{
    "code": 0,
    "message": "success",
    "data": null
}
```

## 3. 用户注册

### 接口信息
- 请求路径：`/api/user/register`
- 请求方式：POST
- 接口描述：用户注册接口，需要验证码

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| email | String | 是 | 用户邮箱 |
| password | String | 是 | 用户密码 |
| verificationCode | String | 是 | 邮箱验证码 |

### 响应格式
```json
{
    "code": 0,
    "message": "success",
    "data": null
}
```

### 错误响应
```json
{
    "code": 1,
    "message": "注册失败，邮箱已存在",
    "data": null
}
```

## 4. 用户登出

### 接口信息
- 请求路径：`/api/user/logout`
- 请求方式：POST
- 接口描述：用户登出，清除登录状态

### Cookie要求
需要在请求中包含名为 `login_token` 的Cookie

### 响应格式
```json
{
    "code": 0,
    "message": "success",
    "data": null
}
```

### Cookie处理
登出成功后会清除 `login_token` Cookie

## 注意事项
1. 所有接口返回格式统一为 ApiResponse 格式
2. 请求参数均使用 URL 编码
3. 密码传输建议进行加密处理
4. 验证码有效期请注意控制在合理范围内
5. 除了登录和注册相关接口，其他接口都需要先登录才能访问