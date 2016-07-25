clearpool
=========

Clearpool is a High Performance Distributed Database Pool. It is able to manage multiple database, and has a wondorful performance.
<p />
The function of the pool:
<ol>
<li>Be able to manage distributed database</li>
<li>Support JTA transaction</li>
<li>Support monitoring by JMX</li>
<li>Automatically release the invalid connection and get new connection</li>
<li>Automatically collect the idle connection if necessary</li>
<li>Support encrypt the password of the database</li>
</ol>

Here is the result of comparing with druid and tomcat-jdbc.
![image](https://github.com/xionghuiCoder/clearpool/blob/master/src/test/resources/img/compare.png)
<p />
If you want to compare the performance with other popular database pools, please run TestCase: [CompareWithPopularPool.java](https://github.com/xionghuiCoder/clearpool/blob/master/src/test/java/org/opensource/clearpool/CompareWithPopularPool.java).
<p />
It really helps a lot if you are willing to improve clearpool.
<P />
Please import googlestyle-java.xml before any commit.
