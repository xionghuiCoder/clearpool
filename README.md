clearpool
=========

Clearpool is a High Performance Distributed Database Pool.It could manage the distributed database with high performance.Clearpool has a wondorful performance because it abandon the traditional database pool lock,and replaced by atomic operation.However,we should know that it used the Unsafe.java in the package of sun.misc.

The function of the pool:
<ol>
<li>It can manage distributed database.</li>
<li>It support jta.</li>
<li>It can be monitor by JMX.</li>
<li>It will release the connection and get new connection if the connection is invalid.</li>
<li>It will collect the idle connection if necessary.
</ol>

Note:
Thread will fight for the connection in the pool all the time because we abandoned the lock,so the CPU may be busy,thus the program will be a little delayed.

If you want to compare the performance with other popular database pools,please run the test case:https://github.com/xionghuiCoder/clearpool/blob/master/src/test/java/org/opensource/clearpool/CompareWithPopularPoolCase.java.
Here is the result of comparing with druid and tomcat-jdbc.
![image](https://github.com/xionghuiCoder/clearpool/blob/master/src/test/resources/img/comparewithWonderfulpool.jpg)
