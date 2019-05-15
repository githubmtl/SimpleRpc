# SimpleRpc-RPC框架的简单实现
结构图
----
![](https://github.com/githubmtl/SimpleRpc/blob/master/src/Rpc%E7%BB%93%E6%9E%84%E5%9B%BE.jpg)<br>
如何使用
----
### 依赖
* fastjosn-1.2.47.jar
* jedis-2.9.0.jar
* netty-all-4.1.35.Final.jar
* slf4j-api-1.7.25.jar
* spring-aop-5.1.5.RELEASE.jar
* spring-beans-5.1.5.RELEASE.jar
* spring-context-5.1.5.RELEASE.jar
* spring-core-5.1.5.RELEASE.jar
* spring-expression-5.1.5.RELEASE.jar
* spring-jcl-5.1.5.RELEASE.jar

### 客户端代码
###### User服务接口
```Java
public interface UserServiceItf {
	public Integer insert(User user);
	public Integer update(User user);
	public Integer delete(Integer id);
	public User findById(Integer id);
	public List<User> findAll();
}
```
###### User实体类
```Java
public class User {
	private Integer id;
	private String name;
	private Integer age;
	...省略setter和getter
}
```
###### spring xml配置文件
```Java
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
<!-- 客户端配置类 -->
<bean class="com.mtl.rpc.config.RpcClientConfiguration">
    <property name="registCenterConfig" ref="redisRegistCenterConfig"/>
    <!-- 接口所在的包名 -->
    <property name="basePachage" value="com.mtl.rpc.demo.service"/>
    <!-- 客户端每30秒会去服务注册中心获取最新的服务列表，默认为60S -->
    <property name="synRedisSecond" value="30" />
 </bean>
    <!-- redis注册中心配置类 -->
    <bean id="redisRegistCenterConfig" class="com.mtl.rpc.config.RedisRegistCenterConfig">
    	<!-- redis的IP -->
        <property name="ip" value="192.168.17.130" />
        <!-- redis的密码 -->
        <property name="password" value="111111" />
    </bean>
</beans>
```
###### 测试Main方法
```Java
public class ClientMain {
	public static void main(String[] args) throws Exception {
		AbstractApplicationContext applicationContext= new ClassPathXmlApplicationContext("client*");
		try {
			UserServiceItf userServiceItf = applicationContext.getBean(UserServiceItf.class);
			Integer insert = userServiceItf.insert(new User(12,"mike",12));
			System.out.println("insert 结果..."+insert);
			Integer update = userServiceItf.update(new User(12,"mike",12));
			System.out.println("update 结果..."+update);
			Integer delete = userServiceItf.delete(12);
			System.out.println("delete 结果..."+delete);
			User findById = userServiceItf.findById(12);
			System.out.println("findById 结果..."+findById);
			List<User> findAll = userServiceItf.findAll();
			System.out.println("findById 结果..."+findAll);
		}finally {
			applicationContext.close();
		}
	}
}
```

### 服务端代码
###### User服务实现
```Java
@Service 
@RpcService   //有这个注解，才会把服务发布到redis注册中心
public class UserServiceImpl implements UserServiceItf {
	private Random random=new Random();
	@Override
	public Integer insert(User user) {
		System.out.println("invoke insert :"+user);
		return random.nextInt();
	}

	@Override
	public Integer update(User user) {
		System.out.println("invoke update :"+user);
		return random.nextInt();
	}

	@Override
	public Integer delete(Integer id) {
		System.out.println("invoke delete :"+id);
		return random.nextInt();
	}

	@Override
	public User findById(Integer id) {
		System.out.println("invoke findById :"+id);
		byte[] bf=new byte[8];
		random.nextBytes(bf);
		return new User(random.nextInt(100), new String(bf), random.nextInt(50)+10);
	}

	@Override
	public List<User> findAll() {
		int nextInt = random.nextInt(5)+1;
		List<User> list=new ArrayList<>();
		for(int i=0;i<nextInt;i++) {
			byte[] bf=new byte[8];
			random.nextBytes(bf);
			list.add(new User(random.nextInt(100), new String(bf), random.nextInt(50)+10));
		}
		return list;
	}

}
```
###### 服务端spring xml配置
```Java
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
     <!-- 开启注解扫描，当然也可以通过xml的方式配置 -->
    <context:component-scan base-package="com.mtl.rpc.demo.service"></context:component-scan>
    <!-- 服务端配置类 -->
    <bean class="com.mtl.rpc.config.RpcServiceConfiguration">
    	<!-- 服务监听的网络端口 -->
        <property name="prot" value="10086"/>
        <!-- 注册中心配置信息 -->
        <property name="registCenterConfig" ref="redisRegistCenterConfig"/>
    </bean>
    <!-- 注册中心配置信息 -->
    <bean id="redisRegistCenterConfig" class="com.mtl.rpc.config.RedisRegistCenterConfig">
    	<!-- redis注册中心地址 -->
        <property name="ip" value="192.168.17.130" />
        <!-- redis密码 -->
        <property name="password" value="111111" />
        <!-- 服务存活时间，如果20内服务没有向redis发送命令，该服务会被redis注册中心移除，该参数默认为60S -->
        <property name="expireSeconds" value="20"/>
    </bean>
</beans>
```
###### 服务端测试Main方法
```Java
public class ServerMian {
	public static void main(String[] args) {
		ApplicationContext applicationContext= new ClassPathXmlApplicationContext("server*");
        Scanner scanner=new Scanner(System.in);
        System.out.println("服务已启动，输入任意键退出...");
        scanner.next();
        ((ClassPathXmlApplicationContext) applicationContext).close();
        System.out.println("服务已关闭！");
        scanner.close();
	}
}
```
### 启动步骤
1.启动redis服务<br>
2.启动服务端<br>
3.启动客户端
