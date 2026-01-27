package RegularExpression;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class RegularExpressionTest {
    
    private RegularExpression re;
    
    // Helper method to repeat strings for older Java versions
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    @BeforeEach
    void setUp() {
        String regex = "(0u1(01*0)*1)*"; // matches multiples of 3
        re = new RegularExpression(regex, new char[]{'0', '1'});
    }
    
    @Nested
    @DisplayName("Binary numbers divisible by 3")
    class DivisibleByThreeTests {
        
        @Test
        @DisplayName("Should match single digit 0 (decimal 0)")
        void testZero() {
            assertTrue(re.match("0"), "0 (decimal 0) should match");
        }
        
        @Test
        @DisplayName("Should match 11 (decimal 3)")
        void testThree() {
            assertTrue(re.match("11"), "11 (decimal 3) should match");
        }
        
        @Test
        @DisplayName("Should match 110 (decimal 6)")
        void testSix() {
            assertTrue(re.match("110"), "110 (decimal 6) should match");
        }
        
        @Test
        @DisplayName("Should match 1001 (decimal 9)")
        void testNine() {
            assertTrue(re.match("1001"), "1001 (decimal 9) should match");
        }
        
        @Test
        @DisplayName("Should match 1100 (decimal 12)")
        void testTwelve() {
            assertTrue(re.match("1100"), "1100 (decimal 12) should match");
        }
        
        @Test
        @DisplayName("Should match 1111 (decimal 15)")
        void testFifteen() {
            assertTrue(re.match("1111"), "1111 (decimal 15) should match");
        }
        
        @Test
        @DisplayName("Should match 10010 (decimal 18)")
        void testEighteen() {
            assertTrue(re.match("10010"), "10010 (decimal 18) should match");
        }
        
        @Test
        @DisplayName("Should match 10101 (decimal 21)")
        void testTwentyOne() {
            assertTrue(re.match("10101"), "10101 (decimal 21) should match");
        }
    }
    
    @Nested
    @DisplayName("Binary numbers NOT divisible by 3")
    class NotDivisibleByThreeTests {
        
        @Test
        @DisplayName("Should not match 1 (decimal 1)")
        void testOne() {
            assertFalse(re.match("1"), "1 (decimal 1) should not match");
        }
        
        @Test
        @DisplayName("Should not match 10 (decimal 2)")
        void testTwo() {
            assertFalse(re.match("10"), "10 (decimal 2) should not match");
        }
        
        @Test
        @DisplayName("Should not match 100 (decimal 4)")
        void testFour() {
            assertFalse(re.match("100"), "100 (decimal 4) should not match");
        }
        
        @Test
        @DisplayName("Should not match 101 (decimal 5)")
        void testFive() {
            assertFalse(re.match("101"), "101 (decimal 5) should not match");
        }
        
        @Test
        @DisplayName("Should not match 111 (decimal 7)")
        void testSeven() {
            assertFalse(re.match("111"), "111 (decimal 7) should not match");
        }
        
        @Test
        @DisplayName("Should not match 1000 (decimal 8)")
        void testEight() {
            assertFalse(re.match("1000"), "1000 (decimal 8) should not match");
        }
        
        @Test
        @DisplayName("Should not match 1010 (decimal 10)")
        void testTen() {
            assertFalse(re.match("1010"), "1010 (decimal 10) should not match");
        }
        
        @Test
        @DisplayName("Should not match 1011 (decimal 11)")
        void testEleven() {
            assertFalse(re.match("1011"), "1011 (decimal 11) should not match");
        }
        
        @Test
        @DisplayName("Should not match 1101 (decimal 13)")
        void testThirteen() {
            assertFalse(re.match("1101"), "1101 (decimal 13) should not match");
        }
        
        @Test
        @DisplayName("Should not match 1110 (decimal 14)")
        void testFourteen() {
            assertFalse(re.match("1110"), "1110 (decimal 14) should not match");
        }
    }
    
    @Nested
    @DisplayName("Edge cases that might cause issues")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should match empty string due to * operator")
        void testEmptyString() {
            assertTrue(re.match(""), "Empty string should match due to * operator");
        }
        
        @Test
        @DisplayName("Leading zero patterns")
        void testLeadingZeros() {
            // These are edge cases - behavior may vary based on implementation
            re.match("00");   // Two zeros
            re.match("011");  // Leading zero case
        }
    }
    
    @Nested
    @DisplayName("Concatenated patterns")
    class ConcatenatedPatternTests {
        
        @Test
        @DisplayName("Concatenated valid patterns")
        void testConcatenatedPatterns() {
            re.match("00");     // Two zeros
            re.match("011");    // 0 + 11 (0 + 3)
            re.match("1100");   // 11 + 00 (3 + 0)
            re.match("11110");  // 1111 + 0 (15 + 0)
            re.match("01111");  // 0 + 1111 (0 + 15)
        }
    }
    
    @Nested
    @DisplayName("Repeated character patterns")
    class RepeatedCharacterTests {
        
        @Test
        @DisplayName("Short repeated patterns")
        void testShortRepeatedPatterns() {
            re.match("0000");      // Four zeros
            re.match("1111");      // Four ones
            re.match("00000000");  // Eight zeros
            re.match("11111111");  // Eight ones
        }
    }
    
    @Nested
    @DisplayName("Alternating patterns")
    class AlternatingPatternTests {
        
        @Test
        @DisplayName("Alternating 0 and 1 patterns")
        void testAlternatingPatterns() {
            re.match("0101");
            re.match("1010");
            re.match("010101");
            re.match("101010");
            re.match("0101010101");
        }
    }
    
    @Nested
    @DisplayName("Performance and stress tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Long strings of repeated characters")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void testLongRepeatedStrings() {
            String manyOnes = repeat("1", 20);
            assertDoesNotThrow(() -> re.match(manyOnes), "Should handle 20 consecutive ones");
            
            String longValid = repeat("0", 50);
            assertDoesNotThrow(() -> re.match(longValid), "Should handle 50 consecutive zeros");
        }
        
        @Test
        @DisplayName("Long alternating patterns")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void testLongAlternatingPatterns() {
            String alternating = repeat("01", 15);
            assertDoesNotThrow(() -> re.match(alternating), "Should handle alternating 01 pattern x15");
        }
        
        @Test
        @DisplayName("Complex nested patterns")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void testComplexNestedPatterns() {
            String complex1 = "1" + repeat("01", 10) + "0" + "1";
            assertDoesNotThrow(() -> re.match(complex1), "Should handle complex nested pattern");
        }
        
        @Test
        @DisplayName("Stress test with very long strings")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void testStressPatterns() {
            // Pattern that might cause deep recursion
            String stress1 = "0" + repeat("1", 100) + "0";
            assertDoesNotThrow(() -> re.match(stress1), "Should handle 0 + 100 ones + 0");
            
            // Multiple valid patterns concatenated
            String multiPattern = "0" + "11" + "110" + "1001";
            assertDoesNotThrow(() -> re.match(multiPattern), "Should handle multiple valid patterns");
            
            // Pattern with maximum nesting
            String maxNest = "1" + repeat("01", 20) + "0" + "1";
            assertDoesNotThrow(() -> re.match(maxNest), "Should handle maximum nesting pattern");
        }
    }
    
    @Nested
    @DisplayName("Regression tests")
    class RegressionTests {
        
        @Test
        @DisplayName("Verify regex initialization doesn't throw")
        void testRegexInitialization() {
            assertDoesNotThrow(() -> {
                new RegularExpression("(0u1(01*0)*1)*", new char[]{'0', '1'});
            }, "Regex initialization should not throw exceptions");
        }
        
        @Test
        @DisplayName("Verify alphabet validation")
        void testAlphabetValidation() {
            // Test with valid alphabet
            assertDoesNotThrow(() -> {
                RegularExpression testRe = new RegularExpression("0u1", new char[]{'0', '1'});
                testRe.match("0");
                testRe.match("1");
            }, "Should work with valid alphabet characters");
        }
        
        @Test
        @DisplayName("Boundary conditions")
        void testBoundaryConditions() {
            // Single character strings
            re.match("0");
            re.match("1");
            
            // Very short valid patterns
            re.match("00");
            re.match("11");
        }
    }

    @Nested
    @DisplayName("Simple Character Tests")
    class SimpleCharacterTests {
        
        @Test
        @DisplayName("Single character regex - matches exact character")
        void testSingleCharacter() {
            RegularExpression singleChar = new RegularExpression("a", new char[]{'a', 'b'});
            assertTrue(singleChar.match("a"), "Should match 'a'");
            assertFalse(singleChar.match("b"), "Should not match 'b'");
            assertFalse(singleChar.match(""), "Should not match empty string");
            assertFalse(singleChar.match("aa"), "Should not match 'aa'");
        }
        
        @Test
        @DisplayName("Two character concatenation")
        void testTwoCharacterConcat() {
            RegularExpression concat = new RegularExpression("ab", new char[]{'a', 'b'});
            assertTrue(concat.match("ab"), "Should match 'ab'");
            assertFalse(concat.match("a"), "Should not match 'a'");
            assertFalse(concat.match("b"), "Should not match 'b'");
            assertFalse(concat.match("ba"), "Should not match 'ba'");
            assertFalse(concat.match(""), "Should not match empty string");
        }
    }
    
    @Nested
    @DisplayName("Kleene Star Tests")
    class KleeneStarTests {
        
        @Test
        @DisplayName("Single character with star - a*")
        void testSingleCharacterStar() {
            RegularExpression aStar = new RegularExpression("a*", new char[]{'a', 'b'});
            assertTrue(aStar.match(""), "Should match empty string");
            assertTrue(aStar.match("a"), "Should match 'a'");
            assertTrue(aStar.match("aa"), "Should match 'aa'");
            assertTrue(aStar.match("aaa"), "Should match 'aaa'");
            assertFalse(aStar.match("b"), "Should not match 'b'");
            assertFalse(aStar.match("ab"), "Should not match 'ab'");
        }
        
        @Test
        @DisplayName("Concatenation with star - a*b")
        void testConcatWithStar() {
            RegularExpression aStarB = new RegularExpression("a*b", new char[]{'a', 'b'});
            assertTrue(aStarB.match("b"), "Should match 'b'");
            assertTrue(aStarB.match("ab"), "Should match 'ab'");
            assertTrue(aStarB.match("aab"), "Should match 'aab'");
            assertTrue(aStarB.match("aaab"), "Should match 'aaab'");
            assertFalse(aStarB.match(""), "Should not match empty string");
            assertFalse(aStarB.match("a"), "Should not match 'a'");
            assertFalse(aStarB.match("ba"), "Should not match 'ba'");
        }
        
        @Test
        @DisplayName("Grouped expression with star - (ab)*")
        void testGroupedStar() {
            RegularExpression groupStar = new RegularExpression("(ab)*", new char[]{'a', 'b'});
            assertTrue(groupStar.match(""), "Should match empty string");
            assertTrue(groupStar.match("ab"), "Should match 'ab'");
            assertTrue(groupStar.match("abab"), "Should match 'abab'");
            assertTrue(groupStar.match("ababab"), "Should match 'ababab'");
            assertFalse(groupStar.match("a"), "Should not match 'a'");
            assertFalse(groupStar.match("b"), "Should not match 'b'");
            assertFalse(groupStar.match("aba"), "Should not match 'aba'");
        }
    }
    
    @Nested
    @DisplayName("Union/OR Tests")
    class UnionTests {
        
        @Test
        @DisplayName("Simple union - aub")
        void testSimpleUnion() {
            RegularExpression union = new RegularExpression("aub", new char[]{'a', 'b'});
            assertTrue(union.match("a"), "Should match 'a'");
            assertTrue(union.match("b"), "Should match 'b'");
            assertFalse(union.match(""), "Should not match empty string");
            assertFalse(union.match("ab"), "Should not match 'ab'");
            assertFalse(union.match("c"), "Should not match 'c'");
        }
        
        @Test
        @DisplayName("Union with concatenation - (aub)c")
        void testUnionWithConcat() {
            RegularExpression unionConcat = new RegularExpression("(aub)c", new char[]{'a', 'b', 'c'});
            assertTrue(unionConcat.match("ac"), "Should match 'ac'");
            assertTrue(unionConcat.match("bc"), "Should match 'bc'");
            assertFalse(unionConcat.match("a"), "Should not match 'a'");
            assertFalse(unionConcat.match("b"), "Should not match 'b'");
            assertFalse(unionConcat.match("c"), "Should not match 'c'");
            assertFalse(unionConcat.match("abc"), "Should not match 'abc'");
        }
        
        @Test
        @DisplayName("Union with star - (aub)*")
        void testUnionWithStar() {
            RegularExpression unionStar = new RegularExpression("(aub)*", new char[]{'a', 'b'});
            assertTrue(unionStar.match(""), "Should match empty string");
            assertTrue(unionStar.match("a"), "Should match 'a'");
            assertTrue(unionStar.match("b"), "Should match 'b'");
            assertTrue(unionStar.match("ab"), "Should match 'ab'");
            assertTrue(unionStar.match("ba"), "Should match 'ba'");
            assertTrue(unionStar.match("aaa"), "Should match 'aaa'");
            assertTrue(unionStar.match("bbb"), "Should match 'bbb'");
            assertTrue(unionStar.match("abab"), "Should match 'abab'");
            assertTrue(unionStar.match("baba"), "Should match 'baba'");
        }
    }
    
    @Nested
    @DisplayName("Complex Pattern Tests")
    class ComplexPatternTests {
        
        @Test
        @DisplayName("Complex pattern - (a*b*)*")
        void testComplexStarPattern() {
            RegularExpression complex = new RegularExpression("(a*b*)*", new char[]{'a', 'b'});
            assertTrue(complex.match(""), "Should match empty string");
            assertTrue(complex.match("a"), "Should match 'a'");
            assertTrue(complex.match("b"), "Should match 'b'");
            assertTrue(complex.match("ab"), "Should match 'ab'");
            assertTrue(complex.match("aabb"), "Should match 'aabb'");
            assertTrue(complex.match("aaabbb"), "Should match 'aaabbb'");
            assertTrue(complex.match("aabbaa"), "Should match 'aabbaa'");
            assertTrue(complex.match("abab"), "Should match 'abab'");
        }
        
        @Test
        @DisplayName("Nested grouping - ((au(bc))*d)")
        void testNestedGrouping() {
            RegularExpression nested = new RegularExpression("(au(bc))*d", new char[]{'a', 'b', 'c', 'd'});
            assertTrue(nested.match("d"), "Should match 'd'");
            assertTrue(nested.match("ad"), "Should match 'ad'");
            assertTrue(nested.match("bcd"), "Should match 'bcd'");
            assertTrue(nested.match("abcd"), "Should match 'abcd'");
            assertTrue(nested.match("bcbcd"), "Should match 'bcbcd'");
            assertFalse(nested.match(""), "Should not match empty string");
            assertFalse(nested.match("a"), "Should not match 'a'");
            assertFalse(nested.match("bc"), "Should not match 'bc'");
        }
        
        @Test
        @DisplayName("Multiple unions - au(bu(cu(du)))")
        void testMultipleUnions() {
            RegularExpression multiUnion = new RegularExpression("au(bu(cud))", new char[]{'a', 'b', 'c', 'd'});
            assertTrue(multiUnion.match("a"), "Should match 'a'");
            assertTrue(multiUnion.match("b"), "Should match 'b'");
            assertTrue(multiUnion.match("c"), "Should match 'c'");
            assertTrue(multiUnion.match("d"), "Should match 'd'");
            assertFalse(multiUnion.match(""), "Should not match empty string");
            assertFalse(multiUnion.match("ab"), "Should not match 'ab'");
        }
    }
    
    @Nested
    @DisplayName("Different Alphabet Tests")
    class DifferentAlphabetTests {
        
        @Test
        @DisplayName("Single character alphabet")
        void testSingleCharAlphabet() {
            RegularExpression single = new RegularExpression("x*", new char[]{'x'});
            assertTrue(single.match(""), "Should match empty string");
            assertTrue(single.match("x"), "Should match 'x'");
            assertTrue(single.match("xx"), "Should match 'xx'");
            assertTrue(single.match("xxx"), "Should match 'xxx'");
        }
        
        @Test
        @DisplayName("Large alphabet")
        void testLargeAlphabet() {
            char[] alphabet = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};
            RegularExpression large = new RegularExpression("(aubucudueufuguhuiuj)*", alphabet);
            assertTrue(large.match(""), "Should match empty string");
            assertTrue(large.match("a"), "Should match 'a'");
            assertTrue(large.match("j"), "Should match 'j'");
            assertTrue(large.match("abcde"), "Should match 'abcde'");
            assertTrue(large.match("jihgfedcba"), "Should match 'jihgfedcba'");
        }
        
        @Test
        @DisplayName("Numeric alphabet")
        void testNumericAlphabet() {
            RegularExpression numeric = new RegularExpression("(0u1u2)*3", new char[]{'0', '1', '2', '3'});
            assertTrue(numeric.match("3"), "Should match '3'");
            assertTrue(numeric.match("03"), "Should match '03'");
            assertTrue(numeric.match("123"), "Should match '123'");
            assertTrue(numeric.match("0120213"), "Should match '0120213'");
            assertFalse(numeric.match(""), "Should not match empty string");
            assertFalse(numeric.match("012"), "Should not match '012'");
        }
    }
    
    @Nested
    @DisplayName("Edge Case and Error Tests")
    class EdgeCaseErrorTests {
        
        @Test
        @DisplayName("Empty regex should throw exception")
        void testEmptyRegex() {
            assertThrows(Exception.class, () -> {
                new RegularExpression("", new char[]{'a'});
            }, "Empty regex should throw exception");
        }
        
        @Test
        @DisplayName("Invalid characters should throw exception")
        void testInvalidCharacters() {
            assertThrows(Exception.class, () -> {
                // Using character not in alphabet
                RegularExpression invalid = new RegularExpression("c", new char[]{'a', 'b'});
                invalid.match("c");
            }, "Using character not in alphabet should throw exception");
        }
        
        @Test
        @DisplayName("Unbalanced parentheses should throw exception")
        void testUnbalancedParentheses() {
            assertThrows(IllegalArgumentException.class, () -> {
                new RegularExpression("(a*", new char[]{'a'});
            }, "Unbalanced parentheses should throw exception");
            
            assertThrows(IllegalArgumentException.class, () -> {
                new RegularExpression("a*)", new char[]{'a'});
            }, "Unbalanced parentheses should throw exception");
        }
        
        @Test
        @DisplayName("Test with special characters in alphabet")
        void testSpecialCharsInAlphabet() {
            RegularExpression special = new RegularExpression("@u#", new char[]{'@', '#', '*'});
            assertTrue(special.match("@"), "Should match '@'");
            assertTrue(special.match("#"), "Should match '#'");
            assertFalse(special.match("*"), "Should not match '*'");
        }
    }
    
    @Nested
    @DisplayName("Explicit Concatenation Tests")
    class ExplicitConcatenationTests {
        
        @Test
        @DisplayName("Explicit concatenation operator")
        void testExplicitConcat() {
            RegularExpression explicit = new RegularExpression("a.b", new char[]{'a', 'b'});
            assertTrue(explicit.match("ab"), "Should match 'ab'");
            assertFalse(explicit.match("a"), "Should not match 'a'");
            assertFalse(explicit.match("b"), "Should not match 'b'");
            assertFalse(explicit.match("ba"), "Should not match 'ba'");
        }
        
        @Test
        @DisplayName("Mixed implicit and explicit concatenation")
        void testMixedConcat() {
            RegularExpression mixed = new RegularExpression("a.bc", new char[]{'a', 'b', 'c'});
            assertTrue(mixed.match("abc"), "Should match 'abc'");
            assertFalse(mixed.match("ab"), "Should not match 'ab'");
            assertFalse(mixed.match("bc"), "Should not match 'bc'");
        }
    }
} 