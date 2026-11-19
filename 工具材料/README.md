# 工具材料

**TestLink。** [五项用例 XML](TestLink五项用例.xml)是从小组用例库摘出的设计记录，可直接阅读。已有 Keyboard MES 项目时按编号查看原记录。用例执行的构建、状态及观察值见[设计与执行对照](../共用数据/01-TestLink用例与记录.csv)。

**Postman。** 在 Postman 导入[集合](MES接口测试.postman_collection.json)，新建本地环境，填写 `baseUrl`、`employeeNo`、`password`，按集合顺序运行。工单列表需要有可读数据，后续详情请求使用列表返回的标识。会话由登录请求提取。复测后保存断言摘要，环境文件和完整登录响应留在本机。

**JMeter。** [MES 只读负载](MES只读负载.jmx)默认访问本机 8088 端口。使用其他实验实例时先在计划中调整请求地址。在仓库外的本机属性文件填写 `employeeNo` 和 `password`，然后在工具材料目录运行。每次使用新的输出文件名。

```text
jmeter -n -t "MES只读负载.jmx" -q "本机属性文件.properties" -Jthreads=1 -Jramp=1 -Jloops=10 -l "baseline.jtl"
jmeter -n -t "MES只读负载.jmx" -q "本机属性文件.properties" -Jthreads=10 -Jramp=5 -Jloops=20 -l "load.jtl"
```

**Badboy。** 在本地已构建后端的 `target/classes/static/` 放入[录制辅助页](badboy-lab6.html)，通过同一服务访问 `/badboy-lab6.html`。正常启动 Badboy 图形界面，开启录制，依次打开页面、点击健康检查、接口索引和接口契约，保存并导出自己的脚本。[带响应断言的 JMeter 导出示例](Badboy导出校验.jmx)可对照四个只读请求的顺序和断言。业务负载统计与此处的四请求回放分别记录。
