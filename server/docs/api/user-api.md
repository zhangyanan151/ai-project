# 用户接口文档

## 1. 用户登录

### 接口信息
- 请求路径：`/api/user/login`
- 请求方式：POST
- 接口描述：用户登录接口，验证用户邮箱和密码

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| email | String | 是 | 用户邮箱，用于身份验证和后续请求 |
| password | String | 是 | 用户密码 |

### 响应格式
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "id": 1,
        "email": "example@email.com",
        "name": "用户名称",
        "createTime": "2024-01-01 12:00:00"
    }
}
```

### 身份验证
登录成功后，后续请求需要在请求参数中携带email参数用于身份验证。

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

### 错误响应
```json
{
    "code": 1,
    "message": "验证码发送失败",
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

## 注意事项
1. 所有接口返回格式统一为 ApiResponse 格式：
   - 成功时：code=0, message="success"
   - 失败时：code=1, message=错误信息
2. 请求参数均使用 URL 编码
3. 密码传输建议进行加密处理
4. 验证码有效期请注意控制在合理范围内
5. 除了登录和注册相关接口，其他接口都需要在请求参数中携带email进行身份验证