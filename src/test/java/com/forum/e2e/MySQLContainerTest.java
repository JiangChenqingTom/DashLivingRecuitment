package com.forum.e2e;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;

// 启用Testcontainers扩展，自动管理容器生命周期
@Testcontainers
public class MySQLContainerTest {

    // 静态代码块：配置Docker客户端（适配Windows环境）
    static {
        // 指定Docker API版本（与你的Docker Engine匹配）
        System.setProperty("docker.api.version", "1.51");
        // 强制Windows下的Docker连接路径（npipe协议）
        System.setProperty("docker.host", "npipe:////./pipe/docker_engine");
        // 启用调试日志（可选，用于排查问题）
        System.setProperty("testcontainers.debug", "true");
    }

    // 定义MySQL容器（使用JUnit 5的@Container注解，自动启动/停止）
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.33"))
            .withEnv("MYSQL_ROOT_PASSWORD", "root_pass") // 必须设置root密码
            .withDatabaseName("test_db") // 可选：指定数据库名
            .withUsername("test_user") // 可选：创建自定义用户
            .withPassword("test_pass") // 可选：自定义用户密码
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("MySQLContainer"))) // 容器日志输出
            .waitingFor(Wait.forLogMessage(".*ready for connections.*", 1) // 等待MySQL就绪
                    .withStartupTimeout(Duration.ofMinutes(3))); // 延长超时时间

    // 测试容器启动及数据库连接
    @Test
    void testMySQLContainer() throws Exception {
        // 验证容器状态
        assert mysql.isRunning() : "MySQL容器未启动";
        System.out.println("MySQL容器已启动，JDBC URL: " + mysql.getJdbcUrl());

        // 验证数据库连接
        try (Connection connection = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword()
        )) {
            assert connection != null : "数据库连接失败";
            System.out.println("数据库连接成功！");

            // 执行简单查询验证功能
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE test (id INT)");
                statement.execute("INSERT INTO test VALUES (1)");
                ResultSet resultSet = statement.executeQuery("SELECT id FROM test");

                assert resultSet.next() : "查询结果为空";
                assert resultSet.getInt("id") == 1 : "查询结果不正确";
                System.out.println("数据库操作验证成功！");
            }
        }
    }
}
