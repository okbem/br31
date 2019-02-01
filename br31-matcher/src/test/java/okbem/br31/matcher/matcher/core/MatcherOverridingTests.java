
package okbem.br31.matcher.matcher.core;

import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import okbem.br31.matcher.test.LogAssertion;

import org.junit.*;
import org.mockito.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * {@link Matcher}의 overriding 가능한 메서드를 검증하는 테스트.
 * 이 테스트가 검증하는 메서드는 다음과 같다:
 * {@link Matcher#playMatch(MatchRule, Set)},
 * {@link Matcher#findMatches(MatchRule, Set)}
 */
public class MatcherOverridingTests {

    private LogAssertion logAssertion;


    @Before
    public void initLogAssertion() {
        this.logAssertion = new LogAssertion(Matcher.class);
    }


    private <U, R extends Enum<R>> void assertMatcherErrorLog(
        Matcher<U, R> matcher,
        Class<? extends Throwable> exceptionType,
        String exceptionMessage
    ) {
        String name = matcher.getClass().toString();

        this.logAssertion.assertLog(
            "ERROR",
            "An exception occurred while running " + name,
            exceptionType,
            exceptionMessage
        );
    }


    private static enum Rule {
        COLOR,
        FOOD;

        private static enum Color { RED, GREEN, BLUE }
        private static enum Food { 피자, 치킨, 감자, 고구마 }
    }


    private static enum NoRule {
    }


    private static void testUseCase0(TestMatcher<String, Rule> matcher) {
        EnumMap<Rule, Set<?>> A = new EnumMap<>(Rule.class);
        A.put(Rule.COLOR, EnumSet.of(Rule.Color.GREEN, Rule.Color.BLUE));
        A.put(Rule.FOOD, EnumSet.of(Rule.Food.피자, Rule.Food.고구마));

        EnumMap<Rule, Set<?>> B = new EnumMap<>(Rule.class);
        B.put(Rule.COLOR, EnumSet.of(Rule.Color.BLUE));
        B.put(Rule.FOOD, EnumSet.of(Rule.Food.피자));

        EnumMap<Rule, Set<?>> C = new EnumMap<>(Rule.class);
        C.put(Rule.COLOR, EnumSet.of(Rule.Color.RED, Rule.Color.BLUE));
        C.put(Rule.FOOD, EnumSet.of(Rule.Food.피자, Rule.Food.치킨));

        EnumMap<Rule, Set<?>> D = new EnumMap<>(Rule.class);
        D.put(Rule.COLOR, EnumSet.of(Rule.Color.BLUE));
        D.put(Rule.FOOD, EnumSet.of(Rule.Food.피자, Rule.Food.감자));

        EnumMap<Rule, Set<?>> E = new EnumMap<>(Rule.class);
        E.put(Rule.COLOR, EnumSet.of(Rule.Color.RED));
        E.put(Rule.FOOD, EnumSet.of(Rule.Food.감자, Rule.Food.고구마));

        EnumMap<Rule, Set<?>> F = new EnumMap<>(Rule.class);
        F.put(Rule.COLOR, EnumSet.of(Rule.Color.RED, Rule.Color.GREEN));
        F.put(Rule.FOOD, EnumSet.of(Rule.Food.감자));

        EnumMap<Rule, Set<?>> ruleSetMap1 = new EnumMap<>(Rule.class);
        ruleSetMap1.put(Rule.COLOR, EnumSet.of(Rule.Color.BLUE));
        ruleSetMap1.put(Rule.FOOD, EnumSet.of(Rule.Food.피자));

        EnumMap<Rule, Set<?>> ruleSetMap2 = new EnumMap<>(Rule.class);
        ruleSetMap2.put(Rule.COLOR, EnumSet.of(Rule.Color.RED));
        ruleSetMap2.put(Rule.FOOD, EnumSet.of(Rule.Food.감자));

        matcher.submit("A", new HashSet<>(Arrays.asList(2, 3, 4)), A);
        matcher.submit("B", new HashSet<>(Arrays.asList(2)), B);
        matcher.submit("C", new HashSet<>(Arrays.asList(2, 3)), C);
        matcher.submit("D", new HashSet<>(Arrays.asList(2)), D);
        matcher.submit("E", new HashSet<>(Arrays.asList(2, 3)), E);
        matcher.submit("F", new HashSet<>(Arrays.asList(2, 3)), F);

        assertThat(matcher.isMatched())
            .isTrue();

        assertThat(matcher.matchResult)
            .hasSize(2);

        matcher.assertMatch(ruleSetMap1, 2, 2, "A", "B", "C", "D");
        matcher.assertMatch(ruleSetMap1, 3, 0);
        matcher.assertMatch(ruleSetMap1, 4, 0);
        matcher.assertMatch(ruleSetMap2, 2, 1, "E", "F");
        matcher.assertMatch(ruleSetMap2, 3, 0);
        matcher.assertMatch(ruleSetMap2, 4, 0);
    }


    @Test
    public void playMatch_WorksFine_IfRuleMapIsModified() {
        TestMatcher<String, Rule> matcher = new TestMatcher<String, Rule>(
            Rule.class,
            Duration.ofMillis(500L),
            new CountDownLatch(3),
            Duration.ofMillis(1000L)
        ) {
            @Override
            protected void playMatch(
                MatchRule<Rule> matchRule,
                Set<String> match
            ) {
                super.playMatch(matchRule, match);

                matchRule.getRuleMap().clear();
            }
        };

        testUseCase0(matcher);
    }


    @Test
    public void playMatch_WorksFine_IfMatchIsModified() {
        TestMatcher<String, Rule> matcher = new TestMatcher<String, Rule>(
            Rule.class,
            Duration.ofMillis(500L),
            new CountDownLatch(3),
            Duration.ofMillis(1000L)
        ) {
            @Override
            protected void playMatch(
                MatchRule<Rule> matchRule,
                Set<String> match
            ) {
                super.playMatch(matchRule, match);

                match.clear();
            }
        };

        testUseCase0(matcher);
    }


    @Test
    public void findMatches_WorksFine_IfRuleMapIsModified() {
        TestMatcher<String, Rule> matcher = new TestMatcher<String, Rule>(
            Rule.class,
            Duration.ofMillis(500L),
            new CountDownLatch(3),
            Duration.ofMillis(1000L)
        ) {
            @Override
            protected List<Set<String>> findMatches(
                MatchRule<Rule> matchRule,
                Set<String> userSet
            ) {
                List<Set<String>> matchList
                    = super.findMatches(matchRule, userSet);

                matchRule.getRuleMap().clear();

                return matchList;
            }
        };

        testUseCase0(matcher);
    }


    @Test
    public void findMatches_WorksFine_IfUserSetIsModified() {
        TestMatcher<String, Rule> matcher = new TestMatcher<String, Rule>(
            Rule.class,
            Duration.ofMillis(500L),
            new CountDownLatch(3),
            Duration.ofMillis(1000L)
        ) {
            @Override
            protected List<Set<String>> findMatches(
                MatchRule<Rule> matchRule,
                Set<String> userSet
            ) {
                List<Set<String>> matchList
                    = super.findMatches(matchRule, userSet);

                userSet.clear();

                return matchList;
            }
        };

        testUseCase0(matcher);
    }


    @Test
    public void findMatches_ThrowsException_IfMatchHasInvalidUser() {
        TestMatcher<String, NoRule> matcher = new TestMatcher<String, NoRule>(
            NoRule.class,
            Duration.ofMillis(500L),
            new CountDownLatch(1),
            Duration.ofMillis(1000L)
        ) {
            @Override
            protected List<Set<String>> findMatches(
                MatchRule<NoRule> matchRule,
                Set<String> userSet
            ) {
                return Arrays.asList(new HashSet<>(Arrays.asList("B", "C")));
            }
        };

        matcher.submit("A", new HashSet<>(Arrays.asList(2, 3)));
        matcher.submit("B", new HashSet<>(Arrays.asList(2)));
        matcher.submit("C", new HashSet<>(Arrays.asList(3)));

        assertThat(matcher.isMatched())
            .isFalse();

        this.assertMatcherErrorLog(
            matcher,
            IllegalStateException.class,
            "match must be a subset of userSet"
        );
    }


    @Test
    public void findMatches_ThrowsException_IfMatchesOverlapEachOther() {
        TestMatcher<String, NoRule> matcher = new TestMatcher<String, NoRule>(
            NoRule.class,
            Duration.ofMillis(500L),
            new CountDownLatch(2),
            Duration.ofMillis(1000L)
        ) {
            @Override
            protected List<Set<String>> findMatches(
                MatchRule<NoRule> matchRule,
                Set<String> userSet
            ) {
                return Arrays.asList(
                    new HashSet<>(Arrays.asList("A", "B")),
                    new HashSet<>(Arrays.asList("B", "C"))
                );
            }
        };

        matcher.submit("A", new HashSet<>(Arrays.asList(2)));
        matcher.submit("B", new HashSet<>(Arrays.asList(2)));
        matcher.submit("C", new HashSet<>(Arrays.asList(2)));
        matcher.submit("D", new HashSet<>(Arrays.asList(2)));

        assertThat(matcher.isMatched())
            .isFalse();

        this.assertMatcherErrorLog(
            matcher,
            IllegalStateException.class,
            "match must be a subset of userSet"
        );
    }


    @Test
    public void findMatches_ThrowsException_IfMatchHasInvalidSize() {
        TestMatcher<String, NoRule> matcher = new TestMatcher<String, NoRule>(
            NoRule.class,
            Duration.ofMillis(500L),
            new CountDownLatch(2),
            Duration.ofMillis(1000L)
        ) {
            @Override
            protected List<Set<String>> findMatches(
                MatchRule<NoRule> matchRule,
                Set<String> userSet
            ) {
                return Arrays.asList(
                    new HashSet<>(Arrays.asList("A", "B", "C")),
                    new HashSet<>(Arrays.asList("D"))
                );
            }
        };

        matcher.submit("A", new HashSet<>(Arrays.asList(2)));
        matcher.submit("B", new HashSet<>(Arrays.asList(2)));
        matcher.submit("C", new HashSet<>(Arrays.asList(2)));
        matcher.submit("D", new HashSet<>(Arrays.asList(2)));

        assertThat(matcher.isMatched())
            .isFalse();

        this.assertMatcherErrorLog(
            matcher,
            IllegalStateException.class,
            "match must have exactly the same size as matchSize"
        );
    }


    @Test
    public void findMatches_WorksFine_IfUsersArePartiallyMatched() {
        TestMatcher<String, NoRule> matcher = new TestMatcher<String, NoRule>(
            NoRule.class,
            Duration.ofMillis(1500L),
            new CountDownLatch(4),
            Duration.ofMillis(2000L)
        ) {
            @Override
            protected List<Set<String>> findMatches(
                MatchRule<NoRule> matchRule,
                Set<String> userSet
            ) {
                return Arrays.asList(
                    super.findMatches(matchRule, userSet).get(0)
                );
            }
        };

        EnumMap<NoRule, Set<?>> ruleSetMap = new EnumMap<>(NoRule.class);

        matcher.submit("A", new HashSet<>(Arrays.asList(2)));
        matcher.submit("B", new HashSet<>(Arrays.asList(2)));
        matcher.submit("C", new HashSet<>(Arrays.asList(2)));
        matcher.submit("D", new HashSet<>(Arrays.asList(2)));
        matcher.submit("E", new HashSet<>(Arrays.asList(2)));
        matcher.submit("F", new HashSet<>(Arrays.asList(3)));
        matcher.submit("G", new HashSet<>(Arrays.asList(3)));
        matcher.submit("H", new HashSet<>(Arrays.asList(3)));
        matcher.submit("I", new HashSet<>(Arrays.asList(3)));
        matcher.submit("J", new HashSet<>(Arrays.asList(3)));
        matcher.submit("K", new HashSet<>(Arrays.asList(3)));

        assertThat(matcher.isMatched())
            .isFalse();

        matcher.assertMatch(ruleSetMap, 2, 1, "A", "B", "C", "D", "E");
        matcher.assertMatch(ruleSetMap, 3, 1, "F", "G", "H", "I", "J", "K");

        assertThat(matcher.isMatched())
            .isTrue();

        matcher.assertMatch(ruleSetMap, 2, 2, "A", "B", "C", "D", "E");
        matcher.assertMatch(ruleSetMap, 3, 2, "F", "G", "H", "I", "J", "K");
    }

}

