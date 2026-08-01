# AssetForge 接口文档
> 版本：v2.1
> 日期：2026-07-18
> 状态：基于当前需求与数据库结构重整理
> 基础路径：`/api/v1`
> 数据格式：`application/json`
> 设计原则：除登录、刷新令牌、文件上传等少数场景外，尽量统一使用 `POST`

---

## 一、设计说明

本文档基于以下内容整理：
- [实物管理系统需求分析文档.md](D:\Mystudy\AssetForge\实物管理系统需求分析文档.md)
- [database-schema.sql](D:\Mystudy\AssetForge\database-schema.sql)

接口设计目标不是追求完全 RESTful，而是优先服务于单人开发和快速联调：

1. 列表统一使用 `POST /xxx/page`
2. 详情统一使用 `POST /xxx/detail`
3. 新增统一使用 `POST /xxx/create`
4. 修改统一使用 `POST /xxx/update`
5. 删除统一使用 `POST /xxx/delete`
6. 审批、确认、提交等动作统一使用 `POST /xxx/action`

---

## 二、通用规范

### 2.1 请求头

| Header | 必填 | 说明 |
|------|:----:|------|
| `Authorization` | 否 | 登录后传 `Bearer {accessToken}` |
| `Content-Type` | 是 | `application/json` |

### 2.2 统一返回格式

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

### 2.3 分页返回格式

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [],
    "total": 0,
    "page": 1,
    "size": 20
  }
}
```

### 2.4 建议错误码

| code | 说明 |
|------|------|
| `200` | 成功 |
| `400` | 参数错误 |
| `401` | 未登录或令牌失效 |
| `403` | 无权限 |
| `404` | 数据不存在 |
| `409` | 业务冲突 |
| `500` | 系统异常 |

### 2.5 通用分页请求体

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "sortField": "createdAt",
  "sortOrder": "desc"
}
```

### 2.6 通用 ID 请求体

```json
{
  "id": 1
}
```

---

## 三、枚举约定

### 3.1 角色编码

- `EMPLOYEE` 普通员工
- `STOREKEEPER` 仓管员
- `ASSET_ADMIN` 资产管理员
- `DEPT_MANAGER` 部门主管

### 3.2 用户状态

- `ACTIVE`
- `DISABLED`

### 3.3 资产状态

- `STOCK` 库存
- `ASSIGNED` 已领用
- `SCRAPPED` 已报废
- `LOST` 盘亏

### 3.4 审批状态

- `PENDING`
- `APPROVED`
- `REJECTED`

### 3.5 资产用途

- `OFFICE`
- `PRODUCTION`
- `RD`
- `ADMIN`

### 3.6 资产来源

- `PURCHASE`
- `LEASE`

### 3.7 归还状态

- `NORMAL`
- `MINOR_DAMAGE`
- `MAJOR_DAMAGE`

### 3.8 盘点结果

- `NORMAL`
- `LOSS`
- `GAIN`
- `MISMATCH`

### 3.9 盘点任务状态

- `PENDING`
- `IN_PROGRESS`
- `COMPLETED`

### 3.10 盘亏处理状态

- `PENDING_COMPENSATION`
- `COMPENSATED`
- `EXEMPTING`
- `EXEMPTED`

---

## 四、认证模块

### 4.1 登录

`POST /auth/login`

说明：用户名密码登录，登录成功后返回 JWT。后续请求统一通过 `Authorization: Bearer {token}` 传递登录态。

请求体：

```json
{
  "username": "admin",
  "password": "123456"
}
```

响应体：

```json
{
  "token": "string",
  "tokenType": "Bearer",
  "expiresIn": 7200,
  "user": {
    "id": 1,
    "username": "string",
    "realName": "string",
    "employeeNo": "string",
    "departmentId": 1,
    "departmentName": "string",
    "status": "ACTIVE",
    "roles": [
      {
        "id": 1,
        "code": "EMPLOYEE",
        "name": "普通员工"
      }
    ]
  }
}
```

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.xxx.yyy",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "employeeNo": "E0001",
      "departmentId": 1,
      "departmentName": "总公司",
      "status": "ACTIVE",
      "roles": [
        {
          "id": 1,
          "code": "ASSET_ADMIN",
          "name": "资产管理员"
        }
      ]
    }
  }
}
```

### 4.2 获取当前用户

`POST /auth/me`

说明：从当前请求的 JWT 中解析用户信息，不依赖 Spring Security 上下文。

请求体：

```json
{}
```

请求头：

```text
Authorization: Bearer {token}
```

响应体：

```json
{
  "id": 1,
  "username": "string",
  "realName": "string",
  "employeeNo": "string",
  "phone": "string",
  "email": "string",
  "departmentId": 1,
  "departmentName": "string",
  "status": "ACTIVE",
  "roles": [
    {
      "id": 1,
      "code": "EMPLOYEE",
      "name": "普通员工"
    }
  ]
}
```

### 4.3 退出登录

`POST /auth/logout`

说明：如果采用纯 JWT 无状态方案，服务端可以不做任何清理，前端删除本地 token 即可。若后续增加黑名单机制，可继续保留本接口。

请求体：

```json
{}
```

响应体：

```json
{
  "success": true
}
```

### 4.4 Filter 认证约定

说明：认证链路采用 JWT + 自定义 Filter。

处理规则：

1. 命中白名单路径直接放行，例如 `/auth/login`
2. 未携带 `Authorization` 请求头时返回 `401`
3. `Authorization` 不是 `Bearer xxx` 格式时返回 `401`
4. token 过期、伪造、签名错误时返回 `401`
5. token 校验通过后，将当前用户信息写入 `ThreadLocal` 或 `request attribute`

认证失败返回示例：

```json
{
  "code": 401,
  "msg": "未登录或登录已失效",
  "data": null
}
```

---

## 五、部门管理

### 5.1 部门树

`POST /department/tree`

说明：返回树形部门结构，供组织架构树、下拉选择器使用。`managerUserName` 为关联 `user` 表的回显字段。

请求体：

```json
{
  "keyword": ""
}
```

响应体：

```json
[
  {
    "id": 1,
    "name": "string",
    "parentId": 0,
    "managerUserId": 1,
    "managerUserName": "string",
    "sortOrder": 1,
    "children": []
  }
]
```

### 5.2 部门分页

`POST /department/page`

说明：分页查询部门信息。`parentName`、`managerUserName` 为关联回显字段，不是 `department` 表原始列。

请求体：

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "parentId": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "name": "string",
      "parentId": 0,
      "parentName": "string",
      "managerUserId": 1,
      "managerUserName": "string",
      "sortOrder": 1,
      "createdAt": "2026-07-18 10:00:00",
      "updatedAt": "2026-07-18 10:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 5.3 部门详情

`POST /department/detail`

说明：查询单个部门详情。

请求体：

```json
{
  "id": 3
}
```

响应体：

```json
{
  "id": 3,
  "name": "研发部",
  "parentId": 1,
  "parentName": "总公司",
  "managerUserId": 8,
  "managerUserName": "李主管",
  "sortOrder": 20,
  "createdAt": "2026-07-18 10:00:00",
  "updatedAt": "2026-07-18 10:00:00"
}
```

### 5.4 新增部门

`POST /department/create`

说明：新增部门节点。

请求体：

```json
{
  "name": "后端组",
  "parentId": 3,
  "managerUserId": 12,
  "sortOrder": 10
}
```

响应体：

```json
{
  "id": 9
}
```

### 5.5 修改部门

`POST /department/update`

说明：修改部门信息。

请求体：

```json
{
  "id": 9,
  "name": "后端开发组",
  "parentId": 3,
  "managerUserId": 12,
  "sortOrder": 11
}
```

响应体：

```json
{
  "success": true
}
```

### 5.6 删除部门

`POST /department/delete`

说明：删除部门前应校验是否存在子部门、用户或资产关联。

请求体：

```json
{
  "id": 9
}
```

响应体：

```json
{
  "success": true
}
```

---

## 六、资产分类

### 6.1 分类树

`POST /category/tree`

说明：返回资产分类树。当前数据库表为 `asset_category`，核心字段是 `name`、`parent_id`、`standard_life_months`、`sort_order`。

请求体：

```json
{
  "keyword": ""
}
```

响应体：

```json
[
  {
    "id": 1,
    "name": "电子设备",
    "parentId": 0,
    "standardLifeMonths": null,
    "sortOrder": 1,
    "children": []
  }
]
```

### 6.2 分类分页

`POST /category/page`

说明：分页查询资产分类。`parentName` 为关联回显字段，不是 `asset_category` 表原始列。

请求体：

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "parentId": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "name": "电子设备",
      "parentId": 0,
      "parentName": "",
      "standardLifeMonths": null,
      "sortOrder": 1,
      "createdAt": "2026-07-18 10:00:00",
      "updatedAt": "2026-07-18 10:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 6.3 分类详情

`POST /category/detail`

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "id": 1,
  "name": "电子设备",
  "parentId": 0,
  "parentName": "",
  "standardLifeMonths": null,
  "sortOrder": 1,
  "createdAt": "2026-07-18 10:00:00",
  "updatedAt": "2026-07-18 10:00:00"
}
```

### 6.4 新增分类

`POST /category/create`

请求体：

```json
{
  "name": "笔记本电脑",
  "parentId": 1,
  "standardLifeMonths": 36,
  "sortOrder": 10
}
```

响应体：

```json
{
  "id": 8
}
```

### 6.5 修改分类

`POST /category/update`

请求体：

```json
{
  "id": 8,
  "name": "轻薄笔记本电脑",
  "parentId": 1,
  "standardLifeMonths": 36,
  "sortOrder": 11
}
```

响应体：

```json
{
  "success": true
}
```

### 6.6 删除分类

`POST /category/delete`

请求体：

```json
{
  "id": 8
}
```

响应体：

```json
{
  "success": true
}
```

---

## 七、用户与角色

### 7.1 用户分页

`POST /user/page`

说明：分页查询用户。

请求体：

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "departmentId": null,
  "status": null,
  "roleCode": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "username": "zhangsan",
      "realName": "张三",
      "employeeNo": "E1001",
      "phone": "13800000000",
      "email": "zhangsan@example.com",
      "departmentId": 3,
      "departmentName": "研发部",
      "status": "ACTIVE",
      "roles": ["EMPLOYEE"]
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 7.2 用户详情

`POST /user/detail`

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "id": 1,
  "username": "zhangsan",
  "realName": "张三",
  "employeeNo": "E1001",
  "phone": "13800000000",
  "email": "zhangsan@example.com",
  "departmentId": 3,
  "departmentName": "研发部",
  "status": "ACTIVE",
  "roleIds": [1]
}
```

### 7.3 新增用户

`POST /user/create`

请求体：

```json
{
  "username": "lisi",非空唯一
  "password": "123456",非空
  "realName": "李四",非空
  "employeeNo": "E1002",
  "phone": "13900000000",
  "email": "lisi@example.com",
  "departmentId": 4,
  "status": "ACTIVE",
  "roleIds": [1, 2]
}
```

响应体：

```json
{
  "id": 2
}
```

### 7.4 修改用户

`POST /user/update`

请求体：

```json
{
  "id": 2,
  "realName": "李四",
  "employeeNo": "E1002",
  "phone": "13900000000",
  "email": "lisi@example.com",
  "departmentId": 4,
  "status": "ACTIVE",
  "roleIds": [1]
}
```

响应体：

```json
{
  "success": true
}
```

### 7.5 重置密码

`POST /user/reset-password`

请求体：

```json
{
  "id": 2,
  "newPassword": "123456"
}
```

响应体：

```json
{
  "success": true
}
```

### 7.6 删除用户

`POST /user/delete`

**删除用户时应校验：**

1. **用户是否存在且未被删除** — `user.isDeleted != 1`
2. **是否为部门管理员** — 查 `department.manager_user_id`，若有则需先交接
3. **是否持有资产** — 查 `asset.current_user_id`，若有资产未归还则不允许删除
4. **是否有未完成的审批** — 查 `approval_record`，`PENDING` 状态的申请单/审批单不能删除
5. **清理 user_role 关联** — 删除用户后 `user_role` 中对应记录应一并清理

请求体：

```json
{
  "id": 2
}
```

响应体：

```json
{
  "success": true
}
```

### 7.7 角色列表

`POST /role/list`

说明：供用户表单、权限展示使用。

请求体：

```json
{}
```

响应体：

```json
[
  {
    "id": 1,
    "code": "EMPLOYEE",
    "name": "普通员工"
  }
]
```

---

## 八、系统配置

### 8.1 获取系统配置

`POST /system/config/detail`

说明：当前数据库为 Key-Value 结构，接口建议返回配置项列表，或由服务层组装成对象后再返回。

请求体：

```json
{}
```

响应体：

```json
[
  {
    "id": 1,
    "configKey": "default_residual_rate",
    "configValue": "0.05",
    "description": "默认残值率"
  },
  {
    "id": 2,
    "configKey": "barcode_prefix",
    "configValue": "AST-",
    "description": "条码前缀"
  }
]
```

### 8.2 修改系统配置

`POST /system/config/update`

说明：建议按配置项逐条更新，避免和当前表结构冲突。

请求体：

```json
[
  {
    "id": 1,
    "configKey": "default_residual_rate",
    "configValue": "0.05"
  },
  {
    "id": 4,
    "configKey": "barcode_prefix",
    "configValue": "AST-"
  }
]
```

响应体：

```json
{
  "success": true
}
```

---

## 九、资产管理

### 9.1 资产分页

`POST /asset/page`

说明：分页查询资产列表。`categoryName`、`currentUserName` 为关联或冗余回显字段。

请求体：

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "assetStatus": null,
  "categoryId": null,
  "departmentId": null,
  "userId": null,
  "sourceType": null,
  "purchaseDateStart": null,
  "purchaseDateEnd": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "assetCode": "AF-202607-0001",
      "name": "ThinkPad T14",
      "categoryId": 8,
      "categoryName": "轻薄笔记本电脑",
      "brandModel": "Lenovo ThinkPad T14",
      "purpose": "OFFICE",
      "departmentId": 3,
      "departmentName": "研发部",
      "currentUserId": 1,
      "currentUserName": "张三",
      "status": "ASSIGNED",
      "purchaseAmount": 6500.00,
      "purchaseDate": "2026-07-01",
      "sourceType": "PURCHASE"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 9.2 资产详情

`POST /asset/detail`

说明：`categoryName`、`currentUserName`、`attachmentList` 为关联/扩展回显字段；其中 `attachmentList` 来自 `file_attachment` 表。

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "id": 1,
  "assetCode": "AF-202607-0001",
  "name": "ThinkPad T14",
  "categoryId": 8,
  "categoryName": "轻薄笔记本电脑",
  "departmentId": 3,
  "departmentName": "研发部",
  "location": "A-01-01",
  "currentUserId": 1,
  "currentUserName": "张三",
  "status": "ASSIGNED",
  "specification": "i7/16G/512G",
  "brandModel": "Lenovo ThinkPad T14",
  "sourceType": "PURCHASE",
  "purpose": "OFFICE",
  "purchaseAmount": 6500.00,
  "purchaseDate": "2026-07-01",
  "supplier": "联想供应商",
  "remark": "研发人员办公使用",
  "attachmentList": [
    {
      "id": 1,
      "fileName": "invoice.pdf",
      "fileUrl": "https://example.com/invoice.pdf"
    }
  ]
}
```

### 9.3 资产登记

`POST /asset/create`

请求体：

```json
{
  "name": "ThinkPad T14",
  "categoryId": 8,
  "departmentId": 3,
  "location": "A-01-01",
  "brandModel": "Lenovo ThinkPad T14",
  "specification": "i7/16G/512G",
  "sourceType": "PURCHASE",
  "purpose": "OFFICE",
  "purchaseAmount": 6500.00,
  "purchaseDate": "2026-07-01",
  "supplier": "联想供应商",
  "remark": "研发人员办公使用",
  "attachmentIds": [1, 2]
}
```

响应体：

```json
{
  "id": 1,
  "assetCode": "AF-202607-0001"
}
```

### 9.4 修改资产

`POST /asset/update`

请求体：

```json
{
  "id": 1,
  "name": "ThinkPad T14 Gen2",
  "categoryId": 8,
  "departmentId": 3,
  "location": "A-01-02",
  "brandModel": "Lenovo ThinkPad T14 Gen2",
  "specification": "i7/16G/1T",
  "sourceType": "PURCHASE",
  "purpose": "OFFICE",
  "purchaseAmount": 6500.00,
  "purchaseDate": "2026-07-01",
  "supplier": "联想供应商",
  "remark": "更新了规格信息",
  "attachmentIds": [1, 2]
}
```

响应体：

```json
{
  "success": true
}
```

### 9.5 删除资产

`POST /asset/delete`

说明：只允许删除未被业务单据引用的资产。

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "success": true
}
```

### 9.6 资产台账分页

`POST /asset/ledger/page`

说明：查询资产月度折旧/台账快照。

请求体：

```json
{
  "page": 1,
  "size": 20,
  "ledgerId": null,
  "snapshotMonth": "2026-07",
  "categoryId": null,
  "departmentId": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "ledgerId": 1,
      "assetId": 1,
      "assetCode": "AF-202607-0001",
      "assetName": "ThinkPad T14",
      "snapshotMonth": "2026-07",
      "originalAmount": 6500.00,
      "monthlyDepreciation": 171.53,
      "accumulatedDepreciation": 171.53,
      "netAmount": 6328.47
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 9.7 资产条码信息

`POST /asset/barcode/detail`

说明：当前 `asset` 表只有 `asset_code`，没有单独 `barcode` 字段。若需要打印条码，建议直接使用 `assetCode` 作为条码值。

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "assetId": 1,
  "assetCode": "AF-202607-0001",
  "barcodeValue": "AF-202607-0001",
  "name": "ThinkPad T14",
  "categoryName": "轻薄笔记本电脑",
  "departmentName": "研发部",
  "currentUserName": "张三"
}
```

---

## 十、审批中心

### 10.1 待审批分页

`POST /approval/todo/page`

请求体：

```json
{
  "page": 1,
  "size": 20,
  "businessType": null,
  "keyword": ""
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "processNo": "AP2026070001",
      "businessType": "RECEIVE",
      "businessId": 1,
      "title": "张三提交资产申领",
      "status": "PENDING",
      "applicantId": 1,
      "applicantName": "张三",
      "createdAt": "2026-07-18 10:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 10.2 已审批分页

`POST /approval/done/page`

请求体：

```json
{
  "page": 1,
  "size": 20,
  "businessType": null,
  "keyword": "",
  "decision": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 2,
      "processNo": "AP2026070002",
      "businessType": "SCRAP",
      "title": "李四提交报废申请",
      "decision": "APPROVED",
      "approvedAt": "2026-07-18 11:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 10.3 审批详情

`POST /approval/detail`

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "id": 1,
  "processNo": "AP2026070001",
  "businessType": "RECEIVE",
  "businessId": 1,
  "title": "张三提交资产申领",
  "status": "PENDING",
  "applicantId": 1,
  "applicantName": "张三",
  "currentApproverId": 8,
  "currentApproverName": "李主管",
  "formData": {},
  "historyList": [
    {
      "nodeName": "部门主管审批",
      "approverName": "李主管",
      "decision": "PENDING",
      "comment": "",
      "actionTime": null
    }
  ]
}
```

### 10.4 审批操作

`POST /approval/action`

说明：审批通过或驳回。

请求体：

```json
{
  "id": 1,
  "decision": "APPROVED",
  "comment": "同意办理"
}
```

响应体：

```json
{
  "success": true,
  "status": "APPROVED"
}
```

### 10.5 转交审批

`POST /approval/transfer`

请求体：

```json
{
  "id": 1,
  "targetApproverId": 9,
  "comment": "请协助处理"
}
```

响应体：

```json
{
  "success": true
}
```

---

## 十一、资产申领

### 11.1 创建申领单

`POST /receive-order/create`

说明：当前数据库 `requisition_order_item` 是按“分类 + 数量”建模，不是按具体 `assetId` 建模。

请求体：

```json
{
  "reason": "新员工入职办公使用",
  "expectedDate": null,
  "itemList": [
    {
      "categoryId": 8,
      "quantity": 1
    }
  ]
}
```

响应体：

```json
{
  "id": 1,
  "orderNo": "RE2026070001",
  "approvalStatus": "PENDING",
  "createdAt": "2026-07-19 10:00:00"
}
```

### 11.2 申领单分页

`POST /receive-order/page`

请求体：

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "approvalStatus": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "orderNo": "RE2026070001",
      "applicantName": "张三",
      "approvalStatus": "PENDING",
      "createdAt": "2026-07-18 10:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 11.3 申领单详情

`POST /receive-order/detail`

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "id": 1,
  "orderNo": "RE2026070001",
  "applicantId": 1,
  "applicantName": "张三",
  "reason": "新员工入职办公使用",
  "approvalStatus": "PENDING",
  "itemList": [
    {
      "id": 1,
      "categoryId": 8,
      "categoryName": "轻薄笔记本电脑",
      "quantity": 1,
      "assetCode": "AF-202607-0001",
      "assetName": "ThinkPad T14"
    }
  ]
}
```

### 11.4 出库确认

`POST /receive-order/confirm-outbound`

说明：审批通过后由仓管或资产管理员确认出库，出库时再把具体资产信息回填到 `requisition_order_item`。

请求体：

```json
{
  "id": 1,
  "confirmRemark": "已发放给张三",
  "itemList": [
    {
      "itemId": 1,
      "assetId": 1
    }
  ]
}
```

响应体：

```json
{
  "success": true,
  "orderStatus": "COMPLETED"
}
```

---

## 十二、资产转移

### 12.1 创建转移单

`POST /transfer-order/create`

说明：当前数据库 `transfer_order_item` 没有 `remark` 字段，明细应以具体资产为主。

请求体：

```json
{
  "fromDepartmentId": 3,
  "toDepartmentId": 5,
  "fromUserId": 1,
  "toUserId": 6,
  "reason": "人员调整",
  "itemList": [
    {
      "assetId": 1
    }
  ]
}
```

响应体：

```json
{
  "id": 1,
  "orderNo": "TR2026070001",
  "approvalStatus": "PENDING"
}
```

### 12.2 转移单分页

`POST /transfer-order/page`

请求体：

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "approvalStatus": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "orderNo": "TR2026070001",
      "fromDepartmentName": "研发部",
      "toDepartmentName": "测试部",
      "approvalStatus": "PENDING",
      "createdAt": "2026-07-18 10:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 12.3 转移单详情

`POST /transfer-order/detail`

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "id": 1,
  "orderNo": "TR2026070001",
  "fromDepartmentId": 3,
  "fromDepartmentName": "研发部",
  "toDepartmentId": 5,
  "toDepartmentName": "测试部",
  "fromUserId": 1,
  "fromUserName": "张三",
  "toUserId": 6,
  "toUserName": "王五",
  "reason": "人员调整",
  "approvalStatus": "PENDING",
  "itemList": [
    {
      "assetId": 1,
      "assetCode": "AF-202607-0001",
      "assetName": "ThinkPad T14"
    }
  ]
}
```

---

## 十三、资产归还

### 13.1 创建归还单

`POST /return-order/create`

说明：当前数据库 `return_order_item` 使用 `condition_remark` 表示归还状况描述。

请求体：

```json
{
  "reason": "离职归还",
  "itemList": [
    {
      "assetId": 1,
      "returnCondition": "NORMAL",
      "conditionRemark": "外观完好"
    }
  ]
}
```

响应体：

```json
{
  "id": 1,
  "orderNo": "RT2026070001",
  "approvalStatus": "PENDING"
}
```

### 13.2 归还单分页

`POST /return-order/page`

请求体：

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "approvalStatus": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "orderNo": "RT2026070001",
      "applicantName": "张三",
      "approvalStatus": "PENDING",
      "createdAt": "2026-07-18 10:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 13.3 归还单详情

`POST /return-order/detail`

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "id": 1,
  "orderNo": "RT2026070001",
  "reason": "离职归还",
  "approvalStatus": "PENDING",
  "itemList": [
    {
      "assetId": 1,
      "assetCode": "AF-202607-0001",
      "assetName": "ThinkPad T14",
      "returnCondition": "NORMAL",
      "conditionRemark": "外观完好"
    }
  ]
}
```

### 13.4 入库确认

`POST /return-order/confirm-inbound`

请求体：

```json
{
  "id": 1,
  "storageLocation": "A-02-03",
  "confirmRemark": "已验收并入库"
}
```

响应体：

```json
{
  "success": true,
  "orderStatus": "COMPLETED"
}
```

---

## 十四、资产报废

### 14.1 创建报废单

`POST /scrap-order/create`

说明：当前数据库 `scrap_order_item` 没有明细备注字段，报废原因放在主单 `reason` 中。

请求体：

```json
{
  "reason": "设备损坏严重无法维修",
  "itemList": [
    {
      "assetId": 1
    }
  ],
  "attachmentIds": [3, 4]
}
```

响应体：

```json
{
  "id": 1,
  "orderNo": "SC2026070001",
  "approvalStatus": "PENDING"
}
```

### 14.2 报废单分页

`POST /scrap-order/page`

请求体：

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "approvalStatus": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "orderNo": "SC2026070001",
      "applicantName": "张三",
      "approvalStatus": "PENDING",
      "createdAt": "2026-07-18 10:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 14.3 报废单详情

`POST /scrap-order/detail`

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "id": 1,
  "orderNo": "SC2026070001",
  "reason": "设备损坏严重无法维修",
  "approvalStatus": "PENDING",
  "itemList": [
    {
      "assetId": 1,
      "assetCode": "AF-202607-0001",
      "assetName": "ThinkPad T14"
    }
  ],
  "attachmentList": [
    {
      "id": 3,
      "fileName": "damage.jpg",
      "fileUrl": "https://example.com/damage.jpg"
    }
  ]
}
```

---

## 十五、盘点管理

### 15.1 创建盘点任务

`POST /inventory-task/create`

说明：按范围创建盘点任务，`scopeValue` 和 `assetStatusFilter` 对应数据库 JSON 字段。当前表结构只有 `deadline`，没有 `plannedStartTime`、`plannedEndTime`、`remark`。

请求体：

```json
{
  "taskName": "2026年7月研发部盘点",
  "scopeType": "DEPARTMENT",
  "scopeValue": {
    "departmentIds": [3]
  },
  "assetStatusFilter": ["ASSIGNED", "STOCK"],
  "deadline": "2026-07-25",
  "responsibleUserId": 8
}
```

响应体：

```json
{
  "id": 1,
  "status": "PENDING",
  "detailCount": 20,
  "createdAt": "2026-07-19 10:00:00"
}
```

### 15.2 盘点任务分页

`POST /inventory-task/page`

请求体：

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "status": null,
  "scopeType": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "taskName": "2026年7月研发部盘点",
      "scopeType": "DEPARTMENT",
      "status": "IN_PROGRESS",
      "deadline": "2026-07-25",
      "responsibleUserId": 8,
      "createdAt": "2026-07-19 10:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 15.3 盘点任务详情

`POST /inventory-task/detail`

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "id": 1,
  "taskName": "2026年7月研发部盘点",
  "scopeType": "DEPARTMENT",
  "scopeValue": {
    "departmentIds": [3]
  },
  "assetStatusFilter": ["ASSIGNED", "STOCK"],
  "status": "IN_PROGRESS",
  "deadline": "2026-07-25",
  "responsibleUserId": 8,
  "createdAt": "2026-07-19 10:00:00",
  "updatedAt": "2026-07-19 10:30:00"
}
```

### 15.4 盘点明细分页

`POST /inventory-detail/page`

说明：`systemUserName`、`actualUserName` 可通过关联 `user` 表回显；当前表中没有部门字段。

请求体：

```json
{
  "page": 1,
  "size": 50,
  "taskId": 1,
  "result": null,
  "keyword": ""
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "taskId": 1,
      "assetId": 1,
      "assetCode": "AF-202607-0001",
      "assetName": "ThinkPad T14",
      "systemUserId": 1,
      "systemUserName": "张三",
      "result": "NORMAL",
      "actualUserId": 1,
      "actualUserName": "张三"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 50
}
```

### 15.5 提交盘点结果

`POST /inventory-detail/submit`

说明：逐条或批量提交盘点结果。当前表中没有 `actualDepartmentId` 字段，如需记录部门，需要扩表或从 `actualUserId` 反查。

请求体：

```json
{
  "taskId": 1,
  "detailList": [
    {
      "detailId": 1,
      "result": "NORMAL",
      "actualUserId": 1,
      "remark": "账实一致"
    },
    {
      "detailId": 2,
      "result": "LOSS",
      "actualUserId": null,
      "remark": "现场未找到"
    }
  ]
}
```

响应体：

```json
{
  "success": true,
  "submittedCount": 2
}
```

### 15.6 盘点报告

`POST /inventory-task/report`

说明：该接口为统计型接口，返回值可由 `inventory_detail` 聚合计算得到，不要求报表表结构一一对应。

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "taskId": 1,
  "taskName": "2026年7月研发部盘点",
  "totalCount": 20,
  "normalCount": 18,
  "lossCount": 1,
  "gainCount": 0,
  "mismatchCount": 1,
  "lossRate": 0.05
}
```

### 15.7 更新盘点结论

`POST /inventory-task/conclusion`

说明：当前数据库表 `inventory_task` 没有 `conclusion`、`completed_at` 等字段，接口暂不建议实现。若要保留，需要先调整表结构。

建议调整表结构后再使用，示例请求体：

```json
{
  "id": 1,
  "conclusion": "本次盘点发现1台资产盘亏，已生成盘亏处理记录",
  "status": "COMPLETED"
}
```

响应体：

```json
{
  "success": true
}
```

---

## 十六、盘亏处理

### 16.1 盘亏单分页

`POST /loss-order/page`

请求体：

```json
{
  "page": 1,
  "size": 20,
  "keyword": "",
  "handleStatus": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "lossNo": "LO2026070001",
      "assetCode": "AF-202607-0001",
      "assetName": "ThinkPad T14",
      "handleStatus": "PENDING_COMPENSATION",
      "createdAt": "2026-07-18 12:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 16.2 盘亏单详情

`POST /loss-order/detail`

请求体：

```json
{
  "id": 1
}
```

响应体：

```json
{
  "id": 1,
  "lossNo": "LO2026070001",
  "inventoryTaskId": 1,
  "inventoryDetailId": 2,
  "assetId": 1,
  "assetCode": "AF-202607-0001",
  "assetName": "ThinkPad T14",
  "responsibleUserId": 1,
  "responsibleUserName": "张三",
  "responsibleDepartmentId": 3,
  "responsibleDepartmentName": "研发部",
  "lossAmount": 5200.00,
  "handleStatus": "PENDING_COMPENSATION",
  "remark": "盘点未找到"
}
```

### 16.3 盘亏处理

`POST /loss-order/handle`

说明：支持赔偿、免责等处理方式。

请求体：

```json
{
  "id": 1,
  "handleType": "COMPENSATE",
  "handleAmount": 5200.00,
  "remark": "责任人全额赔偿"
}
```

响应体：

```json
{
  "success": true,
  "handleStatus": "COMPENSATED"
}
```

免责示例：

```json
{
  "id": 1,
  "handleType": "EXEMPT",
  "handleAmount": 0,
  "remark": "因自然损耗免责"
}
```

---

## 十七、报表与折旧

### 17.1 资产明细报表

`POST /report/asset-detail`

说明：报表接口允许返回聚合或关联字段，不要求全部直接存在于单表中。

请求体：

```json
{
  "page": 1,
  "size": 50,
  "keyword": "",
  "categoryId": null,
  "departmentId": null,
  "status": null
}
```

响应体：

```json
{
  "records": [
    {
      "assetId": 1,
      "assetCode": "AF-202607-0001",
      "assetName": "ThinkPad T14",
      "categoryName": "轻薄笔记本电脑",
      "departmentName": "研发部",
      "userName": "张三",
      "status": "ASSIGNED",
      "purchasePrice": 6500.00,
      "netAmount": 6328.47
    }
  ],
  "total": 1,
  "page": 1,
  "size": 50
}
```

### 17.2 月度快照分页

`POST /report/ledger-snapshot/page`

说明：该接口主要来源于 `asset_ledger_snapshot` 表。

请求体：

```json
{
  "page": 1,
  "size": 20,
  "snapshotMonth": "2026-07",
  "categoryId": null,
  "departmentId": null
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "snapshotMonth": "2026-07",
      "assetCode": "AF-202607-0001",
      "assetName": "ThinkPad T14",
      "originalAmount": 6500.00,
      "monthlyDepreciation": 171.53,
      "accumulatedDepreciation": 171.53,
      "netAmount": 6328.47
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

### 17.3 月度汇总

`POST /report/monthly-summary`

说明：该接口为统计聚合接口，可由 `asset_ledger_snapshot` 按月汇总生成。

请求体：

```json
{
  "snapshotMonth": "2026-07"
}
```

响应体：

```json
{
  "snapshotMonth": "2026-07",
  "assetCount": 120,
  "originalAmountTotal": 820000.00,
  "monthlyDepreciationTotal": 22000.00,
  "accumulatedDepreciationTotal": 120000.00,
  "netAmountTotal": 700000.00,
  "categorySummary": [
    {
      "categoryId": 8,
      "categoryName": "轻薄笔记本电脑",
      "assetCount": 40,
      "originalAmountTotal": 260000.00,
      "netAmountTotal": 220000.00
    }
  ]
}
```

### 17.4 手动执行折旧

`POST /depreciation/execute`

请求体：

```json
{
  "runMonth": "2026-07"
}
```

响应体：

```json
{
  "id": 1,
  "runMonth": "2026-07",
  "processedCount": 120,
  "status": "RUNNING"
}
```

### 17.5 折旧执行记录分页

`POST /depreciation/page`

说明：当前数据库实际对应 `depreciation_run_log` 表。

请求体：

```json
{
  "page": 1,
  "size": 20,
  "runMonth": "2026-07"
}
```

响应体：

```json
{
  "records": [
    {
      "id": 1,
      "runMonth": "2026-07",
      "processedCount": 120,
      "skippedCount": 2,
      "totalMonthlyDepreciation": 22000.00,
      "startedAt": "2026-07-31 23:30:00",
      "completedAt": "2026-07-31 23:31:10",
      "status": "SUCCESS",
      "errorMessage": null
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20
}
```

---

## 十八、文件附件

### 18.1 上传文件

`POST /file/upload`

说明：`multipart/form-data` 上传，不走 JSON。

请求参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| `file` | file | 是 | 上传文件 |
| `bizType` | string | 否 | 业务类型，如 `ASSET`、`SCRAP` |

响应体：

```json
{
  "id": 1,
  "fileName": "invoice.pdf",
  "fileUrl": "https://example.com/file/invoice.pdf",
  "fileSize": 12034,
  "contentType": "application/pdf"
}
```

响应示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "fileName": "invoice.pdf",
    "fileUrl": "https://example.com/file/invoice.pdf",
    "fileSize": 12034,
    "contentType": "application/pdf"
  }
}
```

### 18.2 附件列表

`POST /file/list`

请求体：

```json
{
  "bizType": "SCRAP",
  "bizId": 1
}
```

响应体：

```json
[
  {
    "id": 1,
    "fileName": "damage.jpg",
    "fileUrl": "https://example.com/file/damage.jpg",
    "fileSize": 54321,
    "contentType": "image/jpeg"
  }
]
```

### 18.3 绑定业务记录

`POST /file/bind`

请求体：

```json
{
  "bizType": "SCRAP",
  "bizId": 1,
  "fileIds": [1, 2]
}
```

响应体：

```json
{
  "success": true
}
```

---

## 十九、推荐开发顺序

建议按下面顺序实现，方便你边做边联调：

1. 认证模块
2. 部门管理
3. 分类管理
4. 用户管理
5. 资产管理
6. 文件上传
7. 申领流程
8. 转移流程
9. 归还流程
10. 报废流程
11. 审批中心
12. 盘点与盘亏
13. 折旧与报表

---

## 二十、补充说明

### 20.1 为什么大量使用 POST

- 前端请求封装更简单
- 复杂筛选条件可以直接放请求体
- 统一风格后，个人项目开发效率更高

### 20.2 哪些地方后续还可以继续细化

- 为每个接口补充参数校验规则
- 为每个分页接口补充排序字段白名单
- 为审批类接口补充状态流转图
- 为资产、盘点、报表补充导出接口
- 为异常返回补充业务错误示例
