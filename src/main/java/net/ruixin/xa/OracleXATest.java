package net.ruixin.xa;

import oracle.jdbc.xa.OracleXid;
import oracle.jdbc.xa.client.OracleXAConnection;

import java.sql.*;

import javax.transaction.xa.*;

/**
 * Oracle XA test
 * @author mxding
 * @date 2021-03-08 10:42
 */
class OracleXATest {

    public static void main(String[] args) {

        try {
            // 获取connection
//            Connection connection1 = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/orcl", "scott", "tiger");
            Connection connection1 = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.186:1521/orcl", "scott", "tiger");
            OracleXAConnection oracleXAConnection1 = new OracleXAConnection(connection1);
            Connection connection2 = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.186:1521/orcl", "scott", "tiger");
            OracleXAConnection oracleXAConnection2 = new OracleXAConnection(connection2);

            // OracleXid(int fId, byte[] gId, byte[] bId)
            // fId - format ID
            // gId - global transaction ID (no copy performed)
            // bId - branch Qualifier (no copy performed)
            Xid xid1 = new OracleXid(0x1234, "g0001".getBytes(), "bqual001".getBytes());
            Xid xid2 = new OracleXid(0x1234, "g0001".getBytes(), "bqual002".getBytes());

            // 获取资源
            XAResource xaResource1 = oracleXAConnection1.getXAResource();
            XAResource xaResource2 = oracleXAConnection2.getXAResource();

            // Start the Resources
            xaResource1.start(xid1, XAResource.TMNOFLAGS);
            xaResource2.start(xid2, XAResource.TMNOFLAGS);

            try {
                // Do something
                Statement stmt1 = oracleXAConnection1.getConnection().createStatement();
                stmt1.executeUpdate("insert into t(id) values (1)");
                stmt1.close();
                Statement stmt2 = oracleXAConnection2.getConnection().createStatement();
                stmt2.executeUpdate("insert into t(id) values (2)");
                stmt2.close();
            } catch (Exception e)  {
                e.printStackTrace();
            }


            // End the Resources
            xaResource1.end(xid1, XAResource.TMSUCCESS);
            xaResource2.end(xid2, XAResource.TMSUCCESS);

            // Prepare the RMs
            int prepare1 = xaResource1.prepare(xid1);
            int prepare2 = xaResource2.prepare(xid2);
            System.out.println("Return value of prepare 1 is " + prepare1);
            System.out.println("Return value of prepare 2 is " + prepare2);

            System.out.println("Is xaResource1 same as xaResource2 ? " + xaResource1.isSameRM(xaResource2));

            if (prepare1 == XAResource.XA_OK && prepare2 == XAResource.XA_OK) {
                xaResource1.commit(xid1, false);
                xaResource2.commit(xid2, false);
                System.out.println("all commit");
            } else {
                xaResource1.rollback(xid1);
                xaResource2.rollback(xid2);
                System.out.println("all rollback");
            }

            connection1.close();
            connection2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


/*
 * 参考链接：
 * http://www.orafaq.com/wiki/XA_FAQ
 * https://oracle-base.com/articles/11g/dbms_xa_11gR1
 *
 */
