# 智能家电远程控制系统（Spring Cloud + MQTT + MongoDB + MySQL + DataV/ECharts）

## 目录结构

- `infra/`：MySQL、MongoDB、EMQX 的 docker-compose
- `backend/`：Java 17 + Spring Cloud 微服务（Maven 多模块）
- `frontend/`：Vue3 + Vite + DataV + ECharts 大屏

## 运行前置

- **JDK**：17+
- **Node.js**：18+（推荐 20+）
- **Docker Desktop**：用于启动 MySQL/MongoDB/EMQX

## 1) 启动基础设施（MySQL/MongoDB/EMQX）

在项目根目录执行：

```bash
cd infra
docker compose up -d
```

默认端口：

- EMQX 控制台：`http://localhost:18083`（admin/public）
- MQTT：`tcp://localhost:1883`
- MySQL：`localhost:3306`（root/root）
- MongoDB：`localhost:27017`

## 2) 初始化 MySQL 表结构

MySQL 连接信息见 `infra/mysql/init/01_schema.sql`（会在容器启动时自动导入）。

## 3) 启动后端（微服务）

推荐用 IDEA 打开 `backend/`，或命令行启动：

```bash
cd backend
mvn -q -DskipTests spring-boot:run -pl services/gateway
mvn -q -DskipTests spring-boot:run -pl services/user-device
mvn -q -DskipTests spring-boot:run -pl services/device-data
mvn -q -DskipTests spring-boot:run -pl services/device-control
mvn -q -DskipTests spring-boot:run -pl services/datav
```

默认端口：

- 网关：`http://localhost:8080`
- user-device：`http://localhost:8101`
- device-data：`http://localhost:8102`
- device-control：`http://localhost:8103`
- datav：`http://localhost:8104`

## 4) 启动前端（大屏）

```bash
cd frontend
npm i
npm run dev
```

访问：`http://localhost:5173`

## 5) MQTTX 模拟设备（4 个客户端）

建议用 MQTTX 创建 4 个客户端（clientId）：

- 空调：`ac001`
- 冰箱：`fr001`
- 洗衣机：`wa001`
- 灯光：`li001`

主题约定：

- 状态上报：`home/device/{deviceId}/status`
- 指令下发：`home/device/{deviceId}/control`
- 指令回执：`home/device/{deviceId}/control/result`

你可以先用 MQTTX 定时向 `home/device/ac001/status` 发送 JSON 状态，刷新大屏左侧卡片；
再通过大屏点击控制按钮，下发指令到 `.../control`，然后在 MQTTX 手动回执到 `.../control/result`。

