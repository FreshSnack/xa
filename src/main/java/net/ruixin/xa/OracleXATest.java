package net.ruixin.xa;

import java.sql.*;

import oracle.jdbc.xa.OracleXid;
import oracle.jdbc.xa.client.OracleXAConnection;

import javax.transaction.xa.*;

/**
 * Oracle XA test
 * @author mxding
 * @date 2021-03-08 10:42
 */
class OracleXATest {
    public static void main(String[] args) throws Exception {
        // 获取connection
        Connection connection1 = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.115:1521/orcl", "scott", "tiger");
        OracleXAConnection oracleXAConnection1 = new OracleXAConnection(connection1);
        Connection connection2 = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.115:1521/orcl", "scott", "tiger");
        OracleXAConnection oracleXAConnection2 = new OracleXAConnection(connection2);

        // OracleXid(int fId, byte[] gId, byte[] bId)
        // fId - format ID
        // gId - global transaction ID (no copy performed)
        // bId - branch Qualifier (no copy performed)
        Xid xid1 = new OracleXid(0, "g000".getBytes(), "bqual001".getBytes());
        Xid xid2 = new OracleXid(0, "g000".getBytes(), "bqual002".getBytes());

        // 获取资源
        XAResource xaResource1 = oracleXAConnection1.getXAResource();
        XAResource xaResource2 = oracleXAConnection2.getXAResource();

        // Start the Resources
        xaResource1.start(xid1, XAResource.TMNOFLAGS);
        Statement stmt1 = connection1.createStatement();
        stmt1.executeUpdate("insert into t(id) values (1)");
        stmt1.close();
        xaResource1.end(xid1, XAResource.TMSUCCESS);

        xaResource2.start(xid2, XAResource.TMNOFLAGS);
        Statement stmt2 = connection2.createStatement();
        stmt2.executeUpdate("insert into t(id) values (2)");
        stmt2.close();
        xaResource2.end(xid2, XAResource.TMSUCCESS);

        // Prepare the RMs
        int prepare1 = xaResource1.prepare(xid1);
        int prepare2 = xaResource2.prepare(xid2);
        System.out.println("Return value of prepare 1 is " + prepare1);
        System.out.println("Return value of prepare 2 is " + prepare2);

        if(prepare1 == XAResource.XA_OK && prepare2 == XAResource.XA_OK) {
            xaResource1.commit(xid1, false);
            xaResource2.commit(xid2, false);
        } else {
            xaResource1.rollback(xid1);
            xaResource2.rollback(xid2);
        }

        connection1.close();
        connection2.close();
    }
}
