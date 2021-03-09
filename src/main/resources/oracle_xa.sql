
--https://oracle-base.com/articles/11g/dbms_xa_11gR1


--SESSION 1

DECLARE
  L_XID    DBMS_XA_XID := DBMS_XA_XID(999);
  L_RETURN PLS_INTEGER;
BEGIN
  L_RETURN := SYS.DBMS_XA.XA_START(XID => L_XID, FLAG => DBMS_XA.TMNOFLAGS);
  CHECK_RESULT(L_XID, L_RETURN, 'xa_start');
  INSERT INTO T (ID) VALUES (1);
  L_RETURN := SYS.DBMS_XA.XA_END(XID => L_XID, FLAG => DBMS_XA.TMSUSPEND);
  CHECK_RESULT(L_XID, L_RETURN, 'xa_end');
END;
/

--SESSION 2

DECLARE
  L_XID    DBMS_XA_XID := DBMS_XA_XID(999);
  L_RETURN PLS_INTEGER;
BEGIN
  L_RETURN := SYS.DBMS_XA.XA_START(XID => L_XID, FLAG => DBMS_XA.TMRESUME);
  CHECK_RESULT(L_XID, L_RETURN, 'xa_start');
  INSERT INTO T (ID) VALUES (2);
  L_RETURN := SYS.DBMS_XA.XA_END(XID => L_XID, FLAG => DBMS_XA.TMSUCCESS);
  CHECK_RESULT(L_XID, L_RETURN, 'xa_end');
END;
/

--SESSION 3

DECLARE
  L_XID    DBMS_XA_XID := DBMS_XA_XID(999);
  L_RETURN PLS_INTEGER;
BEGIN
  L_RETURN := SYS.DBMS_XA.XA_COMMIT(XID => L_XID, ONEPHASE => TRUE);
  CHECK_RESULT(L_XID, L_RETURN, 'xa_commit');
END;
/


--CHECK XA RESULT PROCEDURE
CREATE OR REPLACE PROCEDURE CHECK_RESULT (P_XID     IN DBMS_XA_XID,
                                          P_RETURN  IN PLS_INTEGER,
                                          P_COMMAND IN VARCHAR2) AS
  L_OUTPUT  VARCHAR2(23767);
  L_RETURN  PLS_INTEGER;
BEGIN
  IF P_RETURN != SYS.DBMS_XA.XA_OK THEN
    CASE P_RETURN
      WHEN SYS.DBMS_XA.XA_RBBASE THEN L_OUTPUT := 'XA_RBBASE: Inclusive lower bound of the rollback codes';
      WHEN SYS.DBMS_XA.XA_RBROLLBACK THEN L_OUTPUT := 'XA_RBROLLBACK: Rollback was caused by an unspecified reason';
      WHEN SYS.DBMS_XA.XA_RBCOMMFAIL THEN L_OUTPUT := 'XA_RBCOMMFAIL: Rollback was caused by a communication failure';
      WHEN SYS.DBMS_XA.XA_RBDEADLOCK THEN L_OUTPUT := 'XA_RBDEADLOCK: Deadlock was detected';
      WHEN SYS.DBMS_XA.XA_RBINTEGRITY THEN L_OUTPUT := 'XA_RBINTEGRITY: Condition that violates the integrity of the resources was detected';
      WHEN SYS.DBMS_XA.XA_RBOTHER THEN L_OUTPUT := 'XA_RBOTHER: Resource manager rolled back the transaction for an unlisted reason';
      WHEN SYS.DBMS_XA.XA_RBPROTO THEN L_OUTPUT := 'XA_RBPROTO: Protocol error occurred in the resource manager';
      WHEN SYS.DBMS_XA.XA_RBTIMEOUT THEN L_OUTPUT := 'XA_RBTIMEOUT: Transaction branch took long';
      WHEN SYS.DBMS_XA.XA_RBTRANSIENT THEN L_OUTPUT := 'XA_RBTRANSIENT: May retry the transaction branch';
      WHEN SYS.DBMS_XA.XA_RBEND THEN L_OUTPUT := 'XA_RBEND: Inclusive upper bound of the rollback codes';
      WHEN SYS.DBMS_XA.XA_NOMIGRATE THEN L_OUTPUT := 'XA_NOMIGRATE: Transaction branch may have been heuristically completed';
      WHEN SYS.DBMS_XA.XA_HEURHAZ THEN L_OUTPUT := 'XA_HEURHAZ: Transaction branch may have been heuristically completed';
      WHEN SYS.DBMS_XA.XA_HEURCOM THEN L_OUTPUT := 'XA_HEURCOM: Transaction branch has been heuristically committed';
      WHEN SYS.DBMS_XA.XA_HEURRB THEN L_OUTPUT := 'XA_HEURRB: Transaction branch has been heuristically rolled back';
      WHEN SYS.DBMS_XA.XA_HEURMIX THEN L_OUTPUT := 'XA_HEURMIX: Some of the transaction branches have been heuristically committed, others rolled back';
      WHEN SYS.DBMS_XA.XA_RETRY THEN L_OUTPUT := 'XA_RETRY: Routine returned with no effect and may be re-issued';
      WHEN SYS.DBMS_XA.XA_RDONLY THEN L_OUTPUT := 'XA_RDONLY: Transaction was read-only and has been committed';
      WHEN SYS.DBMS_XA.XA_OK THEN L_OUTPUT := 'XA_OK: Normal execution';
      WHEN SYS.DBMS_XA.XAER_ASYNC THEN L_OUTPUT := 'XAER_ASYNC: Asynchronous operation already outstanding';
      WHEN SYS.DBMS_XA.XAER_RMERR THEN L_OUTPUT := 'XAER_RMERR: Resource manager error occurred in the transaction branch';
      WHEN SYS.DBMS_XA.XAER_NOTA THEN L_OUTPUT := 'XAER_NOTA: XID is not valid';
      WHEN SYS.DBMS_XA.XAER_INVAL THEN L_OUTPUT := 'XAER_INVAL: Invalid arguments were given';
      WHEN SYS.DBMS_XA.XAER_PROTO THEN L_OUTPUT := 'XAER_PROTO: Routine invoked in an improper context';
      WHEN SYS.DBMS_XA.XAER_RMFAIL THEN L_OUTPUT := 'XAER_RMFAIL: Resource manager unavailable';
      WHEN SYS.DBMS_XA.XAER_DUPID THEN L_OUTPUT := 'XAER_DUPID: XID already exists';
      WHEN SYS.DBMS_XA.XAER_OUTSIDE THEN L_OUTPUT := 'XAER_OUTSIDE: Resource manager doing work outside global transaction';
    END CASE;
    L_RETURN := SYS.DBMS_XA.XA_ROLLBACK(P_XID);
    RAISE_APPLICATION_ERROR(-20000, P_COMMAND || '=' || L_OUTPUT);
  END IF;
END;

