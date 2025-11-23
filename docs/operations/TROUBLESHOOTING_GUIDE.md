# Troubleshooting Guide

## Quick Diagnostics

### System Health Check

```bash
# Run comprehensive health check
./scripts/health-check.sh

# Check all services
kubectl get pods -n supplyboost
kubectl get svc -n supplyboost

# Check metrics
curl http://prometheus:9090/api/v1/query?query=up
```

## Common Issues

### 1. Service Returns 500 Errors

**Symptoms:**
- API returns HTTP 500
- Error rate spike in Grafana
- Errors in application logs

**Diagnosis:**
```bash
# Check service logs
kubectl logs -f deployment/identity-service -n supplyboost

# Check error rate in Prometheus
# Query: rate(http_server_requests_seconds_count{status="500"}[5m])

# Review Jaeger traces for failed requests
```

**Solutions:**
1. Check database connectivity
2. Verify Kafka connection
3. Check for circuit breaker open state
4. Review recent deployments/changes
5. Check resource constraints (CPU/Memory)

### 2. Slow Response Times

**Symptoms:**
- P95 latency > 1s
- Timeout errors
- User complaints

**Diagnosis:**
```bash
# Check response time metrics
# Grafana Dashboard: Response Time (P95/P99)

# Identify slow endpoints in traces
# Jaeger: Filter by duration > 1s

# Check database query performance
kubectl logs deployment/identity-service -n supplyboost | grep "slow query"
```

**Solutions:**
1. Add database indexes
2. Implement caching
3. Optimize N+1 queries
4. Scale horizontally
5. Review circuit breaker settings

### 3. Database Connection Pool Exhausted

**Symptoms:**
- "Connection pool exhausted" errors
- Increasing wait times for connections
- HikariCP metrics showing max connections

**Diagnosis:**
```bash
# Check connection pool metrics
# Query: hikaricp_connections_active / hikaricp_connections_max

# Check for connection leaks
kubectl logs deployment/identity-service -n supplyboost | grep -i "leak"
```

**Solutions:**
```yaml
# Increase pool size in application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### 4. Kafka Consumer Lag

**Symptoms:**
- Messages not processed
- Increasing consumer lag
- Delayed notifications/events

**Diagnosis:**
```bash
# Check consumer lag
kubectl exec -it kafka-0 -n supplyboost -- \
  kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group identity-service-group

# Check Kafka metrics in Grafana
# Query: kafka_consumer_lag
```

**Solutions:**
1. Scale consumers (increase replicas)
2. Increase partition count
3. Optimize message processing
4. Check for consumer errors
5. Review consumer configuration

### 5. Memory Leaks

**Symptoms:**
- Gradual memory increase
- OOMKilled pod restarts
- High GC activity

**Diagnosis:**
```bash
# Check memory usage trend
# Grafana: JVM Heap Memory Usage dashboard

# Generate heap dump
kubectl exec <pod-name> -n supplyboost -- \
  jmap -dump:format=b,file=/tmp/heap.hprof <PID>

# Copy heap dump locally
kubectl cp supplyboost/<pod-name>:/tmp/heap.hprof ./heap.hprof
```

**Solutions:**
1. Analyze heap dump with Eclipse MAT
2. Check for unclosed resources
3. Review cache configurations
4. Increase memory limits
5. Enable GC logging

### 6. Circuit Breaker Open

**Symptoms:**
- Cascade failures
- "Circuit breaker is OPEN" errors
- Fallback methods executing

**Diagnosis:**
```bash
# Check circuit breaker state
curl http://identity-service:8081/actuator/health | jq '.circuitBreakers'

# Review metrics
# Query: resilience4j_circuitbreaker_state
```

**Solutions:**
1. Fix underlying service issue
2. Wait for circuit breaker to half-open
3. Adjust circuit breaker thresholds
4. Implement better fallbacks

### 7. Authentication Failures

**Symptoms:**
- 401 Unauthorized errors
- JWT validation failures
- Token expiration issues

**Diagnosis:**
```bash
# Check Keycloak connectivity
curl http://keycloak:8080/auth/realms/supplyboost

# Verify JWT secret
kubectl get secret identity-service-secret -n supplyboost -o jsonpath='{.data.jwt\.secret}' | base64 -d

# Check token expiration
# Decode JWT at https://jwt.io
```

**Solutions:**
1. Verify JWT secret matches across services
2. Check token expiration time
3. Ensure Keycloak is accessible
4. Review security configuration

### 8. Elasticsearch Connection Issues

**Symptoms:**
- Product search not working
- "Connection refused" to Elasticsearch
- Indexing failures

**Diagnosis:**
```bash
# Check Elasticsearch health
kubectl exec -it elasticsearch-0 -n supplyboost -- \
  curl http://localhost:9200/_cluster/health?pretty

# Verify index exists
kubectl exec -it elasticsearch-0 -n supplyboost -- \
  curl http://localhost:9200/_cat/indices
```

**Solutions:**
1. Restart Elasticsearch pod
2. Recreate indices
3. Check Elasticsearch logs
4. Verify cluster health

## Performance Tuning

### JVM Tuning

```yaml
# Add to deployment.yml
env:
  - name: JAVA_OPTS
    value: |
      -Xms512m
      -Xmx1024m
      -XX:+UseG1GC
      -XX:MaxGCPauseMillis=200
      -XX:+HeapDumpOnOutOfMemoryError
      -XX:HeapDumpPath=/tmp/heapdump.hprof
```

### Database Optimization

```sql
-- Find slow queries
SELECT pid, now() - pg_stat_activity.query_start AS duration, query
FROM pg_stat_activity
WHERE state = 'active' AND now() - pg_stat_activity.query_start > interval '5 seconds';

-- Add missing indexes
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_order_user_id ON orders(user_id);
```

### Caching Strategy

```java
// Add to Spring Boot application
@EnableCaching
@Configuration
public class CacheConfig {

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .disableCachingNullValues();

    return RedisCacheManager.builder(factory)
        .cacheDefaults(config)
        .build();
  }
}
```

## Monitoring Queries

### Useful Prometheus Queries

```promql
# Error rate
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (job)
  / sum(rate(http_server_requests_seconds_count[5m])) by (job)

# Response time P99
histogram_quantile(0.99,
  sum(rate(http_server_requests_seconds_bucket[5m])) by (job, le))

# Memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# Active database connections
hikaricp_connections_active / hikaricp_connections_max

# Kafka consumer lag
kafka_consumer_lag{job=~".*-service"}
```

## Escalation Path

1. **Level 1**: Check logs and metrics
2. **Level 2**: Review recent changes, consult runbooks
3. **Level 3**: Contact on-call engineer
4. **Level 4**: Initiate major incident response
5. **Level 5**: Executive escalation

## Additional Resources

- [Deployment Runbook](./DEPLOYMENT_RUNBOOK.md)
- [Monitoring Playbook](./MONITORING_PLAYBOOK.md)
- [Architecture Documentation](../ARCHITECTURE.md)
- [Grafana Dashboards](http://grafana.supplyboost.example.com)
- [Jaeger UI](http://jaeger.supplyboost.example.com)
