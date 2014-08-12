clearpool
=========

Clearpool is a High Performance Database Pool,its biggest characteristic is to abandon the traditional database pool lock,it is replaced by the atomic operation.However,we need to know that it has a ultra low latency because it invoked the Unsafe.java under the package of sun.misc.On the other side,we should know that its source code is just about 6500 lines,so we could learn and modify it in a short time.

The function of the pool:
1)it support distributed Database.
2)we can monitor it by JMX.
3)it will release the useless connection and get new connection if the Database restarted.
4)it will collect the idle connection if necessary.

Note:
Thread may fight for connection in clearpool all the time because we abandoned the lock,so the CPU will be busy.

If you want to compare the performance with other database pool,please run the test case:https://github.com/xionghuiCoder/clearpool/blob/master/src/test/java/org/opensource/clearpool/CompareCase.java.
