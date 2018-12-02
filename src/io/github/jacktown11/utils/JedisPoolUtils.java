package io.github.jacktown11.utils;


import java.io.IOException;
import java.util.Properties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolUtils {
	private static JedisPool pool;
	static {
		JedisPoolConfig conf = new JedisPoolConfig();
		Properties p = new Properties();
		try {
			p.load(Jedis.class.getClassLoader().getResourceAsStream("redis.properties"));
			conf.setMinIdle(Integer.parseInt(p.getProperty("minIdle")));
			conf.setMaxIdle(Integer.parseInt(p.getProperty("maxIdle")));
			conf.setMaxTotal(Integer.parseInt(p.getProperty("maxTotal")));
			pool = new JedisPool(conf, p.getProperty("url"), Integer.parseInt(p.getProperty("port")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	public static Jedis getJedis() {
		return pool.getResource();
	}
}
