maven-kie-deployer-plugin
=========================

Maven plugin to deploy kjars to a running instance ofJBoss BPM Suite 6 for use in a CI environment

* Example usage
```
<plugin>
    <groupId>org.kie.maven.plugins</groupId>
    <artifactId>maven-kie-deployer-plugin</artifactId>
    <version>1.0.4-SNAPSHOT</version>
    <executions>
        <execution>
            <id>deploy-kie-modules</id>
            <phase>package</phase>
            <goals>
                <goal>deploy</goal>
            </goals>
            <configuration>
                <debug>true</debug>
                <serverUri>http://localhost:16080/business-central</serverUri>
                <username>admin</username>
                <password>admin</password>
                <timeoutInSeconds>3600</timeoutInSeconds>
                <deployables>
                    <deployable>
                        <groupId>org.jboss</groupId>
                        <artifactId>business-rules</artifactId>
                        <version>1.0-SNAPSHOT</version>
                        <strategy>PER_PROCESS_INSTANCE</strategy> <!-- SINGLETON, PRE_REQUEST or PER_PROCESS_INSTANCE -->
                    </deployable>
                </deployables>
                <models>
                   <model>
                        <groupId>org.jboss</groupId>
                        <artifactId>rules-model</artifactId>
                        <version>1.0-SNAPSHOT</version>
                   </model>
                </models>
            </configuration>
        </execution>
    </executions>
```
