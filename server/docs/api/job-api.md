# 职位接口文档

## 1. 获取职位列表

### 接口信息
- 请求路径：`/api/job/list`
- 请求方式：GET
- 接口描述：获取职位列表，支持分页

### 请求参数
| 参数名 | 类型 | 必填 | 描述 | 默认值 |
| --- | --- | --- | --- | --- |
| email | String | 是 | 用户邮箱 | - |
| page | Integer | 否 | 页码，从0开始 | 0 |
| size | Integer | 否 | 每页数量 | 5 |

### 响应格式
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "content": [
            {
                "id": 1,
                "title": "职位标题",
                "description": "职位描述"
                // 其他职位信息字段
            }
        ],
        "totalElements": 100,
        "totalPages": 20,
        "size": 5,
        "number": 0
    }
}
```

### 错误响应
```json
{
    "code": 1,
    "message": "获取列表失败",
    "data": null
}
```

## 2. 获取职位详情

### 接口信息
- 请求路径：`/api/job/{jobId}`
- 请求方式：GET
- 接口描述：获取指定职位的详细信息

### 路径参数
| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| jobId | Long | 是 | 职位ID |

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| email | String | 是 | 用户邮箱 |

### 响应格式
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "id": 1,
        "title": "职位标题",
        "description": "职位描述"
        // 其他职位信息字段
    }
}
```

### 错误响应
```json
{
    "code": 1,
    "message": "获取详情失败",
    "data": null
}
```

## 3. 搜索职位

### 接口信息
- 请求路径：`/api/job/search`
- 请求方式：GET
- 接口描述：根据关键词搜索职位

### 请求参数
| 参数名 | 类型 | 必填 | 描述 | 默认值 |
| --- | --- | --- | --- | --- |
| email | String | 是 | 用户邮箱 | - |
| keyword | String | 是 | 搜索关键词 | - |
| page | Integer | 否 | 页码，从0开始 | 0 |
| size | Integer | 否 | 每页数量 | 5 |

### 响应格式
```json
{
    "code": 0,
    "message": "success",
    "data": {
        "content": [
            {
                "id": 1,
                "title": "职位标题",
                "description": "职位描述"
                // 其他职位信息字段
            }
        ],
        "totalElements": 100,
        "totalPages": 20,
        "size": 5,
        "number": 0
    }
}
```

### 错误响应
```json
{
    "code": 1,
    "message": "搜索失败",
    "data": null
}
```

## 4. 创建职位

### 接口信息
- 请求路径：`/api/job/create`
- 请求方式：POST
- 接口描述：创建新的职位信息

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| --- | --- | --- | --- |
| email | String | 是 | 用户邮箱 |
| title | String | 是 | 职位标题 |
| description | String | 是 | 职位描述 |
// 其他职位信息字段
```

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
    "message": "创建失败",
    "data": null
}
```

## 注意事项
1. 所有接口返回格式统一为 ApiResponse 格式
2. 分页接口的页码从0开始计数
3. 需要登录后才能访问这些接口
4. 创建职位时注意数据的完整性和合法性