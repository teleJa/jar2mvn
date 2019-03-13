package installJarToMVN;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * 读取jar包内的pom.properties 获得groupid
 * version,artifactId可以从jar包名称获取,也可以从pom.properties获取
 * 
 * @author Tele
 *
 */

public class InstallJar {
	// 默认jar包路径,填写到目录
	private static String jarPath = "d:/jartoMVN/";
	private static BufferedReader reader;
	public static void main(String[] args) {

		if (args.length > 0) {
			if (args[0] != null && args[0].trim().length() > 0) {
				jarPath = args[0];
			}
		}

		File dir = new File(jarPath);
		if (!dir.exists()) {
			throw new RuntimeException("jar包目录不存在!");
		} else {
			if (!dir.isDirectory()) {
				throw new RuntimeException("输入的参数必须为jar包所在目录!");
			} else {
				File[] listFiles = dir.listFiles();
				if (listFiles.length == 0) {
					throw new RuntimeException("当前目录下没有文件");
				}

				String[] params = new String[4];
				// 遍历
				for (int i = 0; i < listFiles.length; i++) {
					File jarFile = listFiles[i];

					// 过滤非jar文件
					if (!jarFile.getName().contains(".jar")) {
						continue;
					}

					// 去除后缀,jar的名字可能含有多个 ".",hadoop-yarn-server-applicationhistoryservice-3.1.1.jar
					String jarName = jarFile.getName();
					// 保留原始的jar名称
					String orginalName = jarName;

					// hadoop-yarn-server-applicationhistoryservice-3.1.1
					jarName = jarName.substring(0, jarName.lastIndexOf("."));

					// 获得artifactId
					String artifactId = jarName.substring(0, jarName.lastIndexOf("-"));

					// 获得版本号
					String version = jarName.substring(jarName.lastIndexOf("-") + 1);

					// 获得groupId
					String groupId = readPomproperties(jarPath + orginalName);
					if (groupId == null) {
						throw new RuntimeException("获取groupId失败");
					}
					groupId = groupId.split("=")[1];

					// 封装参数
					params[0] = jarPath + orginalName;
					params[1] = groupId;
					params[2] = artifactId;
					params[3] = version;

					install(params);

				}

			}

		}

	}

	
	/**
	 * 
	 * @param path groupId=org.apache.hadoop
	 * @return 获得groupId,在pom.properties文件的第四行
	 */
	public static String readPomproperties(String path) {
		JarFile jarFile = null;
		String groupId = null;
		// groupId在第四行
		int number = 4;
		try {
			jarFile = new JarFile(path);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry jarEntry = entries.nextElement();

				String name = jarEntry.getName();

				if (name.contains("pom.properties")) {
					reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(jarEntry), "utf-8"));
					String line = "";

					// 计行数
					int count = 0;

					while ((line = reader.readLine()) != null) {

						count++;
						if (count == 4) {
							groupId = line;
						}
					}

				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return groupId;
	}

	// 执行安装命令
	public static void install(String[] params) {
		// 拼接命令
		String order = "mvn install:install-file" + " -Dfile=" + params[0] + " -DgroupId=" + params[1]
				+ " -DartifactId=" + params[2] + " -Dversion=" + params[3] + " -Dpackaging=jar";

		Runtime rt = Runtime.getRuntime();
		// 执行安装
		System.out.println(order);
		Process p;
		try {
			p = rt.exec("cmd.exe /c " + " " + order);

			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			// 输出进程
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			if (reader != null) {
				reader.close();
			}

			// waitFor()是阻塞方法,等待外部命令执行结束
			p.waitFor();

			p.destroy();
			p = null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
