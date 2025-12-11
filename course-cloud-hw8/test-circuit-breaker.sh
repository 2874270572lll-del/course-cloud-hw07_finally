#!/bin/bash

echo "🔥 熔断降级测试开始"
echo "===================="
echo ""

echo "1️⃣ 停止所有 user-service 实例..."
docker stop user-service-1 user-service-2 user-service-3
echo "✅ 已停止"
echo ""

sleep 2

echo "2️⃣ 触发熔断 - 连续请求5次..."
for i in {1..5}; do
  echo "--- 第 $i 次请求 ---"
  response=$(curl -s http://localhost:8083/api/enrollments/test)
  status=$(echo $response | jq -r '.["user-service"].status // .["user-service"].error' 2>/dev/null)
  echo "User Service 状态: $status"
  sleep 1
done
echo ""

echo "3️⃣ 查看熔断日志..."
docker logs enrollment-service --tail 20 | grep -i "circuit\|fallback\|error" | tail -5
echo ""

echo "4️⃣ 恢复 user-service..."
docker start user-service-1 user-service-2 user-service-3
echo "✅ 已启动,等待30秒..."
sleep 30
echo ""

echo "5️⃣ 测试服务恢复..."
echo "--- 立即请求 (熔断器仍开启) ---"
curl -s http://localhost:8083/api/enrollments/test | jq '.["user-service"]'
echo ""

echo "等待12秒让熔断器进入半开状态..."
sleep 12

echo "--- 12秒后请求 (熔断器半开/关闭) ---"
curl -s http://localhost:8083/api/enrollments/test | jq '.["user-service"]'
echo ""

echo "✅ 熔断降级测试完成!"
