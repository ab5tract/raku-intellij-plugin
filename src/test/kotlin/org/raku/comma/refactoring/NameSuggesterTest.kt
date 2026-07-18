package org.raku.comma.refactoring

import org.raku.comma.CommaFixtureTestCase

import java.util.Arrays
import java.util.Collections

class NameSuggesterTest : CommaFixtureTestCase() {
  fun testSinglePartName() {
    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("test", ""))
    assertEquals(Collections.singletonList("\$TEST"), RakuNameSuggester.getNamePieces("TEST", ""))
  }

  fun testSnakeCaseName() {
    assertEquals(Arrays.asList("\$test-me", "\$me"), RakuNameSuggester.getNamePieces("test-me", ""))
    assertEquals(Arrays.asList("\$do-test-me", "\$test-me", "\$me"), RakuNameSuggester.getNamePieces("do-test-me", ""))
    assertEquals(Arrays.asList("\$test-me", "\$me"), RakuNameSuggester.getNamePieces("is-test-me", ""))
    assertEquals(Arrays.asList("\$test-me", "\$me"), RakuNameSuggester.getNamePieces("get-test-me", ""))
    assertEquals(Arrays.asList("\$test-me", "\$me"), RakuNameSuggester.getNamePieces("get-is-test-me", ""))

    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("is-test", ""))
    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("get-test", ""))
    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("get-is-test", ""))

    assertEquals(Arrays.asList("\$CALL-ME", "\$ME"), RakuNameSuggester.getNamePieces("CALL-ME", ""))
  }

  fun testUnderscoreCaseName() {
    assertEquals(Arrays.asList("\$test_me", "\$me"), RakuNameSuggester.getNamePieces("test_me", ""))
    assertEquals(Arrays.asList("\$do_test_me", "\$test_me", "\$me"), RakuNameSuggester.getNamePieces("do_test_me", ""))
    assertEquals(Arrays.asList("\$test_me", "\$me"), RakuNameSuggester.getNamePieces("is_test_me", ""))
    assertEquals(Arrays.asList("\$test_me", "\$me"), RakuNameSuggester.getNamePieces("get_test_me", ""))
    assertEquals(Arrays.asList("\$test_me", "\$me"), RakuNameSuggester.getNamePieces("get_is_test_me", ""))

    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("is_test", ""))
    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("get_test", ""))
    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("get_is_test", ""))

    assertEquals(Arrays.asList("\$CALL_ME", "\$ME"), RakuNameSuggester.getNamePieces("CALL_ME", ""))
  }

  fun testCamelCaseName() {
    assertEquals(Arrays.asList("\$testMe", "\$me"), RakuNameSuggester.getNamePieces("isTestMe", ""))
    assertEquals(Arrays.asList("\$testMe", "\$me"), RakuNameSuggester.getNamePieces("testMe", ""))
    assertEquals(Arrays.asList("\$doTestMe", "\$testMe", "\$me"), RakuNameSuggester.getNamePieces("doTestMe", ""))
    assertEquals(Arrays.asList("\$testMe", "\$me"), RakuNameSuggester.getNamePieces("getTestMe", ""))
    assertEquals(Arrays.asList("\$testMe", "\$me"), RakuNameSuggester.getNamePieces("getIsTestMe", ""))

    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("isTest", ""))
    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("getTest", ""))
    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("getIsTest", ""))

    assertEquals(Arrays.asList("\$itGDPRCompliant", "\$gdprCompliant", "\$compliant"), RakuNameSuggester.getNamePieces("isItGDPRCompliant", ""))
  }

  fun testSigil() {
    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("test", "Any"))
    assertEquals(Collections.singletonList("\$test"), RakuNameSuggester.getNamePieces("test", "Mu"))

    assertEquals(Collections.singletonList("%test"), RakuNameSuggester.getNamePieces("test", "Hash[Int]"))
    assertEquals(Collections.singletonList("%test"), RakuNameSuggester.getNamePieces("test", "Map[Int]"))

    assertEquals(Collections.singletonList("@test"), RakuNameSuggester.getNamePieces("test", "List[Int]"))
    assertEquals(Collections.singletonList("@test"), RakuNameSuggester.getNamePieces("test", "Array[Int]"))
    assertEquals(Collections.singletonList("@test"), RakuNameSuggester.getNamePieces("test", "Positional[Int]"))
    assertEquals(Collections.singletonList("@test"), RakuNameSuggester.getNamePieces("test", "Seq[Int]"))
    assertEquals(Collections.singletonList("@test"), RakuNameSuggester.getNamePieces("test", "Iterable[Int]"))
  }
}
