///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.junit.jupiter:junit-jupiter-engine:5.14.4
//DEPS org.junit.jupiter:junit-jupiter-params:5.14.4
//DEPS org.junit.platform:junit-platform-console-standalone:1.14.4
//DEPS org.assertj:assertj-core:3.25.1

//SOURCES ../../../../main/**/*.java

package dev.jbang.fmt;

import org.junit.jupiter.api.Test;
import org.junit.platform.console.ConsoleLauncher;
import org.assertj.core.api.Assertions;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// JUnit5 Test class for fmt
public class RangeTest {

	@Test
	public void testDirectives() throws Exception {

		assertThat(CodeRange.isJBangDirective("//DEPS org.junit.jupiter:junit-jupiter-engine:5.12.2"))
			.isTrue();
		Assertions.assertThat(CodeRange.isJBangDirective("//JAVA 21+")).isTrue();
		Assertions.assertThat(CodeRange.isJBangDirective("//DEPS org.junit.jupiter:junit-jupiter-engine:5.12.2"))
			.isTrue();
		Assertions.assertThat(CodeRange.isJBangDirective("// DEPS")).isFalse();

	}

	@Test
	public void testPureJava() throws Exception {

		String alljava = """
				package com.example;
				System.out.println("Hello");
				""";

		List<CodeRange> ranges = CodeRange.identifyJavaRanges(alljava);

		Assertions.assertThat(ranges).hasSize(1);

		Assertions.assertThat(ranges.get(0).start()).isEqualTo(0);
		Assertions.assertThat(ranges.get(0).end()).isEqualTo(alljava.length());
	}

	@Test
	public void testDoubleSlashinString() throws Exception {
		String transientCode = """
							private static final String PROTO_SCHEMA = \"\"\"																																																																																																														            // File name: Schema.proto
				// Generated from : Schema.proto
						\"\"\";
						""";

		List<CodeRange> ranges = CodeRange.identifyJavaRanges(transientCode);
		Assertions.assertThat(ranges).hasSize(1);

		Assertions.assertThat(ranges.get(0).start()).isEqualTo(0);
		Assertions.assertThat(ranges.get(0).end()).isEqualTo(transientCode.length());
	}

	@Test
	public void testPureJBang() throws Exception {

		String pureJBang = """
				  //DEPS org.junit.jupiter:junit-jupiter-engine:5.12.2
				//JAVA 21+""";

		List<CodeRange> ranges = CodeRange.identifyJavaRanges(pureJBang);

		Assertions.assertThat(ranges).hasSize(0);
	}

	@Test
	public void testMixed() throws Exception {

		String alljava = """
				///usr/bin/env jbang "$0" "$@" ; exit $?
				      //DEPS org.junit.jupiter:junit-jupiter-engine:5.12.2
				    //DEPS org.junit.jupiter:junit-jupiter-params:5.12.2
				    //DEPS org.junit.platform:junit-platform-console:1.12.2
				public class TestClass{public static void main(String[]args){System.out.println("Hello");}}
				""";

		List<CodeRange> ranges = CodeRange.identifyJavaRanges(alljava);

		Assertions.assertThat(ranges).hasSize(1);

		Assertions.assertThat(ranges.get(0).start()).isEqualTo(alljava.indexOf("public"));
		Assertions.assertThat(ranges.get(0).end()).isEqualTo(alljava.length());

	}

	// Scan the system classpath for tests
	// Include those found in /cache/jars/ which is where
	// jbang will by default put them. Adjust as needed.
	public static void main(final String... args) {
		String jarsList = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
			.filter(path -> path.contains("/cache/jars/"))
			.reduce((a, b) -> a + File.pathSeparator + b)
			.orElse("");

		ConsoleLauncher.main("execute", "--scan-class-path", "-cp", jarsList);
	}
}
