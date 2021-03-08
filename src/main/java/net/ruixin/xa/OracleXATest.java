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
            Connection connection1 = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/helowin", "system", "system");
            OracleXAConnection oracleXAConnection1 = new OracleXAConnection(connection1);
            Connection connection2 = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/helowin", "scott", "tiger");
            OracleXAConnection oracleXAConnection2 = new OracleXAConnection(connection2);

            // OracleXid(int fId, byte[] gId, byte[] bId)
            // fId - format ID
            // gId - global transaction ID (no copy performed)
            // bId - branch Qualifier (no copy performed)
//            Xid xid1 = new OracleXid(0x1234, "g0001".getBytes(), "bqual001".getBytes());
//            Xid xid2 = new OracleXid(0x1234, "g0001".getBytes(), "bqual002".getBytes());
            Xid xid1 = createXid(1);
            Xid xid2 = createXid(2);

            // 获取资源
            XAResource xaResource1 = oracleXAConnection1.getXAResource();
            XAResource xaResource2 = oracleXAConnection2.getXAResource();

            // Start the Resources
            xaResource1.start(xid1, XAResource.TMNOFLAGS);
            xaResource2.start(xid2, XAResource.TMNOFLAGS);

            // Do something
            Statement stmt1 = oracleXAConnection1.getConnection().createStatement();
            stmt1.executeUpdate("insert into t(id) values (1)");
            stmt1.close();
            Statement stmt2 = oracleXAConnection2.getConnection().createStatement();
            stmt2.executeUpdate("insert into t(id) values (2)");
            stmt2.close();

            // End the Resources
            xaResource1.end(xid1, XAResource.TMSUCCESS);
            xaResource2.end(xid2, XAResource.TMSUCCESS);

            // Prepare the RMs
            int prepare1 = xaResource1.prepare(xid1);
            int prepare2 = xaResource2.prepare(xid2);
            System.out.println("Return value of prepare 1 is " + prepare1);
            System.out.println("Return value of prepare 2 is " + prepare2);

            System.out.println("Is xaResource1 same as xaResource2 ? " + xaResource1.isSameRM(xaResource2));

//            if (prepare1 == XAResource.XA_OK && prepare2 == XAResource.XA_OK) {
//                xaResource1.commit(xid1, false);
//                xaResource2.commit(xid2, false);
//            } else {
//                xaResource1.rollback(xid1);
//                xaResource2.rollback(xid2);
//            }

            if(prepare2 == XAResource.XA_OK) {
                xaResource2.commit(xid2, false);
            } else {
                xaResource2.rollback(xid2);
            }

            if(prepare1 == XAResource.XA_OK) {
                xaResource1.commit(xid1, false);
            } else {
                xaResource1.rollback(xid1);
            }

            connection1.close();
            connection2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Xid createXid(int bids)
            throws XAException
    {
        byte[] gid = new byte[1]; gid[0]= (byte) 9;
        byte[] bid = new byte[1]; bid[0]= (byte) bids;
        byte[] gtrid = new byte[64];
        byte[] bqual = new byte[64];
        System.arraycopy (gid, 0, gtrid, 0, 1);
        System.arraycopy (bid, 0, bqual, 0, 1);
        return new OracleXid(0x1234, gtrid, bqual);
    }
}
