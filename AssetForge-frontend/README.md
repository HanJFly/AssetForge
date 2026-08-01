# AssetForge Frontend

## 启动说明

默认采用演示模式，不会主动请求后端接口，这样在后端尚未完全实现时页面也能正常打开，不会在浏览器控制台持续出现 `404`。

### 1. 演示模式

创建 `.env`：

```bash
VITE_ENABLE_API=false
```

### 2. 联调模式

如果后端接口已经实现，再改成：

```bash
VITE_ENABLE_API=true
VITE_API_BASE_URL=http://localhost:8080
```

如果后端最终使用 `/api/v1` 前缀，可以写成：

```bash
VITE_ENABLE_API=true
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## 当前说明

当前仓库里的后端 Controller 只有少量接口已经落地，比如：

- `/department/tree`
- `/department/page`
- `/department/detail`
- `/department/create`
- `/department/update`
- `/department/delete`
- `/user/page`
- `/user/detail`

其余很多控制器还是空壳，所以前端默认关闭真实请求更适合当前阶段。
