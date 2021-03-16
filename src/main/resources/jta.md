
### 概述
`Java`事务`API`（`Java Transaction API`，简称`JTA`）是一个`Java`企业版的应用程序接口，在`Java`环境中，允许完成跨越多个`XA`资源的分布式事务。<br/>
`JTA`和它的同胞`Java`事务服务(`JTS；Java TransactionService`)，为`J2EE`平台提供了分布式事务服务。不过`JTA`只是提供了一个接口，并没有提供具体的实现，而是由`j2ee`服务器提供商 根据`JTS`规范提供的，常见的`JTA`实现有以下几种：
1. `J2EE`容器所提供的`JTA`实现(`JBoss`)
2. 独立的`JTA`实现：如`JOTM`，`Atomikos`.这些实现可以应用在那些不使用`J2EE`应用服务器的环境里用以提供分布事事务保证。如`Tomcat`,`Jetty`以及普通的`java`应用。

### JTA API
`JTA`里面提供了`java.transaction.UserTransaction`，里面定义了下面几个方法：
1. `begin`：开启一个事务
2. `commit`：提交当前事务
3. `rollback`：回滚当前事务
4. `setRollbackOnly`：把当前事务标记为回滚
5. `setTransactionTimeout`：设置事务的事件，超过这个事件，就抛出异常，回滚事务

```java
// 取得JTA事务
javax.transaction.UserTransaction tx = (javax.transaction.UserTransaction) context.lookup("java:comp/UserTransaction");
// 取得数据库连接池，必须有支持XA的数据库、驱动程序 
javax.sql.DataSource ds = (javax.sql.DataSource) context.lookup("java:/XAOracleDS");  
tx.begin();
java.sql.Connection conn = ds.getConnection();
// 将自动提交设置为 false，若设置为 true 则数据库将会把每一次数据更新认定为一个事务并自动提交
conn.setAutoCommit(false);
stmt = conn.createStatement(); 
// 将 A 账户中的金额减少 500 
stmt.execute("update t_account set amount = amount - 500 where account_id = 'A'");
// 将 B 账户中的金额增加 500 
stmt.execute("update t_account set amount = amount + 500 where account_id = 'B'");
// 提交事务 若出现异常则使用 tx.rollback()
tx.commit();
```
### JTA的优缺点
标准的`JTA`方式的事务管理在日常开发中并不常用<br/>
优点：提供了分布式事务的解决方案，严格的`ACID`<br/>
缺点：
1. 实现复杂
2. 通常情况下，`JTA UserTransaction`需要从`JNDI`获取。这意味着，如果我们使用`JTA`，就需要同时使用`JTA`和`JNDI`。
3. `JTA`本身就是个笨重的`API`，通常`JTA`只能在应用服务器环境下使用，因此使用`JTA`会限制代码的复用性。

---
*参考链接：*
1. [MarkDown基本语法](https://www.jianshu.com/p/191d1e21f7ed)
2. [JTA事务简述](https://blog.csdn.net/rentuo53/article/details/84923350)
3. [JTA Oracle Home Page](https://www.oracle.com/java/technologies/jta.html)
4. [Spring的分布式事务实现(JTA+XA/2PC)](https://www.jdon.com/48829)

