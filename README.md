1. Add as dependency: https://jitpack.io/#RoboMWM/UsefulUtil

2. Add the shading plugin to the build. Replace **`YOUR.OWN.PACKAGE`** with your own package.
    
   ```xml
        <build>

            <plugins>
			
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-shade-plugin</artifactId>
                    <version>3.2.0</version>
                    <configuration>
                        <artifactSet>
                            <includes>
                                <include>com.github.RoboMWM:UsefulUtil</include>
                            </includes>
                        </artifactSet>
                        <relocations>
                            <relocation>
                                <pattern>com.robomwm.usefulutil</pattern>
                                <shadedPattern>YOUR.OWN.PACKAGE</shadedPattern>
                            </relocation>
                        </relocations>
                    <createDependencyReducedPom>false</createDependencyReducedPom>
                    <minimizeJar>true</minimizeJar>
                    </configuration>
                    <executions>
                        <execution>
                            <phase>package</phase>
                            <goals>
                                <goal>shade</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
				
            </plugins>

        </build>
   ```
