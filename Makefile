MYCP=gson.jar:sqlite4java.jar:jersey-bundle.jar:jetty-runner.jar

default: clean database.sqlite run

run: bundle.jar
	java -cp ${MYCP} org.eclipse.jetty.runner.Runner --port 8082 bundle.jar

very-clean: clean
	-rm *.jar *.zip *.so *.dll *.jnilib

clean:
	find topack -name '*.class' -delete
	-rm database.sqlite bundle.jar

.PHONY : default run clean very-clean

bundle.jar: topack/WEB-INF/classes/our_package/Hello.class topack/WEB-INF/classes/our_package/ResponseCorsFilter.class
	jar cvf bundle.jar -C topack .

topack/WEB-INF/classes/our_package/Hello.class: jersey-bundle.jar asm.jar jetty-runner.jar gson.jar sqlite4java.jar
	javac -cp ${MYCP} topack/WEB-INF/classes/our_package/Hello.java

topack/WEB-INF/classes/our_package/ResponseCorsFilter.class: jersey-bundle.jar asm.jar jetty-runner.jar gson.jar sqlite4java.jar
	javac -cp ${MYCP} topack/WEB-INF/classes/our_package/ResponseCorsFilter.java

database.sqlite:
	-sqlite3 -init init.sql database.sqlite .quit

jersey-bundle.jar:
	wget -O jersey-bundle.jar "http://search.maven.org/remotecontent?filepath=com/sun/jersey/jersey-bundle/1.19/jersey-bundle-1.19.jar"

asm.jar:
	wget -O asm.jar "http://search.maven.org/remotecontent?filepath=org/eclipse/jetty/orbit/org.objectweb.asm/3.3.1.v201105211655/org.objectweb.asm-3.3.1.v201105211655.jar"

jetty-runner.jar:
	wget -O jetty-runner.jar "http://search.maven.org/remotecontent?filepath=org/eclipse/jetty/jetty-runner/9.3.2.v20150730/jetty-runner-9.3.2.v20150730.jar"

gson.jar:
	wget -O gson.jar "http://search.maven.org/remotecontent?filepath=com/google/code/gson/gson/2.3.1/gson-2.3.1.jar"

sqlite4java.jar: sqlite4java.zip
	unzip -j sqlite4java.zip

sqlite4java.zip:
	wget -O sqlite4java.zip "https://d1.almworks.com/.files/sqlite4java/sqlite4java-392.zip"
