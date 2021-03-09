package net.ruixin.xa;

import com.mysql.jdbc.jdbc2.optional.MysqlXAConnection;
import com.mysql.jdbc.jdbc2.optional.MysqlXid;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * MySql xa test
 *
 * @author mxding
 * @date 2021-03-08 10:42
 */
public class MySQLXATest {
    public static void main(String[] args) throws Exception {
        // 获得资源管理器操作接口实例 RM1
        Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "1234");
        XAConnection xaConn1 = new MysqlXAConnection((com.mysql.jdbc.Connection) conn1, true);
        XAResource rm1 = xaConn1.getXAResource();
        // 获得资源管理器操作接口实例 RM2
        Connection conn2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "1234");
        XAConnection xaConn2 = new MysqlXAConnection((com.mysql.jdbc.Connection) conn2, true);
        XAResource rm2 = xaConn2.getXAResource();
        // AP请求TM执行一个分布式事务，TM生成全局事务id
        byte[] gtrid = "g12345".getBytes();
        int formatId = 1;
        try {
            // ==============分别执行RM1和RM2上的事务分支====================
            // TM生成rm1上的事务分支id
            byte[] bqual1 = "b00001".getBytes();
            Xid xid1 = new MysqlXid(gtrid, bqual1, formatId);
            // 执行rm1上的事务分支
            rm1.start(xid1, XAResource.TMNOFLAGS);//One of TMNOFLAGS, TMJOIN, or TMRESUME.
            PreparedStatement ps1 = conn1.prepareStatement("INSERT into t(name) VALUES ('rm1po')");
            ps1.execute();
            rm1.end(xid1, XAResource.TMSUCCESS);
            // TM生成rm2上的事务分支id
            byte[] bqual2 = "b00002".getBytes();
            Xid xid2 = new MysqlXid(gtrid, bqual2, formatId);
            // 执行rm2上的事务分支
            rm2.start(xid2, XAResource.TMNOFLAGS);
            PreparedStatement ps2 = conn2.prepareStatement("INSERT into t(name) VALUES ('123456789012345678901')");
            ps2.execute();
            rm2.end(xid2, XAResource.TMSUCCESS);
            // ===================两阶段提交================================
            // phase1：询问所有的RM 准备提交事务分支
            int rm1_prepare = rm1.prepare(xid1);
            int rm2_prepare = rm2.prepare(xid2);
            // phase2：提交所有事务分支
            boolean onePhase = false; //TM判断有2个事务分支，所以不能优化为一阶段提交
            if (rm1_prepare == XAResource.XA_OK
                    && rm2_prepare == XAResource.XA_OK
            ) {//所有事务分支都prepare成功，提交所有事务分支
                rm1.commit(xid1, onePhase);
                rm2.commit(xid2, onePhase);
            } else {//如果有事务分支没有成功，则回滚
                rm1.rollback(xid1);
                rm1.rollback(xid2);
            }
        } catch (XAException e) {
            // 如果出现异常，也要进行回滚
            e.printStackTrace();
        }
    }
}

/*
 * 参考链接：
 * https://blog.csdn.net/l1028386804/article/details/79769043
 */

/*
 * Start Example:
 * mysql> XA START 'g12345';  //其中'g12345’就是xid的值
 * Query OK, 0 rows affected (0.00 sec)
 *
 * mysql> INSERT into t(name) VALUES ('rm1po')
 * Query OK, 1 row affected (0.00 sec)
 *
 * mysql> XA END 'g12345';
 * Query OK, 0 rows affected (0.00 sec)
 *
 * mysql> XA PREPARE 'g12345';
 * Query OK, 0 rows affected (0.01 sec)
 *
 * mysql> XA COMMIT 'g12345';
 * Query OK, 0 rows affected (0.01 sec)
 *
 * End Example
 */
