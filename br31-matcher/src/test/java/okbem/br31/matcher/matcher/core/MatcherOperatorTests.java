
package okbem.br31.matcher.matcher.core;

import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.*;
import org.mockito.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * {@link Matcher}의 operator 메서드를 검증하는 테스트.
 * 이 테스트가 검증하는 메서드는 다음과 같다:
 * {@link Matcher#submit(Object, Set)},
 * {@link Matcher#submit(Object, Set, EnumMap)},
 * {@link Matcher#cancel(Object)}
 */
public class MatcherOperatorTests {

    private static final class DummyMatcher<U, R extends Enum<R>>
        extends Matcher<U, R> {

        public DummyMatcher(Class<R> ruleKeyType) {
            super(ruleKeyType, Duration.ofHours(1L));
        }

        @Override
        protected void playMatch(MatchRule<R> matchRule, Set<U> match) {
        }

    }


    private static enum Rule {
        COLOR,
        FOOD;

        private static enum Color { RED, GREEN, BLUE }
        private static enum Food { 피자, 치킨, 감자, 고구마 }
    }


    private static enum NoRule {
    }


    private static final Matcher<String, Rule> matcher
        = new DummyMatcher<>(Rule.class);


    private static final Matcher<String, NoRule> noRuleMatcher
        = new DummyMatcher<>(NoRule.class);


    private static Set<Integer> createValidMatchSizeSet() {
        return new HashSet<>(Arrays.asList(5, 6));
    }


    private static EnumMap<Rule, Set<?>> createValidRuleSetMap() {
        EnumMap<Rule, Set<?>> ruleSetMap = new EnumMap<>(Rule.class);

        ruleSetMap.put(Rule.COLOR, EnumSet.of(Rule.Color.BLUE));
        ruleSetMap.put(Rule.FOOD, EnumSet.of(Rule.Food.피자, Rule.Food.고구마));

        return ruleSetMap;
    }


    @Test
    public void submit_ThrowsException_IfUserIsNull() {
        String user = null;
        Set<Integer> matchSizeSet = createValidMatchSizeSet();
        EnumMap<Rule, Set<?>> ruleSetMap = createValidRuleSetMap();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> matcher.submit(user, matchSizeSet, ruleSetMap))
            .withMessage("user cannot be null");
    }


    @Test
    public void submit_ThrowsException_IfMatchSizeSetIsNull() {
        String user = "submit_ThrowsException_IfMatchSizeSetIsNull";
        Set<Integer> matchSizeSet = null;
        EnumMap<Rule, Set<?>> ruleSetMap = createValidRuleSetMap();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> matcher.submit(user, matchSizeSet, ruleSetMap))
            .withMessage("matchSizeSet cannot be null or empty");
    }


    @Test
    public void submit_ThrowsException_IfMatchSizeSetIsEmpty() {
        String user = "submit_ThrowsException_IfMatchSizeSetIsEmpty";
        Set<Integer> matchSizeSet = new HashSet<>();
        EnumMap<Rule, Set<?>> ruleSetMap = createValidRuleSetMap();

        assertThatIllegalArgumentException()
            .isThrownBy(() -> matcher.submit(user, matchSizeSet, ruleSetMap))
            .withMessage("matchSizeSet cannot be null or empty");
    }


    @Test
    public void submit_ThrowsException_IfMatchSizeIsLessThanTwo() {
        String user = "submit_ThrowsException_IfMatchSizeIsLessThanTwo";
        Set<Integer> matchSizeSet = createValidMatchSizeSet();
        EnumMap<Rule, Set<?>> ruleSetMap = createValidRuleSetMap();

        matchSizeSet.add(0);
        matchSizeSet.add(1);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> matcher.submit(user, matchSizeSet, ruleSetMap))
            .withMessage("matchSize cannot be less than two");
    }


    @Test
    public void submit_ThrowsException_IfRuleSetMapIsIncomplete() {
        String user = "submit_ThrowsException_IfRuleSetMapIsIncomplete";
        Set<Integer> matchSizeSet = createValidMatchSizeSet();
        EnumMap<Rule, Set<?>> ruleSetMap = createValidRuleSetMap();

        ruleSetMap.remove(Rule.COLOR);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> matcher.submit(user, matchSizeSet, ruleSetMap))
            .withMessage(
                "ruleSetMap must contain the following keys: [COLOR, FOOD]"
            );

        assertThatIllegalArgumentException()
            .isThrownBy(() -> matcher.submit(user, matchSizeSet))
            .withMessage(
                "ruleSetMap must contain the following keys: [COLOR, FOOD]"
            );
    }


    @Test
    public void submit_ThrowsException_IfRuleValueSetIsNull() {
        String user = "submit_ThrowsException_IfRuleValueSetIsNull";
        Set<Integer> matchSizeSet = createValidMatchSizeSet();
        EnumMap<Rule, Set<?>> ruleSetMap = createValidRuleSetMap();

        ruleSetMap.put(Rule.COLOR, null);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> matcher.submit(user, matchSizeSet, ruleSetMap))
            .withMessage("ruleValueSet cannot be null or empty");
    }


    @Test
    public void submit_ThrowsException_IfRuleValueSetIsEmpty() {
        String user = "submit_ThrowsException_IfRuleValueSetIsEmpty";
        Set<Integer> matchSizeSet = createValidMatchSizeSet();
        EnumMap<Rule, Set<?>> ruleSetMap = createValidRuleSetMap();

        ruleSetMap.put(Rule.COLOR, EnumSet.noneOf(Rule.Color.class));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> matcher.submit(user, matchSizeSet, ruleSetMap))
            .withMessage("ruleValueSet cannot be null or empty");
    }


    @Test
    public void submit_ReturnsFalse_IfUserAlreadyExists() {
        String user = "submit_ReturnsFalse_IfUserAlreadyExists";
        Set<Integer> matchSizeSet = createValidMatchSizeSet();
        EnumMap<Rule, Set<?>> ruleSetMap = createValidRuleSetMap();

        assertThat(matcher.submit(user, matchSizeSet, ruleSetMap))
            .isTrue();

        assertThat(matcher.submit(user, matchSizeSet, ruleSetMap))
            .isFalse();
    }


    @Test
    public void submit_ReturnsTrue_IfSuccessful() {
        String user = "submit_ReturnsTrue_IfSuccessful";
        Set<Integer> matchSizeSet = createValidMatchSizeSet();
        EnumMap<Rule, Set<?>> ruleSetMap = createValidRuleSetMap();

        assertThat(matcher.submit(user, matchSizeSet, ruleSetMap))
            .isTrue();
    }


    @Test
    public void submit_ReturnsTrue_IfSuccessfulWithoutRule() {
        Set<Integer> matchSizeSet = createValidMatchSizeSet();

        String user1 = "submit_ReturnsTrue_IfSuccessfulWithoutRule_1";
        assertThat(noRuleMatcher.submit(user1, matchSizeSet))
            .isTrue();

        String user2 = "submit_ReturnsTrue_IfSuccessfulWithoutRule_2";
        EnumMap<NoRule, Set<?>> ruleSetMap2 = null;
        assertThat(noRuleMatcher.submit(user2, matchSizeSet, ruleSetMap2))
            .isTrue();

        String user3 = "submit_ReturnsTrue_IfSuccessfulWithoutRule_3";
        EnumMap<NoRule, Set<?>> ruleSetMap3 = new EnumMap<>(NoRule.class);
        assertThat(noRuleMatcher.submit(user3, matchSizeSet, ruleSetMap3))
            .isTrue();
    }


    @Test
    public void cancel_ThrowsException_IfUserIsNull() {
        String user = null;

        assertThatIllegalArgumentException()
            .isThrownBy(() -> matcher.cancel(user))
            .withMessage("user cannot be null");
    }


    @Test
    public void cancel_ReturnsFalse_IfUserDoesNotExist() {
        String user = "cancel_ReturnsFalse_IfUserDoesNotExist";

        assertThat(matcher.cancel(user))
            .isFalse();
    }


    @Test
    public void cancel_ReturnsTrue_IfSuccessful() {
        String user = "cancel_ReturnsTrue_IfSuccessful";
        Set<Integer> matchSizeSet = createValidMatchSizeSet();
        EnumMap<Rule, Set<?>> ruleSetMap = createValidRuleSetMap();

        assertThat(matcher.submit(user, matchSizeSet, ruleSetMap))
            .isTrue();

        assertThat(matcher.cancel(user))
            .isTrue();
    }

}

